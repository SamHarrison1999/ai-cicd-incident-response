import { getPipelineTimeline } from "../src/api/pipelineRuns";

describe("getPipelineTimeline", () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it("serialises supported filters and the cursor into the timeline request", async () => {
        const fetchMock = vi.fn().mockResolvedValue(
            new Response(
                JSON.stringify({
                    items: [],
                    nextCursor: null,
                    hasNext: false,
                }),
                { status: 200 },
            ),
        );
        vi.stubGlobal("fetch", fetchMock);

        await getPipelineTimeline("token", "org-1", "project-1", {
            status: "FAILED",
            branch: "refs/heads/main",
            eventType: "PIPELINE_RUN_COMPLETED",
            cursor: "opaque-cursor",
            limit: 25,
        });

        const requestUrl = String(fetchMock.mock.calls[0]?.[0]);
        expect(requestUrl).toContain(
            "/control-plane/api/v1/organisations/org-1/projects/project-1/timeline?",
        );
        expect(requestUrl).toContain("status=FAILED");
        expect(requestUrl).toContain("branch=refs%2Fheads%2Fmain");
        expect(requestUrl).toContain("eventType=PIPELINE_RUN_COMPLETED");
        expect(requestUrl).toContain("cursor=opaque-cursor");
        expect(requestUrl).toContain("limit=25");
    });
});
