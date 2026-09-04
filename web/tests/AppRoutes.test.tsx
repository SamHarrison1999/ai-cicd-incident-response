import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";

import { AppRoutes } from "../src/app/AppRoutes";
import { AuthProvider } from "../src/auth/AuthContext";

const authenticatedUser = {
    userId: "00000000-0000-0000-0000-000000000001",
    email: "sam@example.com",
    displayName: "Sam",
};

function createJsonResponse(body: unknown, status = 200) {
    return new Response(JSON.stringify(body), {
        status,
        headers: { "Content-Type": "application/json" },
    });
}

function installAuthenticatedFetchMock() {
    const fetchMock = vi.fn((input: RequestInfo | URL) => {
        const url =
            input instanceof Request
                ? input.url
                : input instanceof URL
                  ? input.href
                  : input;

        if (url.endsWith("/api/v1/auth/refresh")) {
            return Promise.resolve(
                createJsonResponse({
                    accessToken: "test-access-token",
                    tokenType: "Bearer",
                    expiresInSeconds: 900,
                    user: authenticatedUser,
                }),
            );
        }

        if (url.endsWith("/api/v1/system/status")) {
            return Promise.resolve(
                createJsonResponse({
                    service: "control-plane",
                    version: "0.1.0-SNAPSHOT",
                    status: "UP",
                    timestamp: "2026-08-03T12:00:00Z",
                }),
            );
        }

        return Promise.resolve(createJsonResponse({}));
    });

    vi.stubGlobal("fetch", fetchMock);
    return fetchMock;
}

function renderRoute(initialEntry: string) {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: { retry: false },
        },
    });

    return render(
        <MemoryRouter initialEntries={[initialEntry]}>
            <QueryClientProvider client={queryClient}>
                <AuthProvider>
                    <AppRoutes />
                </AuthProvider>
            </QueryClientProvider>
        </MemoryRouter>,
    );
}

describe("AppRoutes", () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it("renders the operational overview for an authenticated user", async () => {
        installAuthenticatedFetchMock();
        renderRoute("/");

        expect(
            await screen.findByRole("heading", {
                name: "Operational readiness",
            }),
        ).toBeInTheDocument();
    });

    it("supports accessible primary navigation", async () => {
        installAuthenticatedFetchMock();
        const user = userEvent.setup();
        renderRoute("/");

        await screen.findByRole("heading", {
            name: "Operational readiness",
        });

        await user.click(
            screen.getByRole("link", {
                name: "Pipeline runs",
            }),
        );

        expect(
            await screen.findAllByRole("heading", {
                name: "Pipeline runs",
            }),
        ).not.toHaveLength(0);
    });

    it("renders the not-found route", async () => {
        installAuthenticatedFetchMock();
        renderRoute("/missing-route");

        expect(
            await screen.findByRole("heading", {
                name: "Page not found",
            }),
        ).toBeInTheDocument();
    });
});
