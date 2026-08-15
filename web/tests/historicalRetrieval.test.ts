import { getHistoricalRetrieval } from "../src/api/historicalRetrieval";

describe("historical retrieval API", () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it("requests bounded tenant-scoped filters and cursor", async () => {
        const fetchMock = vi.fn(() =>
            Promise.resolve(
                new Response(
                    JSON.stringify({
                        items: [],
                        nextCursor: null,
                        hasNext: false,
                    }),
                    { status: 200 },
                ),
            ),
        );
        vi.stubGlobal("fetch", fetchMock);

        await getHistoricalRetrieval("token", "org-1", "project-1", {
            diagnosisCategory: "DEPENDENCY_FAILURE_SUSPECTED",
            cursor: "cursor-value",
            limit: 10,
        });

        expect(fetchMock).toHaveBeenCalledWith(
            "/control-plane/api/v1/organisations/org-1/projects/project-1/historical-retrieval?diagnosisCategory=DEPENDENCY_FAILURE_SUSPECTED&cursor=cursor-value&limit=10",
            expect.objectContaining({ credentials: "include" }),
        );
    });
});
