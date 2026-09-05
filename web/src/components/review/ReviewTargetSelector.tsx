import { useQuery } from "@tanstack/react-query";
import { useEffect, useMemo } from "react";

import { getIncidents } from "../../api/incidents";
import { getRecommendations } from "../../api/recommendations";

interface ReviewTargetSelectorProps {
    accessToken: string | null;
    organisationId: string;
    projectId: string;
    incidentId: string;
    recommendationId: string;
    onIncidentChange: (incidentId: string) => void;
    onRecommendationChange: (recommendationId: string) => void;
}

export function ReviewTargetSelector({
    accessToken,
    organisationId,
    projectId,
    incidentId,
    recommendationId,
    onIncidentChange,
    onRecommendationChange,
}: ReviewTargetSelectorProps) {
    const queryAccessToken = accessToken === null ? "" : accessToken;

    const canDiscover =
        accessToken !== null &&
        organisationId.length > 0 &&
        projectId.length > 0;

    const incidents = useQuery({
        queryKey: ["incidents", organisationId, projectId],
        queryFn: () =>
            getIncidents(queryAccessToken, organisationId, projectId),
        enabled: canDiscover,
    });

    const recommendations = useQuery({
        queryKey: ["recommendations", organisationId, projectId],
        queryFn: () =>
            getRecommendations(queryAccessToken, organisationId, projectId),
        enabled: canDiscover,
    });

    const incidentOptions = useMemo(
        () => incidents.data ?? [],
        [incidents.data],
    );

    const recommendationOptions = useMemo(() => {
        const items = recommendations.data?.items ?? [];

        if (incidentId.length === 0) {
            return items;
        }

        return items.filter((item) => item.incidentId === incidentId);
    }, [incidentId, recommendations.data]);

    useEffect(() => {
        if (incidents.data === undefined) {
            return;
        }

        const currentIsValid = incidentOptions.some(
            (incident) => incident.id === incidentId,
        );

        if (currentIsValid) {
            return;
        }

        const unresolved = incidentOptions.filter(
            (incident) => incident.status !== "RESOLVED",
        );

        const preferred =
            unresolved.length === 1
                ? unresolved[0]
                : incidentOptions.length === 1
                  ? incidentOptions[0]
                  : undefined;

        if (preferred !== undefined) {
            onIncidentChange(preferred.id);
            return;
        }

        if (incidentId.length > 0) {
            onIncidentChange("");
        }
    }, [incidentId, incidentOptions, incidents.data, onIncidentChange]);

    useEffect(() => {
        if (recommendations.data === undefined) {
            return;
        }

        const currentIsValid = recommendationOptions.some(
            (recommendation) => recommendation.id === recommendationId,
        );

        if (currentIsValid) {
            return;
        }

        const recommended = recommendationOptions.filter(
            (recommendation) => recommendation.status === "RECOMMENDED",
        );

        const preferred =
            recommended.length === 1
                ? recommended[0]
                : recommendationOptions.length === 1
                  ? recommendationOptions[0]
                  : undefined;

        if (preferred !== undefined) {
            onRecommendationChange(preferred.id);
            return;
        }

        if (recommendationId.length > 0) {
            onRecommendationChange("");
        }
    }, [
        onRecommendationChange,
        recommendationId,
        recommendationOptions,
        recommendations.data,
    ]);

    return (
        <>
            <label>
                Incident
                <select
                    value={incidentId}
                    disabled={
                        !canDiscover ||
                        incidents.isPending ||
                        incidents.isError ||
                        incidentOptions.length === 0
                    }
                    onChange={(event) => {
                        onIncidentChange(event.target.value);
                        onRecommendationChange("");
                    }}
                >
                    <option value="">Select incident</option>

                    {incidentOptions.map((incident) => (
                        <option key={incident.id} value={incident.id}>
                            {incident.title} — {incident.status}
                        </option>
                    ))}
                </select>
            </label>

            <label>
                Recommendation
                <select
                    value={recommendationId}
                    disabled={
                        !canDiscover ||
                        recommendations.isPending ||
                        recommendations.isError ||
                        recommendationOptions.length === 0
                    }
                    onChange={(event) => {
                        onRecommendationChange(event.target.value);
                    }}
                >
                    <option value="">Select recommendation</option>

                    {recommendationOptions.map((recommendation) => (
                        <option
                            key={recommendation.id}
                            value={recommendation.id}
                        >
                            {recommendation.category}: {recommendation.summary}
                        </option>
                    ))}
                </select>
            </label>

            {incidents.isError ? (
                <span className="field-hint" role="status">
                    Incidents could not be loaded.
                </span>
            ) : null}

            {recommendations.isError ? (
                <span className="field-hint" role="status">
                    Recommendations could not be loaded.
                </span>
            ) : null}
        </>
    );
}
