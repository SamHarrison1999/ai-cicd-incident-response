import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";

const mocks = vi.hoisted(() => ({
    getIncidents: vi.fn(),
    getRecommendations: vi.fn(),
}));

vi.mock("../src/api/incidents", () => ({
    getIncidents: mocks.getIncidents,
}));

vi.mock("../src/api/recommendations", () => ({
    getRecommendations: mocks.getRecommendations,
}));

import { ReviewTargetSelector } from "../src/components/review/ReviewTargetSelector";

function Harness({
    accessToken = "token",
    initialIncidentId = "",
    initialRecommendationId = "",
}: {
    accessToken?: string | null;
    initialIncidentId?: string;
    initialRecommendationId?: string;
}) {
    const [incidentId, setIncidentId] = useState(initialIncidentId);
    const [recommendationId, setRecommendationId] = useState(
        initialRecommendationId,
    );
    return (
        <QueryClientProvider
            client={
                new QueryClient({
                    defaultOptions: {
                        queries: {
                            retry: false,
                        },
                    },
                })
            }
        >
            <ReviewTargetSelector
                accessToken={accessToken}
                organisationId="org"
                projectId="project"
                incidentId={incidentId}
                recommendationId={recommendationId}
                onIncidentChange={setIncidentId}
                onRecommendationChange={setRecommendationId}
            />

            <span data-testid="incident">{incidentId}</span>
            <span data-testid="recommendation">{recommendationId}</span>
        </QueryClientProvider>
    );
}

