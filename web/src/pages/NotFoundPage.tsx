import { Link } from "react-router-dom";

export function NotFoundPage() {
    return (
        <section className="empty-state">
            <div className="empty-state-icon" aria-hidden="true">
                404
            </div>
            <h2>Page not found</h2>
            <p>The requested platform route does not exist.</p>
            <Link className="primary-link" to="/">
                Return to overview
            </Link>
        </section>
    );
}
