import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

const mocks = vi.hoisted(() => ({
    accessToken: null as string | null,
    getOrganisations: vi.fn(),
    getProjects: vi.fn(),
}));

vi.mock("../src/auth/useAuth", () => ({
    useAuth: () => ({
        accessToken: mocks.accessToken,
    }),
}));

vi.mock("../src/api/organisations", () => ({
    getOrganisations: mocks.getOrganisations,
}));

vi.mock("../src/api/projects", () => ({
    getProjects: mocks.getProjects,
}));

import { WorkspaceSwitcher } from "../src/components/layout/WorkspaceSwitcher";
import {
    readWorkspaceSelection,
    WORKSPACE_STORAGE_KEY,
} from "../src/workspace/useWorkspace";

const organisationOne = {
    id: "org-1",
    name: "Portfolio Verification",
    slug: "portfolio-verification",
    createdAt: "now",
    updatedAt: "now",
    version: 1,
};

const organisationTwo = {
    ...organisationOne,
    id: "org-2",
    name: "Engineering Sandbox",
    slug: "engineering-sandbox",
};

const projectOne = {
    id: "project-1",
    organisationId: "org-1",
    name: "Incident Demo",
    slug: "incident-demo",
    description: "Portfolio project",
    status: "ACTIVE",
    createdAt: "now",
    updatedAt: "now",
    version: 1,
};

const projectTwo = {
    ...projectOne,
    id: "project-2",
    organisationId: "org-2",
    name: "Payments",
    slug: "payments",
};

const projectThree = {
    ...projectTwo,
    id: "project-3",
    name: "Platform",
    slug: "platform",
};

const archivedProject = {
    ...projectOne,
    id: "project-archived",
    status: "ARCHIVED",
    name: "Archived project",
};

function renderSwitcher() {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: {
                retry: false,
            },
        },
    });

    return render(
        <QueryClientProvider client={queryClient}>
            <WorkspaceSwitcher />
        </QueryClientProvider>,
    );
}

