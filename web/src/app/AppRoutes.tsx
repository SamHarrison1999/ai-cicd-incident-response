import { Navigate, Route, Routes } from "react-router-dom";

import { AppLayout } from "../components/layout/AppLayout";
import { DashboardPage } from "../pages/DashboardPage";
import { IncidentsPage } from "../pages/IncidentsPage";
import { NotFoundPage } from "../pages/NotFoundPage";
import { PipelinesPage } from "../pages/PipelinesPage";
import { SettingsPage } from "../pages/SettingsPage";

export function AppRoutes() {
    return (
        <Routes>
            <Route element={<AppLayout />}>
                <Route index element={<DashboardPage />} />
                <Route path="pipelines" element={<PipelinesPage />} />
                <Route path="incidents" element={<IncidentsPage />} />
                <Route path="settings" element={<SettingsPage />} />
                <Route path="home" element={<Navigate to="/" replace />} />
                <Route path="*" element={<NotFoundPage />} />
            </Route>
        </Routes>
    );
}
