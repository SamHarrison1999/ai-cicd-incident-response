import { useQuery } from "@tanstack/react-query";
import { useState } from "react";

import {
    getHistoricalRetrieval,
    type HistoricalRetrievalQuery,
} from "../api/historicalRetrieval";
import { useAuth } from "../auth/useAuth";
import { useWorkspace } from "../workspace/useWorkspace";

export function HistoricalRetrievalPage() {
    const { accessToken } = useAuth();
    const { organisationId, projectId, setOrganisationId, setProjectId } =
        useWorkspace();
    const [query, setQuery] = useState<HistoricalRetrievalQuery>({});
    const [cursor, setCursor] = useState<string | undefined>();
    const canQuery =
        organisationId.trim().length > 0 && projectId.trim().length > 0;
    const retrieval = useQuery({
        queryKey: [
            "historical-retrieval",
            organisationId,
            projectId,
            query,
            cursor,
        ],
        queryFn: () =>
            getHistoricalRetrieval(
                accessToken ?? "",
                organisationId,
                projectId,
                {
                    ...query,
                    cursor,
                },
            ),
        enabled: canQuery && accessToken !== null,
    });

    function updateQuery(key: keyof HistoricalRetrievalQuery, value: string) {
        setCursor(undefined);
        setQuery((current) => ({ ...current, [key]: value || undefined }));
    }

    return (
        <>
            <section className="page-heading">
                <p className="eyebrow">Review context</p>
                <h2>Historical retrieval</h2>
                <p>
                    Find bounded, tenant-scoped prior incident-response context
                    to support human review. Results are not causal conclusions.
                </p>
            </section>
            <section
                className="settings-panel"
                aria-labelledby="retrieval-scope-heading"
            >
                <h3 id="retrieval-scope-heading">Workspace scope</h3>
                <div className="retrieval-scope-form">
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
                <div className="retrieval-filter-grid">
                    <label>
                        Diagnosis category
                        <input
                            onChange={(event) => {
                                updateQuery(
                                    "diagnosisCategory",
                                    event.target.value,
                                );
                            }}
                        />
                    </label>
                    <label>
                        Provider
                        <input
                            onChange={(event) => {
                                updateQuery("provider", event.target.value);
                            }}
                        />
                    </label>
                    <label>
                        Pipeline
                        <input
                            onChange={(event) => {
                                updateQuery("pipeline", event.target.value);
                            }}
                        />
                    </label>
                    <label>
                        Search text
                        <input
                            onChange={(event) => {
                                updateQuery("query", event.target.value);
                            }}
                        />
                    </label>
                </div>
                {!canQuery ? (
                    <p className="field-hint">
                        Enter both IDs to load historical retrieval results.
                    </p>
                ) : null}
            </section>
            {retrieval.isPending && canQuery ? (
                <p>Loading historical context...</p>
            ) : null}
            {retrieval.isError ? (
                <div className="notice notice-error" role="alert">
                    Historical context could not be loaded.
                </div>
            ) : null}
            {retrieval.data?.items.length === 0 ? (
                <div className="empty-state">
                    <h3>No historical matches</h3>
                    <p>
                        Try broader filters or review the current incident
                        without historical context.
                    </p>
                </div>
            ) : null}
            {retrieval.data && retrieval.data.items.length > 0 ? (
                <section
                    className="retrieval-result-list"
                    aria-label="Historical retrieval results"
                >
                    {retrieval.data.items.map((item) => (
                        <article
                            className="retrieval-result-card"
                            key={item.id}
                        >
                            <div className="retrieval-result-heading">
                                <div>
                                    <p className="eyebrow">{item.sourceKind}</p>
                                    <h3>{item.summary}</h3>
                                </div>
                                <time dateTime={item.occurredAt}>
                                    {item.occurredAt}
                                </time>
                            </div>
                            <p className="retrieval-explanation">
                                {item.matchExplanation}
                            </p>
                            <div className="retrieval-meta">
                                <span>Source {item.provenanceReference}</span>
                                {item.provider ? (
                                    <span>{item.provider}</span>
                                ) : null}
                                {item.diagnosisCategory ? (
                                    <span>{item.diagnosisCategory}</span>
                                ) : null}
                            </div>
                        </article>
                    ))}
                    {retrieval.data.hasNext ? (
                        <button
                            className="button button-secondary"
                            onClick={() => {
                                setCursor(
                                    retrieval.data.nextCursor ?? undefined,
                                );
                            }}
                        >
                            Load more historical context
                        </button>
                    ) : null}
                </section>
            ) : null}
        </>
    );
}
