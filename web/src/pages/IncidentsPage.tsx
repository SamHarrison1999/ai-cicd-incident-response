import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    getIncidents,
    type IncidentStatus,
    transitionIncident,
} from "../api/incidents";
import { useAuth } from "../auth/useAuth";
import { useWorkspace } from "../workspace/useWorkspace";

const statusOptions: IncidentStatus[] = [
    "DETECTED",
    "TRIAGED",
    "MITIGATING",
    "MONITORING",
    "RESOLVED",
    "REOPENED",
];

export function IncidentsPage() {
    const { accessToken } = useAuth();
    const queryClient = useQueryClient();
    const {
        organisationId,
        projectId,
        setOrganisationId,
        setProjectId,
        setIncidentId,
    } = useWorkspace();
    const canQuery =
        organisationId.trim().length > 0 && projectId.trim().length > 0;
    const incidents = useQuery({
        queryKey: ["incidents", organisationId, projectId],
        queryFn: () =>
            getIncidents(accessToken ?? "", organisationId, projectId),
        enabled: canQuery && accessToken !== null,
    });
    const transition = useMutation({
        mutationFn: ({
            incidentId,
            status,
        }: {
            incidentId: string;
            status: IncidentStatus;
        }) =>
            transitionIncident(
                accessToken ?? "",
                organisationId,
                projectId,
                incidentId,
                status,
            ),
        onSuccess: () => {
            void queryClient.invalidateQueries({
                queryKey: ["incidents", organisationId, projectId],
            });
        },
    });

    return (
        <>
            <section className="page-heading">
                <p className="eyebrow">Incident operations</p>
                <h2>Incidents</h2>
                <p>
                    Review correlated failures and move incidents through the
                    audited lifecycle.
                </p>
            </section>
            <section
                className="settings-panel"
                aria-labelledby="incident-scope-heading"
            >
                <h3 id="incident-scope-heading">Workspace scope</h3>
                <div className="scope-form">
                    <label>
                        Organisation ID
                        <input
                            value={organisationId}
                            onChange={(event) => {
                                setOrganisationId(event.target.value);
                            }}
                        />
                    </label>
                    <label>
                        Project ID
                        <input
                            value={projectId}
                            onChange={(event) => {
                                setProjectId(event.target.value);
                            }}
                        />
                    </label>
                </div>
                {!canQuery ? (
                    <p className="field-hint">
                        Enter both IDs to load the tenant-scoped incident view.
                    </p>
                ) : null}
            </section>
            {incidents.isPending && canQuery ? (
                <p>Loading incidents...</p>
            ) : null}
            {incidents.isError ? (
                <div className="notice notice-error" role="alert">
                    Incidents could not be loaded.
                </div>
            ) : null}
            {transition.isError ? (
                <div className="notice notice-error" role="alert">
                    The incident status could not be changed.
                </div>
            ) : null}
            {incidents.data?.length === 0 ? (
                <div className="empty-state">
                    <h3>No incidents available</h3>
                    <p>
                        Correlated failure events will appear here when the
                        incident engine records them.
                    </p>
                </div>
            ) : null}
            {incidents.data && incidents.data.length > 0 ? (
                <div className="incident-list" aria-label="Incidents">
                    {incidents.data.map((incident) => (
                        <article className="incident-card" key={incident.id}>
                            <div className="incident-card-header">
                                <div>
                                    <p className="eyebrow">{incident.id}</p>
                                    <h3>{incident.title}</h3>
                                </div>
                                <span
                                    className={
                                        "incident-status incident-status-" +
                                        incident.status.toLowerCase()
                                    }
                                >
                                    {incident.status}
                                </span>
                            </div>
                            <p className="incident-summary">
                                {incident.summary}
                            </p>
                            <div className="incident-meta">
                                <span>Detected {incident.detectedAt}</span>
                                {incident.resolvedAt ? (
                                    <span>Resolved {incident.resolvedAt}</span>
                                ) : null}
                            </div>
                            <button
                                className="button button-secondary"
                                type="button"
                                onClick={() => {
                                    setIncidentId(incident.id);
                                }}
                            >
                                Use incident
                            </button>
                            <label className="incident-transition">
                                Change status
                                <select
                                    aria-label={
                                        "Change status for " + incident.title
                                    }
                                    value={incident.status}
                                    disabled={transition.isPending}
                                    onChange={(event) => {
                                        const nextStatus = event.target
                                            .value as IncidentStatus;
                                        if (nextStatus !== incident.status) {
                                            transition.mutate({
                                                incidentId: incident.id,
                                                status: nextStatus,
                                            });
                                        }
                                    }}
                                >
                                    {statusOptions.map((status) => (
                                        <option key={status} value={status}>
                                            {status}
                                        </option>
                                    ))}
                                </select>
                            </label>
                        </article>
                    ))}
                </div>
            ) : null}
        </>
    );
}
