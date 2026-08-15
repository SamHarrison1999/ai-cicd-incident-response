import { describe, expect, it } from "vitest";

import type { Recommendation } from "../src/api/recommendations";

describe("recommendation workspace contract", () => {
    it("represents bounded human-review metadata", () => {
        const result: Recommendation = {
            id: "recommendation-1",
            category: "dependency",
            summary: "Investigate dependency availability.",
            likelyCause: "dependency availability",
            confidence: 0.72,
            confidenceExplanation: "A supported signal was present.",
            status: "RECOMMENDED",
            abstentionReason: null,
            providerName: "deterministic-local",
            modelVersion: "rules-1",
            retrievalSetVersion: "retrieval-1",
            citations: 1,
        };

        expect(result.status).toBe("RECOMMENDED");
        expect(result.confidence).toBeLessThanOrEqual(1);
        expect(result).not.toHaveProperty("rawContent");
    });
});
