import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";

import { AppProviders } from "./app/AppProviders";
import { AppRoutes } from "./app/AppRoutes";
import "./styles/evidence.css";
import "./styles/global.css";

const rootElement = document.getElementById("root");

if (rootElement === null) {
    throw new Error("Root element was not found");
}

createRoot(rootElement).render(
    <StrictMode>
        <BrowserRouter>
            <AppProviders>
                <AppRoutes />
            </AppProviders>
        </BrowserRouter>
    </StrictMode>,
);
