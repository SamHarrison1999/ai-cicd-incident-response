import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { type SyntheticEvent, useState } from "react";

import { createOrganisation, getOrganisations } from "../api/organisations";
import { createProject, getProjects } from "../api/projects";
import { useAuth } from "../auth/useAuth";
import { useWorkspace } from "../workspace/useWorkspace";

export function OrganisationsPage() {
    const { accessToken } = useAuth();
    const queryClient = useQueryClient();

    const { organisationId, projectId, setOrganisationId, setProjectId } =
        useWorkspace();

    const [name, setName] = useState("");
    const [slug, setSlug] = useState("");

    const [projectName, setProjectName] = useState("");
    const [projectSlug, setProjectSlug] = useState("");
    const [projectDescription, setProjectDescription] = useState("");

    const organisations = useQuery({
        queryKey: ["organisations"],
        queryFn: () => getOrganisations(requireAccessToken(accessToken)),
    });

    const projects = useQuery({
        queryKey: ["projects", organisationId],
        queryFn: () =>
            getProjects(requireAccessToken(accessToken), organisationId),
        enabled: organisationId.length > 0,
    });

    const createMutation = useMutation({
        mutationFn: () =>
            createOrganisation(requireAccessToken(accessToken), {
                name,
                slug,
            }),
        onSuccess: async (organisation) => {
            setOrganisationId(organisation.id);
            setName("");
            setSlug("");

            await queryClient.invalidateQueries({
                queryKey: ["organisations"],
            });
        },
    });

    const createProjectMutation = useMutation({
        mutationFn: () =>
            createProject(requireAccessToken(accessToken), organisationId, {
                name: projectName,
                slug: projectSlug,
                description: projectDescription,
            }),
        onSuccess: async (project) => {
            setProjectId(project.id);
            setProjectName("");
            setProjectSlug("");
            setProjectDescription("");

            await queryClient.invalidateQueries({
                queryKey: ["projects", organisationId],
            });
        },
    });

    function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
        event.preventDefault();
        createMutation.mutate();
    }

    function handleProjectSubmit(event: SyntheticEvent<HTMLFormElement>) {
        event.preventDefault();
        createProjectMutation.mutate();
    }

    return (
        <section>
            <div className="page-heading">
                <div>
                    <p className="eyebrow">Tenant administration</p>
                    <h2>Organisations</h2>
                    <p>
                        Create and select the workspace used for projects,
                        pipelines, incidents, evidence, and governed
                        recommendations.
                    </p>
                </div>
            </div>

            <div className="organisation-grid">
                <section className="panel">
                    <h3>Your organisations</h3>

                    {organisations.isPending ? (
                        <p>{"Loading organisations\u2026"}</p>
                    ) : null}

                    {organisations.isError ? (
                        <div className="notice notice-error" role="alert">
                            Organisations could not be loaded.
                        </div>
                    ) : null}

                    {organisations.data?.length === 0 ? (
                        <p className="empty-copy">
                            No organisations yet. Create the first tenant
                            workspace.
                        </p>
                    ) : null}

                    <ul className="organisation-list">
                        {organisations.data?.map((organisation) => (
                            <li key={organisation.id}>
                                <div>
                                    <strong>{organisation.name}</strong>
                                    <span>{organisation.slug}</span>
                                </div>

                                <span className="status-badge">Active</span>

                                <button
                                    className="button button-secondary"
                                    type="button"
                                    aria-pressed={
                                        organisationId === organisation.id
                                    }
                                    onClick={() => {
                                        setOrganisationId(organisation.id);
                                    }}
                                >
                                    {organisationId === organisation.id
                                        ? "Selected workspace"
                                        : "Use workspace"}
                                </button>
                            </li>
                        ))}
                    </ul>
                </section>

                <section className="panel">
                    <h3>Create organisation</h3>

                    <form className="auth-form" onSubmit={handleSubmit}>
                        <label>
                            Name
                            <input
                                value={name}
                                onChange={(event) => {
                                    setName(event.target.value);
                                }}
                                required
                            />
                        </label>

                        <label>
                            Slug
                            <input
                                value={slug}
                                pattern="^[a-z0-9]+(?:-[a-z0-9]+)*$"
                                onChange={(event) => {
                                    setSlug(event.target.value);
                                }}
                                required
                            />
                            <span className="field-hint">
                                Lowercase letters, numbers, and hyphens.
                            </span>
                        </label>

                        {createMutation.isError ? (
                            <div className="notice notice-error" role="alert">
                                Organisation creation failed.
                            </div>
                        ) : null}

                        <button
                            className="button"
                            disabled={createMutation.isPending}
                        >
                            {createMutation.isPending
                                ? "Creating\u2026"
                                : "Create organisation"}
                        </button>
                    </form>
                </section>

                <section className="panel">
                    <h3>Projects in selected organisation</h3>

                    {organisationId.length === 0 ? (
                        <p className="empty-copy">
                            Select an organisation to manage its projects.
                        </p>
                    ) : null}

                    {organisationId.length > 0 && projects.isPending ? (
                        <p>{"Loading projects\u2026"}</p>
                    ) : null}

                    {organisationId.length > 0 && projects.isError ? (
                        <div className="notice notice-error" role="alert">
                            Projects could not be loaded.
                        </div>
                    ) : null}

                    {organisationId.length > 0 &&
                    projects.data?.length === 0 ? (
                        <p className="empty-copy">
                            No projects yet. Create the first project in this
                            organisation.
                        </p>
                    ) : null}

                    <ul className="organisation-list">
                        {projects.data?.map((project) => (
                            <li key={project.id}>
                                <div>
                                    <strong>{project.name}</strong>
                                    <span>{project.slug}</span>
                                </div>

                                <span className="status-badge">
                                    {project.status}
                                </span>

                                <button
                                    className="button button-secondary"
                                    type="button"
                                    aria-pressed={projectId === project.id}
                                    onClick={() => {
                                        setProjectId(project.id);
                                    }}
                                >
                                    {projectId === project.id
                                        ? "Selected project"
                                        : "Use project"}
                                </button>
                            </li>
                        ))}
                    </ul>
                </section>

                <section className="panel">
                    <h3>Create project</h3>

                    {organisationId.length === 0 ? (
                        <p className="field-hint">
                            Select an organisation before creating a project.
                        </p>
                    ) : null}

                    <form className="auth-form" onSubmit={handleProjectSubmit}>
                        <label>
                            Project name
                            <input
                                value={projectName}
                                onChange={(event) => {
                                    setProjectName(event.target.value);
                                }}
                                required
                            />
                        </label>

                        <label>
                            Project slug
                            <input
                                value={projectSlug}
                                pattern="^[a-z0-9]+(?:-[a-z0-9]+)*$"
                                onChange={(event) => {
                                    setProjectSlug(event.target.value);
                                }}
                                required
                            />
                            <span className="field-hint">
                                Lowercase letters, numbers, and hyphens.
                            </span>
                        </label>

                        <label>
                            Project description
                            <textarea
                                value={projectDescription}
                                onChange={(event) => {
                                    setProjectDescription(event.target.value);
                                }}
                            />
                        </label>

                        {createProjectMutation.isError ? (
                            <div className="notice notice-error" role="alert">
                                Project creation failed.
                            </div>
                        ) : null}

                        <button
                            className="button"
                            disabled={
                                organisationId.length === 0 ||
                                createProjectMutation.isPending
                            }
                        >
                            {createProjectMutation.isPending
                                ? "Creating project\u2026"
                                : "Create project"}
                        </button>
                    </form>
                </section>
            </div>
        </section>
    );
}

function requireAccessToken(accessToken: string | null): string {
    if (accessToken === null) {
        throw new Error("An authenticated access token is required.");
    }

    return accessToken;
}
