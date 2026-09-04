import { type SyntheticEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { ApiError } from "../api/httpClient";
import { useAuth } from "../auth/useAuth";

export function RegisterPage() {
    const auth = useAuth();
    const navigate = useNavigate();
    const [displayName, setDisplayName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    async function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
        event.preventDefault();
        setError(null);
        setSubmitting(true);

        try {
            await auth.register({ displayName, email, password });
            await navigate("/login", {
                replace: true,
                state: { registrationComplete: true },
            });
        } catch (caught) {
            setError(
                caught instanceof ApiError
                    ? caught.message
                    : "Registration failed. Please try again.",
            );
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <main className="auth-page">
            <section className="auth-panel" aria-labelledby="register-heading">
                <p className="eyebrow">Create your secure workspace account</p>
                <h1 id="register-heading">Register</h1>

                {error !== null ? (
                    <div className="notice notice-error" role="alert">
                        {error}
                    </div>
                ) : null}

                <form className="auth-form" onSubmit={handleSubmit}>
                    <label>
                        Display name
                        <input
                            autoComplete="name"
                            value={displayName}
                            onChange={(event) => {
                                setDisplayName(event.target.value);
                            }}
                            required
                        />
                    </label>

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
                            autoComplete="new-password"
                            minLength={12}
                            value={password}
                            onChange={(event) => {
                                setPassword(event.target.value);
                            }}
                            required
                        />
                        <span className="field-hint">
                            Use at least 12 characters.
                        </span>
                    </label>

                    <button className="button" disabled={submitting}>
                        {submitting
                            ? "Creating accountÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦"
                            : "Create account"}
                    </button>
                </form>

                <p>
                    Already registered? <Link to="/login">Sign in</Link>
                </p>
            </section>
        </main>
    );
}
