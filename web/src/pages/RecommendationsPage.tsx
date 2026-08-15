import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";

import {
    generateRecommendation,
    getRecommendations,
} from "../api/recommendations";
import { useAuth } from "../auth/useAuth";

function formatConfidence(value: number) {
    return String(Math.round(value * 100)) + "%";
}

export function RecommendationsPage() {
    const { accessToken } = useAuth();
    const queryClient = useQueryClient();
    const [organisationId, setOrganisationId] = useState("");
    const [projectId, setProjectId] = useState("");
    const canQuery =
        organisationId.trim().length > 0 && projectId.trim().length > 0;
    const recommendations = useQuery({
        queryKey: ["recommendations", organisationId, projectId],
        queryFn: () =>
            getRecommendations(accessToken ?? "", organisationId, projectId),
        enabled: canQuery && accessToken !== null,
    });
    const generate = useMutation({
        mutationFn: () =>
            generateRecommendation(
                accessToken ?? "",
                organisationId,
                projectId,
                [],
                [],
            ),
        onSuccess: () => {
            void queryClient.invalidateQueries({
                queryKey: ["recommendations", organisationId, projectId],
            });
        },
    });

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
                <button
                    type="button"
                    disabled={!canQuery || generate.isPending}
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
