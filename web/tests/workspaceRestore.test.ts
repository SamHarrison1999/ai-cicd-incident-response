const api = vi.hoisted(() => ({
    getOrganisations: vi.fn(),
    getProjects: vi.fn(),
}));

vi.mock("../src/api/organisations", () => ({
    getOrganisations: api.getOrganisations,
}));

vi.mock("../src/api/projects", () => ({
    getProjects: api.getProjects,
}));

import { restoreWorkspaceForAccount } from "../src/workspace/restoreWorkspace";
import {
    readWorkspaceSelection,
    WORKSPACE_STORAGE_KEY,
} from "../src/workspace/useWorkspace";

const organisation = {
    id: "org-1",
    name: "Portfolio Verification",
    slug: "portfolio-verification",
    createdAt: "now",
    updatedAt: "now",
    version: 1,
};

const secondOrganisation = {
    ...organisation,
    id: "org-2",
    name: "Second",
    slug: "second",
};

const activeProject = {
    id: "project-1",
    organisationId: "org-1",
    name: "Incident Demo",
    slug: "incident-demo",
    description: "Portfolio demo",
    status: "ACTIVE",
    createdAt: "now",
    updatedAt: "now",
    version: 1,
};

const secondActiveProject = {
    ...activeProject,
    id: "project-2",
    name: "Second project",
    slug: "second-project",
};

const archivedProject = {
    ...activeProject,
    id: "project-archived",
    name: "Archived",
    slug: "archived",
    status: "ARCHIVED",
};

describe("account workspace restoration", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        sessionStorage.clear();
        api.getOrganisations.mockResolvedValue([organisation]);
        api.getProjects.mockResolvedValue([activeProject]);
    });

    it("auto-selects the only organisation and active project", async () => {
        await restoreWorkspaceForAccount("token");

        expect(api.getOrganisations).toHaveBeenCalledWith("token");
        expect(api.getProjects).toHaveBeenCalledWith("token", "org-1");

        expect(readWorkspaceSelection()).toEqual({
            organisationId: "org-1",
            projectId: "project-1",
            incidentId: "",
        });
    });

    it("preserves a valid stored workspace and incident", async () => {
        sessionStorage.setItem(
            WORKSPACE_STORAGE_KEY,
            JSON.stringify({
                organisationId: "org-1",
                projectId: "project-2",
                incidentId: "incident-1",
            }),
        );

        api.getOrganisations.mockResolvedValue([
            organisation,
            secondOrganisation,
        ]);

        api.getProjects.mockResolvedValue([activeProject, secondActiveProject]);

        await restoreWorkspaceForAccount("token");

        expect(readWorkspaceSelection()).toEqual({
            organisationId: "org-1",
            projectId: "project-2",
            incidentId: "incident-1",
        });
    });

    it("replaces a stale workspace when there is one valid choice", async () => {
        sessionStorage.setItem(
            WORKSPACE_STORAGE_KEY,
            JSON.stringify({
                organisationId: "stale-org",
                projectId: "stale-project",
                incidentId: "stale-incident",
            }),
        );

        api.getProjects.mockResolvedValue([archivedProject, activeProject]);

        await restoreWorkspaceForAccount("token");

        expect(readWorkspaceSelection()).toEqual({
            organisationId: "org-1",
            projectId: "project-1",
            incidentId: "",
        });
    });

    it("requires explicit organisation selection when several are available", async () => {
        sessionStorage.setItem(
            WORKSPACE_STORAGE_KEY,
            JSON.stringify({
                organisationId: "stale-org",
                projectId: "stale-project",
                incidentId: "stale-incident",
            }),
        );

        api.getOrganisations.mockResolvedValue([
            organisation,
            secondOrganisation,
        ]);

        await restoreWorkspaceForAccount("token");

        expect(api.getProjects).not.toHaveBeenCalled();

        expect(readWorkspaceSelection()).toEqual({
            organisationId: "",
            projectId: "",
            incidentId: "",
        });
    });

    it("requires explicit project selection when several active projects are available", async () => {
        api.getProjects.mockResolvedValue([activeProject, secondActiveProject]);

        await restoreWorkspaceForAccount("token");

        expect(readWorkspaceSelection()).toEqual({
            organisationId: "org-1",
            projectId: "",
            incidentId: "",
        });
    });

    it("ignores archived projects when choosing the only active project", async () => {
        api.getProjects.mockResolvedValue([archivedProject, activeProject]);

        await restoreWorkspaceForAccount("token");

        expect(readWorkspaceSelection()).toEqual({
            organisationId: "org-1",
            projectId: "project-1",
            incidentId: "",
        });
    });

    it("leaves the project empty when no active project exists", async () => {
        api.getProjects.mockResolvedValue([archivedProject]);

        await restoreWorkspaceForAccount("token");

        expect(readWorkspaceSelection()).toEqual({
            organisationId: "org-1",
            projectId: "",
            incidentId: "",
        });
    });
});
