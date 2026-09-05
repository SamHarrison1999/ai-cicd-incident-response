import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

const mocks = vi.hoisted(() => ({
    getOrganisations: vi.fn(),
    createOrganisation: vi.fn(),
    getProjects: vi.fn(),
    createProject: vi.fn(),
}));

vi.mock("../src/auth/useAuth", () => ({
    useAuth: () => ({
        accessToken: "token",
    }),
}));

vi.mock("../src/api/organisations", () => ({
    getOrganisations: mocks.getOrganisations,
    createOrganisation: mocks.createOrganisation,
}));

vi.mock("../src/api/projects", () => ({
    getProjects: mocks.getProjects,
    createProject: mocks.createProject,
}));

import { OrganisationsPage } from "../src/pages/OrganisationsPage";

function renderPage() {
    cleanup();

    const queryClient = new QueryClient({
        defaultOptions: {
            queries: { retry: false },
            mutations: { retry: false },
        },
    });

    return render(
        <QueryClientProvider client={queryClient}>
            <OrganisationsPage />
        </QueryClientProvider>,
    );
}

function organisation() {
    return {
        id: "org",
        name: "Portfolio organisation",
        slug: "portfolio-organisation",
        createdAt: "now",
        updatedAt: "now",
        version: 1,
    };
}

function project() {
    return {
        id: "project",
        organisationId: "org",
        name: "Incident response",
        slug: "incident-response",
        description: "Portfolio project",
        status: "ACTIVE",
        createdAt: "now",
        updatedAt: "now",
        version: 1,
    };
}

describe("organisation project workspace", () => {
    beforeEach(() => {
        sessionStorage.clear();
        vi.clearAllMocks();

        mocks.getOrganisations.mockResolvedValue([organisation()]);
        mocks.getProjects.mockResolvedValue([project()]);

        mocks.createOrganisation.mockResolvedValue({
            ...organisation(),
            id: "new-org",
            name: "New organisation",
            slug: "new-organisation",
        });

        mocks.createProject.mockResolvedValue({
            ...project(),
            id: "new-project",
            name: "New project",
            slug: "new-project",
        });
    });

    it("selects and creates organisations and projects without UUID entry", async () => {
        const user = userEvent.setup();

        renderPage();

        expect(
            screen.getByText("Select an organisation to manage its projects."),
        ).toBeInTheDocument();

        expect(
            screen.getByRole("button", {
                name: "Create project",
            }),
        ).toBeDisabled();

        await user.click(
            await screen.findByRole("button", {
                name: "Use workspace",
            }),
        );

        expect(
            await screen.findByText("Incident response"),
        ).toBeInTheDocument();

        expect(
            screen.getByRole("button", {
                name: "Selected workspace",
            }),
        ).toHaveAttribute("aria-pressed", "true");

        await user.click(
            screen.getByRole("button", {
                name: "Use project",
            }),
        );

        expect(
            screen.getByRole("button", {
                name: "Selected project",
            }),
        ).toHaveAttribute("aria-pressed", "true");

        let stored = JSON.parse(
            sessionStorage.getItem("incident-response.workspace") ?? "{}",
        ) as {
            organisationId?: string;
            projectId?: string;
        };

        expect(stored.organisationId).toBe("org");
        expect(stored.projectId).toBe("project");

        await user.type(screen.getByLabelText("Project name"), "New project");
        await user.type(screen.getByLabelText(/^Project slug/), "new-project");
        await user.type(
            screen.getByLabelText("Project description"),
            "Created in the browser",
        );

        await user.click(
            screen.getByRole("button", {
                name: "Create project",
            }),
        );

        await waitFor(() => {
            expect(mocks.createProject).toHaveBeenCalledWith("token", "org", {
                name: "New project",
                slug: "new-project",
                description: "Created in the browser",
            });
        });

        stored = JSON.parse(
            sessionStorage.getItem("incident-response.workspace") ?? "{}",
        ) as {
            organisationId?: string;
            projectId?: string;
        };

        expect(stored.projectId).toBe("new-project");

        await user.type(screen.getByLabelText("Name"), "New organisation");
        await user.type(screen.getByLabelText(/^Slug/), "new-organisation");

        await user.click(
            screen.getByRole("button", {
                name: "Create organisation",
            }),
        );

        await waitFor(() => {
            expect(mocks.createOrganisation).toHaveBeenCalled();
        });

        stored = JSON.parse(
            sessionStorage.getItem("incident-response.workspace") ?? "{}",
        ) as {
            organisationId?: string;
            projectId?: string;
        };

        expect(stored.organisationId).toBe("new-org");
        expect(stored.projectId).toBe("");
    });

    it("shows project loading, empty, error, and mutation states", async () => {
        const user = userEvent.setup();
        const never = new Promise<never>(() => undefined);

        mocks.getProjects.mockReset();
        mocks.getProjects.mockReturnValue(never);

        renderPage();

        await user.click(
            await screen.findByRole("button", {
                name: "Use workspace",
            }),
        );

        expect(
            await screen.findByText("Loading projects\u2026"),
        ).toBeInTheDocument();

        cleanup();
        sessionStorage.clear();

        mocks.getProjects.mockReset();
        mocks.getProjects.mockRejectedValue(new Error("projects"));

        renderPage();

        await user.click(
            await screen.findByRole("button", {
                name: "Use workspace",
            }),
        );

        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Projects could not be loaded.",
        );

        cleanup();
        sessionStorage.clear();

        mocks.getProjects.mockReset();
        mocks.getProjects.mockResolvedValue([]);

        renderPage();

        await user.click(
            await screen.findByRole("button", {
                name: "Use workspace",
            }),
        );

        expect(
            await screen.findByText(
                "No projects yet. Create the first project in this organisation.",
            ),
        ).toBeInTheDocument();

        cleanup();
        sessionStorage.clear();

        mocks.getProjects.mockReset();
        mocks.getProjects.mockResolvedValue([]);
        mocks.createProject.mockReset();
        mocks.createProject.mockRejectedValue(new Error("create project"));

        renderPage();

        await user.click(
            await screen.findByRole("button", {
                name: "Use workspace",
            }),
        );

        await user.type(
            screen.getByLabelText("Project name"),
            "Broken project",
        );
        await user.type(
            screen.getByLabelText(/^Project slug/),
            "broken-project",
        );

        await user.click(
            screen.getByRole("button", {
                name: "Create project",
            }),
        );

        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Project creation failed.",
        );

        cleanup();
        sessionStorage.clear();

        mocks.createProject.mockReset();
        mocks.createProject.mockReturnValue(never);

        renderPage();

        await user.click(
            await screen.findByRole("button", {
                name: "Use workspace",
            }),
        );

        await user.type(
            screen.getByLabelText("Project name"),
            "Pending project",
        );
        await user.type(
            screen.getByLabelText(/^Project slug/),
            "pending-project",
        );

        await user.click(
            screen.getByRole("button", {
                name: "Create project",
            }),
        );

        expect(
            await screen.findByRole("button", {
                name: "Creating project\u2026",
            }),
        ).toBeDisabled();
    });
});
