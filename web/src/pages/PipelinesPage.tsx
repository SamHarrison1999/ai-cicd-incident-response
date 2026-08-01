import { EmptyState } from "../components/common/EmptyState";

export function PipelinesPage() {
    return (
        <>
            <section className="page-heading">
                <p className="eyebrow">Delivery visibility</p>
                <h2>Pipeline runs</h2>
                <p>
                    Normalised Jenkins and GitHub Actions-style runs will appear
                    here.
                </p>
            </section>
            <EmptyState
                title="No pipeline runs available"
                description="Pipeline aggregation is implemented in Phase 4."
            />
        </>
    );
}
