import { Outlet } from "react-router-dom";

import { SideNavigation } from "./SideNavigation";
import { TopBar } from "./TopBar";

export function AppLayout() {
    return (
        <div className="app-shell">
            <a className="skip-link" href="#main-content">
                Skip to main content
            </a>
            <SideNavigation />
            <div className="app-main">
                <TopBar />
                <main id="main-content" className="page-content">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}
