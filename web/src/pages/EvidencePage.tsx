import {
    useInfiniteQuery,
    useMutation,
    useQuery,
    useQueryClient,
} from "@tanstack/react-query";
import { useState } from "react";

import {
    createEvidence,
    getEvidence,
    getEvidenceItem,
    type EvidenceKind,
    type EvidenceRetentionClass,
} from "../api/evidence";
import { useAuth } from "../auth/useAuth";
import { useWorkspace } from "../workspace/useWorkspace";

const evidenceKinds: EvidenceKind[] = [
    "LOG_EXCERPT",
    "TRACE_OBSERVATION",
    "DEPLOYMENT_RECORD",
    "EVENT_SNAPSHOT",
    "STATUS_CHANGE",
];

const retentionClasses: EvidenceRetentionClass[] = [
    "SHORT",
    "STANDARD",
    "EXTENDED",
];

function formatDate(value: string) {
    return new Intl.DateTimeFormat(undefined, {
        dateStyle: "medium",
        timeStyle: "short",
    }).format(new Date(value));
}

export function EvidencePage() {
    const { accessToken } = useAuth();
    const queryClient = useQueryClient();
    const { organisationId, projectId, setOrganisationId, setProjectId } =
        useWorkspace();

    const [createKind, setCreateKind] = useState<EvidenceKind>("LOG_EXCERPT");
    const [retentionClass, setRetentionClass] =
        useState<EvidenceRetentionClass>("STANDARD");
    const [createSourceSystem, setCreateSourceSystem] =
        useState("portfolio-demo");
    const [createSourceReference, setCreateSourceReference] = useState(
        "browser-manual-evidence",
    );
    const [createContent, setCreateContent] = useState("");

    const [kind, setKind] = useState<EvidenceKind | "">("");
    const [sourceSystem, setSourceSystem] = useState("");
    const [query, setQuery] = useState("");
    const [selectedEvidenceId, setSelectedEvidenceId] = useState<string | null>(
        null,
    );
    const canQuery =
        organisationId.trim().length > 0 && projectId.trim().length > 0;
    const evidence = useInfiniteQuery({
        queryKey: [
            "evidence",
            organisationId,
            projectId,
            kind,
            sourceSystem,
            query,
        ],
        queryFn: ({ pageParam }) =>
            getEvidence(accessToken ?? "", organisationId, projectId, {
                kind: kind || undefined,
                sourceSystem: sourceSystem || undefined,
                query: query || undefined,
                cursor: pageParam ?? undefined,
                limit: 25,
            }),
        initialPageParam: null as string | null,
        getNextPageParam: (lastPage) => lastPage.nextCursor ?? undefined,
        enabled: canQuery && accessToken !== null,
    });
    const viewer = useQuery({
        queryKey: [
            "evidence-viewer",
            organisationId,
            projectId,
            selectedEvidenceId,
        ],
        queryFn: () =>
            getEvidenceItem(
                accessToken ?? "",
                organisationId,
                projectId,
                selectedEvidenceId ?? "",
            ),
        enabled:
            canQuery && accessToken !== null && selectedEvidenceId !== null,
    });
    const createMutation = useMutation({
        mutationFn: () =>
            createEvidence(accessToken ?? "", organisationId, projectId, {
                kind: createKind,
                retentionClass,
                sourceSystem: createSourceSystem,
                sourceReference: createSourceReference,
                occurredAt: new Date().toISOString(),
                content: createContent,
            }),
        onSuccess: async (created) => {
            setCreateContent("");
            setSelectedEvidenceId(created.id);

            await queryClient.invalidateQueries({
                queryKey: ["evidence", organisationId, projectId],
            });
        },
    });

    const items = evidence.data?.pages.flatMap((page) => page.items) ?? [];

    return (
        <>
            <section className="page-heading">
                <p className="eyebrow">Investigation workspace</p>
                <h2>Evidence</h2>
                <p>
                    Search bounded technical evidence and inspect the redacted
                    content used to support tenant-scoped incident review.
                </p>
            </section>
            <section
                className="settings-panel"
                aria-labelledby="evidence-scope-heading"
            >
                <h3 id="evidence-scope-heading">Workspace scope</h3>
                <div className="scope-form">
                    <label>
                        Organisation ID
                        <input
                            value={organisationId}
                            onChange={(event) => {
                                setOrganisationId(event.target.value);
                                setSelectedEvidenceId(null);
                            }}
                        />
                    </label>
                    <label>
                        Project ID
                        <input
                            value={projectId}
                            onChange={(event) => {
                                setProjectId(event.target.value);
                                setSelectedEvidenceId(null);
                            }}
                        />
                    </label>
                </div>
                {!canQuery ? (
                    <p className="field-hint">
                        Enter both IDs to load the tenant-scoped evidence
                        workspace.
                    </p>
                ) : null}
            </section>
            <section
                className="settings-panel"
                aria-labelledby="create-evidence-heading"
            >
                <div className="section-heading">
                    <div>
                        <p className="eyebrow">Investigation input</p>
                        <h3 id="create-evidence-heading">Add evidence</h3>
                    </div>
                    <span className="updated-label">Server sanitised</span>
                </div>

                <p>
                    Add bounded technical evidence directly from the browser for
                    investigation and recommendation grounding.
                </p>

                <div className="evidence-filter-grid">
                    <label>
                        New evidence kind
                        <select
                            value={createKind}
                            onChange={(event) => {
                                setCreateKind(
                                    event.target.value as EvidenceKind,
                                );
                            }}
                        >
                            {evidenceKinds.map((option) => (
                                <option key={option} value={option}>
                                    {option}
                                </option>
                            ))}
                        </select>
                    </label>

                    <label>
                        Retention class
                        <select
                            value={retentionClass}
                            onChange={(event) => {
                                setRetentionClass(
                                    event.target
                                        .value as EvidenceRetentionClass,
                                );
                            }}
                        >
                            {retentionClasses.map((option) => (
                                <option key={option} value={option}>
                                    {option}
                                </option>
                            ))}
                        </select>
                    </label>

                    <label>
                        New evidence source system
                        <input
                            value={createSourceSystem}
                            maxLength={80}
                            required
                            onChange={(event) => {
                                setCreateSourceSystem(event.target.value);
                            }}
                        />
                    </label>

                    <label>
                        Source reference
                        <input
                            value={createSourceReference}
                            maxLength={200}
                            required
                            onChange={(event) => {
                                setCreateSourceReference(event.target.value);
                            }}
                        />
                    </label>

                    <label className="evidence-query-field">
                        Evidence content
                        <textarea
                            value={createContent}
                            required
                            onChange={(event) => {
                                setCreateContent(event.target.value);
                            }}
                        />
                    </label>
                </div>

                <button
                    className="button"
                    type="button"
                    disabled={
                        !canQuery ||
                        accessToken === null ||
                        createContent.trim().length === 0 ||
                        createSourceSystem.trim().length === 0 ||
                        createSourceReference.trim().length === 0 ||
                        createMutation.isPending
                    }
                    onClick={() => {
                        createMutation.mutate();
                    }}
                >
                    {createMutation.isPending
                        ? "Adding evidence..."
                        : "Add evidence"}
                </button>

                {!canQuery ? (
                    <p className="field-hint">
                        Select an organisation and project before adding
                        evidence.
                    </p>
                ) : null}

                {createMutation.isSuccess ? (
                    <div className="notice notice-success" role="status">
                        Evidence created and added to the selected workspace.
                    </div>
                ) : null}

                {createMutation.isError ? (
                    <div className="notice notice-error" role="alert">
                        Evidence creation failed.
                    </div>
                ) : null}
            </section>

            <section
                className="settings-panel"
                aria-labelledby="evidence-filter-heading"
            >
                <div className="section-heading">
                    <div>
                        <p className="eyebrow">Bounded metadata query</p>
                        <h3 id="evidence-filter-heading">Evidence filters</h3>
                    </div>
                    <span className="updated-label">Newest evidence first</span>
                </div>
                <div className="evidence-filter-grid">
                    <label>
                        Evidence kind
                        <select
                            value={kind}
                            onChange={(event) => {
                                setKind(
                                    event.target.value as EvidenceKind | "",
                                );
                            }}
                        >
                            <option value="">Any kind</option>
                            {evidenceKinds.map((option) => (
                                <option key={option} value={option}>
                                    {option}
                                </option>
                            ))}
                        </select>
                    </label>
                    <label>
                        Source system
                        <input
                            value={sourceSystem}
                            onChange={(event) => {
                                setSourceSystem(event.target.value);
                            }}
                        />
                    </label>
                    <label className="evidence-query-field">
                        Search redacted content
                        <input
                            value={query}
                            onChange={(event) => {
                                setQuery(event.target.value);
                            }}
                        />
                    </label>
                </div>
            </section>
            {evidence.isPending && canQuery ? <p>Loading evidence</p> : null}
            {evidence.isError ? (
                <div className="notice notice-error" role="alert">
                    Evidence could not be loaded.
                </div>
            ) : null}
            {canQuery && !evidence.isPending && items.length === 0 ? (
                <div className="empty-state">
                    <h3>No evidence matches these filters</h3>
                    <p>Try a broader filter or select another project scope.</p>
                </div>
            ) : null}
            {items.length > 0 ? (
                <section
                    className="evidence-workspace"
                    aria-labelledby="evidence-results-heading"
                >
                    <div>
                        <div className="section-heading">
                            <div>
                                <p className="eyebrow">Tenant-scoped results</p>
                                <h3 id="evidence-results-heading">
                                    Evidence items
                                </h3>
                            </div>
                            <span className="updated-label">
                                {items.length} loaded
                            </span>
                        </div>
                        <div
                            className="evidence-list"
                            aria-label="Evidence items"
                        >
                            {items.map((item) => (
                                <button
                                    className={
                                        selectedEvidenceId === item.id
                                            ? "evidence-card evidence-card-selected"
                                            : "evidence-card"
                                    }
                                    key={item.id}
                                    type="button"
                                    onClick={() => {
                                        setSelectedEvidenceId(item.id);
                                    }}
                                    aria-pressed={
                                        selectedEvidenceId === item.id
                                    }
                                >
                                    <span className="evidence-card-heading">
                                        <strong>{item.kind}</strong>
                                        <span>{item.sourceSystem}</span>
                                    </span>
                                    <span className="evidence-card-reference">
                                        {item.sourceReference}
                                    </span>
                                    <span className="evidence-card-meta">
                                        {formatDate(item.occurredAt)} ·{" "}
                                        {item.contentLineCount} lines
                                    </span>
                                </button>
                            ))}
                        </div>
                        {evidence.hasNextPage ? (
                            <button
                                className="button button-secondary evidence-load-more"
                                type="button"
                                onClick={() => evidence.fetchNextPage()}
                                disabled={evidence.isFetchingNextPage}
                            >
                                {evidence.isFetchingNextPage
                                    ? "Loading more"
                                    : "Load more evidence"}
                            </button>
                        ) : null}
                    </div>
                    <aside
                        className="evidence-viewer"
                        aria-labelledby="evidence-viewer-heading"
                    >
                        <div className="section-heading">
                            <div>
                                <p className="eyebrow">Redacted projection</p>
                                <h3 id="evidence-viewer-heading">
                                    Evidence viewer
                                </h3>
                            </div>
                        </div>
                        {selectedEvidenceId === null ? (
                            <p className="field-hint">
                                Select an evidence item to inspect it.
                            </p>
                        ) : null}
                        {viewer.isPending ? (
                            <p>Loading evidence detail</p>
                        ) : null}
                        {viewer.isError ? (
                            <div className="notice notice-error" role="alert">
                                Evidence detail could not be loaded.
                            </div>
                        ) : null}
                        {viewer.data ? (
                            <>
                                <dl className="evidence-detail-list">
                                    <div>
                                        <dt>Source</dt>
                                        <dd>{viewer.data.sourceSystem}</dd>
                                    </div>
                                    <div>
                                        <dt>Reference</dt>
                                        <dd>{viewer.data.sourceReference}</dd>
                                    </div>
                                    <div>
                                        <dt>Links</dt>
                                        <dd>
                                            {viewer.data.incidentIds.length}{" "}
                                            incidents ·{" "}
                                            {viewer.data.eventIds.length} events
                                        </dd>
                                    </div>
                                </dl>
                                <pre className="evidence-content">
                                    <code>{viewer.data.content}</code>
                                </pre>
                            </>
                        ) : null}
                    </aside>
                </section>
            ) : null}
        </>
    );
}
