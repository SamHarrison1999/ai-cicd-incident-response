import { requestJson } from "./httpClient";

export type EvidenceKind =
    | "LOG_EXCERPT"
    | "TRACE_OBSERVATION"
    | "DEPLOYMENT_RECORD"
    | "EVENT_SNAPSHOT"
    | "STATUS_CHANGE";

export interface EvidenceSummary {
    id: string;
    kind: EvidenceKind;
    retentionClass: string;
    sourceSystem: string;
    sourceReference: string;
    occurredAt: string;
    ingestedAt: string;
    contentHash: string;
    contentLineCount: number;
}

export interface EvidenceSearchResponse {
    items: EvidenceSummary[];
    nextCursor: string | null;
}

export interface EvidenceViewer extends EvidenceSummary {
    content: string;
    incidentIds: string[];
    eventIds: string[];
}

interface EvidenceQuery {
    kind?: EvidenceKind;
    sourceSystem?: string;
    query?: string;
    cursor?: string;
    limit?: number;
}

function evidencePath(organisationId: string, projectId: string): string {
    return (
        "/api/v1/organisations/" +
        organisationId +
        "/projects/" +
        projectId +
        "/evidence"
    );
}

export function getEvidence(
    accessToken: string,
    organisationId: string,
    projectId: string,
    query: EvidenceQuery = {},
): Promise<EvidenceSearchResponse> {
    const params = new URLSearchParams();
    if (query.kind) params.set("kind", query.kind);
    if (query.sourceSystem) params.set("sourceSystem", query.sourceSystem);
    if (query.query) params.set("q", query.query);
    if (query.cursor) params.set("cursor", query.cursor);
    params.set("limit", String(query.limit ?? 25));
    return requestJson(
        evidencePath(organisationId, projectId) + "?" + params.toString(),
        {
            headers: { Authorization: "Bearer " + accessToken },
        },
    );
}

export function getEvidenceItem(
    accessToken: string,
    organisationId: string,
    projectId: string,
    evidenceId: string,
): Promise<EvidenceViewer> {
    return requestJson(
        evidencePath(organisationId, projectId) + "/" + evidenceId,
        {
            headers: { Authorization: "Bearer " + accessToken },
        },
    );
}
