import { getControlPlaneStatus } from "../src/api/controlPlane";

describe("getControlPlaneStatus", () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it("returns typed status data", async () => {
        vi.stubGlobal(
            "fetch",
            vi.fn().mockResolvedValue(
                new Response(
                    JSON.stringify({
                        service: "control-plane",
                        version: "0.1.0-SNAPSHOT",
                        status: "UP",
                        timestamp: "2026-08-01T12:00:00Z",
                    }),
                    { status: 200 },
                ),
            ),
        );

        await expect(getControlPlaneStatus()).resolves.toEqual({
            service: "control-plane",
            version: "0.1.0-SNAPSHOT",
            status: "UP",
            timestamp: "2026-08-01T12:00:00Z",
        });
    });

    it("throws for unsuccessful responses", async () => {
        vi.stubGlobal(
            "fetch",
            vi.fn().mockResolvedValue(new Response(null, { status: 503 })),
        );

        await expect(getControlPlaneStatus()).rejects.toThrow(
            "Control plane status request failed with HTTP 503",
        );
    });
});
