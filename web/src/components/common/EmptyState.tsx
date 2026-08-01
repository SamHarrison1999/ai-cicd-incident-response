interface EmptyStateProps {
    title: string;
    description: string;
}

export function EmptyState({ title, description }: EmptyStateProps) {
    return (
        <section className="empty-state">
            <div className="empty-state-icon" aria-hidden="true">
                â†—
            </div>
            <h3>{title}</h3>
            <p>{description}</p>
        </section>
    );
}
