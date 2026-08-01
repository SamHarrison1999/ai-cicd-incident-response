interface ServiceStatusCardProps {
    name: string;
    description: string;
    status: "healthy" | "unavailable" | "loading";
    detail: string;
}

const statusLabels = {
    healthy: "Healthy",
    unavailable: "Unavailable",
    loading: "Checking",
} as const;

export function ServiceStatusCard({
    name,
    description,
    status,
    detail,
}: ServiceStatusCardProps) {
    return (
        <article className="status-card">
            <div className="status-card-heading">
                <div>
                    <h3>{name}</h3>
                    <p>{description}</p>
                </div>
                <span className={`status-pill status-pill-${status}`}>
                    {statusLabels[status]}
                </span>
            </div>
            <p className="status-detail">{detail}</p>
        </article>
    );
}
