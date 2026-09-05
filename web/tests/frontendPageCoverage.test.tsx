/* eslint-disable @typescript-eslint/no-non-null-assertion, @typescript-eslint/unbound-method */
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import type { ReactElement } from "react";

const mocks = vi.hoisted(() => ({
    auth: {
        accessToken: null as string | null,
        currentUser: {
            userId: "user",
            email: "user@example.com",
            displayName: "User",
        },
        isInitialising: false,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
    },
    status: {
        isPending: false,
        isSuccess: true,
        data: { version: "1.0", status: "UP" },
        isError: false,
    },
    getDiagnosis: vi.fn(),
    getEvidence: vi.fn(),
    getEvidenceItem: vi.fn(),
    getFeedback: vi.fn(),
    getHistoricalRetrieval: vi.fn(),
    getIncidents: vi.fn(),
    transitionIncident: vi.fn(),
    getLearningComparison: vi.fn(),
    getLearningTrends: vi.fn(),
    createOrganisation: vi.fn(),
    getOrganisations: vi.fn(),
    getPipelineRuns: vi.fn(),
    getPipelineTimeline: vi.fn(),
    generateRecommendation: vi.fn(),
    getRecommendations: vi.fn(),
    createResolution: vi.fn(),
    getReviewHistory: vi.fn(),
    submitReview: vi.fn(),
}));

vi.mock("../src/auth/useAuth", () => ({ useAuth: () => mocks.auth }));
vi.mock("../src/hooks/useControlPlaneStatus", () => ({
    useControlPlaneStatus: () => mocks.status,
}));
vi.mock("../src/api/diagnosis", () => ({
    getDiagnosis: mocks.getDiagnosis,
}));
vi.mock("../src/api/evidence", () => ({
    getEvidence: mocks.getEvidence,
    getEvidenceItem: mocks.getEvidenceItem,
}));
vi.mock("../src/api/feedback", () => ({ getFeedback: mocks.getFeedback }));
vi.mock("../src/api/historicalRetrieval", () => ({
    getHistoricalRetrieval: mocks.getHistoricalRetrieval,
}));
vi.mock("../src/api/incidents", () => ({
    getIncidents: mocks.getIncidents,
    transitionIncident: mocks.transitionIncident,
}));
vi.mock("../src/api/learning", () => ({
    getLearningComparison: mocks.getLearningComparison,
    getLearningTrends: mocks.getLearningTrends,
}));
vi.mock("../src/api/organisations", () => ({
    createOrganisation: mocks.createOrganisation,
    getOrganisations: mocks.getOrganisations,
}));
vi.mock("../src/api/pipelineRuns", () => ({
    getPipelineRuns: mocks.getPipelineRuns,
    getPipelineTimeline: mocks.getPipelineTimeline,
}));
vi.mock("../src/api/recommendations", () => ({
    generateRecommendation: mocks.generateRecommendation,
    getRecommendations: mocks.getRecommendations,
}));
vi.mock("../src/api/reviews", () => ({
    createResolution: mocks.createResolution,
    getReviewHistory: mocks.getReviewHistory,
    submitReview: mocks.submitReview,
}));

import { AppLayout } from "../src/components/layout/AppLayout";
import { SideNavigation } from "../src/components/layout/SideNavigation";
import { TopBar } from "../src/components/layout/TopBar";
import { EmptyState } from "../src/components/common/EmptyState";
import { ServiceStatusCard } from "../src/components/status/ServiceStatusCard";
import { DashboardPage } from "../src/pages/DashboardPage";
import { DiagnosisPage } from "../src/pages/DiagnosisPage";
import { EvidencePage } from "../src/pages/EvidencePage";
import { FeedbackPage } from "../src/pages/FeedbackPage";
import { HistoricalRetrievalPage } from "../src/pages/HistoricalRetrievalPage";
import { IncidentsPage } from "../src/pages/IncidentsPage";
import { LearningPage } from "../src/pages/LearningPage";
import { NotFoundPage } from "../src/pages/NotFoundPage";
import { OrganisationsPage } from "../src/pages/OrganisationsPage";
import { PipelinesPage } from "../src/pages/PipelinesPage";
import { RecommendationsPage } from "../src/pages/RecommendationsPage";
import { ReviewPage } from "../src/pages/ReviewPage";
import { SettingsPage } from "../src/pages/SettingsPage";

function client() {
    return new QueryClient({
        defaultOptions: {
            queries: { retry: false },
            mutations: { retry: false },
        },
    });
}

function renderPage(element: ReactElement) {
    cleanup();
    return render(
        <MemoryRouter>
            <QueryClientProvider client={client()}>
                {element}
            </QueryClientProvider>
        </MemoryRouter>,
    );
}

async function fillScope(
    user: ReturnType<typeof userEvent.setup>,
    labels: string[],
) {
    for (const [index, label] of labels.entries()) {
        await user.type(screen.getAllByRole("textbox")[index]!, label);
    }
}

