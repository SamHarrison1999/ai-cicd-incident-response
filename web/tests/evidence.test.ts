import { getEvidence, getEvidenceItem } from "../src/api/evidence";

describe("evidence API", () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it("requests evidence inside the organisation and project scope", async () => {
        const fetchMock = vi.fn().mockResolvedValue(
            new Response(JSON.stringify({ items: [], nextCursor: null }), {
                status: 200,
            }),
        );
        vi.stubGlobal("fetch", fetchMock);

        await getEvidence("token", "org-1", "project-1", {
            kind: "LOG_EXCERPT",
            query: "timeout",
            limit: 25,
        });

        const requestUrl = String(fetchMock.mock.calls[0]?.[0]);
        expect(requestUrl).toContain(
            "/control-plane/api/v1/organisations/org-1/projects/project-1/evidence?",
        );
        expect(requestUrl).toContain("kind=LOG_EXCERPT");
        expect(requestUrl).toContain("q=timeout");
        expect(requestUrl).toContain("limit=25");
    });

    it("loads a single bounded viewer projection", async () => {
        const fetchMock = vi.fn().mockResolvedValue(
            new Response(
                JSON.stringify({
                    id: "evidence-1",
                    kind: "LOG_EXCERPT",
                    retentionClass: "STANDARD",
                    sourceSystem: "github",
                    sourceReference: "delivery-1",
                    occurredAt: "2026-08-14T12:00:00Z",
                    ingestedAt: "2026-08-14T12:01:00Z",
                    contentHash: "a".repeat(64),
                    content: "token=[REDACTED]",
                    contentLineCount: 1,
                    incidentIds: [],
                    eventIds: [],
                }),
                { status: 200 },
            ),
        );
        vi.stubGlobal("fetch", fetchMock);

        const result = await getEvidenceItem(
            "token",
            "org-1",
            "project-1",
            "evidence-1",
        );

        expect(result.content).not.toContain("raw-secret");
        expect(String(fetchMock.mock.calls[0]?.[0])).toContain(
            "/evidence/evidence-1",
        );
    });
});
