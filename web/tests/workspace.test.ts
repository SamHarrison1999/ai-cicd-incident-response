import { act, renderHook, waitFor } from "@testing-library/react";

import {
    readWorkspaceSelection,
    useWorkspace,
    WORKSPACE_STORAGE_KEY,
} from "../src/workspace/useWorkspace";

describe("workspace selection", () => {
    beforeEach(() => {
        sessionStorage.clear();
    });

    it("persists hierarchical workspace state for the browser session", () => {
        const first = renderHook(() => useWorkspace());

        expect(first.result.current.organisationId).toBe("");
        expect(first.result.current.projectId).toBe("");
        expect(first.result.current.incidentId).toBe("");

        act(() => {
            first.result.current.setOrganisationId("organisation");
        });

        expect(first.result.current.organisationId).toBe("organisation");
        expect(first.result.current.projectId).toBe("");
        expect(first.result.current.incidentId).toBe("");

        act(() => {
            first.result.current.setProjectId("project");
        });

        expect(first.result.current.projectId).toBe("project");

        act(() => {
            first.result.current.setIncidentId("incident");
        });

        expect(first.result.current.incidentId).toBe("incident");

        act(() => {
            first.result.current.setProjectId("replacement-project");
        });

        expect(first.result.current.projectId).toBe("replacement-project");
        expect(first.result.current.incidentId).toBe("");

        first.unmount();

        const restored = renderHook(() => useWorkspace());

        expect(restored.result.current.organisationId).toBe("organisation");

        expect(restored.result.current.projectId).toBe("replacement-project");

        act(() => {
            restored.result.current.clearWorkspace();
        });

        expect(restored.result.current).toMatchObject({
            organisationId: "",
            projectId: "",
            incidentId: "",
        });
    });

    it("synchronises separate hook consumers in the same tab", async () => {
        const first = renderHook(() => useWorkspace());
        const second = renderHook(() => useWorkspace());

        act(() => {
            first.result.current.setOrganisationId("organisation");
        });

        await waitFor(() => {
            expect(second.result.current.organisationId).toBe("organisation");
        });

        act(() => {
            second.result.current.setProjectId("project");
        });

        await waitFor(() => {
            expect(first.result.current.projectId).toBe("project");
        });

        act(() => {
            first.result.current.setIncidentId("incident");
        });

        await waitFor(() => {
            expect(second.result.current.incidentId).toBe("incident");
        });

        first.unmount();
        second.unmount();
    });

    it("defensively normalises incomplete hierarchical data", () => {
        sessionStorage.setItem(
            WORKSPACE_STORAGE_KEY,
            JSON.stringify({
                organisationId: 42,
                projectId: "project",
                incidentId: "incident",
            }),
        );

        expect(readWorkspaceSelection()).toEqual({
            organisationId: "",
            projectId: "",
            incidentId: "",
        });

        sessionStorage.setItem(
            WORKSPACE_STORAGE_KEY,
            JSON.stringify({
                organisationId: "organisation",
                projectId: null,
                incidentId: "incident",
            }),
        );

        expect(readWorkspaceSelection()).toEqual({
            organisationId: "organisation",
            projectId: "",
            incidentId: "",
        });
    });

    it("recovers from malformed browser storage", () => {
        sessionStorage.setItem(WORKSPACE_STORAGE_KEY, "{not-json");

        expect(readWorkspaceSelection()).toEqual({
            organisationId: "",
            projectId: "",
            incidentId: "",
        });
    });
});
