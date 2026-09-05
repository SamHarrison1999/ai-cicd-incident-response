import { requestJson } from "./httpClient";

export interface HistoricalRetrievalItem {
    id: string;
    incidentId: string | null;
    sourceKind: string;
    sourceId: string;
    occurredAt: string;
    provider: string | null;
    pipelineName: string | null;
    environmentName: string | null;
    gitRef: string | null;
    commitSha: string | null;
    diagnosisCategory: string | null;
    summary: string;
    matchExplanation: string;
    provenanceReference: string;
}

export interface HistoricalRetrievalResponse {
    items: HistoricalRetrievalItem[];
    nextCursor: string | null;
    hasNext: boolean;
}

export interface HistoricalRetrievalQuery {
    diagnosisCategory?: string;
    provider?: string;
    pipeline?: string;
    environment?: string;
    branch?: string;
    commitSha?: string;
    from?: string;
    to?: string;
    query?: string;
    cursor?: string;
    limit?: number;
}

function retrievalPath(organisationId: string, projectId: string): string {
    return (
        "/api/v1/organisations/" +
        organisationId +
        "/projects/" +
        projectId +
        "/historical-retrieval"
    );
}

export function getHistoricalRetrieval(
    accessToken: string,
    organisationId: string,
    projectId: string,
    query: HistoricalRetrievalQuery = {},
): Promise<HistoricalRetrievalResponse> {
    const params = new URLSearchParams();
    for (const [key, value] of Object.entries(query)) {
        if (value !== undefined && value !== "") {
            params.set(key === "query" ? "q" : key, String(value));
        }
    }
    params.set("limit", String(query.limit ?? 25));
    return requestJson(
        retrievalPath(organisationId, projectId) + "?" + params.toString(),
        { headers: { Authorization: "Bearer " + accessToken } },
    );
}
