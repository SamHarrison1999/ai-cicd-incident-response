import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { type SyntheticEvent, useState } from "react";

import { createOrganisation, getOrganisations } from "../api/organisations";
import { useAuth } from "../auth/useAuth";

export function OrganisationsPage() {
    const { accessToken } = useAuth();
    const queryClient = useQueryClient();
    const [name, setName] = useState("");
    const [slug, setSlug] = useState("");

    const organisations = useQuery({
        queryKey: ["organisations"],
        queryFn: () => getOrganisations(requireAccessToken(accessToken)),
    });

    const createMutation = useMutation({
        mutationFn: () =>
            createOrganisation(requireAccessToken(accessToken), {
                name,
                slug,
            }),
        onSuccess: async () => {
            setName("");
            setSlug("");
            await queryClient.invalidateQueries({
                queryKey: ["organisations"],
            });
        },
    });

    function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
        event.preventDefault();
        createMutation.mutate();
    }

    return (
        <section>
            <div className="page-heading">
                <div>
                    <p className="eyebrow">Tenant administration</p>
                    <h2>Organisations</h2>
                    <p>
                        Select the tenant boundary used for projects, pipelines,
                        incidents, and evidence.
                    </p>
                </div>
            </div>

            <div className="organisation-grid">
                <section className="panel">
                    <h3>Your organisations</h3>

                    {organisations.isPending ? (
                        <p>Loading organisationsÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦</p>
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
                                ? "CreatingÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦"
                                : "Create organisation"}
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
