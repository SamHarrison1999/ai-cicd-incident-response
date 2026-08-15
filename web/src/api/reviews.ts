import { requestJson } from "./httpClient";

export type ReviewAction = "ACCEPT" | "EDIT" | "REJECT";
export type ReviewReason =
    | "NONE"
    | "NOT_GROUNDED"
    | "INCORRECT_SCOPE"
    | "DUPLICATE"
    | "UNSAFE"
    | "OTHER";

export interface ReviewItem {
    id: string;
    action: ReviewAction;
    reason: ReviewReason;
    comment: string | null;
    reviewedVersionId: string | null;
    createdAt: string;
}

const base = (
    organisationId: string,
    projectId: string,
    recommendationId: string,
) =>
    "/api/v1/organisations/" +
    organisationId +
    "/projects/" +
    projectId +
    "/recommendations/" +
    recommendationId +
    "/reviews";

export function getReviewHistory(
    accessToken: string,
    organisationId: string,
    projectId: string,
    recommendationId: string,
): Promise<{ items: ReviewItem[] }> {
    return requestJson(base(organisationId, projectId, recommendationId), {
        headers: { Authorization: "Bearer " + accessToken },
    });
}

export function submitReview(
    accessToken: string,
    organisationId: string,
    projectId: string,
    recommendationId: string,
    body: {
        action: ReviewAction;
        reason: ReviewReason;
        comment: string | null;
        editedSummary: string | null;
        editedCause: string | null;
    },
): Promise<ReviewItem> {
    return requestJson(base(organisationId, projectId, recommendationId), {
        method: "POST",
        headers: {
            Authorization: "Bearer " + accessToken,
            "Content-Type": "application/json",
        },
        body: JSON.stringify(body),
    });
}

export function createResolution(
    accessToken: string,
    organisationId: string,
    projectId: string,
    incidentId: string,
    body: {
        recommendationId: string;
        reviewedVersionId: string;
        resolutionText: string;
    },
): Promise<{ id: string }> {
    return requestJson(
        "/api/v1/organisations/" +
            organisationId +
            "/projects/" +
            projectId +
            "/incidents/" +
            incidentId +
            "/resolutions",
        {
            method: "POST",
            headers: {
                Authorization: "Bearer " + accessToken,
                "Content-Type": "application/json",
            },
            body: JSON.stringify(body),
        },
    );
}
