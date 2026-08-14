import { getIncidents, transitionIncident } from "../src/api/incidents";

describe("incident API", () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it("requests incidents within the organisation and project scope", async () => {
        const fetchMock = vi
            .fn()
            .mockResolvedValue(
                new Response(JSON.stringify([]), { status: 200 }),
            );
        vi.stubGlobal("fetch", fetchMock);

        await getIncidents("token", "org-1", "project-1");

        expect(String(fetchMock.mock.calls[0]?.[0])).toContain(
            "/control-plane/api/v1/organisations/org-1/projects/project-1/incidents",
        );
    });

    it("sends a status transition without raw event data", async () => {
        const fetchMock = vi.fn().mockResolvedValue(
            new Response(
                JSON.stringify({
                    id: "incident-1",
                    status: "TRIAGED",
                    title: "Pipeline failure",
                    summary: "Bounded summary",
                    detectedAt: "2026-08-14T12:00:00Z",
                    resolvedAt: null,
                    createdAt: "2026-08-14T12:00:00Z",
                    updatedAt: "2026-08-14T12:01:00Z",
                }),
                { status: 200 },
            ),
        );
        vi.stubGlobal("fetch", fetchMock);

        await transitionIncident(
            "token",
            "org-1",
            "project-1",
            "incident-1",
            "TRIAGED",
        );

        const request = fetchMock.mock.calls[0]?.[1] as RequestInit;
        expect(request.method).toBe("PATCH");
        expect(request.body).toEqual(expect.any(String));
        const body = request.body as string;
        expect(body).toContain('"status":"TRIAGED"');
        expect(body).not.toContain("payload");
        expect(body).not.toContain("signature");
    });
});
