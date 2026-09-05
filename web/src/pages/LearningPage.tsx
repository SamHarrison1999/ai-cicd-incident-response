import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";

import { getLearningComparison, getLearningTrends } from "../api/learning";
import { useAuth } from "../auth/useAuth";
import { useWorkspace } from "../workspace/useWorkspace";

const DIMENSION_OPTIONS = [
    {
        value: "INCIDENT_CATEGORY",
        label: "Incident category",
    },
    {
        value: "DIAGNOSIS_OUTCOME",
        label: "Diagnosis outcome",
    },
    {
        value: "RECOMMENDATION_OUTCOME",
        label: "Recommendation outcome",
    },
    {
        value: "REVIEW_EFFORT",
        label: "Review effort",
    },
    {
        value: "RESOLUTION_PATTERN",
        label: "Resolution pattern",
    },
] as const;

function formatDimensionKey(value: string): string {
    return value
        .toLowerCase()
        .split("_")
        .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
        .join(" ");
}

export function LearningPage() {
    const { accessToken } = useAuth();
    const { organisationId, projectId } = useWorkspace();

    const [dimension, setDimension] = useState("");
    const [dimensionKey, setDimensionKey] = useState("");

    const canQuery = Boolean(
        organisationId.trim() && projectId.trim() && accessToken,
    );

    const queryAccessToken = accessToken ?? "";

    const catalog = useQuery({
        queryKey: ["learning-trend-catalog", organisationId, projectId],
        queryFn: () =>
            getLearningTrends(queryAccessToken, organisationId, projectId, {
                limit: 50,
            }),
        enabled: canQuery,
    });

    const dimensionKeyOptions = useMemo(() => {
        if (dimension.length === 0) {
            return [];
        }

        const keys = (catalog.data?.items ?? [])
            .filter((item) => item.dimension === dimension)
            .map((item) => item.dimensionKey);

        return Array.from(new Set(keys)).sort();
    }, [catalog.data, dimension]);

    const query = {
        dimension,
        dimensionKey,
    };

    const trends = useQuery({
        queryKey: ["learning-trends", organisationId, projectId, query],
        queryFn: () =>
            getLearningTrends(
                queryAccessToken,
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
                queryAccessToken,
                organisationId,
                projectId,
                query,
            ),
        enabled: canQuery,
    });

    const hasLoadError =
        catalog.isError || trends.isError || comparison.isError;

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
                <h3 id="learning-scope-heading">Learning filters</h3>

                <div className="scope-form">
                    <label>
                        Dimension
                        <select
                            value={dimension}
                            onChange={(event) => {
                                setDimension(event.target.value);
                                setDimensionKey("");
                            }}
                        >
                            <option value="">All dimensions</option>

                            {DIMENSION_OPTIONS.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    </label>

                    <label>
                        Dimension key
                        <select
                            value={dimensionKey}
                            disabled={
                                !canQuery ||
                                dimension.length === 0 ||
                                catalog.isPending ||
                                catalog.isError ||
                                dimensionKeyOptions.length === 0
                            }
                            onChange={(event) => {
                                setDimensionKey(event.target.value);
                            }}
                        >
                            <option value="">All dimension keys</option>

                            {dimensionKeyOptions.map((key) => (
                                <option key={key} value={key}>
                                    {formatDimensionKey(key)}
                                </option>
                            ))}
                        </select>
                    </label>
                </div>

                {!canQuery ? (
                    <p className="field-hint">
                        Select an organisation and project using the workspace
                        controls above to load operational learning.
                    </p>
                ) : null}
            </section>

            {hasLoadError ? (
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
                        {comparison.data.dimensionKey === null
                            ? "No comparable dimension"
                            : formatDimensionKey(comparison.data.dimensionKey)}
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
                                <h3>{formatDimensionKey(item.dimensionKey)}</h3>

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
