import { AccountMenu } from "./AccountMenu";
import { WorkspaceSwitcher } from "./WorkspaceSwitcher";

export function TopBar() {
    return (
        <header className="top-bar">
            <div>
                <p className="eyebrow">AI-assisted CI/CD incident response</p>
                <h1>Engineering operations</h1>
            </div>

            <div className="top-bar-actions">
                <WorkspaceSwitcher />

                <div className="review-control">
                    <span
                        className="status-dot status-dot-healthy"
                        aria-hidden="true"
                    />
                    Human review required
                </div>

                <AccountMenu />
            </div>
        </header>
    );
}
