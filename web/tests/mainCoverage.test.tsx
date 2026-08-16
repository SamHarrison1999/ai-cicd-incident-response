import { createRoot } from "react-dom/client";
import type { ReactNode } from "react";

const root = vi.hoisted(() => ({ render: vi.fn() }));

vi.mock("react-dom/client", () => ({
    createRoot: vi.fn(() => root),
}));

vi.mock("../src/app/AppProviders", () => ({
    AppProviders: ({ children }: { children: ReactNode }) => children,
}));

vi.mock("../src/app/AppRoutes", () => ({
    AppRoutes: () => null,
}));

describe("main entrypoint", () => {
    it("creates the root and renders the application shell", async () => {
        document.body.innerHTML = '<div id="root"></div>';
        await import("../src/main");
        expect(createRoot).toHaveBeenCalledWith(
            document.getElementById("root"),
        );
        expect(root.render).toHaveBeenCalled();
    });

    it("rejects a document without the application root", async () => {
        vi.resetModules();
        document.body.innerHTML = "";
        await expect(import("../src/main")).rejects.toThrow(
            "Root element was not found",
        );
    });
});
