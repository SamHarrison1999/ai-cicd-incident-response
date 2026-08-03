import { expect, test } from "@playwright/test";

test("navigates through the authenticated foundation workspace", async ({
    page,
}) => {
    await page.route("**/api/v1/auth/refresh", async (route) => {
        await route.fulfill({
            contentType: "application/json",
            status: 200,
            body: JSON.stringify({
                accessToken: "playwright-access-token",
                tokenType: "Bearer",
                expiresInSeconds: 900,
                user: {
                    userId: "00000000-0000-0000-0000-000000000001",
                    email: "sam@example.com",
                    displayName: "Sam",
                },
            }),
        });
    });

    await page.route("**/api/v1/system/status", async (route) => {
        await route.fulfill({
            contentType: "application/json",
            status: 200,
            body: JSON.stringify({
                service: "control-plane",
                version: "0.1.0-SNAPSHOT",
                status: "UP",
                timestamp: "2026-08-03T12:00:00Z",
            }),
        });
    });

    await page.route("**/api/v1/organisations", async (route) => {
        await route.fulfill({
            contentType: "application/json",
            status: 200,
            body: JSON.stringify([]),
        });
    });

    await page.goto("/");

    await expect(
        page.getByRole("heading", { name: "Operational readiness" }),
    ).toBeVisible();

    await page.getByRole("link", { name: "Pipeline runs" }).click();
    await expect(
        page.getByRole("heading", { name: "Pipeline runs" }).first(),
    ).toBeVisible();

    await page.getByRole("link", { name: "Incidents" }).click();
    await expect(
        page.getByRole("heading", { name: "Incidents" }).first(),
    ).toBeVisible();

    await page.getByRole("link", { name: "Organisations" }).click();
    await expect(
        page.getByRole("heading", { name: "Organisations" }).first(),
    ).toBeVisible();
});
