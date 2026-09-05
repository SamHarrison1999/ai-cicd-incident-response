import { useCallback, useEffect, useState } from "react";

export const WORKSPACE_STORAGE_KEY = "incident-response.workspace";

const WORKSPACE_CHANGED_EVENT = "incident-response.workspace.changed";

export interface WorkspaceSelection {
    organisationId: string;
    projectId: string;
    incidentId: string;
}

export function emptyWorkspaceSelection(): WorkspaceSelection {
    return {
        organisationId: "",
        projectId: "",
        incidentId: "",
    };
}

export function readWorkspaceSelection(): WorkspaceSelection {
    const stored = sessionStorage.getItem(WORKSPACE_STORAGE_KEY);

    if (stored === null) {
        return emptyWorkspaceSelection();
    }

    try {
        const parsed = JSON.parse(stored) as Partial<WorkspaceSelection> | null;

        const organisationId =
            typeof parsed?.organisationId === "string"
                ? parsed.organisationId
                : "";

        const projectId =
            organisationId.length > 0 && typeof parsed?.projectId === "string"
                ? parsed.projectId
                : "";

        const incidentId =
            projectId.length > 0 && typeof parsed?.incidentId === "string"
                ? parsed.incidentId
                : "";

        return {
            organisationId,
            projectId,
            incidentId,
        };
    } catch {
        return emptyWorkspaceSelection();
    }
}

export function writeWorkspaceSelection(workspace: WorkspaceSelection): void {
    sessionStorage.setItem(WORKSPACE_STORAGE_KEY, JSON.stringify(workspace));
}

function publishWorkspaceChange(): void {
    window.dispatchEvent(new Event(WORKSPACE_CHANGED_EVENT));
}

export function useWorkspace() {
    const [workspace, setWorkspace] = useState<WorkspaceSelection>(
        readWorkspaceSelection,
    );

    useEffect(() => {
        const handleWorkspaceChange = () => {
            setWorkspace(readWorkspaceSelection());
        };

        window.addEventListener(WORKSPACE_CHANGED_EVENT, handleWorkspaceChange);

        return () => {
            window.removeEventListener(
                WORKSPACE_CHANGED_EVENT,
                handleWorkspaceChange,
            );
        };
    }, []);

    const updateWorkspace = useCallback(
        (transform: (current: WorkspaceSelection) => WorkspaceSelection) => {
            const current = readWorkspaceSelection();

            const next = transform(current);

            writeWorkspaceSelection(next);
            setWorkspace(next);
            publishWorkspaceChange();
        },
        [],
    );

    const setOrganisationId = useCallback(
        (organisationId: string) => {
            updateWorkspace(() => ({
                organisationId,
                projectId: "",
                incidentId: "",
            }));
        },
        [updateWorkspace],
    );

    const setProjectId = useCallback(
        (projectId: string) => {
            updateWorkspace((current) => ({
                ...current,
                projectId,
                incidentId: "",
            }));
        },
        [updateWorkspace],
    );

    const setIncidentId = useCallback(
        (incidentId: string) => {
            updateWorkspace((current) => ({
                ...current,
                incidentId,
            }));
        },
        [updateWorkspace],
    );

    const clearWorkspace = useCallback(() => {
        updateWorkspace(() => emptyWorkspaceSelection());
    }, [updateWorkspace]);

    return {
        ...workspace,
        setOrganisationId,
        setProjectId,
        setIncidentId,
        clearWorkspace,
    };
}
