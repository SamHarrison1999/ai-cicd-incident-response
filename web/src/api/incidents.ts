import { requestJson } from "./httpClient";

export type IncidentStatus =
    | "DETECTED"
    | "TRIAGED"
    | "MITIGATING"
    | "MONITORING"
    | "RESOLVED"
    | "REOPENED";

export interface Incident {
    id: string;
    status: IncidentStatus;
    title: string;
    summary: string;
    detectedAt: string;
    resolvedAt: string | null;
    createdAt: string;
    updatedAt: string;
}

function incidentPath(organisationId: string, projectId: string): string {
    return (
        "/api/v1/organisations/" +
        organisationId +
        "/projects/" +
        projectId +
        "/incidents"
    );
}

export function getIncidents(
    accessToken: string,
    organisationId: string,
    projectId: string,
): Promise<Incident[]> {
    return requestJson(incidentPath(organisationId, projectId), {
        headers: { Authorization: "Bearer " + accessToken },
    });
}

export function transitionIncident(
    accessToken: string,
    organisationId: string,
    projectId: string,
    incidentId: string,
    status: IncidentStatus,
): Promise<Incident> {
    return requestJson(
        incidentPath(organisationId, projectId) + "/" + incidentId + "/status",
        {
            method: "PATCH",
            headers: {
                Authorization: "Bearer " + accessToken,
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                status,
                occurredAt: new Date().toISOString(),
            }),
        },
    );
}
