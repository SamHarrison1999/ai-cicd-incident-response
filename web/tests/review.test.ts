import { describe, expect, it } from "vitest";

import { ReviewPage } from "../src/pages/ReviewPage";

describe("ReviewPage", () => {
    it("exports the human review workspace", () => {
        expect(ReviewPage).toBeDefined();
    });
});
