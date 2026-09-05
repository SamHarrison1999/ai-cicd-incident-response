import { requestJson } from "./httpClient";

export type DemoCiOutcome = "FAILED" | "SUCCEEDED";

export interface DemoCiSimulationRequest {
    pipelineName: string;
    branch: string;
    outcome: DemoCiOutcome;
}

export interface DemoCiSimulationResponse {
    eventSourceId: string;
    providerDeliveryId: string;
    externalRunId: string;
    pipelineName: string;
    branch: string;
    outcome: DemoCiOutcome;
    deliveryId: string;
    duplicate: boolean;
    deliveryStatus: string;
    receivedAt: string;
}

export function simulateDemoCiRun(
    accessToken: string,
    organisationId: string,
    projectId: string,
    request: DemoCiSimulationRequest,
): Promise<DemoCiSimulationResponse> {
    return requestJson(
        "/api/v1/organisations/" +
            organisationId +
            "/projects/" +
            projectId +
            "/demo/ci-runs",
        {
            method: "POST",
            headers: {
                Authorization: "Bearer " + accessToken,
            },
            body: JSON.stringify(request),
        },
    );
}
