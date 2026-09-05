import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";

import { simulateDemoCiRun } from "../api/demoCi";
import { createOrganisation, getOrganisations } from "../api/organisations";
import { createProject, getProjects } from "../api/projects";
import { useAuth } from "../auth/useAuth";
import { EmptyState } from "../components/common/EmptyState";
import { ServiceStatusCard } from "../components/status/ServiceStatusCard";
import { useControlPlaneStatus } from "../hooks/useControlPlaneStatus";
import { useWorkspace } from "../workspace/useWorkspace";

const DEMO_ORGANISATION = {
    name: "Portfolio Verification",
    slug: "portfolio-verification",
};

const DEMO_PROJECT = {
    name: "Incident Demo",
    slug: "incident-demo",
    description: "Portfolio project",
};

export function DashboardPage() {
    const { accessToken, currentUser } = useAuth();
    const queryClient = useQueryClient();
    const { organisationId, projectId, setOrganisationId, setProjectId } =
        useWorkspace();
    const controlPlaneStatus = useControlPlaneStatus();

    const backendState = controlPlaneStatus.isPending
        ? "loading"
        : controlPlaneStatus.isSuccess
          ? "healthy"
          : "unavailable";

    const backendDetail = controlPlaneStatus.isPending
        ? "Requesting the system-status endpoint."
        : controlPlaneStatus.isSuccess
          ? `Version ${controlPlaneStatus.data.version} \u00b7 ${controlPlaneStatus.data.status}`
          : "Start the Spring Boot control plane to enable live status.";

    const canRunDemo = accessToken !== null;
    const demoOrganisation = {
        ...DEMO_ORGANISATION,
        slug: `${DEMO_ORGANISATION.slug}-${currentUser?.userId ?? "anonymous"}`,
    };

    const demoSimulation = useMutation({
        mutationFn: async () => {
            const token = accessToken as string;
            let resolvedOrganisationId = organisationId.trim();
            let resolvedProjectId = projectId.trim();

            if (resolvedOrganisationId.length === 0) {
                const organisations = await getOrganisations(token);
                const existingOrganisation = organisations.find(
                    (organisation) =>
                        organisation.slug === demoOrganisation.slug,
                );
                const organisation =
                    existingOrganisation ??
                    (await createOrganisation(token, demoOrganisation));
                resolvedOrganisationId = organisation.id;
            }

            if (resolvedProjectId.length === 0) {
                const projects = await getProjects(
                    token,
                    resolvedOrganisationId,
                );
                const existingProject = projects.find(
                    (project) => project.slug === DEMO_PROJECT.slug,
                );
                const project =
                    existingProject ??
                    (await createProject(
                        token,
                        resolvedOrganisationId,
                        DEMO_PROJECT,
                    ));
                resolvedProjectId = project.id;
            }

            if (resolvedOrganisationId !== organisationId) {
                setOrganisationId(resolvedOrganisationId);
            }
            if (resolvedProjectId !== projectId) {
                setProjectId(resolvedProjectId);
            }

            const response = await simulateDemoCiRun(
                token,
                resolvedOrganisationId,
                resolvedProjectId,
                {
                    pipelineName: "portfolio-demo",
                    branch: "main",
                    outcome: "FAILED",
                },
            );

            return {
                response,
                organisationId: resolvedOrganisationId,
                projectId: resolvedProjectId,
            };
        },
        onSuccess: async ({
            organisationId: resolvedOrganisationId,
            projectId: resolvedProjectId,
        }) => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ["organisations"],
                }),
                queryClient.invalidateQueries({
                    queryKey: ["projects", resolvedOrganisationId],
                }),
                queryClient.invalidateQueries({
                    queryKey: [
                        "pipeline-runs",
                        resolvedOrganisationId,
                        resolvedProjectId,
                    ],
                }),
                queryClient.invalidateQueries({
                    queryKey: [
                        "pipeline-timeline",
                        resolvedOrganisationId,
                        resolvedProjectId,
                    ],
                }),
            ]);
        },
    });

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

            <section
                className="settings-panel"
                aria-labelledby="portfolio-demo-heading"
            >
                <div className="section-heading">
                    <div>
                        <p className="eyebrow">Interactive portfolio demo</p>
                        <h2 id="portfolio-demo-heading">
                            Run the incident-response demo
                        </h2>
                    </div>
                    <span className="updated-label">One click</span>
                </div>

                <p>
                    Create a deterministic failed CI run through signed
                    ingestion, normalisation, incident correlation, and evidence
                    capture. It uses the selected workspace, or creates a
                    dedicated portfolio workspace when none exists.
                </p>

                <button
                    className="button"
                    type="button"
                    disabled={!canRunDemo || demoSimulation.isPending}
                    onClick={() => {
                        demoSimulation.mutate();
                    }}
                >
                    {demoSimulation.isPending
                        ? "Preparing demo..."
                        : "Run one-click demo"}
                </button>

                {!canRunDemo ? (
                    <p className="field-hint">
                        Sign in before running the portfolio demo.
                    </p>
                ) : organisationId.trim().length === 0 ||
                  projectId.trim().length === 0 ? (
                    <p className="field-hint">
                        A portfolio demo workspace will be created automatically
                        if needed.
                    </p>
                ) : null}

                {demoSimulation.isSuccess ? (
                    <div className="notice notice-success" role="status">
                        Demo CI run accepted.{" "}
                        <Link className="primary-link" to="/pipelines">
                            Open pipeline runs
                        </Link>{" "}
                        to inspect the generated event.
                    </div>
                ) : null}

                {demoSimulation.isError ? (
                    <div className="notice notice-error" role="alert">
                        Portfolio demo could not be started.
                    </div>
                ) : null}

                <p className="field-hint">
                    This creates observational incident-response data only. It
                    does not execute automated remediation.
                </p>
            </section>

            <section aria-labelledby="activity-heading">
                <div className="section-heading">
                    <div>
                        <p className="eyebrow">Recent activity</p>
                        <h2 id="activity-heading">Incident stream</h2>
                    </div>
                </div>
                <EmptyState
                    title="Incident activity is project-scoped"
                    description="Signed event ingestion and deterministic incident correlation are available. Open the Incidents workspace with an organisation and project ID to review correlated failures."
                />
            </section>
        </>
    );
}
