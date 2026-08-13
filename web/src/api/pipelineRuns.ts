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
