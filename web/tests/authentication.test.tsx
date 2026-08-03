import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";

import { AppRoutes } from "../src/app/AppRoutes";
import { AuthProvider } from "../src/auth/AuthContext";

function renderApplication(initialEntry: string) {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: { retry: false },
        },
    });

    return render(
        <MemoryRouter
            initialEntries={[initialEntry]}
            future={{
                v7_relativeSplatPath: true,
                v7_startTransition: true,
            }}
        >
            <QueryClientProvider client={queryClient}>
                <AuthProvider>
                    <AppRoutes />
                </AuthProvider>
            </QueryClientProvider>
        </MemoryRouter>,
    );
}

describe("frontend authentication", () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it("redirects an unauthenticated user to sign in", async () => {
        vi.stubGlobal(
            "fetch",
            vi.fn().mockResolvedValue(
                new Response(
                    JSON.stringify({
                        code: "INVALID_REFRESH_TOKEN",
                        message: "The refresh token is invalid.",
                    }),
                    {
                        status: 401,
                        headers: { "Content-Type": "application/json" },
                    },
                ),
            ),
        );

        renderApplication("/");

        expect(
            await screen.findByRole("heading", { name: "Sign in" }),
        ).toBeInTheDocument();
    });

    it("submits login and opens the protected workspace", async () => {
        const fetchMock = vi
            .fn()
            .mockResolvedValueOnce(
                new Response("{}", {
                    status: 401,
                    headers: { "Content-Type": "application/json" },
                }),
            )
            .mockResolvedValueOnce(
                new Response(
                    JSON.stringify({
                        accessToken: "access-token",
                        tokenType: "Bearer",
                        expiresInSeconds: 900,
                        user: {
                            userId: "00000000-0000-0000-0000-000000000001",
                            email: "sam@example.com",
                            displayName: "Sam",
                        },
                    }),
                    {
                        status: 200,
                        headers: { "Content-Type": "application/json" },
                    },
                ),
            )
            .mockResolvedValue(
                new Response(
                    JSON.stringify({
                        service: "control-plane",
                        version: "0.1.0-SNAPSHOT",
                        status: "UP",
                        timestamp: "2026-08-03T12:00:00Z",
                    }),
                    {
                        status: 200,
                        headers: { "Content-Type": "application/json" },
                    },
                ),
            );

        vi.stubGlobal("fetch", fetchMock);

        const user = userEvent.setup();
        renderApplication("/login");

        await user.type(
            screen.getByRole("textbox", { name: "Email address" }),
            "sam@example.com",
        );
        await user.type(screen.getByLabelText("Password"), "correct-password");
        await user.click(screen.getByRole("button", { name: "Sign in" }));

        expect(
            await screen.findByRole("heading", {
                name: "Operational readiness",
            }),
        ).toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledWith(
            "/control-plane/api/v1/auth/login",
            expect.objectContaining({
                credentials: "include",
                method: "POST",
            }),
        );
    });
});
