import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";

import {
    createResolution,
    getReviewHistory,
    submitReview,
    type ReviewAction,
    type ReviewReason,
} from "../api/reviews";
import { useAuth } from "../auth/useAuth";
import { ReviewTargetSelector } from "../components/review/ReviewTargetSelector";
import { useWorkspace } from "../workspace/useWorkspace";

export function ReviewPage() {
    const { accessToken } = useAuth();
    const queryClient = useQueryClient();
    const {
        organisationId,
        projectId,
        incidentId,
        setOrganisationId,
        setProjectId,
        setIncidentId,
    } = useWorkspace();
    const [recommendationId, setRecommendationId] = useState("");
    const [action, setAction] = useState<ReviewAction>("ACCEPT");
    const [reason, setReason] = useState<ReviewReason>("NONE");
    const [comment, setComment] = useState("");
    const [editedSummary, setEditedSummary] = useState("");
    const [editedCause, setEditedCause] = useState("");
    const [resolutionText, setResolutionText] = useState("");
    const canQuery = Boolean(
        organisationId && projectId && recommendationId && accessToken,
    );
    const reviews = useQuery({
        queryKey: [
            "review-history",
            organisationId,
            projectId,
            recommendationId,
        ],
        queryFn: () =>
            getReviewHistory(
                accessToken ?? "",
                organisationId,
                projectId,
                recommendationId,
            ),
        enabled: canQuery,
    });
    const review = useMutation({
        mutationFn: () =>
            submitReview(
                accessToken ?? "",
                organisationId,
                projectId,
                recommendationId,
                {
                    action,
                    reason,
                    comment: comment || null,
                    editedSummary: editedSummary || null,
                    editedCause: editedCause || null,
                },
            ),
        onSuccess: () => {
            void queryClient.invalidateQueries({
                queryKey: [
                    "review-history",
                    organisationId,
                    projectId,
                    recommendationId,
                ],
            });

            void queryClient.invalidateQueries({
                queryKey: ["feedback", organisationId, projectId],
            });
        },
    });
    const resolve = useMutation({
        mutationFn: () =>
            createResolution(
                accessToken ?? "",
                organisationId,
                projectId,
                incidentId,
                {
                    recommendationId,
                    reviewedVersionId:
                        reviews.data?.items.find(
                            (item) => item.reviewedVersionId,
                        )?.reviewedVersionId ?? "",
                    resolutionText,
                },
            ),
        onSuccess: () => {
            setResolutionText("");

            void queryClient.invalidateQueries({
                queryKey: ["feedback", organisationId, projectId],
            });
        },
    });

    return (
        <>
            <section className="page-heading">
                <p className="eyebrow">Human governance</p>
                <h2>Review recommendations</h2>
                <p>
                    Accept, edit, or reject bounded decision support. No action
                    here executes remediation.
                </p>
            </section>
            <section
                className="settings-panel review-panel"
                aria-labelledby="review-scope-heading"
            >
                <h3 id="review-scope-heading">Review scope</h3>
                <div className="scope-form">
                    <label>
                        Organisation ID
                        <input
                            value={organisationId}
                            onChange={(event) => {
                                setOrganisationId(event.target.value);
                            }}
                        />
                    </label>
                    <label>
                        Project ID
                        <input
                            value={projectId}
                            onChange={(event) => {
                                setProjectId(event.target.value);
                            }}
                        />
                    </label>
                    <ReviewTargetSelector
                        accessToken={accessToken}
                        organisationId={organisationId}
                        projectId={projectId}
                        incidentId={incidentId}
                        recommendationId={recommendationId}
                        onIncidentChange={setIncidentId}
                        onRecommendationChange={setRecommendationId}
                    />
                </div>
                {!canQuery ? (
                    <p className="field-hint">
                        Enter all scope identifiers to load review history.
                    </p>
                ) : null}
            </section>
            {reviews.isError || review.isError || resolve.isError ? (
                <div className="notice notice-error" role="alert">
                    The review operation could not be completed.
                </div>
            ) : null}
            {canQuery ? (
                <section className="review-layout">
                    <div className="review-card">
                        <h3>Submit review</h3>
                        <label>
                            Action
                            <select
                                value={action}
                                onChange={(event) => {
                                    setAction(
                                        event.target.value as ReviewAction,
                                    );
                                }}
                            >
                                <option value="ACCEPT">Accept</option>
                                <option value="EDIT">Edit</option>
                                <option value="REJECT">Reject</option>
                            </select>
                        </label>
                        <label>
                            Reason
                            <select
                                value={reason}
                                onChange={(event) => {
                                    setReason(
                                        event.target.value as ReviewReason,
                                    );
                                }}
                            >
                                <option value="NONE">No reason</option>
                                <option value="NOT_GROUNDED">
                                    Not grounded
                                </option>
                                <option value="INCORRECT_SCOPE">
                                    Incorrect scope
                                </option>
                                <option value="DUPLICATE">Duplicate</option>
                                <option value="UNSAFE">Unsafe</option>
                                <option value="OTHER">Other</option>
                            </select>
                        </label>
                        <label>
                            Comment
                            <textarea
                                maxLength={500}
                                value={comment}
                                onChange={(event) => {
                                    setComment(event.target.value);
                                }}
                            />
                        </label>
                        {action === "EDIT" ? (
                            <>
                                <label>
                                    Edited summary
                                    <textarea
                                        maxLength={2000}
                                        value={editedSummary}
                                        onChange={(event) => {
                                            setEditedSummary(
                                                event.target.value,
                                            );
                                        }}
                                    />
                                </label>
                                <label>
                                    Edited cause
                                    <textarea
                                        maxLength={1000}
                                        value={editedCause}
                                        onChange={(event) => {
                                            setEditedCause(event.target.value);
                                        }}
                                    />
                                </label>
                            </>
                        ) : null}
                        <button
                            type="button"
                            disabled={
                                review.isPending ||
                                (action === "REJECT" && reason === "NONE")
                            }
                            onClick={() => {
                                review.mutate();
                            }}
                        >
                            {review.isPending
                                ? "Submitting..."
                                : "Submit review"}
                        </button>
                    </div>
                    <div className="review-card">
                        <h3>Review history</h3>
                        {reviews.isPending ? (
                            <p>Loading review history...</p>
                        ) : null}
                        {reviews.data?.items.map((item) => (
                            <article
                                className="review-history-item"
                                key={item.id}
                            >
                                <strong>{item.action}</strong>
                                <span>{item.reason}</span>
                                <p>{item.comment ?? "No comment"}</p>
                            </article>
                        ))}
                        {reviews.data?.items.length === 0 ? (
                            <p>No reviews recorded for this recommendation.</p>
                        ) : null}
                    </div>
                </section>
            ) : null}
            {canQuery ? (
                <section className="settings-panel review-resolution">
                    <h3>Record incident resolution</h3>

                    <label>
                        Resolution text
                        <textarea
                            maxLength={2000}
                            value={resolutionText}
                            onChange={(event) => {
                                setResolutionText(event.target.value);
                            }}
                        />
                    </label>
                    <button
                        type="button"
                        disabled={
                            !incidentId ||
                            !resolutionText ||
                            resolve.isPending ||
                            !reviews.data?.items.some(
                                (item) => item.reviewedVersionId,
                            )
                        }
                        onClick={() => {
                            resolve.mutate();
                        }}
                    >
                        Record bounded resolution
                    </button>
                </section>
            ) : null}
        </>
    );
}
