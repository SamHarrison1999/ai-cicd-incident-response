/* eslint-disable @typescript-eslint/consistent-type-imports, @typescript-eslint/no-confusing-void-expression */
import { cleanup, render } from "@testing-library/react";
import type { ReactElement } from "react";

const auth = vi.hoisted(() => ({ accessToken: null }));
const api = vi.hoisted(() => ({
    getDiagnosis: vi.fn().mockResolvedValue({}),
    getEvidence: vi.fn().mockResolvedValue({ items: [], nextCursor: null }),
    getEvidenceItem: vi.fn().mockResolvedValue({}),
    getFeedback: vi.fn().mockResolvedValue({ items: [] }),
    getHistoricalRetrieval: vi
        .fn()
        .mockResolvedValue({ items: [], nextCursor: null, hasNext: false }),
    getIncidents: vi.fn().mockResolvedValue([]),
    transitionIncident: vi.fn().mockResolvedValue({}),
    getLearningComparison: vi.fn().mockResolvedValue({}),
    getLearningTrends: vi.fn().mockResolvedValue({ items: [] }),
    createOrganisation: vi.fn().mockResolvedValue({}),
    getOrganisations: vi.fn().mockResolvedValue([]),
    getPipelineRuns: vi.fn().mockResolvedValue([]),
    getPipelineTimeline: vi
        .fn()
        .mockResolvedValue({ items: [], nextCursor: null, hasNext: false }),
    generateRecommendation: vi.fn().mockResolvedValue({}),
    getRecommendations: vi.fn().mockResolvedValue({ items: [] }),
    createResolution: vi.fn().mockResolvedValue({}),
    getReviewHistory: vi.fn().mockResolvedValue({ items: [] }),
    submitReview: vi.fn().mockResolvedValue({}),
}));

vi.mock("@tanstack/react-query", async () => {
    const actual = await vi.importActual<
        typeof import("@tanstack/react-query")
    >("@tanstack/react-query");
    return {
        ...actual,
        useQuery: (options: { queryFn: () => unknown }) => {
            try {
                options.queryFn();
            } catch {
                // The page intentionally throws for a missing token in this branch.
            }
            return { isPending: false, isError: false, data: undefined };
        },
        useInfiniteQuery: (options: {
            queryFn: (context: { pageParam: null }) => unknown;
        }) => {
            try {
                options.queryFn({ pageParam: null });
            } catch {
                // The page intentionally throws for a missing token in this branch.
            }
            return {
                isPending: false,
                isError: false,
                data: undefined,
                hasNextPage: false,
                isFetchingNextPage: false,
                fetchNextPage: vi.fn(),
            };
        },
        useMutation: (options: {
            mutationFn: (...args: unknown[]) => unknown;
        }) => {
            try {
                options.mutationFn({ incidentId: "", status: "DETECTED" });
            } catch {
                // The page intentionally throws for a missing token in this branch.
            }
            return { isPending: false, isError: false, mutate: vi.fn() };
        },
        useQueryClient: () => ({ invalidateQueries: vi.fn() }),
    };
});

vi.mock("../src/auth/useAuth", () => ({ useAuth: () => auth }));
vi.mock("../src/api/diagnosis", () => ({ getDiagnosis: api.getDiagnosis }));
vi.mock("../src/api/evidence", () => ({
    getEvidence: api.getEvidence,
    getEvidenceItem: api.getEvidenceItem,
}));
vi.mock("../src/api/feedback", () => ({ getFeedback: api.getFeedback }));
vi.mock("../src/api/historicalRetrieval", () => ({
    getHistoricalRetrieval: api.getHistoricalRetrieval,
}));
vi.mock("../src/api/incidents", () => ({
    getIncidents: api.getIncidents,
    transitionIncident: api.transitionIncident,
}));
vi.mock("../src/api/learning", () => ({
    getLearningComparison: api.getLearningComparison,
    getLearningTrends: api.getLearningTrends,
}));
vi.mock("../src/api/organisations", () => ({
    createOrganisation: api.createOrganisation,
    getOrganisations: api.getOrganisations,
}));
vi.mock("../src/api/pipelineRuns", () => ({
    getPipelineRuns: api.getPipelineRuns,
    getPipelineTimeline: api.getPipelineTimeline,
}));
vi.mock("../src/api/recommendations", () => ({
    generateRecommendation: api.generateRecommendation,
    getRecommendations: api.getRecommendations,
}));
vi.mock("../src/api/reviews", () => ({
    createResolution: api.createResolution,
    getReviewHistory: api.getReviewHistory,
    submitReview: api.submitReview,
}));

import { DiagnosisPage } from "../src/pages/DiagnosisPage";
import { EvidencePage } from "../src/pages/EvidencePage";
import { FeedbackPage } from "../src/pages/FeedbackPage";
import { HistoricalRetrievalPage } from "../src/pages/HistoricalRetrievalPage";
import { IncidentsPage } from "../src/pages/IncidentsPage";
import { LearningPage } from "../src/pages/LearningPage";
import { OrganisationsPage } from "../src/pages/OrganisationsPage";
import { PipelinesPage } from "../src/pages/PipelinesPage";
import { RecommendationsPage } from "../src/pages/RecommendationsPage";
import { ReviewPage } from "../src/pages/ReviewPage";

describe("defensive access-token fallbacks", () => {
    afterEach(() => cleanup());

    it("executes every disabled-query and mutation fallback safely", () => {
        const pages: ReactElement[] = [
            <DiagnosisPage key="diagnosis" />,
            <EvidencePage key="evidence" />,
            <FeedbackPage key="feedback" />,
            <HistoricalRetrievalPage key="history" />,
            <IncidentsPage key="incidents" />,
            <LearningPage key="learning" />,
            <OrganisationsPage key="organisations" />,
            <PipelinesPage key="pipelines" />,
            <RecommendationsPage key="recommendations" />,
            <ReviewPage key="review" />,
        ];
        render(<>{pages}</>);

        expect(api.getDiagnosis).toHaveBeenCalledWith("", "", "");
        expect(api.getEvidence).toHaveBeenCalledWith(
            "",
            "",
            "",
            expect.any(Object),
        );
        expect(api.getFeedback).toHaveBeenCalledWith(
            "",
            "",
            "",
            expect.any(Object),
        );
        expect(api.getHistoricalRetrieval).toHaveBeenCalledWith(
            "",
            "",
            "",
            expect.any(Object),
        );
        expect(api.getIncidents).toHaveBeenCalledWith("", "", "");
        expect(api.getLearningTrends).toHaveBeenCalledWith(
            "",
            "",
            "",
            expect.any(Object),
        );
        expect(api.getLearningComparison).toHaveBeenCalledWith(
            "",
            "",
            "",
            expect.any(Object),
        );
        expect(api.getPipelineRuns).toHaveBeenCalledWith("", "", "");
        expect(api.getPipelineTimeline).toHaveBeenCalledWith(
            "",
            "",
            "",
            expect.any(Object),
        );
        expect(api.getRecommendations).toHaveBeenCalledWith("", "", "");
        expect(api.generateRecommendation).toHaveBeenCalledWith(
            "",
            "",
            "",
            null,
            [],
            [],
        );
        expect(api.getReviewHistory).toHaveBeenCalledWith("", "", "", "");
        expect(api.submitReview).toHaveBeenCalledWith(
            "",
            "",
            "",
            "",
            expect.any(Object),
        );
        expect(api.createResolution).toHaveBeenCalledWith(
            "",
            "",
            "",
            "",
            expect.any(Object),
        );
    });
});
