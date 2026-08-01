# ADR 0009: Use React, Vite, and TanStack Query

- Status: Accepted
- Date: 2026-08-01
- Decision owners: Project maintainer
- Related phase: Phase 1

## Context

The platform requires an accessible browser interface for pipeline runs, incidents, evidence, recommendations, evaluations, and operational metrics.

The frontend needs predictable routing, typed remote-state handling, fast development builds, independent unit tests, browser-level end-to-end tests, and a production container.

## Decision

Use:

- React 19 and TypeScript.
- Vite for development and production builds.
- React Router for browser routes.
- TanStack Query for remote server state.
- A small typed API-client layer.
- Plain CSS with reusable design tokens and components.
- Vitest and Testing Library for component and client tests.
- Playwright for browser end-to-end tests.
- nginx for the production static image and backend proxy.

The browser communicates with the Java control plane. It does not call the intelligence service directly.

## Consequences

### Positive

- Clear separation between view components and remote-state logic.
- Fast local development and builds.
- Type-checked route and API integration.
- Accessible testing at component and browser levels.
- A lightweight visual system without an unnecessary component framework.
- Production routing can proxy API traffic through the same origin.

### Negative

- The custom CSS system requires ongoing maintenance.
- React Router and TanStack Query add conventions developers must learn.
- Runtime API responses are trusted after HTTP success during the foundation phase.
- Browser-to-control-plane contracts are not yet generated from OpenAPI.

## Guardrails

- Components do not issue ad hoc fetch calls.
- Server state is managed through TanStack Query hooks.
- The browser never calls the Python intelligence service directly.
- Pages must remain keyboard accessible.
- Automatic remediation controls are not exposed.
- Human-review requirements remain visible.
- OpenAPI-based client generation will be evaluated after the API stabilises.

## Alternatives considered

### Next.js

Rejected because server-side rendering and a Node production server are not required for this internal developer-tooling interface.

### A large component framework

Deferred because the foundation benefits from a small, inspectable design system. Adoption requires demonstrated accessibility or delivery value.

### Direct fetch calls in components

Rejected because it couples transport concerns to presentation and complicates testing.
