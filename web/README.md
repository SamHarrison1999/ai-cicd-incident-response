# Web Application

The web application is the React and TypeScript user interface for platform engineers, release engineers, developers, and engineering managers.

## Technology baseline

- Node.js 24 LTS.
- React 19.
- TypeScript.
- Vite.
- React Router.
- TanStack Query.
- Vitest and Testing Library.
- Playwright.
- ESLint and Prettier.
- nginx production image.

Exact resolved dependencies are recorded in `package-lock.json`.

## Foundation routes

- `/` â€” operational overview.
- `/pipelines` â€” pipeline-run placeholder.
- `/incidents` â€” incident workspace placeholder.
- `/settings` â€” foundation safety settings.
- Unknown routes render an accessible not-found page.

## Install

```powershell
npm ci
```

## Quality checks

```powershell
npm run format:check
npm run lint
npm run test
npm run build
```

## End-to-end tests

Install Chromium once:

```powershell
npm run test:e2e:install
```

Run:

```powershell
npm run test:e2e
```

## Local development

```powershell
npm run dev
```

The Vite server runs on `http://localhost:5173`.

Requests under `/control-plane` are proxied to `http://localhost:8080`. If the backend is unavailable, the dashboard displays an unavailable control-plane state rather than failing to render.

## Production container

```powershell
docker build -t incident-response-web:phase-1 .
docker run --rm -p 8081:8080 --name incident-response-web incident-response-web:phase-1
```

Open `http://localhost:8081`.
