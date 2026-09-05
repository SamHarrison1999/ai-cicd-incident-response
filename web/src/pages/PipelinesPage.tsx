import { useInfiniteQuery, useQuery } from "@tanstack/react-query";
import { useState } from "react";

import { getPipelineRuns, getPipelineTimeline } from "../api/pipelineRuns";
import { useAuth } from "../auth/useAuth";
import { useWorkspace } from "../workspace/useWorkspace";

const statusOptions = [
    "QUEUED",
    "RUNNING",
    "SUCCEEDED",
    "FAILED",
    "CANCELLED",
    "SKIPPED",
    "TIMED_OUT",
    "UNKNOWN",
];

const eventTypeOptions = [
    "PIPELINE_RUN_STARTED",
    "PIPELINE_RUN_COMPLETED",
    "PIPELINE_JOB_STARTED",
    "PIPELINE_JOB_COMPLETED",
    "DEPLOYMENT_STARTED",
    "DEPLOYMENT_COMPLETED",
];

function formatDate(value: string) {
    return new Intl.DateTimeFormat(undefined, {
        dateStyle: "medium",
        timeStyle: "short",
    }).format(new Date(value));
}

export function PipelinesPage() {
    const { accessToken } = useAuth();
    const { organisationId, projectId, setOrganisationId, setProjectId } =
        useWorkspace();
    const [status, setStatus] = useState("");
    const [branch, setBranch] = useState("");
    const [commitSha, setCommitSha] = useState("");
    const [environment, setEnvironment] = useState("");
    const [eventType, setEventType] = useState("");
    const canQuery =
        organisationId.trim().length > 0 && projectId.trim().length > 0;
    const runs = useQuery({
        queryKey: ["pipeline-runs", organisationId, projectId],
        queryFn: () =>
            getPipelineRuns(accessToken ?? "", organisationId, projectId),
        enabled: canQuery && accessToken !== null,
    });
    const timeline = useInfiniteQuery({
        queryKey: [
            "pipeline-timeline",
            organisationId,
            projectId,
            status,
            branch,
            commitSha,
            environment,
            eventType,
        ],
        queryFn: ({ pageParam }) =>
            getPipelineTimeline(accessToken ?? "", organisationId, projectId, {
                status,
                branch,
                commitSha,
                environment,
                eventType,
                cursor: pageParam ?? undefined,
                limit: 25,
            }),
        initialPageParam: null as string | null,
        getNextPageParam: (lastPage) => lastPage.nextCursor ?? undefined,
        enabled: canQuery && accessToken !== null,
    });
    const timelineEvents =
        timeline.data?.pages.flatMap((page) => page.items) ?? [];

    return (
        <>
            <section className="page-heading">
                <p className="eyebrow">Delivery visibility</p>
                <h2>Pipeline runs</h2>
                <p>
                    Explore provider-neutral pipeline events and run projections
                    within an organisation and project boundary.
                </p>
            </section>
            <section
                className="settings-panel"
                aria-labelledby="pipeline-scope-heading"
            >
                <h3 id="pipeline-scope-heading">Workspace scope</h3>
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
                </div>
                {!canQuery ? (
                    <p className="field-hint">
                        Enter both IDs to load the tenant-scoped pipeline view.
                    </p>
                ) : null}
            </section>
            <section
                className="settings-panel"
                aria-labelledby="timeline-filter-heading"
            >
                <div className="section-heading">
                    <div>
                        <p className="eyebrow">Query the read model</p>
                        <h3 id="timeline-filter-heading">Timeline filters</h3>
                    </div>
                    <span className="updated-label">Newest events first</span>
                </div>
                <div className="timeline-filter-grid">
                    <label>
                        Status
                        <select
                            value={status}
                            onChange={(event) => {
                                setStatus(event.target.value);
                            }}
                        >
                            <option value="">Any status</option>
                            {statusOptions.map((option) => (
                                <option key={option} value={option}>
                                    {option}
                                </option>
                            ))}
                        </select>
                    </label>
                    <label>
                        Event type
                        <select
                            value={eventType}
                            onChange={(event) => {
                                setEventType(event.target.value);
                            }}
                        >
                            <option value="">Any event type</option>
                            {eventTypeOptions.map((option) => (
                                <option key={option} value={option}>
                                    {option}
                                </option>
                            ))}
                        </select>
                    </label>
                    <label>
                        Branch
                        <input
                            value={branch}
                            onChange={(event) => {
                                setBranch(event.target.value);
                            }}
                        />
                    </label>
                    <label>
                        Commit SHA
                        <input
                            value={commitSha}
                            onChange={(event) => {
                                setCommitSha(event.target.value);
                            }}
                        />
                    </label>
                    <label>
                        Environment
                        <input
                            value={environment}
                            onChange={(event) => {
                                setEnvironment(event.target.value);
                            }}
                        />
                    </label>
                </div>
            </section>
            {runs.isPending && canQuery ? (
                <p>Loading pipeline runs...</p>
            ) : null}
            {runs.isError ? (
                <div className="notice notice-error" role="alert">
                    Pipeline runs could not be loaded.
                </div>
            ) : null}
            {runs.data?.length === 0 ? (
                <div className="empty-state">
                    <h3>No pipeline runs available</h3>
                    <p>
                        Run the signed webhook simulator to create a
                        deterministic demo event.
                    </p>
                </div>
            ) : null}
            {runs.data && runs.data.length > 0 ? (
                <section aria-labelledby="run-summary-heading">
                    <div className="section-heading">
                        <div>
                            <p className="eyebrow">Aggregated projections</p>
                            <h3 id="run-summary-heading">Pipeline runs</h3>
                        </div>
                    </div>
                    <div className="run-list" aria-label="Pipeline runs">
                        {runs.data.map((run) => (
                            <article className="run-card" key={run.id}>
                                <div>
                                    <p className="eyebrow">{run.provider}</p>
                                    <h4>{run.name}</h4>
                                    <p>
                                        {run.externalRunId} - attempt{" "}
                                        {run.attempt}
                                    </p>
                                </div>
                                <strong
                                    className={
                                        "run-status run-status-" +
                                        run.status.toLowerCase()
                                    }
                                >
                                    {run.status}
                                </strong>
                            </article>
                        ))}
                    </div>
                </section>
            ) : null}
            <section aria-labelledby="timeline-events-heading">
                <div className="section-heading">
                    <div>
                        <p className="eyebrow">Canonical event stream</p>
                        <h3 id="timeline-events-heading">Timeline events</h3>
                    </div>
                    {timelineEvents.length > 0 ? (
                        <span className="updated-label">
                            {timelineEvents.length} loaded
                        </span>
                    ) : null}
                </div>
                {timeline.isPending && canQuery ? (
                    <p>Loading timeline events...</p>
                ) : null}
                {timeline.isError ? (
                    <div className="notice notice-error" role="alert">
                        Timeline events could not be loaded.
                    </div>
                ) : null}
                {!timeline.isPending &&
                !timeline.isError &&
                canQuery &&
                timelineEvents.length === 0 ? (
                    <div className="empty-state">
                        <h3>No events match these filters</h3>
                        <p>
                            Try clearing a filter or run the signed webhook
                            simulator to create a timeline event.
                        </p>
                    </div>
                ) : null}
                {timelineEvents.length > 0 ? (
                    <div
                        className="timeline-event-list"
                        aria-label="Pipeline timeline"
                    >
                        {timelineEvents.map((event) => (
                            <article className="timeline-event" key={event.id}>
                                <div
                                    className="timeline-event-marker"
                                    aria-hidden="true"
                                />
                                <div className="timeline-event-content">
                                    <div className="timeline-event-heading">
                                        <div>
                                            <p className="eyebrow">
                                                {event.provider}
                                            </p>
                                            <h4>{event.eventType}</h4>
                                        </div>
                                        <strong
                                            className={
                                                "run-status run-status-" +
                                                event.status.toLowerCase()
                                            }
                                        >
                                            {event.status}
                                        </strong>
                                    </div>
                                    <p>{event.evidenceSummary}</p>
                                    <div className="timeline-meta">
                                        <span>{event.pipelineName}</span>
                                        <span>Run {event.externalRunId}</span>
                                        <span>Attempt {event.attempt}</span>
                                        <time dateTime={event.occurredAt}>
                                            {formatDate(event.occurredAt)}
                                        </time>
                                    </div>
                                </div>
                            </article>
                        ))}
                    </div>
                ) : null}
                {timeline.hasNextPage ? (
                    <button
                        className="button button-secondary timeline-load-more"
                        type="button"
                        onClick={() => timeline.fetchNextPage()}
                        disabled={timeline.isFetchingNextPage}
                    >
                        {timeline.isFetchingNextPage
                            ? "Loading more..."
                            : "Load more events"}
                    </button>
                ) : null}
            </section>
        </>
    );
}
