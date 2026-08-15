import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { AppProviders } from "./app/AppProviders";
import { AppRoutes } from "./app/AppRoutes";
import "./styles/diagnosis.css";
import "./styles/evidence.css";
import "./styles/feedback.css";
import "./styles/global.css";
import "./styles/recommendations.css";
import "./styles/retrieval.css";
import "./styles/review.css";

const rootElement = document.getElementById("root");
if (rootElement === null) throw new Error("Root element was not found");
createRoot(rootElement).render(
    <StrictMode>
        <BrowserRouter>
            <AppProviders>
                <AppRoutes />
            </AppProviders>
        </BrowserRouter>
    </StrictMode>,
);
