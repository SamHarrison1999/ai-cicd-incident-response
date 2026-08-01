import { NavLink } from "react-router-dom";

const navigationItems = [
    { to: "/", label: "Overview", end: true },
    { to: "/pipelines", label: "Pipeline runs", end: false },
    { to: "/incidents", label: "Incidents", end: false },
    { to: "/settings", label: "Settings", end: false },
] as const;

export function SideNavigation() {
    return (
        <aside className="side-navigation" aria-label="Primary navigation">
            <div className="brand">
                <span className="brand-mark" aria-hidden="true">
                    IR
                </span>
                <div>
                    <strong>Incident Response</strong>
                    <span>Platform engineering</span>
                </div>
            </div>

            <nav>
                <ul className="navigation-list">
                    {navigationItems.map((item) => (
                        <li key={item.to}>
                            <NavLink
                                className={({ isActive }) =>
                                    isActive
                                        ? "navigation-link navigation-link-active"
                                        : "navigation-link"
                                }
                                end={item.end}
                                to={item.to}
                            >
                                {item.label}
                            </NavLink>
                        </li>
                    ))}
                </ul>
            </nav>

            <p className="environment-badge">Local foundation</p>
        </aside>
    );
}
