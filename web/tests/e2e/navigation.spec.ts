import { expect, test } from "@playwright/test";

test("navigates through the foundation workspace", async ({ page }) => {
    await page.goto("/");

    await expect(
        page.getByRole("heading", { name: "Operational readiness" }),
    ).toBeVisible();

    await page.getByRole("link", { name: "Pipeline runs" }).click();
    await expect(
        page.getByRole("heading", { name: "Pipeline runs", exact: true }),
    ).toBeVisible();

    await page.getByRole("link", { name: "Incidents" }).click();
    await expect(
        page.getByRole("heading", { name: "Incidents", exact: true }),
    ).toBeVisible();

    await page.getByRole("link", { name: "Settings" }).click();
    await expect(
        page.getByRole("heading", { name: "Settings", exact: true }),
    ).toBeVisible();
    await expect(page.getByText("Automatic remediation")).toBeVisible();
    await expect(page.getByText("Disabled")).toBeVisible();
});
