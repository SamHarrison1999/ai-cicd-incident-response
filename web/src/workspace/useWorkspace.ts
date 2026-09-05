import { useState } from "react";

const STORAGE_KEY = "incident-response.workspace";

export interface WorkspaceSelection {
    organisationId: string;
    projectId: string;
    incidentId: string;
}

function emptyWorkspace(): WorkspaceSelection {
    return {
        organisationId: "",
        projectId: "",
        incidentId: "",
    };
}

function readWorkspace(): WorkspaceSelection {
    const stored = sessionStorage.getItem(STORAGE_KEY);

    return stored === null
        ? emptyWorkspace()
        : (JSON.parse(stored) as WorkspaceSelection);
}

export function useWorkspace() {
    const [workspace, setWorkspace] =
        useState<WorkspaceSelection>(readWorkspace);

    function updateWorkspace(
        transform: (current: WorkspaceSelection) => WorkspaceSelection,
    ) {
        setWorkspace((current) => {
            const next = transform(current);
            sessionStorage.setItem(STORAGE_KEY, JSON.stringify(next));
            return next;
        });
    }

    function setOrganisationId(organisationId: string) {
        updateWorkspace(() => ({
            organisationId,
            projectId: "",
            incidentId: "",
        }));
    }

    function setProjectId(projectId: string) {
        updateWorkspace((current) => ({
            ...current,
            projectId,
            incidentId: "",
        }));
    }

    function setIncidentId(incidentId: string) {
        updateWorkspace((current) => ({
            ...current,
            incidentId,
        }));
    }

    function clearWorkspace() {
        updateWorkspace(() => emptyWorkspace());
    }

    return {
        ...workspace,
        setOrganisationId,
        setProjectId,
        setIncidentId,
        clearWorkspace,
    };
}
