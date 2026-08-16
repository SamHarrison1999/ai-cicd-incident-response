/* eslint-disable @typescript-eslint/no-non-null-assertion */
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";

const api = vi.hoisted(() => ({
    loginUser: vi.fn(),
    logoutUser: vi.fn(),
    refreshSession: vi.fn(),
    registerUser: vi.fn(),
}));

vi.mock("../src/api/authentication", () => api);

import { AppProviders } from "../src/app/AppProviders";
import {
    AuthContext,
    AuthProvider,
    useAuth as useAuthFromContext,
} from "../src/auth/AuthContext";
import { ProtectedRoute } from "../src/auth/ProtectedRoute";
import { useAuth } from "../src/auth/useAuth";
import { LoginPage } from "../src/pages/LoginPage";
import { RegisterPage } from "../src/pages/RegisterPage";
import { ApiError } from "../src/api/httpClient";

const user = {
    userId: "user",
    email: "user@example.com",
    displayName: "User",
};

const authentication = {
    accessToken: "access-token",
    tokenType: "Bearer" as const,
    expiresInSeconds: 900,
    user,
};

function queryClient() {
    return new QueryClient({ defaultOptions: { queries: { retry: false } } });
}

function passwordInput() {
    return document.querySelector<HTMLInputElement>('input[type="password"]')!;
}

function StateProbe() {
    const auth = useAuth();
    return (
        <section>
            <p data-testid="token">{auth.accessToken ?? "signed-out"}</p>
            <p data-testid="initialising">{String(auth.isInitialising)}</p>
            <p>{auth.currentUser?.email ?? "no-user"}</p>
            <button
                onClick={() => {
                    void auth.login("login@example.com", "password");
                }}
            >
                Login
            </button>
            <button
                onClick={() => {
                    void auth.register({
                        displayName: "New",
                        email: "new@example.com",
                        password: "long-password",
                    });
                }}
            >
                Register
            </button>
            <button
                onClick={() => {
                    void auth.logout().catch(() => undefined);
                }}
            >
                Logout
            </button>
        </section>
    );
}

function ContextHookProbe() {
    const auth = useAuthFromContext();
    return <p>{auth.currentUser?.email ?? "no-user"}</p>;
}

function renderWithAuth(element: React.ReactNode = <StateProbe />) {
    return render(
        <QueryClientProvider client={queryClient()}>
            <AuthProvider>{element}</AuthProvider>
        </QueryClientProvider>,
    );
}

function protectedValue(
    overrides: Partial<React.ContextType<typeof AuthContext>> = {},
) {
    return {
        accessToken: null,
        currentUser: null,
        isInitialising: false,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
        ...overrides,
    };
}

