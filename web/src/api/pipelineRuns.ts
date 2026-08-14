import { requestJson } from "./httpClient";

export interface PipelineRun {
    id: string;
    eventSourceId: string;
    provider: string;
    externalRunId: string;
    name: string;
    attempt: number;
    status: string;
    commitSha: string | null;
    gitRef: string | null;
    environmentName: string | null;
    startedAt: string | null;
    completedAt: string | null;
    lastEventOccurredAt: string;
    updatedAt: string;
}

export interface PipelineTimelineEvent {
    id: string;
    pipelineRunId: string | null;
    provider: string;
    eventType: string;
    status: string;
    externalRunId: string;
    pipelineName: string;
    attempt: number;
    commitSha: string | null;
    gitRef: string | null;
    environmentName: string | null;
    occurredAt: string;
    receivedAt: string;
    evidenceSummary: string;
}

export interface PipelineTimelineResponse {
    items: PipelineTimelineEvent[];
    nextCursor: string | null;
    hasNext: boolean;
}

export interface PipelineTimelineFilters {
    status?: string;
    branch?: string;
    commitSha?: string;
    environment?: string;
    eventType?: string;
    from?: string;
    to?: string;
    cursor?: string;
    limit?: number;
}

export function getPipelineRuns(
    accessToken: string,
    organisationId: string,
    projectId: string,
): Promise<PipelineRun[]> {
    const path =
        "/api/v1/organisations/" +
        organisationId +
        "/projects/" +
        projectId +
        "/pipeline-runs";
    return requestJson(path, {
        headers: { Authorization: "Bearer " + accessToken },
    });
}

export function getPipelineTimeline(
    accessToken: string,
    organisationId: string,
    projectId: string,
    filters: PipelineTimelineFilters = {},
): Promise<PipelineTimelineResponse> {
    const params = new URLSearchParams();
    const entries: Array<
        [keyof PipelineTimelineFilters, string | number | undefined]
    > = [
        ["status", filters.status],
        ["branch", filters.branch],
        ["commitSha", filters.commitSha],
        ["environment", filters.environment],
        ["eventType", filters.eventType],
        ["from", filters.from],
        ["to", filters.to],
        ["cursor", filters.cursor],
        ["limit", filters.limit],
    ];
    for (const [key, value] of entries) {
        if (value !== undefined && value !== "") {
            params.set(key, String(value));
        }
    }

    const query = params.toString();
    const path =
        "/api/v1/organisations/" +
        organisationId +
        "/projects/" +
        projectId +
        "/timeline" +
        (query.length > 0 ? "?" + query : "");
    return requestJson(path, {
        headers: { Authorization: "Bearer " + accessToken },
    });
}
