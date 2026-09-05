import { useQuery } from "@tanstack/react-query";
import { useEffect, useMemo } from "react";

import { getOrganisations } from "../../api/organisations";
import { getProjects } from "../../api/projects";
import { useAuth } from "../../auth/useAuth";
import { useWorkspace } from "../../workspace/useWorkspace";

export function WorkspaceSwitcher() {
    const { accessToken } = useAuth();

    const queryAccessToken = accessToken === null ? "" : accessToken;

    const { organisationId, projectId, setOrganisationId, setProjectId } =
        useWorkspace();

    const organisations = useQuery({
        queryKey: ["organisations"],
        queryFn: () => getOrganisations(queryAccessToken),
        enabled: accessToken !== null,
    });

    const organisationOptions = useMemo(
        () => (Array.isArray(organisations.data) ? organisations.data : []),
        [organisations.data],
    );

    const projects = useQuery({
        queryKey: ["projects", organisationId],
        queryFn: () => getProjects(queryAccessToken, organisationId),
        enabled: accessToken !== null && organisationId.length > 0,
    });

    const activeProjects = useMemo(
        () =>
            Array.isArray(projects.data)
                ? projects.data.filter((project) => project.status === "ACTIVE")
                : [],
        [projects.data],
    );

    useEffect(() => {
        if (organisations.data === undefined) {
            return;
        }

        const currentIsValid = organisationOptions.some(
            (organisation) => organisation.id === organisationId,
        );

        if (currentIsValid) {
            return;
        }

        const onlyOrganisation =
            organisationOptions.length === 1
                ? organisationOptions[0]
                : undefined;

        if (onlyOrganisation !== undefined) {
            setOrganisationId(onlyOrganisation.id);
            return;
        }

        if (organisationId.length > 0) {
            setOrganisationId("");
        }
    }, [
        organisationId,
        organisationOptions,
        organisations.data,
        setOrganisationId,
    ]);

    useEffect(() => {
        if (organisationId.length === 0 || projects.data === undefined) {
            return;
        }

        const currentIsValid = activeProjects.some(
            (project) => project.id === projectId,
        );

        if (currentIsValid) {
            return;
        }

        const onlyProject =
            activeProjects.length === 1 ? activeProjects[0] : undefined;

        if (onlyProject !== undefined) {
            setProjectId(onlyProject.id);
            return;
        }

        if (projectId.length > 0) {
            setProjectId("");
        }
    }, [
        activeProjects,
        organisationId,
        projectId,
        projects.data,
        setProjectId,
    ]);

    return (
        <div className="workspace-switcher" aria-label="Workspace selection">
            <label>
                <span>Organisation</span>
                <select
                    value={organisationId}
                    disabled={
                        accessToken === null ||
                        organisations.isPending ||
                        organisations.isError ||
                        organisationOptions.length === 0
                    }
                    onChange={(event) => {
                        setOrganisationId(event.target.value);
                    }}
                >
                    <option value="">Select organisation</option>

                    {organisationOptions.map((organisation) => (
                        <option key={organisation.id} value={organisation.id}>
                            {organisation.name}
                        </option>
                    ))}
                </select>
            </label>

            <label>
                <span>Project</span>
                <select
                    value={projectId}
                    disabled={
                        accessToken === null ||
                        organisationId.length === 0 ||
                        projects.isPending ||
                        projects.isError ||
                        activeProjects.length === 0
                    }
                    onChange={(event) => {
                        setProjectId(event.target.value);
                    }}
                >
                    <option value="">Select project</option>

                    {activeProjects.map((project) => (
                        <option key={project.id} value={project.id}>
                            {project.name}
                        </option>
                    ))}
                </select>
            </label>

            {organisations.isError ? (
                <span className="workspace-switcher-error" role="status">
                    Organisations unavailable
                </span>
            ) : null}

            {projects.isError ? (
                <span className="workspace-switcher-error" role="status">
                    Projects unavailable
                </span>
            ) : null}
        </div>
    );
}
