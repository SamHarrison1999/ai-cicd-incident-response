import { EmptyState } from "../components/common/EmptyState";
import { ServiceStatusCard } from "../components/status/ServiceStatusCard";
import { useControlPlaneStatus } from "../hooks/useControlPlaneStatus";

export function DashboardPage() {
    const controlPlaneStatus = useControlPlaneStatus();

    const backendState = controlPlaneStatus.isPending
        ? "loading"
        : controlPlaneStatus.isSuccess
          ? "healthy"
          : "unavailable";

    const backendDetail = controlPlaneStatus.isPending
        ? "Requesting the system-status endpoint."
        : controlPlaneStatus.isSuccess
          ? `Version ${controlPlaneStatus.data.version} Â· ${controlPlaneStatus.data.status}`
          : "Start the Spring Boot control plane to enable live status.";

    return (
        <>
            <section className="page-heading">
                <p className="eyebrow">Platform overview</p>
                <h2>Operational readiness</h2>
                <p>
                    Track service availability, pipeline failures, and incidents
                    from a single evidence-focused workspace.
                </p>
            </section>

            <section aria-labelledby="service-status-heading">
                <div className="section-heading">
                    <div>
                        <p className="eyebrow">Runtime status</p>
                        <h2 id="service-status-heading">Platform services</h2>
                    </div>
                    <span className="updated-label">
                        Foundation environment
                    </span>
                </div>

                <div className="status-grid">
                    <ServiceStatusCard
                        name="Control plane"
                        description="Authoritative incident workflow"
                        status={backendState}
                        detail={backendDetail}
                    />
                    <ServiceStatusCard
                        name="Intelligence service"
                        description="Safe analysis and abstention"
                        status="healthy"
                        detail="Foundation API contract available on port 8000."
                    />
                    <ServiceStatusCard
                        name="Web application"
                        description="Engineering operations interface"
                        status="healthy"
                        detail="React application loaded successfully."
                    />
                </div>
            </section>

            <section aria-labelledby="activity-heading">
                <div className="section-heading">
                    <div>
                        <p className="eyebrow">Recent activity</p>
                        <h2 id="activity-heading">Incident stream</h2>
                    </div>
                </div>
                <EmptyState
                    title="No incidents have been ingested"
                    description="Signed event ingestion and incident correlation are introduced in later phases."
                />
            </section>
        </>
    );
}
