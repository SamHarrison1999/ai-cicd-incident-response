import {
    createEvidence,
    getEvidence,
    getEvidenceItem,
} from "../src/api/evidence";

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

    it("creates tenant-scoped evidence", async () => {
        const fetchMock = vi.fn<
            (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>
        >(() =>
            Promise.resolve(
                new Response(
                    JSON.stringify({
                        id: "evidence-new",
                        kind: "LOG_EXCERPT",
                        retentionClass: "STANDARD",
                        sourceSystem: "portfolio-demo",
                        sourceReference: "browser-manual-evidence",
                        occurredAt: "2026-09-05T16:00:00Z",
                        ingestedAt: "2026-09-05T16:00:01Z",
                        contentHash: "a".repeat(64),
                        contentLineCount: 1,
                    }),
                    { status: 201 },
                ),
            ),
        );

        vi.stubGlobal("fetch", fetchMock);

        await createEvidence("token", "org-1", "project-1", {
            kind: "LOG_EXCERPT",
            retentionClass: "STANDARD",
            sourceSystem: "portfolio-demo",
            sourceReference: "browser-manual-evidence",
            occurredAt: "2026-09-05T16:00:00Z",
            content: "dependency timeout",
        });

        const firstCall = fetchMock.mock.calls[0];

        if (firstCall === undefined) {
            throw new Error("Expected fetch to be called.");
        }

        const [url, init] = firstCall;

        if (init === undefined || typeof init.body !== "string") {
            throw new Error("Expected JSON request options.");
        }

        if (typeof url !== "string") {
            throw new Error("Expected evidence request URL to be a string.");
        }

        expect(url).toContain(
            "/organisations/org-1/projects/project-1/evidence",
        );
        expect(init.method).toBe("POST");
        expect(new Headers(init.headers).get("Authorization")).toBe(
            "Bearer token",
        );

        expect(JSON.parse(init.body)).toEqual({
            kind: "LOG_EXCERPT",
            retentionClass: "STANDARD",
            sourceSystem: "portfolio-demo",
            sourceReference: "browser-manual-evidence",
            occurredAt: "2026-09-05T16:00:00Z",
            content: "dependency timeout",
        });
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