beforeEach(() => {
    vi.clearAllMocks();
    mocks.auth.accessToken = "token";
    mocks.status.isPending = false;
    mocks.status.isSuccess = true;
    mocks.status.isError = false;
    mocks.getDiagnosis.mockResolvedValue({
        ruleVersion: "rules-1",
        category: "UNKNOWN",
        confidence: 0.42,
        supportingSignalIds: ["signal"],
        missingEvidence: ["logs"],
        warnings: ["review"],
        abstentionReason: null,
    });
    mocks.getEvidence.mockResolvedValue({
        items: [
            {
                id: "evidence",
                kind: "LOG_EXCERPT",
                retentionClass: "STANDARD",
                sourceSystem: "github",
                sourceReference: "delivery",
                occurredAt: "2026-08-16T10:00:00Z",
                ingestedAt: "2026-08-16T10:01:00Z",
                contentHash: "hash",
                contentLineCount: 2,
            },
        ],
        nextCursor: null,
    });
    mocks.getEvidenceItem.mockResolvedValue({
        id: "evidence",
        kind: "LOG_EXCERPT",
        retentionClass: "STANDARD",
        sourceSystem: "github",
        sourceReference: "delivery",
        occurredAt: "2026-08-16T10:00:00Z",
        ingestedAt: "2026-08-16T10:01:00Z",
        contentHash: "hash",
        contentLineCount: 2,
        content: "redacted",
        incidentIds: ["incident"],
        eventIds: ["event"],
    });
    mocks.getFeedback.mockResolvedValue({
        items: [
            {
                id: "feedback",
                policyVersion: "policy-1",
                windowStart: "2026-08-15T00:00:00Z",
                windowEnd: "2026-08-16T00:00:00Z",
                sampleCount: 10,
                acceptedCount: 4,
                editedCount: 2,
                rejectedCount: 1,
                resolvedCount: 3,
                suppressionReason: "SMALL_SAMPLE",
            },
        ],
    });
    mocks.getHistoricalRetrieval.mockResolvedValue({
        items: [
            {
                id: "history",
                incidentId: "incident",
                sourceKind: "REVIEW",
                sourceId: "source",
                occurredAt: "2026-08-16T10:00:00Z",
                provider: "github",
                pipelineName: "build",
                environmentName: "prod",
                gitRef: "main",
                commitSha: "abc",
                diagnosisCategory: "UNKNOWN",
                summary: "Prior failure",
                matchExplanation: "Same category",
                provenanceReference: "review-1",
            },
        ],
        nextCursor: "next",
        hasNext: true,
    });
    mocks.getIncidents.mockResolvedValue([
        {
            id: "incident",
            status: "DETECTED",
            title: "Pipeline failed",
            summary: "Bounded summary",
            detectedAt: "2026-08-16T10:00:00Z",
            resolvedAt: null,
            createdAt: "2026-08-16T10:00:00Z",
            updatedAt: "2026-08-16T10:00:00Z",
        },
    ]);
    mocks.transitionIncident.mockResolvedValue({});
    mocks.getLearningTrends.mockResolvedValue({
        items: [
            {
                id: "trend",
                dimension: "INCIDENT_CATEGORY",
                dimensionKey: "UNKNOWN",
                windowStart: "2026-08-15T00:00:00Z",
                windowEnd: "2026-08-16T00:00:00Z",
                aggregationVersion: "v1",
                sampleCount: 10,
                observedCount: 5,
                sourceReference: "feedback",
                suppressionReason: "NONE",
            },
        ],
        nextCursor: null,
        hasNext: false,
    });
    mocks.getLearningComparison.mockResolvedValue({
        dimensionKey: "UNKNOWN",
        currentCount: 5,
        previousCount: 3,
        delta: 2,
        suppressionReason: "NONE",
    });
    mocks.getOrganisations.mockResolvedValue([
        {
            id: "org",
            name: "Org",
            slug: "org",
            createdAt: "now",
            updatedAt: "now",
            version: 1,
        },
    ]);
    mocks.createOrganisation.mockResolvedValue({});
    mocks.getPipelineRuns.mockResolvedValue([
        {
            id: "run",
            eventSourceId: "source",
            provider: "GITHUB_ACTIONS",
            externalRunId: "42",
            name: "build",
            attempt: 1,
            status: "FAILED",
            commitSha: "abc",
            gitRef: "main",
            environmentName: "prod",
            startedAt: "now",
            completedAt: "now",
            lastEventOccurredAt: "now",
            updatedAt: "now",
        },
    ]);
    mocks.getPipelineTimeline.mockResolvedValue({
        items: [
            {
                id: "event",
                pipelineRunId: "run",
                provider: "GITHUB_ACTIONS",
                eventType: "PIPELINE_RUN_COMPLETED",
                status: "FAILED",
                externalRunId: "42",
                pipelineName: "build",
                attempt: 1,
                commitSha: "abc",
                gitRef: "main",
                environmentName: "prod",
                occurredAt: "2026-08-16T10:00:00Z",
                receivedAt: "2026-08-16T10:01:00Z",
                evidenceSummary: "failure",
            },
        ],
        nextCursor: null,
        hasNext: false,
    });
    mocks.getRecommendations.mockResolvedValue({
        items: [
            {
                id: "recommendation",
                category: "DEPENDENCY",
                summary: "Investigate dependency",
                likelyCause: "dependency",
                confidence: 0.8,
                confidenceExplanation: "Supported signal",
                status: "RECOMMENDED",
                abstentionReason: null,
                providerName: "deterministic",
                modelVersion: "v1",
                retrievalSetVersion: "v1",
                citations: 1,
            },
        ],
    });
    mocks.generateRecommendation.mockResolvedValue({});
    mocks.getReviewHistory.mockResolvedValue({
        items: [
            {
                id: "review",
                action: "ACCEPT",
                reason: "NONE",
                comment: null,
                reviewedVersionId: "version",
                createdAt: "now",
            },
        ],
    });
    mocks.submitReview.mockResolvedValue({});
    mocks.createResolution.mockResolvedValue({ id: "resolution" });
});