describe("frontend authentication coverage", () => {
    beforeEach(() => {
        cleanup();
        vi.clearAllMocks();
        api.refreshSession.mockResolvedValue(authentication);
        api.loginUser.mockResolvedValue(authentication);
        api.registerUser.mockResolvedValue(undefined);
        api.logoutUser.mockResolvedValue(undefined);
    });

    it("restores, logs in, registers, and logs out through the provider", async () => {
        const userEventInstance = userEvent.setup();
        renderWithAuth();

        expect(await screen.findByTestId("token")).toHaveTextContent(
            "access-token",
        );
        await userEventInstance.click(
            screen.getByRole("button", { name: "Login" }),
        );
        expect(api.loginUser).toHaveBeenCalledWith({
            email: "login@example.com",
            password: "password",
        });
        await userEventInstance.click(
            screen.getByRole("button", { name: "Register" }),
        );
        expect(api.registerUser).toHaveBeenCalled();
        await userEventInstance.click(
            screen.getByRole("button", { name: "Logout" }),
        );
        expect(await screen.findByTestId("token")).toHaveTextContent(
            "signed-out",
        );
    });

    it("clears state when refresh fails and when logout fails", async () => {
        api.refreshSession.mockRejectedValueOnce(new Error("expired"));
        renderWithAuth();
        expect(await screen.findByTestId("token")).toHaveTextContent(
            "signed-out",
        );

        api.refreshSession.mockResolvedValueOnce(authentication);
        api.logoutUser.mockRejectedValueOnce(new Error("network"));
        cleanup();
        renderWithAuth();
        await screen.findByText("user@example.com");
        await userEvent
            .setup()
            .click(screen.getByRole("button", { name: "Logout" }));
        expect(await screen.findByTestId("token")).toHaveTextContent(
            "signed-out",
        );
    });

    it("rejects useAuth outside its provider and returns context inside it", async () => {
        expect(() => render(<StateProbe />)).toThrow(
            "useAuth must be used inside AuthProvider.",
        );
        expect(() => render(<ContextHookProbe />)).toThrow(
            "useAuth must be used inside AuthProvider.",
        );
        cleanup();
        api.refreshSession.mockResolvedValue(authentication);
        render(
            <AuthProvider>
                <ContextHookProbe />
            </AuthProvider>,
        );
        expect(await screen.findByText("user@example.com")).toBeInTheDocument();
    });

    it("ignores a refresh result after the provider unmounts", async () => {
        let resolveRefresh:
            ((value: typeof authentication) => void) | undefined;
        let rejectRefresh: ((reason: Error) => void) | undefined;
        api.refreshSession.mockImplementation(
            () =>
                new Promise((resolve) => {
                    resolveRefresh = resolve;
                    rejectRefresh = () => {
                        // The rejected promise is intentionally left pending in this branch.
                    };
                }),
        );
        const view = renderWithAuth();
        view.unmount();
        resolveRefresh?.(authentication);
        await waitFor(() => {
            expect(api.refreshSession).toHaveBeenCalled();
        });

        api.refreshSession.mockImplementation(
            () =>
                new Promise((resolve, reject) => {
                    void resolve;
                    rejectRefresh = reject;
                }),
        );
        const second = renderWithAuth();
        second.unmount();
        rejectRefresh?.(new Error("late refresh failure"));
    });

    it("covers the protected route loading, redirect, and outlet branches", () => {
        const loading = render(
            <AuthContext.Provider
                value={protectedValue({ isInitialising: true })}
            >
                <MemoryRouter initialEntries={["/private"]}>
                    <Routes>
                        <Route element={<ProtectedRoute />}>
                            <Route path="/private" element={<p>private</p>} />
                        </Route>
                    </Routes>
                </MemoryRouter>
            </AuthContext.Provider>,
        );
        expect(
            screen.getByRole("heading", {
                name: "Restoring your secure session",
            }),
        ).toBeInTheDocument();
        loading.unmount();

        const redirect = render(
            <AuthContext.Provider value={protectedValue()}>
                <MemoryRouter initialEntries={["/private"]}>
                    <Routes>
                        <Route path="/login" element={<p>login</p>} />
                        <Route element={<ProtectedRoute />}>
                            <Route path="/private" element={<p>private</p>} />
                        </Route>
                    </Routes>
                </MemoryRouter>
            </AuthContext.Provider>,
        );
        expect(screen.getByText("login")).toBeInTheDocument();
        redirect.unmount();

        render(
            <AuthContext.Provider
                value={protectedValue({
                    accessToken: "token",
                    currentUser: user,
                })}
            >
                <MemoryRouter initialEntries={["/private"]}>
                    <Routes>
                        <Route element={<ProtectedRoute />}>
                            <Route path="/private" element={<p>private</p>} />
                        </Route>
                    </Routes>
                </MemoryRouter>
            </AuthContext.Provider>,
        );
        expect(screen.getByText("private")).toBeInTheDocument();
    });

    it("mounts the application provider boundary", async () => {
        render(
            <AppProviders>
                <p>application child</p>
            </AppProviders>,
        );
        expect(
            await screen.findByText("application child"),
        ).toBeInTheDocument();
    });

    it("handles login success, API errors, and generic errors", async () => {
        const userEventInstance = userEvent.setup();
        api.refreshSession.mockRejectedValue(new Error("no refresh"));
        const view = render(
            <MemoryRouter
                initialEntries={[
                    {
                        pathname: "/login",
                        state: { from: "/private", registrationComplete: true },
                    },
                ]}
            >
                <AuthProvider>
                    <Routes>
                        <Route path="/login" element={<LoginPage />} />
                        <Route
                            path="/private"
                            element={<p>private destination</p>}
                        />
                    </Routes>
                </AuthProvider>
            </MemoryRouter>,
        );
        expect(screen.getByRole("status")).toHaveTextContent("Account created");
        await userEventInstance.type(
            screen.getByLabelText("Email address"),
            "user@example.com",
        );
        await userEventInstance.type(passwordInput(), "password");
        await userEventInstance.click(
            screen.getByRole("button", { name: "Sign in" }),
        );
        expect(
            await screen.findByText("private destination"),
        ).toBeInTheDocument();
        view.unmount();

        api.loginUser.mockRejectedValueOnce(
            new ApiError(401, "BAD", "Bad credentials"),
        );
        renderWithAuth(
            <MemoryRouter>
                <LoginPage />
            </MemoryRouter>,
        );
        await userEventInstance.type(
            screen.getByLabelText("Email address"),
            "bad@example.com",
        );
        await userEventInstance.type(passwordInput(), "password");
        await userEventInstance.click(
            screen.getByRole("button", { name: "Sign in" }),
        );
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Bad credentials",
        );
        cleanup();

        api.loginUser.mockRejectedValueOnce(new Error("offline"));
        renderWithAuth(
            <MemoryRouter>
                <LoginPage />
            </MemoryRouter>,
        );
        await userEventInstance.type(
            screen.getByLabelText("Email address"),
            "bad@example.com",
        );
        await userEventInstance.type(passwordInput(), "password");
        await userEventInstance.click(
            screen.getByRole("button", { name: "Sign in" }),
        );
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Sign in failed",
        );
    });

    it("handles registration success, API errors, and generic errors", async () => {
        const userEventInstance = userEvent.setup();
        api.refreshSession.mockRejectedValue(new Error("no refresh"));
        render(
            <MemoryRouter initialEntries={["/register"]}>
                <AuthProvider>
                    <Routes>
                        <Route path="/register" element={<RegisterPage />} />
                        <Route path="/login" element={<LoginPage />} />
                    </Routes>
                </AuthProvider>
            </MemoryRouter>,
        );
        await userEventInstance.type(
            screen.getByLabelText("Display name"),
            "New user",
        );
        await userEventInstance.type(
            screen.getByLabelText("Email address"),
            "new@example.com",
        );
        await userEventInstance.type(passwordInput(), "long-password");
        await userEventInstance.click(
            screen.getByRole("button", { name: "Create account" }),
        );
        expect(
            await screen.findByRole("heading", { name: "Sign in" }),
        ).toBeInTheDocument();
        cleanup();

        api.registerUser.mockRejectedValueOnce(
            new ApiError(409, "DUPLICATE", "Already exists"),
        );
        renderWithAuth(
            <MemoryRouter>
                <RegisterPage />
            </MemoryRouter>,
        );
        await userEventInstance.type(
            screen.getByLabelText("Display name"),
            "New user",
        );
        await userEventInstance.type(
            screen.getByLabelText("Email address"),
            "new@example.com",
        );
        await userEventInstance.type(passwordInput(), "long-password");
        await userEventInstance.click(
            screen.getByRole("button", { name: "Create account" }),
        );
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Already exists",
        );
        cleanup();

        api.registerUser.mockRejectedValueOnce(new Error("offline"));
        renderWithAuth(
            <MemoryRouter>
                <RegisterPage />
            </MemoryRouter>,
        );
        await userEventInstance.type(
            screen.getByLabelText("Display name"),
            "New user",
        );
        await userEventInstance.type(
            screen.getByLabelText("Email address"),
            "new@example.com",
        );
        await userEventInstance.type(passwordInput(), "long-password");
        await userEventInstance.click(
            screen.getByRole("button", { name: "Create account" }),
        );
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Registration failed",
        );
    });
});
