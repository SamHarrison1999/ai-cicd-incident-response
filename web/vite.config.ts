import react from "@vitejs/plugin-react";
import { defineConfig } from "vitest/config";

export default defineConfig({
    plugins: [react()],
    server: {
        port: 5173,
        strictPort: true,
        proxy: {
            "/control-plane": {
                target: "http://localhost:8080",
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/control-plane/, ""),
            },
        },
    },
    test: {
        environment: "jsdom",
        globals: true,
        setupFiles: "./tests/setup.ts",
        exclude: [
            "tests/e2e/**",
            "node_modules/**",
            "dist/**",
            "playwright-report/**",
            "test-results/**",
        ],
        coverage: {
            provider: "v8",
            reporter: ["text", "html", "lcov"],
            include: ["src/**/*.{ts,tsx}"],
            exclude: ["src/vite-env.d.ts"],
            thresholds: {
                branches: 100,
                functions: 100,
                lines: 100,
                statements: 100,
            },
        },
    },
});
