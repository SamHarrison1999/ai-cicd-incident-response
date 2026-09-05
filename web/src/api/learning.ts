import { requestJson } from "./httpClient";

export interface LearningTrendItem {
    id: string;
    dimension: string;
    dimensionKey: string;
    windowStart: string;
    windowEnd: string;
    aggregationVersion: string;
    sampleCount: number;
    observedCount: number;
    sourceReference: string;
    suppressionReason: string;
}

export interface LearningTrendQuery {
    dimension?: string;
    dimensionKey?: string;
    from?: string;
    to?: string;
    limit?: number;
}

export interface LearningTrendResponse {
    items: LearningTrendItem[];
    nextCursor: string | null;
    hasNext: boolean;
}

export interface LearningComparison {
    dimensionKey?: string | null;
    currentCount: number;
    previousCount: number;
    delta: number;
    suppressionReason: string;
}

export function getLearningTrends(
    accessToken: string,
    organisationId: string,
    projectId: string,
    query: LearningTrendQuery = {},
): Promise<LearningTrendResponse> {
    const params = new URLSearchParams();
    for (const [key, value] of Object.entries(query)) {
        if (value !== undefined && value !== "") params.set(key, String(value));
    }
    params.set("limit", String(query.limit ?? 50));
    return requestJson(
        "/api/v1/organisations/" +
            organisationId +
            "/projects/" +
            projectId +
            "/operational-learning/trends?" +
            params.toString(),
        { headers: { Authorization: "Bearer " + accessToken } },
    );
}

export function getLearningComparison(
    accessToken: string,
    organisationId: string,
    projectId: string,
    query: LearningTrendQuery = {},
): Promise<LearningComparison> {
    const params = new URLSearchParams();
    for (const [key, value] of Object.entries(query)) {
        if (value !== undefined && value !== "") params.set(key, String(value));
    }
    params.set("limit", String(query.limit ?? 50));
    return requestJson(
        "/api/v1/organisations/" +
            organisationId +
            "/projects/" +
            projectId +
            "/operational-learning/trends/compare?" +
            params.toString(),
        { headers: { Authorization: "Bearer " + accessToken } },
    );
}
