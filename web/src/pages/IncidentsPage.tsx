import { EmptyState } from "../components/common/EmptyState";

export function IncidentsPage() {
    return (
        <>
            <section className="page-heading">
                <p className="eyebrow">Incident operations</p>
                <h2>Incidents</h2>
                <p>
                    Review correlated failures, evidence timelines, and
                    human-approved recommendations.
                </p>
            </section>
            <EmptyState
                title="No incidents created"
                description="Incident correlation and lifecycle management are introduced in Phase 5."
            />
        </>
    );
}