describe("global workspace switcher", () => {
    beforeEach(() => {
        cleanup();
        sessionStorage.clear();
        vi.clearAllMocks();

        mocks.accessToken = "token";

        mocks.getOrganisations.mockResolvedValue([organisationOne]);

        mocks.getProjects.mockResolvedValue([projectOne]);
    });

    it("auto-selects the only organisation and active project", async () => {
        renderSwitcher();

        await waitFor(() => {
            expect(
                screen.getByRole("combobox", {
                    name: "Organisation",
                }),
            ).toHaveValue("org-1");
        });

        await waitFor(() => {
            expect(
                screen.getByRole("combobox", {
                    name: "Project",
                }),
            ).toHaveValue("project-1");
        });

        expect(readWorkspaceSelection()).toEqual({
            organisationId: "org-1",
            projectId: "project-1",
            incidentId: "",
        });
    });

    it("shows all choices and switches organisation and project by name", async () => {
        const user = userEvent.setup();

        sessionStorage.setItem(
            WORKSPACE_STORAGE_KEY,
            JSON.stringify({
                organisationId: "stale",
                projectId: "stale",
                incidentId: "stale",
            }),
        );

        mocks.getOrganisations.mockResolvedValue([
            organisationOne,
            organisationTwo,
        ]);

        mocks.getProjects.mockImplementation(
            (_token: string, organisationId: string) =>
                Promise.resolve(
                    organisationId === "org-2"
                        ? [projectTwo, projectThree]
                        : [projectOne],
                ),
        );

        renderSwitcher();

        const organisationSelect = screen.getByRole("combobox", {
            name: "Organisation",
        });

        await waitFor(() => {
            expect(
                screen.getByRole("option", {
                    name: "Portfolio Verification",
                }),
            ).toBeInTheDocument();

            expect(
                screen.getByRole("option", {
                    name: "Engineering Sandbox",
                }),
            ).toBeInTheDocument();

            expect(organisationSelect).toHaveValue("");
        });

        await user.selectOptions(organisationSelect, "org-2");

        const projectSelect = screen.getByRole("combobox", {
            name: "Project",
        });

        await waitFor(() => {
            expect(mocks.getProjects).toHaveBeenCalledWith("token", "org-2");
        });

        expect(
            await screen.findByRole("option", {
                name: "Payments",
            }),
        ).toBeInTheDocument();

        expect(
            screen.getByRole("option", {
                name: "Platform",
            }),
        ).toBeInTheDocument();

        await user.selectOptions(projectSelect, "project-3");

        await waitFor(() => {
            expect(readWorkspaceSelection()).toEqual({
                organisationId: "org-2",
                projectId: "project-3",
                incidentId: "",
            });
        });

        await user.selectOptions(organisationSelect, "org-1");

        await waitFor(() => {
            expect(projectSelect).toHaveValue("project-1");
        });
    });

    it("clears a stale project when multiple valid projects remain", async () => {
        sessionStorage.setItem(
            WORKSPACE_STORAGE_KEY,
            JSON.stringify({
                organisationId: "org-2",
                projectId: "stale-project",
                incidentId: "stale-incident",
            }),
        );

        mocks.getOrganisations.mockResolvedValue([
            organisationOne,
            organisationTwo,
        ]);

        mocks.getProjects.mockResolvedValue([projectTwo, projectThree]);

        renderSwitcher();

        const projectSelect = screen.getByRole("combobox", {
            name: "Project",
        });

        await waitFor(() => {
            expect(readWorkspaceSelection()).toEqual({
                organisationId: "org-2",
                projectId: "",
                incidentId: "",
            });
        });

        expect(projectSelect).toHaveValue("");
    });

    it("disables selection without authentication", () => {
        mocks.accessToken = null;

        renderSwitcher();

        expect(
            screen.getByRole("combobox", {
                name: "Organisation",
            }),
        ).toBeDisabled();

        expect(
            screen.getByRole("combobox", {
                name: "Project",
            }),
        ).toBeDisabled();
    });

    it("handles an empty account", async () => {
        mocks.getOrganisations.mockResolvedValue([]);

        renderSwitcher();

        await waitFor(() => {
            expect(
                screen.getByRole("combobox", {
                    name: "Organisation",
                }),
            ).toBeDisabled();
        });

        expect(mocks.getProjects).not.toHaveBeenCalled();
    });

    it("shows organisation discovery failure without crashing", async () => {
        mocks.getOrganisations.mockRejectedValue(new Error("unavailable"));

        renderSwitcher();

        expect(
            await screen.findByText("Organisations unavailable"),
        ).toBeInTheDocument();

        expect(
            screen.getByRole("combobox", {
                name: "Organisation",
            }),
        ).toBeDisabled();
    });

    it("shows project discovery failure without losing the organisation", async () => {
        sessionStorage.setItem(
            WORKSPACE_STORAGE_KEY,
            JSON.stringify({
                organisationId: "org-1",
                projectId: "",
                incidentId: "",
            }),
        );

        mocks.getProjects.mockRejectedValue(new Error("unavailable"));

        renderSwitcher();

        expect(
            await screen.findByText("Projects unavailable"),
        ).toBeInTheDocument();

        expect(
            screen.getByRole("combobox", {
                name: "Project",
            }),
        ).toBeDisabled();

        expect(readWorkspaceSelection().organisationId).toBe("org-1");
    });

    it("does not offer archived projects", async () => {
        sessionStorage.setItem(
            WORKSPACE_STORAGE_KEY,
            JSON.stringify({
                organisationId: "org-1",
                projectId: "",
                incidentId: "",
            }),
        );

        mocks.getProjects.mockResolvedValue([archivedProject]);

        renderSwitcher();

        await waitFor(() => {
            expect(mocks.getProjects).toHaveBeenCalled();
        });

        expect(
            screen.queryByRole("option", {
                name: "Archived project",
            }),
        ).not.toBeInTheDocument();

        expect(
            screen.getByRole("combobox", {
                name: "Project",
            }),
        ).toBeDisabled();
    });
});