describe("frontend page and component coverage", () => {
    it("covers shared components and dashboard states", async () => {
        const user = userEvent.setup();
        renderPage(<DashboardPage />);
        expect((await screen.findAllByText("Healthy")).length).toBe(3);

        mocks.status.isPending = true;
        mocks.status.isSuccess = false;
        renderPage(<DashboardPage />);
        expect(screen.getByText("Checking")).toBeInTheDocument();

        mocks.status.isPending = false;
        mocks.status.isError = true;
        renderPage(<DashboardPage />);
        expect(screen.getByText("Unavailable")).toBeInTheDocument();

        renderPage(
            <>
                <EmptyState title="Empty" description="Nothing" />
                <ServiceStatusCard
                    name="Service"
                    description="Description"
                    status="loading"
                    detail="Detail"
                />
                <>
                    <SideNavigation />
                    <TopBar />
                    <AppLayout />
                </>
            </>,
        );
        expect(screen.getByText("Empty")).toBeInTheDocument();
        expect(screen.getAllByRole("navigation").length).toBeGreaterThanOrEqual(
            2,
        );
        await user.click(
            screen.getAllByRole("button", { name: "Sign out" })[0]!,
        );
        expect(mocks.auth.logout).toHaveBeenCalled();
    });

    it("covers diagnosis, evidence, feedback, historical retrieval, and learning", async () => {
        const user = userEvent.setup();

        const diagnosis = renderPage(<DiagnosisPage />);
        await fillScope(user, ["org", "project"]);
        expect(await screen.findByText("UNKNOWN")).toBeInTheDocument();
        expect(screen.getByText(/Missing evidence/)).toBeInTheDocument();
        diagnosis.unmount();

        const evidence = renderPage(<EvidencePage />);
        await fillScope(user, ["org", "project"]);
        expect(await screen.findByText("Evidence items")).toBeInTheDocument();
        await user.click(screen.getByRole("button", { name: /LOG_EXCERPT/ }));
        expect(await screen.findByText("redacted")).toBeInTheDocument();
        evidence.unmount();

        const feedback = renderPage(<FeedbackPage />);
        await fillScope(user, ["org", "project", "policy"]);
        expect(await screen.findByText("policy-1")).toBeInTheDocument();
        expect(screen.getByText(/suppressed/)).toBeInTheDocument();
        feedback.unmount();

        const historical = renderPage(<HistoricalRetrievalPage />);
        await fillScope(user, [
            "org",
            "project",
            "UNKNOWN",
            "github",
            "build",
            "Prior",
        ]);
        expect(await screen.findByText("Prior failure")).toBeInTheDocument();
        await user.click(screen.getByRole("button", { name: /Load more/ }));
        historical.unmount();

        renderPage(<LearningPage />);
        await fillScope(user, ["org", "project", "INCIDENT", "UNKNOWN"]);
        expect(
            await screen.findByText("Adjacent-window comparison"),
        ).toBeInTheDocument();
        expect(screen.getByText("+2 change")).toBeInTheDocument();
    });

    it("covers incidents, pipelines, recommendations, and review workflows", async () => {
        const user = userEvent.setup();

        const incidents = renderPage(<IncidentsPage />);
        await fillScope(user, ["org", "project"]);
        expect(await screen.findByText("Pipeline failed")).toBeInTheDocument();
        await user.selectOptions(
            screen.getByRole("combobox", { name: /Change status/ }),
            "TRIAGED",
        );
        incidents.unmount();

        const pipelines = renderPage(<PipelinesPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByRole("heading", { name: "build" }),
        ).toBeInTheDocument();
        await user.selectOptions(screen.getAllByRole("combobox")[0]!, "FAILED");
        pipelines.unmount();

        const recommendations = renderPage(<RecommendationsPage />);
        await fillScope(user, ["org", "project"]);

        expect(
            await screen.findByText("Investigate dependency"),
        ).toBeInTheDocument();

        const generateButton = screen.getByRole("button", {
            name: /Generate bounded/,
        });

        expect(generateButton).toBeDisabled();

        const evidenceCheckbox = await screen.findByRole("checkbox", {
            name: /Select LOG_EXCERPT delivery/,
        });

        await user.click(evidenceCheckbox);
        expect(generateButton).toBeEnabled();

        await user.click(evidenceCheckbox);
        expect(generateButton).toBeDisabled();

        await user.click(evidenceCheckbox);

        await user.type(
            screen.getByLabelText("Incident ID (optional)"),
            "incident",
        );

        await user.click(generateButton);

        expect(mocks.generateRecommendation).toHaveBeenCalledWith(
            "token",
            "org",
            "project",
            "incident",
            ["evidence"],
            [],
        );

        recommendations.unmount();

        renderPage(<ReviewPage />);
        await fillScope(user, ["org", "project", "recommendation"]);
        expect(await screen.findByText("ACCEPT")).toBeInTheDocument();
        await user.selectOptions(screen.getByLabelText("Action"), "EDIT");
        await user.type(screen.getByLabelText("Edited summary"), "edited");
        await user.type(screen.getByLabelText("Edited cause"), "cause");
        await user.type(screen.getByLabelText("Comment"), "comment");
        await user.click(screen.getByRole("button", { name: "Submit review" }));
        await user.type(screen.getByLabelText("Incident ID"), "incident");
        await user.type(screen.getByLabelText("Resolution text"), "resolution");
        await user.click(
            screen.getByRole("button", { name: /Record bounded/ }),
        );
    });

    it("covers organisation, settings, and not-found presentations", async () => {
        const user = userEvent.setup();
        const organisations = renderPage(<OrganisationsPage />);
        expect(await screen.findByText("Org")).toBeInTheDocument();
        await user.type(screen.getByLabelText("Name"), "New org");
        await user.type(screen.getAllByRole("textbox")[1]!, "new-org");
        await user.click(
            screen.getByRole("button", { name: "Create organisation" }),
        );
        organisations.unmount();

        renderPage(<SettingsPage />);
        expect(screen.getByText("Automatic remediation")).toBeInTheDocument();
        renderPage(<NotFoundPage />);
        expect(
            screen.getByRole("heading", { name: "Page not found" }),
        ).toBeInTheDocument();
    });

    it("covers empty, loading, error, and alternate data branches", async () => {
        const user = userEvent.setup();
        const never = new Promise<never>(() => undefined);

        mocks.getDiagnosis.mockReset();
        mocks.getDiagnosis.mockReturnValue(never);
        renderPage(<DiagnosisPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText("Loading diagnosis..."),
        ).toBeInTheDocument();
        cleanup();

        mocks.getDiagnosis.mockReset();
        mocks.getDiagnosis.mockRejectedValue(new Error("diagnosis"));
        renderPage(<DiagnosisPage />);
        await fillScope(user, ["org", "project"]);
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Diagnosis could not",
        );
        cleanup();

        mocks.getDiagnosis.mockReset();
        mocks.getDiagnosis.mockResolvedValue({
            ruleVersion: "rules-2",
            category: "DEPENDENCY_FAILURE",
            confidence: 0,
            supportingSignalIds: [],
            missingEvidence: [],
            warnings: [],
            abstentionReason: "Insufficient evidence",
        });
        renderPage(<DiagnosisPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText("Insufficient evidence"),
        ).toBeInTheDocument();
        cleanup();

        mocks.getEvidence.mockReset();
        mocks.getEvidence.mockResolvedValue({ items: [], nextCursor: null });
        renderPage(<EvidencePage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText("No evidence matches these filters"),
        ).toBeInTheDocument();
        cleanup();

        mocks.getEvidence.mockReset();
        mocks.getEvidence.mockReturnValue(never);
        renderPage(<EvidencePage />);
        await fillScope(user, ["org", "project"]);
        expect(await screen.findByText("Loading evidence")).toBeInTheDocument();
        cleanup();

        mocks.getEvidence.mockReset();
        mocks.getEvidence.mockRejectedValue(new Error("evidence"));
        renderPage(<EvidencePage />);
        await fillScope(user, ["org", "project"]);
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Evidence could not",
        );
        cleanup();

        mocks.getEvidence.mockReset();
        mocks.getEvidence.mockResolvedValue({
            items: [
                {
                    id: "evidence",
                    kind: "LOG_EXCERPT",
                    retentionClass: "STANDARD",
                    sourceSystem: "github",
                    sourceReference: "delivery",
                    occurredAt: "2026-08-16T10:00:00Z",
                    ingestedAt: "2026-08-16T10:01:00Z",
                    contentHash: "hash",
                    contentLineCount: 2,
                },
            ],
            nextCursor: null,
        });
        mocks.getEvidenceItem.mockReset();
        mocks.getEvidenceItem.mockReturnValue(never);
        renderPage(<EvidencePage />);
        await fillScope(user, ["org", "project"]);
        await screen.findByText("Evidence items");
        await user.click(screen.getByRole("button", { name: /LOG_EXCERPT/ }));
        expect(
            await screen.findByText("Loading evidence detail"),
        ).toBeInTheDocument();
        cleanup();

        mocks.getEvidenceItem.mockReset();
        mocks.getEvidenceItem.mockRejectedValue(new Error("viewer"));
        renderPage(<EvidencePage />);
        await fillScope(user, ["org", "project"]);
        await screen.findByText("Evidence items");
        await user.click(screen.getByRole("button", { name: /LOG_EXCERPT/ }));
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Evidence detail could not",
        );
        cleanup();

        mocks.getEvidence.mockReset();
        mocks.getEvidence.mockResolvedValue({
            items: [
                {
                    id: "evidence",
                    kind: "LOG_EXCERPT",
                    retentionClass: "STANDARD",
                    sourceSystem: "github",
                    sourceReference: "delivery",
                    occurredAt: "2026-08-16T10:00:00Z",
                    ingestedAt: "2026-08-16T10:01:00Z",
                    contentHash: "hash",
                    contentLineCount: 2,
                },
            ],
            nextCursor: "next",
        });
        renderPage(<EvidencePage />);
        await fillScope(user, ["org", "project"]);
        await user.selectOptions(
            screen.getByLabelText("Evidence kind"),
            "LOG_EXCERPT",
        );
        await user.type(screen.getByLabelText("Source system"), "github");
        await user.type(
            screen.getByLabelText("Search redacted content"),
            "timeout",
        );
        await screen.findByText("Evidence items");

        mocks.getEvidence.mockResolvedValueOnce({
            items: [
                {
                    id: "evidence-page-2",
                    kind: "LOG_EXCERPT",
                    retentionClass: "STANDARD",
                    sourceSystem: "github",
                    sourceReference: "delivery-page-2",
                    occurredAt: "2026-08-16T09:55:00Z",
                    ingestedAt: "2026-08-16T09:56:00Z",
                    contentHash: "hash-page-2",
                    contentLineCount: 3,
                },
            ],
            nextCursor: null,
        });

        await user.click(
            screen.getByRole("button", { name: "Load more evidence" }),
        );

        expect(await screen.findByText("2 loaded")).toBeInTheDocument();

        cleanup();

        mocks.getEvidence.mockReset();
        mocks.getEvidence.mockResolvedValue({
            items: [
                {
                    id: "evidence",
                    kind: "LOG_EXCERPT",
                    retentionClass: "STANDARD",
                    sourceSystem: "github",
                    sourceReference: "delivery",
                    occurredAt: "2026-08-16T10:00:00Z",
                    ingestedAt: "2026-08-16T10:01:00Z",
                    contentHash: "hash",
                    contentLineCount: 2,
                },
            ],
            nextCursor: "next",
        });
        renderPage(<EvidencePage />);
        await fillScope(user, ["org", "project"]);
        await screen.findByText("Evidence items");
        mocks.getEvidence.mockReset();
        mocks.getEvidence.mockReturnValue(never);
        await user.click(
            screen.getByRole("button", { name: "Load more evidence" }),
        );
        expect(
            await screen.findByRole("button", { name: "Loading more" }),
        ).toBeDisabled();
        cleanup();

        mocks.getFeedback.mockReset();
        mocks.getFeedback.mockResolvedValue({ items: [] });
        renderPage(<FeedbackPage />);
        await fillScope(user, ["org", "project", "policy"]);
        fireEvent.change(
            document.querySelectorAll('input[type="datetime-local"]')[0]!,
            {
                target: { value: "2026-08-15T00:00" },
            },
        );
        fireEvent.change(
            document.querySelectorAll('input[type="datetime-local"]')[1]!,
            {
                target: { value: "" },
            },
        );
        fireEvent.change(
            document.querySelectorAll('input[type="datetime-local"]')[1]!,
            {
                target: { value: "2026-08-16T00:00" },
            },
        );
        await user.clear(
            document.querySelectorAll('input[type="datetime-local"]')[0]!,
        );
        await user.clear(
            document.querySelectorAll('input[type="datetime-local"]')[1]!,
        );
        const dateInputs = document.querySelectorAll<HTMLInputElement>(
            'input[type="datetime-local"]',
        );
        const valueSetter = Object.getOwnPropertyDescriptor(
            HTMLInputElement.prototype,
            "value",
        )?.set;
        valueSetter?.call(dateInputs[0], "2026-08-15T00:00");
        fireEvent.change(dateInputs[0]!, { target: { value: "" } });
        valueSetter?.call(dateInputs[1], "2026-08-16T00:00");
        fireEvent.change(dateInputs[1]!, { target: { value: "" } });
        expect(
            await screen.findByText(/No feedback aggregates match/),
        ).toBeInTheDocument();
        cleanup();

        mocks.getFeedback.mockReset();
        mocks.getFeedback.mockReturnValue(never);
        renderPage(<FeedbackPage />);
        await fillScope(user, ["org", "project", "policy"]);
        expect(
            await screen.findByText("Loading feedback analytics..."),
        ).toBeInTheDocument();
        cleanup();

        mocks.getFeedback.mockReset();
        mocks.getFeedback.mockRejectedValue(new Error("feedback"));
        renderPage(<FeedbackPage />);
        await fillScope(user, ["org", "project", "policy"]);
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Feedback analytics could not",
        );
        cleanup();

        mocks.getFeedback.mockReset();
        mocks.getFeedback.mockResolvedValue({
            items: [
                {
                    id: "feedback-2",
                    policyVersion: "policy-2",
                    windowStart: "2026-08-15T00:00:00Z",
                    windowEnd: "2026-08-16T00:00:00Z",
                    sampleCount: 4,
                    acceptedCount: 1,
                    editedCount: 1,
                    rejectedCount: 1,
                    resolvedCount: 1,
                    suppressionReason: "NONE",
                },
            ],
        });
        renderPage(<FeedbackPage />);
        await fillScope(user, ["org", "project", "policy"]);
        expect(await screen.findByText("policy-2")).toBeInTheDocument();
        expect(
            screen.queryByText(/This aggregate is suppressed/),
        ).not.toBeInTheDocument();
        cleanup();

        mocks.getHistoricalRetrieval.mockReset();
        mocks.getHistoricalRetrieval.mockResolvedValue({
            items: [],
            nextCursor: null,
            hasNext: false,
        });
        renderPage(<HistoricalRetrievalPage />);
        await fillScope(user, ["org", "project"]);
        fireEvent.change(screen.getAllByRole("textbox")[2]!, {
            target: { value: "" },
        });
        expect(
            await screen.findByText("No historical matches"),
        ).toBeInTheDocument();
        cleanup();

        mocks.getHistoricalRetrieval.mockReset();
        mocks.getHistoricalRetrieval.mockReturnValue(never);
        renderPage(<HistoricalRetrievalPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText("Loading historical context..."),
        ).toBeInTheDocument();
        cleanup();

        mocks.getHistoricalRetrieval.mockReset();
        mocks.getHistoricalRetrieval.mockRejectedValue(new Error("history"));
        renderPage(<HistoricalRetrievalPage />);
        await fillScope(user, ["org", "project"]);
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Historical context could not",
        );
        cleanup();

        mocks.getHistoricalRetrieval.mockReset();
        mocks.getHistoricalRetrieval.mockResolvedValue({
            items: [
                {
                    id: "history-2",
                    incidentId: "incident",
                    sourceKind: "REVIEW",
                    sourceId: "source",
                    occurredAt: "2026-08-16T10:00:00Z",
                    provider: null,
                    pipelineName: null,
                    environmentName: null,
                    gitRef: null,
                    commitSha: null,
                    diagnosisCategory: null,
                    summary: "Minimal history",
                    matchExplanation: "Bounded",
                    provenanceReference: "source",
                },
            ],
            nextCursor: "next",
            hasNext: true,
        });
        renderPage(<HistoricalRetrievalPage />);
        await fillScope(user, ["org", "project"]);
        expect(await screen.findByText("Minimal history")).toBeInTheDocument();
        await user.click(
            screen.getByRole("button", {
                name: "Load more historical context",
            }),
        );
        cleanup();

        mocks.getHistoricalRetrieval.mockReset();
        mocks.getHistoricalRetrieval.mockResolvedValue({
            items: [
                {
                    id: "history-3",
                    incidentId: "incident",
                    sourceKind: "REVIEW",
                    sourceId: "source",
                    occurredAt: "2026-08-16T10:00:00Z",
                    provider: null,
                    pipelineName: null,
                    environmentName: null,
                    gitRef: null,
                    commitSha: null,
                    diagnosisCategory: null,
                    summary: "No next page",
                    matchExplanation: "Bounded",
                    provenanceReference: "source",
                },
            ],
            nextCursor: null,
            hasNext: false,
        });
        renderPage(<HistoricalRetrievalPage />);
        await fillScope(user, ["org", "project"]);
        expect(await screen.findByText("No next page")).toBeInTheDocument();
        await user.type(screen.getAllByRole("textbox")[2]!, "category");
        await user.clear(screen.getAllByRole("textbox")[2]!);
        cleanup();

        mocks.getHistoricalRetrieval.mockReset();
        mocks.getHistoricalRetrieval.mockResolvedValue({
            items: [
                {
                    id: "history-4",
                    incidentId: "incident",
                    sourceKind: "REVIEW",
                    sourceId: "source",
                    occurredAt: "2026-08-16T10:00:00Z",
                    provider: null,
                    pipelineName: null,
                    environmentName: null,
                    gitRef: null,
                    commitSha: null,
                    diagnosisCategory: null,
                    summary: "Null cursor",
                    matchExplanation: "Bounded",
                    provenanceReference: "source",
                },
            ],
            nextCursor: null,
            hasNext: true,
        });
        renderPage(<HistoricalRetrievalPage />);
        await fillScope(user, ["org", "project"]);
        await screen.findByText("Null cursor");
        await user.click(
            screen.getByRole("button", {
                name: "Load more historical context",
            }),
        );
        cleanup();

        mocks.getIncidents.mockReset();
        mocks.getIncidents.mockResolvedValue([]);
        renderPage(<IncidentsPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText("No incidents available"),
        ).toBeInTheDocument();
        cleanup();

        mocks.getIncidents.mockReset();
        mocks.getIncidents.mockReturnValue(never);
        renderPage(<IncidentsPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText("Loading incidents..."),
        ).toBeInTheDocument();
        cleanup();

        mocks.getIncidents.mockReset();
        mocks.getIncidents.mockRejectedValue(new Error("incidents"));
        renderPage(<IncidentsPage />);
        await fillScope(user, ["org", "project"]);
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Incidents could not",
        );
        cleanup();

        mocks.getIncidents.mockReset();
        mocks.getIncidents.mockResolvedValue([
            {
                id: "incident-2",
                status: "RESOLVED",
                title: "Resolved incident",
                summary: "Done",
                detectedAt: "now",
                resolvedAt: "later",
                createdAt: "now",
                updatedAt: "later",
            },
        ]);
        mocks.transitionIncident.mockRejectedValueOnce(new Error("transition"));
        renderPage(<IncidentsPage />);
        await fillScope(user, ["org", "project"]);
        await screen.findByText("Resolved incident");
        const incidentSelect = screen.getByRole("combobox", {
            name: /Resolved incident/,
        });
        await user.selectOptions(incidentSelect, "RESOLVED");
        await user.selectOptions(incidentSelect, "DETECTED");
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "status could not",
        );
        cleanup();

        mocks.getLearningTrends.mockReset();
        mocks.getLearningTrends.mockResolvedValue({
            items: [],
            nextCursor: null,
            hasNext: false,
        });
        mocks.getLearningComparison.mockReset();
        mocks.getLearningComparison.mockResolvedValue({
            dimensionKey: null,
            currentCount: 0,
            previousCount: 2,
            delta: -2,
            suppressionReason: "SMALL_SAMPLE",
        });
        renderPage(<LearningPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText("No comparable dimension"),
        ).toBeInTheDocument();
        expect(screen.getByText("-2 change")).toBeInTheDocument();
        expect(
            screen.getByText(/No trend projections match/),
        ).toBeInTheDocument();
        cleanup();

        mocks.getLearningTrends.mockReset();
        mocks.getLearningTrends.mockReturnValue(never);
        renderPage(<LearningPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText("Loading trends..."),
        ).toBeInTheDocument();
        cleanup();

        mocks.getLearningTrends.mockReset();
        mocks.getLearningTrends.mockRejectedValue(new Error("learning"));
        renderPage(<LearningPage />);
        await fillScope(user, ["org", "project"]);
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Operational learning could not",
        );
    }, 20_000);

    it("covers organisation, pipeline, recommendation, and review edge states", async () => {
        const user = userEvent.setup();
        const never = new Promise<never>(() => undefined);

        mocks.getOrganisations.mockReset();
        mocks.getOrganisations.mockResolvedValue([]);
        renderPage(<OrganisationsPage />);
        expect(
            await screen.findByText(/No organisations yet/),
        ).toBeInTheDocument();
        cleanup();

        mocks.getOrganisations.mockReset();
        mocks.getOrganisations.mockReturnValue(never);
        renderPage(<OrganisationsPage />);
        expect(
            await screen.findByText(/Loading organisations/),
        ).toBeInTheDocument();
        cleanup();

        mocks.getOrganisations.mockReset();
        mocks.getOrganisations.mockRejectedValue(new Error("organisations"));
        renderPage(<OrganisationsPage />);
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Organisations could not",
        );
        cleanup();

        mocks.getOrganisations.mockReset();
        mocks.getOrganisations.mockResolvedValue([
            {
                id: "org",
                name: "Org",
                slug: "org",
                createdAt: "now",
                updatedAt: "now",
                version: 1,
            },
        ]);
        mocks.createOrganisation.mockReset();
        mocks.createOrganisation.mockRejectedValue(new Error("create"));
        renderPage(<OrganisationsPage />);
        await screen.findByText("Org");
        await user.type(screen.getAllByRole("textbox")[0]!, "New org");
        await user.type(screen.getAllByRole("textbox")[1]!, "new-org");
        await user.click(
            screen.getByRole("button", { name: "Create organisation" }),
        );
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Organisation creation failed",
        );
        cleanup();

        mocks.createOrganisation.mockReset();
        mocks.createOrganisation.mockReturnValue(never);
        renderPage(<OrganisationsPage />);
        await screen.findByText("Org");
        await user.type(screen.getAllByRole("textbox")[0]!, "New org");
        await user.type(screen.getAllByRole("textbox")[1]!, "new-org");
        await user.click(
            screen.getByRole("button", { name: "Create organisation" }),
        );
        expect(
            await screen.findByRole("button", { name: /Creating/ }),
        ).toBeDisabled();
        cleanup();

        mocks.auth.accessToken = null;
        renderPage(<OrganisationsPage />);
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Organisations could not",
        );
        mocks.auth.accessToken = "token";
        cleanup();

        mocks.getPipelineRuns.mockReset();
        mocks.getPipelineRuns.mockResolvedValue([]);
        renderPage(<PipelinesPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText(/No pipeline runs available/),
        ).toBeInTheDocument();
        cleanup();

        mocks.getPipelineRuns.mockReset();
        mocks.getPipelineRuns.mockReturnValue(never);
        renderPage(<PipelinesPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText("Loading pipeline runs..."),
        ).toBeInTheDocument();
        cleanup();

        mocks.getPipelineRuns.mockReset();
        mocks.getPipelineRuns.mockRejectedValue(new Error("runs"));
        renderPage(<PipelinesPage />);
        await fillScope(user, ["org", "project"]);
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Pipeline runs could not",
        );
        cleanup();

        mocks.getPipelineRuns.mockReset();
        mocks.getPipelineRuns.mockResolvedValue([]);
        mocks.getPipelineTimeline.mockReset();
        mocks.getPipelineTimeline.mockResolvedValue({
            items: [],
            nextCursor: null,
            hasNext: false,
        });
        renderPage(<PipelinesPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText(/No events match these filters/),
        ).toBeInTheDocument();
        cleanup();

        mocks.getPipelineRuns.mockReset();
        mocks.getPipelineRuns.mockResolvedValue([
            {
                id: "run",
                eventSourceId: "source",
                provider: "GITHUB_ACTIONS",
                externalRunId: "42",
                name: "build",
                attempt: 1,
                status: "FAILED",
                commitSha: "abc",
                gitRef: "main",
                environmentName: "prod",
                startedAt: "now",
                completedAt: "now",
                lastEventOccurredAt: "now",
                updatedAt: "now",
            },
        ]);
        mocks.getPipelineTimeline.mockReset();
        mocks.getPipelineTimeline.mockRejectedValue(new Error("timeline"));
        renderPage(<PipelinesPage />);
        await fillScope(user, ["org", "project", "main", "abc", "prod"]);
        await user.selectOptions(
            screen.getAllByRole("combobox")[1]!,
            "PIPELINE_RUN_COMPLETED",
        );
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Timeline events could not",
        );
        cleanup();

        mocks.getPipelineTimeline.mockReset();
        mocks.getPipelineTimeline.mockResolvedValue({
            items: [
                {
                    id: "event",
                    pipelineRunId: "run",
                    provider: "GITHUB_ACTIONS",
                    eventType: "PIPELINE_RUN_COMPLETED",
                    status: "FAILED",
                    externalRunId: "42",
                    pipelineName: "build",
                    attempt: 1,
                    commitSha: "abc",
                    gitRef: "main",
                    environmentName: "prod",
                    occurredAt: "2026-08-16T10:00:00Z",
                    receivedAt: "2026-08-16T10:01:00Z",
                    evidenceSummary: "failure",
                },
            ],
            nextCursor: null,
            hasNext: false,
        });
        renderPage(<PipelinesPage />);
        await fillScope(user, ["org", "project", "main", "abc", "prod"]);
        expect(
            (await screen.findAllByText("PIPELINE_RUN_COMPLETED")).length,
        ).toBeGreaterThan(0);
        cleanup();

        mocks.getPipelineTimeline.mockReset();
        mocks.getPipelineTimeline.mockResolvedValue({
            items: [
                {
                    id: "event",
                    pipelineRunId: "run",
                    provider: "GITHUB_ACTIONS",
                    eventType: "PIPELINE_RUN_COMPLETED",
                    status: "FAILED",
                    externalRunId: "42",
                    pipelineName: "build",
                    attempt: 1,
                    commitSha: "abc",
                    gitRef: "main",
                    environmentName: "prod",
                    occurredAt: "2026-08-16T10:00:00Z",
                    receivedAt: "2026-08-16T10:01:00Z",
                    evidenceSummary: "failure",
                },
            ],
            nextCursor: "next",
            hasNext: true,
        });
        renderPage(<PipelinesPage />);
        await fillScope(user, ["org", "project"]);
        await screen.findAllByText("PIPELINE_RUN_COMPLETED");
        mocks.getPipelineTimeline.mockReset();
        mocks.getPipelineTimeline.mockReturnValue(never);
        await user.click(
            screen.getByRole("button", { name: "Load more events" }),
        );
        expect(
            await screen.findByRole("button", { name: "Loading more..." }),
        ).toBeDisabled();
        cleanup();

        mocks.getEvidence.mockReset();
        mocks.getEvidence.mockResolvedValue({
            items: [],
            nextCursor: null,
        });
        mocks.getRecommendations.mockReset();
        mocks.getRecommendations.mockResolvedValue({ items: [] });
        renderPage(<RecommendationsPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText(
                "No evidence inputs are available for this project.",
            ),
        ).toBeInTheDocument();
        cleanup();

        mocks.getEvidence.mockReset();
        mocks.getEvidence.mockReturnValue(never);
        renderPage(<RecommendationsPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText("Loading evidence inputs..."),
        ).toBeInTheDocument();
        cleanup();

        mocks.getEvidence.mockReset();
        mocks.getEvidence.mockRejectedValue(new Error("evidence inputs"));
        renderPage(<RecommendationsPage />);
        await fillScope(user, ["org", "project"]);
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Evidence inputs could not be loaded.",
        );
        cleanup();

        mocks.getEvidence.mockReset();
        mocks.getEvidence.mockResolvedValue({
            items: [
                {
                    id: "evidence",
                    kind: "LOG_EXCERPT",
                    retentionClass: "STANDARD",
                    sourceSystem: "github",
                    sourceReference: "delivery",
                    occurredAt: "2026-08-16T10:00:00Z",
                    ingestedAt: "2026-08-16T10:01:00Z",
                    contentHash: "hash",
                    contentLineCount: 2,
                },
            ],
            nextCursor: null,
        });

        mocks.getRecommendations.mockReset();
        mocks.getRecommendations.mockResolvedValue({ items: [] });
        renderPage(<RecommendationsPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText(/No recommendations have been recorded/),
        ).toBeInTheDocument();
        cleanup();

        mocks.getRecommendations.mockReset();
        mocks.getRecommendations.mockReturnValue(never);
        renderPage(<RecommendationsPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText("Loading recommendations..."),
        ).toBeInTheDocument();
        cleanup();

        mocks.getRecommendations.mockReset();
        mocks.getRecommendations.mockRejectedValue(
            new Error("recommendations"),
        );
        renderPage(<RecommendationsPage />);
        await fillScope(user, ["org", "project"]);
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Recommendations could not",
        );
        cleanup();

        mocks.getRecommendations.mockReset();
        mocks.getRecommendations.mockResolvedValue({
            items: [
                {
                    id: "recommendation-2",
                    category: "UNKNOWN",
                    summary: "Abstained",
                    likelyCause: null,
                    confidence: 0,
                    confidenceExplanation: "Not enough evidence",
                    status: "ABSTAINED",
                    abstentionReason: "Insufficient evidence",
                    providerName: "deterministic",
                    modelVersion: "v1",
                    retrievalSetVersion: "v1",
                    citations: 0,
                },
            ],
        });
        renderPage(<RecommendationsPage />);
        await fillScope(user, ["org", "project"]);
        expect(
            await screen.findByText(/Abstention: Insufficient evidence/),
        ).toBeInTheDocument();
        cleanup();

        mocks.getRecommendations.mockReset();
        mocks.getRecommendations.mockResolvedValue({ items: [] });
        mocks.getEvidence.mockReset();
        mocks.getEvidence.mockResolvedValue({
            items: [
                {
                    id: "evidence",
                    kind: "LOG_EXCERPT",
                    retentionClass: "STANDARD",
                    sourceSystem: "github",
                    sourceReference: "delivery",
                    occurredAt: "2026-08-16T10:00:00Z",
                    ingestedAt: "2026-08-16T10:01:00Z",
                    contentHash: "hash",
                    contentLineCount: 2,
                },
            ],
            nextCursor: null,
        });
        mocks.generateRecommendation.mockReset();
        mocks.generateRecommendation.mockRejectedValue(new Error("generate"));
        renderPage(<RecommendationsPage />);
        await fillScope(user, ["org", "project"]);
        await user.click(
            await screen.findByRole("checkbox", {
                name: /Select LOG_EXCERPT delivery/,
            }),
        );
        await user.click(
            screen.getByRole("button", { name: /Generate bounded/ }),
        );
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Recommendations could not",
        );
        cleanup();

        mocks.getRecommendations.mockReset();
        mocks.getRecommendations.mockResolvedValue({ items: [] });
        mocks.getEvidence.mockReset();
        mocks.getEvidence.mockResolvedValue({
            items: [
                {
                    id: "evidence",
                    kind: "LOG_EXCERPT",
                    retentionClass: "STANDARD",
                    sourceSystem: "github",
                    sourceReference: "delivery",
                    occurredAt: "2026-08-16T10:00:00Z",
                    ingestedAt: "2026-08-16T10:01:00Z",
                    contentHash: "hash",
                    contentLineCount: 2,
                },
            ],
            nextCursor: null,
        });
        mocks.generateRecommendation.mockReset();
        mocks.generateRecommendation.mockReturnValue(never);
        renderPage(<RecommendationsPage />);
        await fillScope(user, ["org", "project"]);
        await user.click(
            await screen.findByRole("checkbox", {
                name: /Select LOG_EXCERPT delivery/,
            }),
        );
        await user.click(
            screen.getByRole("button", { name: /Generate bounded/ }),
        );
        expect(
            await screen.findByRole("button", { name: /Generating/ }),
        ).toBeDisabled();
        cleanup();

        mocks.getReviewHistory.mockReset();
        mocks.getReviewHistory.mockResolvedValue({ items: [] });
        renderPage(<ReviewPage />);
        await fillScope(user, ["org", "project", "recommendation"]);
        expect(
            await screen.findByText(/No reviews recorded/),
        ).toBeInTheDocument();
        const action = screen.getByLabelText("Action");
        await user.selectOptions(action, "REJECT");
        expect(
            screen.getByRole("button", { name: "Submit review" }),
        ).toBeDisabled();
        await user.selectOptions(screen.getByLabelText("Reason"), "UNSAFE");
        await user.click(screen.getByRole("button", { name: "Submit review" }));
        cleanup();

        mocks.getReviewHistory.mockReset();
        mocks.getReviewHistory.mockReturnValue(never);
        renderPage(<ReviewPage />);
        await fillScope(user, ["org", "project", "recommendation"]);
        expect(
            await screen.findByText("Loading review history..."),
        ).toBeInTheDocument();
        cleanup();

        mocks.getReviewHistory.mockReset();
        mocks.getReviewHistory.mockResolvedValue({
            items: [
                {
                    id: "review",
                    action: "ACCEPT",
                    reason: "NONE",
                    comment: null,
                    reviewedVersionId: "version",
                    createdAt: "now",
                },
            ],
        });
        mocks.submitReview.mockReset();
        mocks.submitReview.mockReturnValue(never);
        mocks.createResolution.mockReset();
        mocks.createResolution.mockReturnValue(never);
        renderPage(<ReviewPage />);
        await fillScope(user, ["org", "project", "recommendation"]);
        await screen.findByText("ACCEPT");
        await user.click(screen.getByRole("button", { name: "Submit review" }));
        expect(
            await screen.findByRole("button", { name: "Submitting..." }),
        ).toBeDisabled();
        await user.type(screen.getByLabelText("Incident ID"), "incident");
        await user.type(screen.getByLabelText("Resolution text"), "resolution");
        await user.click(
            screen.getByRole("button", { name: "Record bounded resolution" }),
        );
        expect(
            screen.getByRole("button", { name: "Record bounded resolution" }),
        ).toBeDisabled();
        cleanup();

        mocks.getReviewHistory.mockReset();
        mocks.getReviewHistory.mockRejectedValue(new Error("history"));
        renderPage(<ReviewPage />);
        await fillScope(user, ["org", "project", "recommendation"]);
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "review operation could not",
        );
    }, 20_000);
});
