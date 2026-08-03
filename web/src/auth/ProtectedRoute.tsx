import { Navigate, Outlet, useLocation } from "react-router-dom";

import { useAuth } from "./useAuth";

export function ProtectedRoute() {
    const { accessToken, isInitialising } = useAuth();
    const location = useLocation();

    if (isInitialising) {
        return (
            <main className="auth-loading" aria-live="polite">
                <p className="eyebrow">AI-assisted incident response</p>
                <h1>Restoring your secure session</h1>
            </main>
        );
    }

    if (accessToken === null) {
        return (
            <Navigate to="/login" replace state={{ from: location.pathname }} />
        );
    }

    return <Outlet />;
}
