import { act, renderHook } from "@testing-library/react";

import { useWorkspace } from "../src/workspace/useWorkspace";

describe("workspace selection", () => {
    it("persists hierarchical workspace state for the browser session", () => {
        sessionStorage.clear();

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
        expect(first.result.current.incidentId).toBe("");

        act(() => {
            first.result.current.setIncidentId("incident");
        });

        expect(first.result.current.incidentId).toBe("incident");

        act(() => {
            first.result.current.setProjectId("replacement-project");
        });

        expect(first.result.current.projectId).toBe("replacement-project");
        expect(first.result.current.incidentId).toBe("");

        act(() => {
            first.result.current.setIncidentId("incident-2");
        });

        first.unmount();

        const restored = renderHook(() => useWorkspace());

        expect(restored.result.current.organisationId).toBe("organisation");
        expect(restored.result.current.projectId).toBe("replacement-project");
        expect(restored.result.current.incidentId).toBe("incident-2");

        act(() => {
            restored.result.current.clearWorkspace();
        });

        expect(restored.result.current.organisationId).toBe("");
        expect(restored.result.current.projectId).toBe("");
        expect(restored.result.current.incidentId).toBe("");

        act(() => {
            restored.result.current.setOrganisationId("new-organisation");
        });

        expect(restored.result.current.projectId).toBe("");
        expect(restored.result.current.incidentId).toBe("");
    });
});
