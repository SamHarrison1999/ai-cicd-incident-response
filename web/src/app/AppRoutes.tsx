import { Navigate, Route, Routes } from "react-router-dom";

import { ProtectedRoute } from "../auth/ProtectedRoute";
import { AppLayout } from "../components/layout/AppLayout";
import { DashboardPage } from "../pages/DashboardPage";
import { DiagnosisPage } from "../pages/DiagnosisPage";
import { EvidencePage } from "../pages/EvidencePage";
import { HistoricalRetrievalPage } from "../pages/HistoricalRetrievalPage";
import { IncidentsPage } from "../pages/IncidentsPage";
import { LoginPage } from "../pages/LoginPage";
import { NotFoundPage } from "../pages/NotFoundPage";
import { OrganisationsPage } from "../pages/OrganisationsPage";
import { PipelinesPage } from "../pages/PipelinesPage";
import { RegisterPage } from "../pages/RegisterPage";
import { SettingsPage } from "../pages/SettingsPage";

export function AppRoutes() {
    return (
        <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            <Route element={<ProtectedRoute />}>
                <Route element={<AppLayout />}>
                    <Route index element={<DashboardPage />} />
                    <Route
                        path="organisations"
                        element={<OrganisationsPage />}
                    />
                    <Route path="pipelines" element={<PipelinesPage />} />
                    <Route path="incidents" element={<IncidentsPage />} />
                    <Route path="evidence" element={<EvidencePage />} />
                    <Route
                        path="historical-retrieval"
                        element={<HistoricalRetrievalPage />}
                    />
                    <Route path="diagnosis" element={<DiagnosisPage />} />
                    <Route path="settings" element={<SettingsPage />} />
                    <Route path="home" element={<Navigate to="/" replace />} />
                    <Route path="*" element={<NotFoundPage />} />
                </Route>
            </Route>
        </Routes>
    );
}
