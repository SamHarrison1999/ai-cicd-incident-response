import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";

import { AppRoutes } from "../src/app/AppRoutes";

function renderApplication(initialEntry = "/") {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: {
                retry: false,
            },
        },
    });

    return render(
        <MemoryRouter initialEntries={[initialEntry]}>
            <QueryClientProvider client={queryClient}>
                <AppRoutes />
            </QueryClientProvider>
        </MemoryRouter>,
    );
}

describe("AppRoutes", () => {
    beforeEach(() => {
        vi.stubGlobal(
            "fetch",
            vi.fn().mockResolvedValue(
                new Response(
                    JSON.stringify({
                        service: "control-plane",
                        version: "0.1.0-SNAPSHOT",
                        status: "UP",
                        timestamp: "2026-08-01T12:00:00Z",
                    }),
                    {
                        status: 200,
                        headers: { "Content-Type": "application/json" },
                    },
                ),
            ),
        );
    });

    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it("renders the operational overview", async () => {
        renderApplication();

        expect(
            await screen.findByRole("heading", {
                name: "Operational readiness",
            }),
        ).toBeInTheDocument();
        expect(screen.getByText("Human review required")).toBeInTheDocument();
        expect(
            await screen.findByText("Version 0.1.0-SNAPSHOT Â· UP"),
        ).toBeInTheDocument();
    });

    it("supports accessible primary navigation", async () => {
        const user = userEvent.setup();
        renderApplication();

        await user.click(screen.getByRole("link", { name: "Incidents" }));

        expect(
            screen.getByRole("heading", { name: "Incidents" }),
        ).toBeInTheDocument();
        expect(screen.getByText("No incidents created")).toBeInTheDocument();
    });

    it("renders the not-found route", () => {
        renderApplication("/does-not-exist");

        expect(
            screen.getByRole("heading", { name: "Page not found" }),
        ).toBeInTheDocument();
    });
});
