import { describe, expect, it } from "vitest";

import { FeedbackPage } from "../src/pages/FeedbackPage";

describe("FeedbackPage", () => {
    it("exports the read-only feedback workspace", () => {
        expect(FeedbackPage).toBeDefined();
    });
});
