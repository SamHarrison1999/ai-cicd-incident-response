import { useQuery } from "@tanstack/react-query";
import { useState } from "react";

import { getPipelineRuns } from "../api/pipelineRuns";
import { useAuth } from "../auth/useAuth";

export function PipelinesPage() {
    const { accessToken } = useAuth();
    const [organisationId, setOrganisationId] = useState("");
    const [projectId, setProjectId] = useState("");
    const canQuery =
        organisationId.trim().length > 0 && projectId.trim().length > 0;
    const runs = useQuery({
        queryKey: ["pipeline-runs", organisationId, projectId],
        queryFn: () =>
            getPipelineRuns(accessToken ?? "", organisationId, projectId),
        enabled: canQuery && accessToken !== null,
    });

    return (
        <>
            <section className="page-heading">
                <p className="eyebrow">Delivery visibility</p>
                <h2>Pipeline runs</h2>
                <p>
                    Inspect provider-neutral pipeline projections within an
                    organisation and project boundary.
                </p>
            </section>
            <section
                className="settings-panel"
                aria-labelledby="pipeline-scope-heading"
            >
                <h3 id="pipeline-scope-heading">Workspace scope</h3>
                <div className="scope-form">
                    <label>
                        Organisation ID
                        <input
                            value={organisationId}
                            onChange={(event) => {
                                setOrganisationId(event.target.value);
                            }}
                        />
                    </label>
                    <label>
                        Project ID
                        <input
                            value={projectId}
                            onChange={(event) => {
                                setProjectId(event.target.value);
                            }}
                        />
                    </label>
                </div>
                {!canQuery ? (
                    <p className="field-hint">
                        Enter both IDs to load the tenant-scoped pipeline view.
                    </p>
                ) : null}
            </section>
            {runs.isPending && canQuery ? (
                <p>Loading pipeline runs...</p>
            ) : null}
            {runs.isError ? (
                <div className="notice notice-error" role="alert">
                    Pipeline runs could not be loaded.
                </div>
            ) : null}
            {runs.data?.length === 0 ? (
                <div className="empty-state">
                    <h3>No pipeline runs available</h3>
                    <p>
                        Run the signed webhook simulator to create a
                        deterministic demo event.
                    </p>
                </div>
            ) : null}
            {runs.data && runs.data.length > 0 ? (
                <div className="run-list" aria-label="Pipeline runs">
                    {runs.data.map((run) => (
                        <article className="run-card" key={run.id}>
                            <div>
                                <p className="eyebrow">{run.provider}</p>
                                <h3>{run.name}</h3>
                                <p>
                                    {run.externalRunId} Â· attempt {run.attempt}
                                </p>
                            </div>
                            <strong
                                className={
                                    "run-status run-status-" +
                                    run.status.toLowerCase()
                                }
                            >
                                {run.status}
                            </strong>
                        </article>
                    ))}
                </div>
            ) : null}
        </>
    );
}
