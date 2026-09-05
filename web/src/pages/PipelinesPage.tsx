import {
    useInfiniteQuery,
    useMutation,
    useQuery,
    useQueryClient,
} from "@tanstack/react-query";
import { useState } from "react";

import { simulateDemoCiRun, type DemoCiOutcome } from "../api/demoCi";
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
    const queryClient = useQueryClient();
    const { organisationId, projectId, setOrganisationId, setProjectId } =
        useWorkspace();

    const simulationAccessToken = accessToken ?? "";
    const [demoPipelineName, setDemoPipelineName] = useState("demo-deployment");
    const [demoBranch, setDemoBranch] = useState("main");
    const [demoOutcome, setDemoOutcome] = useState<DemoCiOutcome>("FAILED");

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
    const demoSimulation = useMutation({
        mutationFn: () =>
            simulateDemoCiRun(
                simulationAccessToken,
                organisationId,
                projectId,
                {
                    pipelineName: demoPipelineName,
                    branch: demoBranch,
                    outcome: demoOutcome,
                },
            ),
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ["pipeline-runs", organisationId, projectId],
                }),
                queryClient.invalidateQueries({
                    queryKey: ["pipeline-timeline", organisationId, projectId],
                }),
            ]);
        },
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
                aria-labelledby="demo-ci-heading"
            >
                <div className="section-heading">
                    <div>
                        <p className="eyebrow">Interactive portfolio demo</p>
                        <h3 id="demo-ci-heading">Demo CI simulator</h3>
                    </div>
                    <span className="updated-label">GitHub Actions</span>
                </div>

                <p>
                    Send a deterministic signed CI event through the real
                    webhook ingestion, normalisation, incident correlation, and
                    evidence capture pipeline.
                </p>

                <div className="timeline-filter-grid">
                    <label>
                        Pipeline name
                        <input
                            value={demoPipelineName}
                            onChange={(event) => {
                                setDemoPipelineName(event.target.value);
                            }}
                        />
                    </label>

                    <label>
                        Demo branch
                        <input
                            value={demoBranch}
                            onChange={(event) => {
                                setDemoBranch(event.target.value);
                            }}
                        />
                    </label>

                    <label>
                        Outcome
                        <select
                            value={demoOutcome}
                            onChange={(event) => {
                                setDemoOutcome(
                                    event.target.value as DemoCiOutcome,
                                );
                            }}
                        >
                            <option value="FAILED">Failed</option>
                            <option value="SUCCEEDED">Succeeded</option>
                        </select>
                    </label>
                </div>

                <button
                    className="button"
                    type="button"
                    disabled={
                        !canQuery ||
                        accessToken === null ||
                        demoSimulation.isPending
                    }
                    onClick={() => {
                        demoSimulation.mutate();
                    }}
                >
                    {demoSimulation.isPending
                        ? "Simulating..."
                        : "Simulate CI run"}
                </button>

                {!canQuery ? (
                    <p className="field-hint">
                        Select an organisation and project before running the
                        browser demo.
                    </p>
                ) : null}

                {demoSimulation.isSuccess ? (
                    <div className="notice notice-success" role="status">
                        Simulated {demoSimulation.data.outcome} pipeline run
                        accepted through signed GitHub Actions ingestion.
                    </div>
                ) : null}

                {demoSimulation.isError ? (
                    <div className="notice notice-error" role="alert">
                        Demo CI simulation failed.
                    </div>
                ) : null}

                <p className="field-hint">
                    This creates observational incident-response data only. It
                    does not execute automated remediation.
                </p>
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
                        Use the Demo CI simulator above to create a
                        deterministic pipeline event in this workspace.
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
                            Try clearing a filter or use the Demo CI simulator
                            above to create a timeline event.
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
