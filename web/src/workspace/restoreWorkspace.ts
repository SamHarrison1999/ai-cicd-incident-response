import { getOrganisations } from "../api/organisations";
import { getProjects } from "../api/projects";
import {
    emptyWorkspaceSelection,
    readWorkspaceSelection,
    writeWorkspaceSelection,
} from "./useWorkspace";

export async function restoreWorkspaceForAccount(
    accessToken: string,
): Promise<void> {
    const stored = readWorkspaceSelection();
    const organisations = await getOrganisations(accessToken);

    const storedOrganisation = organisations.find(
        (organisation) => organisation.id === stored.organisationId,
    );

    const selectedOrganisation =
        storedOrganisation ??
        (organisations.length === 1 ? organisations[0] : undefined);

    if (selectedOrganisation === undefined) {
        writeWorkspaceSelection(emptyWorkspaceSelection());
        return;
    }

    const projects = await getProjects(accessToken, selectedOrganisation.id);

    const activeProjects = projects.filter(
        (project) => project.status === "ACTIVE",
    );

    const storedProject =
        stored.organisationId === selectedOrganisation.id
            ? activeProjects.find((project) => project.id === stored.projectId)
            : undefined;

    const selectedProject =
        storedProject ??
        (activeProjects.length === 1 ? activeProjects[0] : undefined);

    const projectId = selectedProject?.id ?? "";

    const preserveIncident =
        projectId.length > 0 &&
        stored.organisationId === selectedOrganisation.id &&
        stored.projectId === projectId;

    writeWorkspaceSelection({
        organisationId: selectedOrganisation.id,
        projectId,
        incidentId: preserveIncident ? stored.incidentId : "",
    });
}
