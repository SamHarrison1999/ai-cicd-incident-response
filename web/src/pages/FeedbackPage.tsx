import { useQuery } from "@tanstack/react-query";
import { useState } from "react";

import { getFeedback } from "../api/feedback";
import { useAuth } from "../auth/useAuth";

export function FeedbackPage() {
    const { accessToken } = useAuth();
    const [organisationId, setOrganisationId] = useState("");
    const [projectId, setProjectId] = useState("");
    const [policyVersion, setPolicyVersion] = useState("");
    const [from, setFrom] = useState("");
    const [to, setTo] = useState("");
    const canQuery = Boolean(
        organisationId.trim() && projectId.trim() && accessToken,
    );
    const feedback = useQuery({
        queryKey: [
            "feedback",
            organisationId,
            projectId,
            policyVersion,
            from,
            to,
        ],
        queryFn: () =>
            getFeedback(accessToken ?? "", organisationId, projectId, {
                policyVersion,
                from,
                to,
            }),
        enabled: canQuery,
    });

    return (
        <>
            <section className="page-heading">
                <p className="eyebrow">Governance analytics</p>
                <h2>Feedback signals</h2>
                <p>
                    Inspect bounded review outcomes. These aggregates are
                    advisory and do not retrain providers or execute
                    remediation.
                </p>
            </section>
            <section
                className="settings-panel feedback-scope"
                aria-labelledby="feedback-scope-heading"
            >
                <h3 id="feedback-scope-heading">Feedback scope</h3>
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
                    <label>
                        Policy version
                        <input
                            value={policyVersion}
                            onChange={(event) => {
                                setPolicyVersion(event.target.value);
                            }}
                        />
                    </label>
                    <label>
                        From
                        <input
                            type="datetime-local"
                            value={from}
                            onChange={(event) => {
                                setFrom(
                                    event.target.value
                                        ? new Date(
                                              event.target.value,
                                          ).toISOString()
                                        : "",
                                );
                            }}
                        />
                    </label>
                    <label>
                        To
                        <input
                            type="datetime-local"
                            value={to}
                            onChange={(event) => {
                                setTo(
                                    event.target.value
                                        ? new Date(
                                              event.target.value,
                                          ).toISOString()
                                        : "",
                                );
                            }}
                        />
                    </label>
                </div>
                {!canQuery ? (
                    <p className="field-hint">
                        Enter both tenant identifiers to load feedback
                        analytics.
                    </p>
                ) : null}
            </section>
            {feedback.isError ? (
                <div className="notice notice-error" role="alert">
                    Feedback analytics could not be loaded.
                </div>
            ) : null}
            {feedback.isPending && canQuery ? (
                <p>Loading feedback analytics...</p>
            ) : null}
            {feedback.data?.items.length === 0 ? (
                <p className="empty-state">
                    No feedback aggregates match this scope.
                </p>
            ) : null}
            {feedback.data?.items.length ? (
                <section
                    className="feedback-grid"
                    aria-label="Feedback aggregates"
                >
                    {feedback.data.items.map((item) => (
                        <article className="feedback-card" key={item.id}>
                            <div className="feedback-card-heading">
                                <h3>{item.policyVersion}</h3>
                                <span className="status-badge">
                                    {item.suppressionReason}
                                </span>
                            </div>
                            <p className="feedback-window">
                                {new Date(item.windowStart).toLocaleString()} to{" "}
                                {new Date(item.windowEnd).toLocaleString()}
                            </p>
                            <strong>{item.sampleCount} samples</strong>
                            <dl className="feedback-counts">
                                <div>
                                    <dt>Accepted</dt>
                                    <dd>{item.acceptedCount}</dd>
                                </div>
                                <div>
                                    <dt>Edited</dt>
                                    <dd>{item.editedCount}</dd>
                                </div>
                                <div>
                                    <dt>Rejected</dt>
                                    <dd>{item.rejectedCount}</dd>
                                </div>
                                <div>
                                    <dt>Resolved</dt>
                                    <dd>{item.resolvedCount}</dd>
                                </div>
                            </dl>
                            {item.suppressionReason !== "NONE" ? (
                                <p className="field-hint">
                                    This aggregate is suppressed and is not an
                                    actionable provider signal.
                                </p>
                            ) : null}
                        </article>
                    ))}
                </section>
            ) : null}
        </>
    );
}
