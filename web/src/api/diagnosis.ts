import { requestJson } from "./httpClient";

export type DiagnosisCategory =
    | "DEPENDENCY_FAILURE_SUSPECTED"
    | "DEPLOYMENT_REGRESSION_SUSPECTED"
    | "CONFIGURATION_CHANGE_SUSPECTED"
    | "RESOURCE_EXHAUSTION_SUSPECTED"
    | "INSUFFICIENT_EVIDENCE"
    | "UNKNOWN";

export interface DiagnosisResult {
    ruleVersion: string;
    category: DiagnosisCategory;
    confidence: number;
    supportingSignalIds: string[];
    missingEvidence: string[];
    warnings: string[];
    abstentionReason: string | null;
}

export function getDiagnosis(
    accessToken: string,
    organisationId: string,
    projectId: string,
): Promise<DiagnosisResult> {
    return requestJson(
        "/api/v1/organisations/" +
            organisationId +
            "/projects/" +
            projectId +
            "/diagnosis",
        { headers: { Authorization: "Bearer " + accessToken } },
    );
}
