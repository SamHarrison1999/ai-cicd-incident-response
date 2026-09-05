import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";

const mocks: {
    accessToken: string | null;
    currentUser: { userId: string } | null;
    simulateDemoCiRun: ReturnType<typeof vi.fn>;
    getOrganisations: ReturnType<typeof vi.fn>;
    createOrganisation: ReturnType<typeof vi.fn>;
    getProjects: ReturnType<typeof vi.fn>;
    createProject: ReturnType<typeof vi.fn>;
} = vi.hoisted(() => ({
    accessToken: "token",
    currentUser: { userId: "user-1" },
    simulateDemoCiRun: vi.fn(),
    getOrganisations: vi.fn(),
    createOrganisation: vi.fn(),
    getProjects: vi.fn(),
    createProject: vi.fn(),
}));

vi.mock("../src/auth/useAuth", () => ({
    useAuth: () => ({
        accessToken: mocks.accessToken,
        currentUser: mocks.currentUser,
    }),
}));

vi.mock("../src/hooks/useControlPlaneStatus", () => ({
    useControlPlaneStatus: () => ({
        isPending: false,
        isSuccess: true,
        isError: false,
        data: { version: "1.0", status: "UP" },
    }),
}));

vi.mock("../src/api/demoCi", () => ({
    simulateDemoCiRun: mocks.simulateDemoCiRun,
}));

vi.mock("../src/api/organisations", () => ({
    getOrganisations: mocks.getOrganisations,
    createOrganisation: mocks.createOrganisation,
}));

vi.mock("../src/api/projects", () => ({
    getProjects: mocks.getProjects,
    createProject: mocks.createProject,
}));

import { DashboardPage } from "../src/pages/DashboardPage";

function renderPage() {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: { retry: false },
            mutations: { retry: false },
        },
    });

    return render(
        <MemoryRouter>
            <QueryClientProvider client={queryClient}>
                <DashboardPage />
            </QueryClientProvider>
        </MemoryRouter>,
    );
}

function seedWorkspace() {
    sessionStorage.setItem(
        "incident-response.workspace",
        JSON.stringify({
            organisationId: "org",
            projectId: "project",
            incidentId: "",
        }),
    );
}

beforeEach(() => {
    cleanup();
    sessionStorage.clear();
    vi.clearAllMocks();
    mocks.accessToken = "token";
    mocks.currentUser = { userId: "user-1" };
    mocks.getOrganisations.mockReset();
    mocks.createOrganisation.mockReset();
    mocks.getProjects.mockReset();
    mocks.createProject.mockReset();
    mocks.simulateDemoCiRun.mockReset();
    mocks.simulateDemoCiRun.mockResolvedValue({ outcome: "FAILED" });
});

describe("Overview one-click portfolio demo", () => {
    it("runs the deterministic failed CI demo in the selected workspace", async () => {
        seedWorkspace();
        const user = userEvent.setup();

        renderPage();
        await user.click(
            screen.getByRole("button", { name: "Run one-click demo" }),
        );

        await waitFor(() => {
            expect(mocks.simulateDemoCiRun).toHaveBeenCalledWith(
                "token",
                "org",
                "project",
                {
                    pipelineName: "portfolio-demo",
                    branch: "main",
                    outcome: "FAILED",
                },
            );
        });
        expect(await screen.findByRole("status")).toHaveTextContent(
            "Demo CI run accepted",
        );
    });

    it("creates a portfolio workspace when no workspace is selected", async () => {
        mocks.getOrganisations.mockResolvedValue([]);
        mocks.createOrganisation.mockResolvedValue({
            id: "portfolio-org",
            slug: "portfolio-verification-user-1",
        });
        mocks.getProjects.mockResolvedValue([]);
        mocks.createProject.mockResolvedValue({
            id: "incident-project",
            slug: "incident-demo",
        });
        const user = userEvent.setup();

        renderPage();
        await user.click(
            screen.getByRole("button", { name: "Run one-click demo" }),
        );

        await waitFor(() => {
            expect(mocks.createOrganisation).toHaveBeenCalledWith("token", {
                name: "Portfolio Verification",
                slug: "portfolio-verification-user-1",
            });
            expect(mocks.createProject).toHaveBeenCalledWith(
                "token",
                "portfolio-org",
                {
                    name: "Incident Demo",
                    slug: "incident-demo",
                    description: "Portfolio project",
                },
            );
            expect(mocks.simulateDemoCiRun).toHaveBeenCalled();
        });
    });

    it("reuses an existing portfolio workspace when available", async () => {
        mocks.getOrganisations.mockResolvedValue([
            { id: "portfolio-org", slug: "portfolio-verification-user-1" },
        ]);
        mocks.getProjects.mockResolvedValue([
            { id: "incident-project", slug: "incident-demo" },
        ]);
        const user = userEvent.setup();

        renderPage();
        await user.click(
            screen.getByRole("button", { name: "Run one-click demo" }),
        );

        await waitFor(() => {
            expect(mocks.simulateDemoCiRun).toHaveBeenCalledWith(
                "token",
                "portfolio-org",
                "incident-project",
                {
                    pipelineName: "portfolio-demo",
                    branch: "main",
                    outcome: "FAILED",
                },
            );
        });
        expect(mocks.createOrganisation).not.toHaveBeenCalled();
        expect(mocks.createProject).not.toHaveBeenCalled();
    });

    it("allows an authenticated user without a selected workspace", () => {
        mocks.currentUser = null;
        renderPage();

        expect(
            screen.getByRole("button", { name: "Run one-click demo" }),
        ).not.toBeDisabled();
        expect(
            screen.getByText(
                "A portfolio demo workspace will be created automatically if needed.",
            ),
        ).toBeInTheDocument();
    });

    it("requires authentication", () => {
        mocks.accessToken = null;
        mocks.currentUser = null;
        renderPage();

        expect(
            screen.getByRole("button", { name: "Run one-click demo" }),
        ).toBeDisabled();
    });

    it("shows a pending state while the demo is running", async () => {
        seedWorkspace();
        mocks.simulateDemoCiRun.mockReturnValue(new Promise(() => undefined));
        const user = userEvent.setup();

        renderPage();
        await user.click(
            screen.getByRole("button", { name: "Run one-click demo" }),
        );

        expect(
            await screen.findByRole("button", { name: "Preparing demo..." }),
        ).toBeDisabled();
    });

    it("shows a bounded failure state", async () => {
        seedWorkspace();
        mocks.simulateDemoCiRun.mockRejectedValue(new Error("failed"));
        const user = userEvent.setup();

        renderPage();
        await user.click(
            screen.getByRole("button", { name: "Run one-click demo" }),
        );

        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Portfolio demo could not be started.",
        );
    });
});
