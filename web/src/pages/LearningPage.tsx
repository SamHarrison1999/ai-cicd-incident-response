import { useQuery } from "@tanstack/react-query";
import { useState } from "react";

import { getLearningComparison, getLearningTrends } from "../api/learning";
import { useAuth } from "../auth/useAuth";

export function LearningPage() {
    const { accessToken } = useAuth();
    const [organisationId, setOrganisationId] = useState("");
    const [projectId, setProjectId] = useState("");
    const [dimension, setDimension] = useState("");
    const [dimensionKey, setDimensionKey] = useState("");
    const canQuery = Boolean(
        organisationId.trim() && projectId.trim() && accessToken,
    );
    const query = { dimension, dimensionKey };
    const trends = useQuery({
        queryKey: ["learning-trends", organisationId, projectId, query],
        queryFn: () =>
            getLearningTrends(
                accessToken ?? "",
                organisationId,
                projectId,
                query,
            ),
        enabled: canQuery,
    });
    const comparison = useQuery({
        queryKey: ["learning-comparison", organisationId, projectId, query],
        queryFn: () =>
            getLearningComparison(
                accessToken ?? "",
                organisationId,
                projectId,
                query,
            ),
        enabled: canQuery,
    });

    return (
        <>
            <section className="page-heading">
                <p className="eyebrow">Operational intelligence</p>
                <h2>Operational learning</h2>
                <p>
                    Review bounded, deterministic trends derived from governed
                    records. Results are advisory and never execute changes.
                </p>
            </section>
            <section
                className="settings-panel learning-scope"
                aria-labelledby="learning-scope-heading"
            >
                <h3 id="learning-scope-heading">Learning scope</h3>
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
                    <label>
                        Dimension
                        <input
                            value={dimension}
                            onChange={(event) => {
                                setDimension(event.target.value);
                            }}
                        />
                    </label>
                    <label>
                        Dimension key
                        <input
                            value={dimensionKey}
                            onChange={(event) => {
                                setDimensionKey(event.target.value);
                            }}
                        />
                    </label>
                </div>
                {!canQuery ? (
                    <p className="field-hint">
                        Enter both tenant identifiers to load operational
                        learning.
                    </p>
                ) : null}
            </section>
            {trends.isError || comparison.isError ? (
                <div className="notice notice-error" role="alert">
                    Operational learning could not be loaded.
                </div>
            ) : null}
            {trends.isPending && canQuery ? <p>Loading trends...</p> : null}
            {comparison.data ? (
                <section
                    className="learning-comparison"
                    aria-label="Trend comparison"
                >
                    <h3>Adjacent-window comparison</h3>
                    <p>
                        {comparison.data.dimensionKey ??
                            "No comparable dimension"}
                    </p>
                    <strong>
                        {comparison.data.delta >= 0 ? "+" : ""}
                        {comparison.data.delta} change
                    </strong>
                    <span className="status-badge">
                        {comparison.data.suppressionReason}
                    </span>
                </section>
            ) : null}
            {trends.data?.items.length === 0 ? (
                <p className="empty-state">
                    No trend projections match this scope.
                </p>
            ) : null}
            {trends.data?.items.length ? (
                <section
                    className="learning-grid"
                    aria-label="Trend projections"
                >
                    {trends.data.items.map((item) => (
                        <article className="learning-card" key={item.id}>
                            <div className="learning-card-heading">
                                <h3>{item.dimensionKey}</h3>
                                <span className="status-badge">
                                    {item.suppressionReason}
                                </span>
                            </div>
                            <p>{item.dimension}</p>
                            <strong>{item.observedCount} observed</strong>
                            <p className="learning-window">
                                {new Date(item.windowStart).toLocaleString()} to{" "}
                                {new Date(item.windowEnd).toLocaleString()}
                            </p>
                            <p className="field-hint">
                                Version {item.aggregationVersion}; source{" "}
                                {item.sourceReference}
                            </p>
                        </article>
                    ))}
                </section>
            ) : null}
        </>
    );
}
