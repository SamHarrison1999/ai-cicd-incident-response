import { type SyntheticEvent, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";

import { ApiError } from "../api/httpClient";
import { useAuth } from "../auth/useAuth";

interface NavigationState {
    from?: string;
    registrationComplete?: boolean;
}

export function LoginPage() {
    const auth = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const state = location.state as NavigationState | null;
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    async function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
        event.preventDefault();
        setError(null);
        setSubmitting(true);

        try {
            await auth.login(email, password);
            await navigate(state?.from ?? "/", { replace: true });
        } catch (caught) {
            setError(
                caught instanceof ApiError
                    ? caught.message
                    : "Sign in failed. Please try again.",
            );
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <main className="auth-page">
            <section className="auth-panel" aria-labelledby="login-heading">
                <p className="eyebrow">Secure engineering workspace</p>
                <h1 id="login-heading">Sign in</h1>
                <p>
                    Access incident timelines, pipeline evidence, and
                    human-reviewed AI analysis.
                </p>

                {state?.registrationComplete === true ? (
                    <div className="notice notice-success" role="status">
                        Account created. Sign in to continue.
                    </div>
                ) : null}

                {error !== null ? (
                    <div className="notice notice-error" role="alert">
                        {error}
                    </div>
                ) : null}

                <form className="auth-form" onSubmit={handleSubmit}>
                    <label>
                        Email address
                        <input
                            type="email"
                            autoComplete="email"
                            value={email}
                            onChange={(event) => {
                                setEmail(event.target.value);
                            }}
                            required
                        />
                    </label>

                    <label>
                        Password
                        <input
                            type="password"
                            autoComplete="current-password"
                            value={password}
                            onChange={(event) => {
                                setPassword(event.target.value);
                            }}
                            required
                        />
                    </label>

                    <button className="button" disabled={submitting}>
                        {submitting ? "Signing inÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦" : "Sign in"}
                    </button>
                </form>

                <p>
                    New to the platform?{" "}
                    <Link to="/register">Create an account</Link>
                </p>
            </section>
        </main>
    );
}