describe("review target selection", () => {
    beforeEach(() => {
        vi.clearAllMocks();

        mocks.getIncidents.mockResolvedValue([
            {
                id: "incident-1",
                status: "TRIAGED",
                title: "demo-deployment",
                summary: "failure",
                detectedAt: "now",
                resolvedAt: null,
                createdAt: "now",
                updatedAt: "now",
            },
        ]);

        mocks.getRecommendations.mockResolvedValue({
            items: [
                {
                    id: "recommendation-1",
                    incidentId: "incident-1",
                    category: "DEPENDENCY",
                    summary: "Investigate dependency",
                    status: "RECOMMENDED",
                },
            ],
        });
    });

    it("auto-selects the only governed incident and recommendation", async () => {
        render(<Harness />);

        await waitFor(() => {
            expect(screen.getByTestId("incident")).toHaveTextContent(
                "incident-1",
            );
        });

        await waitFor(() => {
            expect(screen.getByTestId("recommendation")).toHaveTextContent(
                "recommendation-1",
            );
        });

        expect(
            screen.getByRole("option", {
                name: "demo-deployment — TRIAGED",
            }),
        ).toBeInTheDocument();

        expect(
            screen.getByRole("option", {
                name: /DEPENDENCY: Investigate dependency/,
            }),
        ).toBeInTheDocument();
    });

    it("lets the reviewer choose among multiple incidents", async () => {
        const user = userEvent.setup();

        mocks.getIncidents.mockResolvedValue([
            {
                id: "incident-1",
                status: "TRIAGED",
                title: "first",
                summary: "first",
                detectedAt: "now",
                resolvedAt: null,
                createdAt: "now",
                updatedAt: "now",
            },
            {
                id: "incident-2",
                status: "MITIGATING",
                title: "second",
                summary: "second",
                detectedAt: "now",
                resolvedAt: null,
                createdAt: "now",
                updatedAt: "now",
            },
        ]);

        mocks.getRecommendations.mockResolvedValue({
            items: [
                {
                    id: "recommendation-1",
                    incidentId: "incident-1",
                    category: "DEPENDENCY",
                    summary: "First",
                    status: "RECOMMENDED",
                },
                {
                    id: "recommendation-2",
                    incidentId: "incident-2",
                    category: "DEPLOYMENT",
                    summary: "Second",
                    status: "RECOMMENDED",
                },
            ],
        });

        render(<Harness />);

        const incidentSelect = await screen.findByRole("combobox", {
            name: "Incident",
        });

        await screen.findByRole("option", {
            name: "second — MITIGATING",
        });

        expect(incidentSelect).toBeEnabled();

        await user.selectOptions(incidentSelect, "incident-2");

        await waitFor(() => {
            expect(screen.getByTestId("incident")).toHaveTextContent(
                "incident-2",
            );
        });

        await waitFor(() => {
            expect(screen.getByTestId("recommendation")).toHaveTextContent(
                "recommendation-2",
            );
        });

        expect(
            screen.queryByRole("option", {
                name: /DEPENDENCY: First/,
            }),
        ).not.toBeInTheDocument();

        expect(
            screen.getByRole("option", {
                name: /DEPLOYMENT: Second/,
            }),
        ).toBeInTheDocument();
    });

    it("requires explicit choice when several recommendations remain", async () => {
        const user = userEvent.setup();

        mocks.getRecommendations.mockResolvedValue({
            items: [
                {
                    id: "recommendation-1",
                    incidentId: "incident-1",
                    category: "DEPENDENCY",
                    summary: "First",
                    status: "RECOMMENDED",
                },
                {
                    id: "recommendation-2",
                    incidentId: "incident-1",
                    category: "DEPENDENCY",
                    summary: "Second",
                    status: "RECOMMENDED",
                },
            ],
        });

        render(<Harness />);

        const recommendationSelect = await screen.findByRole("combobox", {
            name: "Recommendation",
        });

        await waitFor(() => {
            expect(screen.getByTestId("recommendation")).toBeEmptyDOMElement();
        });

        await user.selectOptions(recommendationSelect, "recommendation-2");

        expect(screen.getByTestId("recommendation")).toHaveTextContent(
            "recommendation-2",
        );
    });

    it("uses the only available fallback even when it is resolved or abstained", async () => {
        mocks.getIncidents.mockResolvedValue([
            {
                id: "incident-resolved",
                status: "RESOLVED",
                title: "resolved-deployment",
                summary: "resolved",
                detectedAt: "now",
                resolvedAt: "now",
                createdAt: "now",
                updatedAt: "now",
            },
        ]);

        mocks.getRecommendations.mockResolvedValue({
            items: [
                {
                    id: "recommendation-abstained",
                    incidentId: "incident-resolved",
                    category: "DEPENDENCY",
                    summary: "Insufficient bounded evidence",
                    status: "ABSTAINED",
                },
            ],
        });

        render(<Harness />);

        await waitFor(() => {
            expect(screen.getByTestId("incident")).toHaveTextContent(
                "incident-resolved",
            );
        });

        await waitFor(() => {
            expect(screen.getByTestId("recommendation")).toHaveTextContent(
                "recommendation-abstained",
            );
        });
    });

    it("clears stale governed targets when several valid choices remain", async () => {
        mocks.getIncidents.mockResolvedValue([
            {
                id: "incident-1",
                status: "TRIAGED",
                title: "first",
                summary: "first",
                detectedAt: "now",
                resolvedAt: null,
                createdAt: "now",
                updatedAt: "now",
            },
            {
                id: "incident-2",
                status: "MITIGATING",
                title: "second",
                summary: "second",
                detectedAt: "now",
                resolvedAt: null,
                createdAt: "now",
                updatedAt: "now",
            },
        ]);

        mocks.getRecommendations.mockResolvedValue({
            items: [
                {
                    id: "recommendation-1",
                    incidentId: "incident-1",
                    category: "DEPENDENCY",
                    summary: "First",
                    status: "RECOMMENDED",
                },
                {
                    id: "recommendation-2",
                    incidentId: "incident-2",
                    category: "DEPLOYMENT",
                    summary: "Second",
                    status: "RECOMMENDED",
                },
            ],
        });

        render(
            <Harness
                initialIncidentId="stale-incident"
                initialRecommendationId="stale-recommendation"
            />,
        );

        await waitFor(() => {
            expect(screen.getByTestId("incident")).toBeEmptyDOMElement();
        });

        await waitFor(() => {
            expect(screen.getByTestId("recommendation")).toBeEmptyDOMElement();
        });

        expect(
            screen.getByRole("combobox", {
                name: "Incident",
            }),
        ).toHaveValue("");

        expect(
            screen.getByRole("combobox", {
                name: "Recommendation",
            }),
        ).toHaveValue("");
    });

    it("does not query without authentication", () => {
        render(<Harness accessToken={null} />);

        expect(
            screen.getByRole("combobox", {
                name: "Incident",
            }),
        ).toBeDisabled();

        expect(
            screen.getByRole("combobox", {
                name: "Recommendation",
            }),
        ).toBeDisabled();

        expect(mocks.getIncidents).not.toHaveBeenCalled();

        expect(mocks.getRecommendations).not.toHaveBeenCalled();
    });

    it("surfaces bounded discovery failures", async () => {
        mocks.getIncidents.mockRejectedValue(
            new Error("incidents unavailable"),
        );

        mocks.getRecommendations.mockRejectedValue(
            new Error("recommendations unavailable"),
        );

        render(<Harness />);

        expect(
            await screen.findByText("Incidents could not be loaded."),
        ).toBeInTheDocument();

        expect(
            await screen.findByText("Recommendations could not be loaded."),
        ).toBeInTheDocument();
    });
});
