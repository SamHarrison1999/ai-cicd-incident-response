import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

const mocks = vi.hoisted(() => ({
    accessToken: "token",
    getLearningTrends: vi.fn(),
    getLearningComparison: vi.fn(),
}));

vi.mock("../src/auth/useAuth", () => ({
    useAuth: () => ({
        accessToken: mocks.accessToken,
    }),
}));

vi.mock("../src/api/learning", () => ({
    getLearningTrends: mocks.getLearningTrends,
    getLearningComparison: mocks.getLearningComparison,
}));

import type {
    LearningTrendItem,
    LearningTrendQuery,
} from "../src/api/learning";
import { LearningPage } from "../src/pages/LearningPage";

const items: LearningTrendItem[] = [
    {
        id: "accepted",
        dimension: "RECOMMENDATION_OUTCOME",
        dimensionKey: "ACCEPTED",
        windowStart: "2026-09-05T10:00:00Z",
        windowEnd: "2026-09-05T12:00:00Z",
        aggregationVersion: "governed-feedback-daily-v1",
        sampleCount: 3,
        observedCount: 3,
        sourceReference: "feedback-signal:accepted",
        suppressionReason: "NONE",
    },
    {
        id: "edited",
        dimension: "RECOMMENDATION_OUTCOME",
        dimensionKey: "EDITED",
        windowStart: "2026-09-05T10:00:00Z",
        windowEnd: "2026-09-05T12:00:00Z",
        aggregationVersion: "governed-feedback-daily-v1",
        sampleCount: 1,
        observedCount: 1,
        sourceReference: "feedback-signal:edited",
        suppressionReason: "INSUFFICIENT_SAMPLE",
    },
    {
        id: "timeout",
        dimension: "DIAGNOSIS_OUTCOME",
        dimensionKey: "TIMEOUT_FAILURE",
        windowStart: "2026-09-05T10:00:00Z",
        windowEnd: "2026-09-05T12:00:00Z",
        aggregationVersion: "v1",
        sampleCount: 2,
        observedCount: 2,
        sourceReference: "diagnosis:timeout",
        suppressionReason: "INSUFFICIENT_SAMPLE",
    },
];

function seedWorkspace() {
    sessionStorage.setItem(
        "incident-response.workspace",
        JSON.stringify({
            organisationId: "org",
            projectId: "project",
            incidentId: "",
        }),
    );
}

function renderPage() {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: {
                retry: false,
            },
        },
    });

    return render(
        <QueryClientProvider client={queryClient}>
            <LearningPage />
        </QueryClientProvider>,
    );
}

describe("Operational Learning filters", () => {
    beforeEach(() => {
        cleanup();
        sessionStorage.clear();
        vi.clearAllMocks();

        mocks.accessToken = "token";

        mocks.getLearningTrends.mockImplementation(
            (
                _token: string,
                _organisationId: string,
                _projectId: string,
                query: LearningTrendQuery = {},
            ) => {
                const filtered = items.filter(
                    (item) =>
                        (!query.dimension ||
                            item.dimension === query.dimension) &&
                        (!query.dimensionKey ||
                            item.dimensionKey === query.dimensionKey),
                );

                return Promise.resolve({
                    items: filtered,
                    nextCursor: null,
                    hasNext: false,
                });
            },
        );

        mocks.getLearningComparison.mockResolvedValue({
            dimensionKey: null,
            currentCount: 0,
            previousCount: 0,
            delta: 0,
            suppressionReason: "INSUFFICIENT_SAMPLE",
        });
    });

    it("uses workspace context and bounded dropdown filters", async () => {
        seedWorkspace();

        const user = userEvent.setup();

        renderPage();

        expect(
            screen.queryByLabelText("Organisation ID"),
        ).not.toBeInTheDocument();

        expect(screen.queryByLabelText("Project ID")).not.toBeInTheDocument();

        const dimension = screen.getByLabelText("Dimension");

        const dimensionKey = screen.getByLabelText("Dimension key");

        expect(dimensionKey).toBeDisabled();

        expect(
            screen.getByRole("option", {
                name: "Recommendation outcome",
            }),
        ).toBeInTheDocument();

        await user.selectOptions(dimension, "RECOMMENDATION_OUTCOME");

        expect(
            await screen.findByRole("option", {
                name: "Accepted",
            }),
        ).toBeInTheDocument();

        expect(
            screen.getByRole("option", {
                name: "Edited",
            }),
        ).toBeInTheDocument();

        expect(
            screen.queryByRole("option", {
                name: "Timeout Failure",
            }),
        ).not.toBeInTheDocument();

        expect(dimensionKey).toBeEnabled();

        await user.selectOptions(dimensionKey, "ACCEPTED");

        await waitFor(() => {
            expect(mocks.getLearningTrends).toHaveBeenCalledWith(
                "token",
                "org",
                "project",
                {
                    dimension: "RECOMMENDATION_OUTCOME",
                    dimensionKey: "ACCEPTED",
                },
            );
        });

        await user.selectOptions(dimension, "DIAGNOSIS_OUTCOME");

        expect(dimensionKey).toHaveValue("");

        expect(
            await screen.findByRole("option", {
                name: "Timeout Failure",
            }),
        ).toBeInTheDocument();
    });

    it("keeps the dimension key bounded while catalogue data is pending", async () => {
        seedWorkspace();

        const user = userEvent.setup();
        const never = new Promise<never>(() => undefined);

        mocks.getLearningTrends.mockReturnValue(never);

        renderPage();

        await user.selectOptions(
            screen.getByLabelText("Dimension"),
            "RECOMMENDATION_OUTCOME",
        );

        expect(screen.getByLabelText("Dimension key")).toBeDisabled();

        expect(
            screen.queryByRole("option", {
                name: "Accepted",
            }),
        ).not.toBeInTheDocument();
    });

    it("requires workspace context without exposing raw identifiers", () => {
        renderPage();

        expect(
            screen.getByText(
                /Select an organisation and project using the workspace controls above/,
            ),
        ).toBeInTheDocument();

        expect(screen.getByLabelText("Dimension key")).toBeDisabled();

        expect(mocks.getLearningTrends).not.toHaveBeenCalled();

        expect(mocks.getLearningComparison).not.toHaveBeenCalled();
    });

    it("shows a bounded load failure", async () => {
        seedWorkspace();

        mocks.getLearningTrends.mockRejectedValue(
            new Error("learning unavailable"),
        );

        mocks.getLearningComparison.mockRejectedValue(
            new Error("comparison unavailable"),
        );

        renderPage();

        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Operational learning could not be loaded.",
        );
    });
});
