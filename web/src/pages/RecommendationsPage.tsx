import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";

import { getEvidence } from "../api/evidence";
import {
    generateRecommendation,
    getRecommendations,
} from "../api/recommendations";
import { useAuth } from "../auth/useAuth";
import { useWorkspace } from "../workspace/useWorkspace";

function formatConfidence(value: number) {
    return String(Math.round(value * 100)) + "%";
}

export function RecommendationsPage() {
    const { accessToken } = useAuth();
    const queryClient = useQueryClient();

    const {
        organisationId,
        projectId,
        incidentId,
        setOrganisationId,
        setProjectId,
        setIncidentId,
    } = useWorkspace();
    const [selectedEvidenceIds, setSelectedEvidenceIds] = useState<string[]>(
        [],
    );

    const canQuery =
        organisationId.trim().length > 0 && projectId.trim().length > 0;

    const recommendations = useQuery({
        queryKey: ["recommendations", organisationId, projectId],
        queryFn: () =>
            getRecommendations(accessToken ?? "", organisationId, projectId),
        enabled: canQuery && accessToken !== null,
    });

    const evidenceInputs = useQuery({
        queryKey: ["recommendation-evidence", organisationId, projectId],
        queryFn: () =>
            getEvidence(accessToken ?? "", organisationId, projectId, {
                limit: 25,
            }),
        enabled: canQuery && accessToken !== null,
    });

    const generate = useMutation({
        mutationFn: () =>
            generateRecommendation(
                accessToken ?? "",
                organisationId,
                projectId,
                incidentId.trim().length > 0 ? incidentId.trim() : null,
                selectedEvidenceIds,
                [],
            ),
        onSuccess: () => {
            void queryClient.invalidateQueries({
                queryKey: ["recommendations", organisationId, projectId],
            });
        },
    });

    const canGenerate =
        canQuery && selectedEvidenceIds.length > 0 && !generate.isPending;

    return (
        <>
            <section className="page-heading">
                <p className="eyebrow">Human review required</p>
                <h2>Recommendations</h2>
                <p>
                    Review bounded, evidence-grounded decision support. These
                    results are not confirmed causes and do not execute actions.
                </p>
            </section>

            <section
                className="settings-panel"
                aria-labelledby="recommendation-scope-heading"
            >
                <h3 id="recommendation-scope-heading">Workspace scope</h3>

                <div className="scope-form">
                    <label>
                        Organisation ID
                        <input
                            value={organisationId}
                            onChange={(event) => {
                                setOrganisationId(event.target.value);
                                setSelectedEvidenceIds([]);
                            }}
                        />
                    </label>

                    <label>
                        Project ID
                        <input
                            value={projectId}
                            onChange={(event) => {
                                setProjectId(event.target.value);
                                setSelectedEvidenceIds([]);
                            }}
                        />
                    </label>

                    <label>
                        Incident ID (optional)
                        <input
                            value={incidentId}
                            onChange={(event) => {
                                setIncidentId(event.target.value);
                            }}
                        />
                    </label>
                </div>

                <div className="recommendation-input-panel">
                    <p className="eyebrow">Evidence grounding</p>
                    <h3>Evidence inputs</h3>
                    <p>
                        Select the sanitised evidence that should support this
                        recommendation.
                    </p>

                    {evidenceInputs.isPending && canQuery ? (
                        <p>Loading evidence inputs...</p>
                    ) : null}

                    {evidenceInputs.isError ? (
                        <div className="notice notice-error" role="alert">
                            Evidence inputs could not be loaded.
                        </div>
                    ) : null}

                    <div
                        className="recommendation-evidence-list"
                        aria-label="Recommendation evidence inputs"
                    >
                        {evidenceInputs.data?.items.map((evidence) => (
                            <label
                                className="recommendation-evidence-option"
                                key={evidence.id}
                            >
                                <input
                                    type="checkbox"
                                    aria-label={
                                        "Select " +
                                        evidence.kind +
                                        " " +
                                        evidence.sourceReference
                                    }
                                    checked={selectedEvidenceIds.includes(
                                        evidence.id,
                                    )}
                                    onChange={(event) => {
                                        setSelectedEvidenceIds((current) =>
                                            event.target.checked
                                                ? [...current, evidence.id]
                                                : current.filter(
                                                      (id) =>
                                                          id !== evidence.id,
                                                  ),
                                        );
                                    }}
                                />
                                <span>
                                    <strong>{evidence.kind}</strong>
                                    <small>
                                        {evidence.sourceSystem} ·{" "}
                                        {evidence.sourceReference}
                                    </small>
                                </span>
                            </label>
                        ))}
                    </div>

                    {canQuery && evidenceInputs.data?.items.length === 0 ? (
                        <p>
                            No evidence inputs are available for this project.
                        </p>
                    ) : null}

                    <p className="recommendation-selection-count">
                        {selectedEvidenceIds.length} evidence item
                        {selectedEvidenceIds.length === 1 ? "" : "s"} selected
                    </p>
                </div>

                <button
                    type="button"
                    disabled={!canGenerate}
                    onClick={() => {
                        generate.mutate();
                    }}
                >
                    {generate.isPending
                        ? "Generating..."
                        : "Generate bounded recommendation"}
                </button>
            </section>

            {recommendations.isPending && canQuery ? (
                <p>Loading recommendations...</p>
            ) : null}

            {recommendations.isError || generate.isError ? (
                <div className="notice notice-error" role="alert">
                    Recommendations could not be loaded.
                </div>
            ) : null}

            <section
                className="recommendation-list"
                aria-label="Recommendations"
            >
                {recommendations.data?.items.map((recommendation) => (
                    <article
                        className="recommendation-card"
                        key={recommendation.id}
                    >
                        <div className="recommendation-card-heading">
                            <div>
                                <p className="eyebrow">
                                    {recommendation.category}
                                </p>
                                <h3>{recommendation.summary}</h3>
                            </div>
                            <span className="recommendation-confidence">
                                {formatConfidence(recommendation.confidence)}
                            </span>
                        </div>

                        <dl className="recommendation-details">
                            <div>
                                <dt>Status</dt>
                                <dd>{recommendation.status}</dd>
                            </div>

                            <div>
                                <dt>Provider</dt>
                                <dd>
                                    {recommendation.providerName} /{" "}
                                    {recommendation.modelVersion}
                                </dd>
                            </div>

                            <div>
                                <dt>Retrieval set</dt>
                                <dd>{recommendation.retrievalSetVersion}</dd>
                            </div>

                            <div>
                                <dt>Citations</dt>
                                <dd>{recommendation.citations}</dd>
                            </div>
                        </dl>

                        <p>{recommendation.confidenceExplanation}</p>

                        {recommendation.abstentionReason ? (
                            <p className="recommendation-warning">
                                Abstention: {recommendation.abstentionReason}
                            </p>
                        ) : null}
                    </article>
                ))}

                {canQuery && recommendations.data?.items.length === 0 ? (
                    <p>No recommendations have been recorded for this scope.</p>
                ) : null}
            </section>
        </>
    );
}
