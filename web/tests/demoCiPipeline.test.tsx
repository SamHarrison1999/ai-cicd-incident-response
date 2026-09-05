import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

const mocks = vi.hoisted(() => ({
    accessToken: null as string | null,
    simulateDemoCiRun: vi.fn(),
    getPipelineRuns: vi.fn(),
    getPipelineTimeline: vi.fn(),
}));

vi.mock("../src/auth/useAuth", () => ({
    useAuth: () => ({
        accessToken: mocks.accessToken,
    }),
}));

vi.mock("../src/api/demoCi", () => ({
    simulateDemoCiRun: mocks.simulateDemoCiRun,
}));

vi.mock("../src/api/pipelineRuns", () => ({
    getPipelineRuns: mocks.getPipelineRuns,
    getPipelineTimeline: mocks.getPipelineTimeline,
}));

import { PipelinesPage } from "../src/pages/PipelinesPage";

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
            queries: { retry: false },
            mutations: { retry: false },
        },
    });

    return render(
        <QueryClientProvider client={queryClient}>
            <PipelinesPage />
        </QueryClientProvider>,
    );
}

describe("Pipeline browser CI simulator", () => {
    beforeEach(() => {
        cleanup();
        sessionStorage.clear();
        vi.clearAllMocks();

        mocks.accessToken = "token";

        mocks.getPipelineRuns.mockResolvedValue([]);

        mocks.getPipelineTimeline.mockResolvedValue({
            items: [],
            nextCursor: null,
            hasNext: false,
        });

        mocks.simulateDemoCiRun.mockResolvedValue({
            eventSourceId: "source",
            providerDeliveryId: "provider-delivery",
            externalRunId: "9001",
            pipelineName: "release",
            branch: "feature/demo",
            outcome: "SUCCEEDED",
            deliveryId: "delivery",
            duplicate: false,
            deliveryStatus: "RECEIVED",
            receivedAt: "2026-09-05T15:00:00Z",
        });
    });

    it("simulates a CI run using the selected workspace", async () => {
        seedWorkspace();
        const user = userEvent.setup();

        renderPage();

        const pipelineName = screen.getByLabelText("Pipeline name");
        await user.clear(pipelineName);
        await user.type(pipelineName, "release");

        const branch = screen.getByLabelText("Demo branch");
        await user.clear(branch);
        await user.type(branch, "feature/demo");

        await user.selectOptions(screen.getByLabelText("Outcome"), "SUCCEEDED");

        await user.click(
            screen.getByRole("button", {
                name: "Simulate CI run",
            }),
        );

        await waitFor(() => {
            expect(mocks.simulateDemoCiRun).toHaveBeenCalledWith(
                "token",
                "org",
                "project",
                {
                    pipelineName: "release",
                    branch: "feature/demo",
                    outcome: "SUCCEEDED",
                },
            );
        });

        expect(await screen.findByRole("status")).toHaveTextContent(
            "Simulated SUCCEEDED pipeline run accepted",
        );

        await waitFor(() => {
            expect(mocks.getPipelineRuns.mock.calls.length).toBeGreaterThan(1);
        });
    });

    it("shows a bounded failure state", async () => {
        seedWorkspace();
        const user = userEvent.setup();

        mocks.simulateDemoCiRun.mockRejectedValue(new Error("failed"));

        renderPage();

        await user.click(
            screen.getByRole("button", {
                name: "Simulate CI run",
            }),
        );

        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Demo CI simulation failed.",
        );
    });

    it("shows the pending state while simulation is running", async () => {
        seedWorkspace();
        const user = userEvent.setup();

        mocks.simulateDemoCiRun.mockReturnValue(new Promise(() => undefined));

        renderPage();

        await user.click(
            screen.getByRole("button", {
                name: "Simulate CI run",
            }),
        );

        expect(
            await screen.findByRole("button", {
                name: "Simulating...",
            }),
        ).toBeDisabled();
    });

    it("requires a selected workspace", () => {
        renderPage();

        expect(
            screen.getByRole("button", {
                name: "Simulate CI run",
            }),
        ).toBeDisabled();

        expect(
            screen.getByText(/Select an organisation and project/),
        ).toBeInTheDocument();
    });

    it("requires authentication even when workspace is selected", () => {
        seedWorkspace();
        mocks.accessToken = null;

        renderPage();

        expect(
            screen.getByRole("button", {
                name: "Simulate CI run",
            }),
        ).toBeDisabled();
    });
});
