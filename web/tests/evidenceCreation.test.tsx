import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

const mocks = vi.hoisted(() => ({
    accessToken: null as string | null,
    createEvidence: vi.fn(),
    getEvidence: vi.fn(),
    getEvidenceItem: vi.fn(),
}));

vi.mock("../src/auth/useAuth", () => ({
    useAuth: () => ({
        accessToken: mocks.accessToken,
    }),
}));

vi.mock("../src/api/evidence", () => ({
    createEvidence: mocks.createEvidence,
    getEvidence: mocks.getEvidence,
    getEvidenceItem: mocks.getEvidenceItem,
}));

import { EvidencePage } from "../src/pages/EvidencePage";

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
            <EvidencePage />
        </QueryClientProvider>,
    );
}

describe("browser evidence creation", () => {
    beforeEach(() => {
        cleanup();
        sessionStorage.clear();
        vi.clearAllMocks();

        mocks.accessToken = "token";
        mocks.getEvidence.mockResolvedValue({
            items: [],
            nextCursor: null,
        });
        mocks.getEvidenceItem.mockResolvedValue({
            id: "created-evidence",
            kind: "LOG_EXCERPT",
            retentionClass: "STANDARD",
            sourceSystem: "portfolio-demo",
            sourceReference: "browser-manual-evidence",
            occurredAt: "2026-09-05T16:00:00Z",
            ingestedAt: "2026-09-05T16:00:01Z",
            contentHash: "hash",
            contentLineCount: 1,
            content: "dependency timeout",
            incidentIds: [],
            eventIds: [],
        });
        mocks.createEvidence.mockResolvedValue({
            id: "created-evidence",
            kind: "LOG_EXCERPT",
            retentionClass: "STANDARD",
            sourceSystem: "portfolio-demo",
            sourceReference: "browser-manual-evidence",
            occurredAt: "2026-09-05T16:00:00Z",
            ingestedAt: "2026-09-05T16:00:01Z",
            contentHash: "hash",
            contentLineCount: 1,
        });
    });

    it("creates evidence in the selected workspace", async () => {
        seedWorkspace();
        const user = userEvent.setup();

        renderPage();

        await user.selectOptions(
            screen.getByLabelText("New evidence kind"),
            "TRACE_OBSERVATION",
        );

        await user.selectOptions(
            screen.getByLabelText("Retention class"),
            "EXTENDED",
        );

        await user.clear(screen.getByLabelText("New evidence source system"));
        await user.type(
            screen.getByLabelText("New evidence source system"),
            "manual-demo",
        );

        await user.clear(screen.getByLabelText("Source reference"));
        await user.type(
            screen.getByLabelText("Source reference"),
            "dependency-signal",
        );

        await user.type(
            screen.getByLabelText("Evidence content"),
            "connection refused to upstream dependency",
        );

        await user.click(
            screen.getByRole("button", {
                name: "Add evidence",
            }),
        );

        await waitFor(() => {
            expect(mocks.createEvidence).toHaveBeenCalledWith(
                "token",
                "org",
                "project",
                expect.objectContaining({
                    kind: "TRACE_OBSERVATION",
                    retentionClass: "EXTENDED",
                    sourceSystem: "manual-demo",
                    sourceReference: "dependency-signal",
                    content: "connection refused to upstream dependency",
                }),
            );
        });

        expect(await screen.findByRole("status")).toHaveTextContent(
            "Evidence created and added",
        );
    });

    it("shows a bounded creation failure", async () => {
        seedWorkspace();
        const user = userEvent.setup();

        mocks.createEvidence.mockRejectedValue(new Error("failed"));

        renderPage();

        await user.type(
            screen.getByLabelText("Evidence content"),
            "dependency timeout",
        );

        await user.click(
            screen.getByRole("button", {
                name: "Add evidence",
            }),
        );

        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Evidence creation failed.",
        );
    });

    it("shows the pending state", async () => {
        seedWorkspace();
        const user = userEvent.setup();

        mocks.createEvidence.mockReturnValue(new Promise(() => undefined));

        renderPage();

        await user.type(
            screen.getByLabelText("Evidence content"),
            "dependency timeout",
        );

        await user.click(
            screen.getByRole("button", {
                name: "Add evidence",
            }),
        );

        expect(
            await screen.findByRole("button", {
                name: "Adding evidence...",
            }),
        ).toBeDisabled();
    });

    it("requires selected workspace and nonblank evidence", async () => {
        const user = userEvent.setup();

        renderPage();

        expect(
            screen.getByRole("button", {
                name: "Add evidence",
            }),
        ).toBeDisabled();

        expect(
            screen.getByText(
                /Select an organisation and project before adding/,
            ),
        ).toBeInTheDocument();

        seedWorkspace();
        cleanup();
        renderPage();

        expect(
            screen.getByRole("button", {
                name: "Add evidence",
            }),
        ).toBeDisabled();

        await user.type(
            screen.getByLabelText("Evidence content"),
            "usable evidence",
        );

        expect(
            screen.getByRole("button", {
                name: "Add evidence",
            }),
        ).toBeEnabled();
    });

    it("requires authentication", async () => {
        seedWorkspace();
        mocks.accessToken = null;
        const user = userEvent.setup();

        renderPage();

        await user.type(
            screen.getByLabelText("Evidence content"),
            "usable evidence",
        );

        expect(
            screen.getByRole("button", {
                name: "Add evidence",
            }),
        ).toBeDisabled();
    });
});
