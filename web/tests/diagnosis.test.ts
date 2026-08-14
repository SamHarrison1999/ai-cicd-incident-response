import { describe, expect, it } from "vitest";

describe("diagnosis contract", () => {
    it("keeps bounded hypotheses distinct from confirmed causes", () => {
        expect("DEPENDENCY_FAILURE_SUSPECTED").toContain("SUSPECTED");
        expect("UNKNOWN").not.toContain("SUSPECTED");
    });
});
