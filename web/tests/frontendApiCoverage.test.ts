import {
    loginUser,
    logoutUser,
    refreshSession,
    registerUser,
} from "../src/api/authentication";
import { getControlPlaneStatus } from "../src/api/controlPlane";
import { getDiagnosis } from "../src/api/diagnosis";
import { getEvidence, getEvidenceItem } from "../src/api/evidence";
import { getFeedback } from "../src/api/feedback";
import { getHistoricalRetrieval } from "../src/api/historicalRetrieval";
import { ApiError, requestJson } from "../src/api/httpClient";
import { getIncidents, transitionIncident } from "../src/api/incidents";
import { getLearningComparison, getLearningTrends } from "../src/api/learning";
import { createOrganisation, getOrganisations } from "../src/api/organisations";
import { getPipelineRuns, getPipelineTimeline } from "../src/api/pipelineRuns";
import {
    generateRecommendation,
    getRecommendations,
} from "../src/api/recommendations";
import {
    createResolution,
    getReviewHistory,
    submitReview,
} from "../src/api/reviews";

function json(body: unknown, status = 200) {
    return new Response(JSON.stringify(body), {
        status,
        headers: { "Content-Type": "application/json" },
    });
}

function requestUrl(input: RequestInfo | URL | undefined) {
    if (typeof input === "string") {
        return input;
    }
    if (input instanceof URL) {
        return input.href;
    }
    return input?.url ?? "";
}

