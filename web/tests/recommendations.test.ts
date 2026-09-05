import { describe, expect, it } from "vitest";

import type { Recommendation } from "../src/api/recommendations";

describe("recommendation workspace contract", () => {
    it("represents bounded human-review metadata", () => {
        const result: Recommendation = {
            id: "recommendation-1",
            organisationId: "organisation-1",
            projectId: "project-1",
            incidentId: "incident-1",
            category: "dependency",
            summary: "Investigate dependency availability.",
            likelyCause: "dependency availability",
            confidence: 0.72,
            confidenceExplanation: "A supported signal was present.",
            status: "RECOMMENDED",
            abstentionReason: null,
            providerName: "deterministic-local",
            modelVersion: "rules-1",
            promptTemplateVersion: "phase-9-prompt-1",
            rulesetVersion: "phase-9-rules-1",
            retrievalSetVersion: "retrieval-1",
            schemaVersion: "recommendation-1",
            generatedAt: "2026-09-05T14:00:00Z",
            createdAt: "2026-09-05T14:00:00Z",
            citations: 1,
        };

        expect(result.status).toBe("RECOMMENDED");
        expect(result.confidence).toBeLessThanOrEqual(1);
        expect(result.incidentId).toBe("incident-1");
        expect(result.citations).toBe(1);
        expect(result).not.toHaveProperty("rawContent");
    });
});
