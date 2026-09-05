import { requestJson } from "./httpClient";

export type RecommendationStatus = "RECOMMENDED" | "ABSTAINED" | "REJECTED";

export interface Recommendation {
    id: string;
    organisationId: string;
    projectId: string;
    incidentId: string | null;
    category: string;
    summary: string;
    likelyCause: string | null;
    confidence: number;
    confidenceExplanation: string;
    status: RecommendationStatus;
    abstentionReason: string | null;
    providerName: string;
    modelVersion: string;
    promptTemplateVersion: string;
    rulesetVersion: string;
    retrievalSetVersion: string;
    schemaVersion: string;
    generatedAt: string;
    createdAt: string;
    citations: number;
}

export function getRecommendations(
    accessToken: string,
    organisationId: string,
    projectId: string,
): Promise<{ items: Recommendation[] }> {
    return requestJson(
        "/api/v1/organisations/" +
            organisationId +
            "/projects/" +
            projectId +
            "/recommendations",
        { headers: { Authorization: "Bearer " + accessToken } },
    );
}

export function generateRecommendation(
    accessToken: string,
    organisationId: string,
    projectId: string,
    incidentId: string | null,
    evidenceIds: string[],
    historicalRecordIds: string[],
): Promise<Recommendation> {
    return requestJson(
        "/api/v1/organisations/" +
            organisationId +
            "/projects/" +
            projectId +
            "/recommendations",
        {
            method: "POST",
            headers: {
                Authorization: "Bearer " + accessToken,
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                incidentId,
                evidenceIds,
                historicalRecordIds,
            }),
        },
    );
}
