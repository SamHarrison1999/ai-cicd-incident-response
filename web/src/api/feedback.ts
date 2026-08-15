import { requestJson } from "./httpClient";

export interface FeedbackItem {
    id: string;
    policyVersion: string;
    windowStart: string;
    windowEnd: string;
    sampleCount: number;
    acceptedCount: number;
    editedCount: number;
    rejectedCount: number;
    resolvedCount: number;
    suppressionReason: string;
}

export interface FeedbackQuery {
    policyVersion?: string;
    from?: string;
    to?: string;
    limit?: number;
}

export function getFeedback(
    accessToken: string,
    organisationId: string,
    projectId: string,
    query: FeedbackQuery = {},
): Promise<{ items: FeedbackItem[] }> {
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
            "/feedback?" +
            params.toString(),
        { headers: { Authorization: "Bearer " + accessToken } },
    );
}