describe("frontend API production coverage", () => {
    afterEach(() => vi.unstubAllGlobals());

    it("covers requestJson success, empty responses, headers, and error payloads", async () => {
        const fetchMock = vi
            .fn<
                (
                    input: RequestInfo | URL,
                    init?: RequestInit,
                ) => Promise<Response>
            >()
            .mockResolvedValueOnce(json({ ok: true }))
            .mockResolvedValueOnce(new Response(null, { status: 204 }))
            .mockResolvedValueOnce(json({ code: "BAD", message: "Nope" }, 400))
            .mockResolvedValueOnce(new Response("not-json", { status: 502 }));
        vi.stubGlobal("fetch", fetchMock);

        await expect(requestJson<{ ok: boolean }>("/ok")).resolves.toEqual({
            ok: true,
        });
        await expect(requestJson<undefined>("/empty")).resolves.toBeUndefined();
        await expect(requestJson("/bad")).rejects.toMatchObject({
            name: "ApiError",
            status: 400,
            code: "BAD",
            message: "Nope",
        });
        await expect(requestJson("/unavailable")).rejects.toMatchObject({
            status: 502,
            code: "HTTP_ERROR",
            message: "Request failed with HTTP 502",
        });

        const init = fetchMock.mock.calls[0]?.[1] as RequestInit;
        expect(init.credentials).toBe("include");
        expect(new Headers(init.headers).get("Accept")).toBe(
            "application/json",
        );
        expect(new Headers(init.headers).get("Content-Type")).toBe(
            "application/json",
        );
        const error = new ApiError(418, "TEAPOT", "short and stout");
        expect(error.name).toBe("ApiError");
        expect(error.status).toBe(418);
        expect(error.code).toBe("TEAPOT");
    });

    it("covers authentication and control-plane API functions", async () => {
        const fetchMock = vi.fn<
            (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>
        >(() =>
            Promise.resolve(
                json({
                    accessToken: "token",
                    tokenType: "Bearer",
                    expiresInSeconds: 900,
                    user: {
                        userId: "user",
                        email: "a@b.test",
                        displayName: "A",
                    },
                }),
            ),
        );
        vi.stubGlobal("fetch", fetchMock);

        await registerUser({
            email: "a@b.test",
            displayName: "A",
            password: "password",
        });
        await loginUser({ email: "a@b.test", password: "password" });
        await refreshSession();
        await logoutUser();
        await getControlPlaneStatus(new AbortController().signal);

        expect(fetchMock).toHaveBeenCalledTimes(5);
        expect(requestUrl(fetchMock.mock.calls[0]?.[0])).toContain(
            "/auth/register",
        );
        expect(requestUrl(fetchMock.mock.calls[1]?.[0])).toContain(
            "/auth/login",
        );
        expect(requestUrl(fetchMock.mock.calls[2]?.[0])).toContain(
            "/auth/refresh",
        );
        expect(requestUrl(fetchMock.mock.calls[3]?.[0])).toContain(
            "/auth/logout",
        );
        expect(requestUrl(fetchMock.mock.calls[4]?.[0])).toContain(
            "/system/status",
        );
    });

    it("covers every API query builder with populated and default queries", async () => {
        const fetchMock = vi.fn<
            (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>
        >(() =>
            Promise.resolve(
                json({ items: [], nextCursor: null, hasNext: false }),
            ),
        );
        vi.stubGlobal("fetch", fetchMock);

        await getDiagnosis("token", "org", "project");
        await getEvidence("token", "org", "project", {
            kind: "LOG_EXCERPT",
            sourceSystem: "github",
            query: "timeout",
            cursor: "cursor",
            limit: 10,
        });
        await getEvidence("token", "org", "project");
        await getEvidenceItem("token", "org", "project", "evidence");
        await getFeedback("token", "org", "project", {
            policyVersion: "policy",
            from: "2026-01-01",
            to: "2026-02-01",
            limit: 5,
        });
        await getFeedback("token", "org", "project");
        await getFeedback("token", "org", "project", {
            policyVersion: "",
            from: undefined,
            to: "",
            limit: 0,
        });
        await getHistoricalRetrieval("token", "org", "project", {
            diagnosisCategory: "UNKNOWN",
            provider: "github",
            pipeline: "build",
            environment: "prod",
            branch: "main",
            commitSha: "abc",
            from: "2026-01-01",
            to: "2026-02-01",
            query: "failure",
            cursor: "cursor",
            limit: 5,
        });
        await getHistoricalRetrieval("token", "org", "project");
        await getHistoricalRetrieval("token", "org", "project", {
            provider: "",
            pipeline: undefined,
            limit: 0,
        });
        await getIncidents("token", "org", "project");
        await transitionIncident(
            "token",
            "org",
            "project",
            "incident",
            "TRIAGED",
        );
        await getLearningTrends("token", "org", "project", {
            dimension: "INCIDENT_CATEGORY",
            dimensionKey: "dependency",
            from: "2026-01-01",
            to: "2026-02-01",
            limit: 5,
        });
        await getLearningTrends("token", "org", "project");
        await getLearningTrends("token", "org", "project", {
            dimension: "",
            dimensionKey: undefined,
            limit: 0,
        });
        await getLearningComparison("token", "org", "project", {
            dimension: "INCIDENT_CATEGORY",
        });
        await getLearningComparison("token", "org", "project");
        await getLearningComparison("token", "org", "project", {
            dimension: "",
            dimensionKey: undefined,
            limit: 0,
        });
        await getOrganisations("token");
        await createOrganisation("token", { name: "Org", slug: "org" });
        await getPipelineRuns("token", "org", "project");
        await getPipelineTimeline("token", "org", "project", {
            status: "FAILED",
            branch: "main",
            commitSha: "abc",
            environment: "prod",
            eventType: "PIPELINE_RUN_COMPLETED",
            from: "2026-01-01",
            to: "2026-02-01",
            cursor: "cursor",
            limit: 5,
        });
        await getPipelineTimeline("token", "org", "project");
        await getRecommendations("token", "org", "project");
        await generateRecommendation(
            "token",
            "org",
            "project",
            "incident",
            ["e"],
            ["h"],
        );
        await getReviewHistory("token", "org", "project", "recommendation");
        await submitReview("token", "org", "project", "recommendation", {
            action: "EDIT",
            reason: "OTHER",
            comment: "comment",
            editedSummary: "summary",
            editedCause: "cause",
        });
        await createResolution("token", "org", "project", "incident", {
            recommendationId: "recommendation",
            reviewedVersionId: "version",
            resolutionText: "resolution",
        });

        expect(fetchMock.mock.calls.length).toBeGreaterThan(23);
    });
});
