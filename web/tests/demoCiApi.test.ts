import { simulateDemoCiRun } from "../src/api/demoCi";

describe("Demo CI API", () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it("posts a tenant-scoped authenticated simulation request", async () => {
        const fetchMock = vi.fn<
            (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>
        >(() =>
            Promise.resolve(
                new Response(
                    JSON.stringify({
                        eventSourceId: "source",
                        providerDeliveryId: "provider-delivery",
                        externalRunId: "9001",
                        pipelineName: "demo-deployment",
                        branch: "main",
                        outcome: "FAILED",
                        deliveryId: "delivery",
                        duplicate: false,
                        deliveryStatus: "RECEIVED",
                        receivedAt: "2026-09-05T15:00:00Z",
                    }),
                    {
                        status: 201,
                        headers: {
                            "Content-Type": "application/json",
                        },
                    },
                ),
            ),
        );

        vi.stubGlobal("fetch", fetchMock);

        const response = await simulateDemoCiRun("token", "org", "project", {
            pipelineName: "demo-deployment",
            branch: "main",
            outcome: "FAILED",
        });

        expect(response.outcome).toBe("FAILED");

        const firstCall = fetchMock.mock.calls[0];

        if (firstCall === undefined) {
            throw new Error("Expected fetch to be called.");
        }

        const [url, init] = firstCall;

        expect(url).toBe(
            "/control-plane/api/v1/organisations/org/projects/project/demo/ci-runs",
        );

        if (init === undefined) {
            throw new Error("Expected fetch request options.");
        }

        if (typeof init.body !== "string") {
            throw new Error("Expected JSON request body.");
        }

        expect(init.method).toBe("POST");

        expect(new Headers(init.headers).get("Authorization")).toBe(
            "Bearer token",
        );

        expect(JSON.parse(init.body)).toEqual({
            pipelineName: "demo-deployment",
            branch: "main",
            outcome: "FAILED",
        });
    });
});
