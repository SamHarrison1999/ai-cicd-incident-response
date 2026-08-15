import { describe, expect, it } from "vitest";

import { getLearningTrends } from "../src/api/learning";

describe("operational learning API", () => {
    it("uses the tenant-scoped trend route and bounded limit", async () => {
        const originalFetch = globalThis.fetch;
        globalThis.fetch = (input: string | URL | Request) => {
            const requestUrl =
                typeof input === "string"
                    ? input
                    : input instanceof URL
                      ? input.toString()
                      : input.url;

            expect(requestUrl).toContain(
                "/operational-learning/trends?dimension=INCIDENT_CATEGORY&limit=50",
            );
            return Promise.resolve(
                new Response(
                    JSON.stringify({
                        items: [],
                        nextCursor: null,
                        hasNext: false,
                    }),
                    {
                        status: 200,
                        headers: { "Content-Type": "application/json" },
                    },
                ),
            );
        };
        await getLearningTrends("token", "org-1", "project-1", {
            dimension: "INCIDENT_CATEGORY",
        });
        globalThis.fetch = originalFetch;
    });
});
