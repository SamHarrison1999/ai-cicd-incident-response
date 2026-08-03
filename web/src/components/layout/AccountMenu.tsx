import { useNavigate } from "react-router-dom";

import { useAuth } from "../../auth/useAuth";

export function AccountMenu() {
    const auth = useAuth();
    const navigate = useNavigate();

    async function handleLogout() {
        await auth.logout();
        navigate("/login", { replace: true });
    }

    return (
        <div className="account-menu">
            <div>
                <strong>{auth.currentUser?.displayName}</strong>
                <span>{auth.currentUser?.email}</span>
            </div>
            <button className="button button-secondary" onClick={handleLogout}>
                Sign out
            </button>
        </div>
    );
}
