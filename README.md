# AI-Assisted CI/CD Incident Response Platform

A portfolio-grade platform-engineering system that ingests simulated CI/CD and infrastructure events, correlates related failures into incidents, and produces evidence-grounded recommendations for human review.

## Project status

**Current phase:** Phase 0 ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â scope, architecture, risks, backlog, repository structure, and initial ADRs.

No runtime services are implemented in Phase 0. Phase 1 will create the Java control plane, Python intelligence service, React frontend, local infrastructure, and continuous-integration foundation.

## Product objective

The platform helps platform engineers, release engineers, developers, and engineering managers answer:

- What failed, and when?
- Which project, pipeline, commit, environment, or deployment was affected?
- Which events and log fragments support the diagnosis?
- Has a similar incident occurred before?
- Which resolution was previously effective?
- How confident is the recommendation?
- Did a human accept, edit, or reject it?

## Safety boundary

Version 1 is decision support, not autonomous remediation.

The platform may recommend diagnostic and recovery steps, but it must not execute destructive or production-changing actions. Every AI recommendation must be evidence-grounded, confidence-scored, auditable, and reviewed by a human.

## Planned architecture

- **Control plane:** Java 25 LTS, Spring Boot, PostgreSQL, Flyway, Spring Security, Testcontainers, OpenAPI, Actuator, Micrometer, and OpenTelemetry.
- **Intelligence service:** Python 3.14, FastAPI, Pydantic, deterministic diagnosis rules, retrieval, optional provider adapters, sanitisation, evaluation, and abstention.
- **Web application:** React, TypeScript, Vite, TanStack Query, accessible components, and Playwright.
- **Local platform:** Docker Compose with PostgreSQL and an OpenTelemetry-compatible observability stack introduced incrementally.
- **Delivery:** GitHub Actions, container images, dependency and security scanning, and documented releases.

Exact dependency patch versions will be pinned when each executable service is created.

## Documentation

- [Architecture](docs/architecture.md)
- [Product scope](docs/product-scope.md)
- [Repository structure](docs/repository-structure.md)
- [Product backlog](docs/product-backlog.md)
- [Risk register](docs/risk-register.md)
- [Testing strategy](docs/testing-strategy.md)
- [Limitations](docs/limitations.md)
- [Progress ledger](docs/progress/ledger.md)
- [Architecture decisions](docs/adr/)

## Phase plan

| Phase | Outcome |
|---|---|
| 0 | Scope, architecture, ADRs, risks, backlog, and documentation foundation |
| 1 | Monorepo and CI foundation |
| 2 | Identity, organisations, projects, and frontend shell |
| 3 | Secure, idempotent event ingestion |
| 4 | Normalised events, pipeline runs, and timelines |
| 5 | Incident correlation and state machine |
| 6 | Log storage, search, evidence linking, and viewer |
| 7 | Sanitisation and deterministic diagnosis |
| 8 | Historical retrieval |
| 9 | Provider abstraction and evidence-grounded AI recommendations |
| 10 | Human review, feedback, and resolutions |
| 11 | Golden dataset and evaluation dashboard |
| 12 | Failure simulation and observability |
| 13 | Security hardening |
| 14 | Deployment, release, demo, and portfolio case study |

## Phase 0 validation

From the repository root:

```bash
find . -maxdepth 3 -type f | sort

git diff --check
```

On PowerShell:

```powershell
Get-ChildItem -Recurse -File | Select-Object -ExpandProperty FullName

git diff --check
```

Expected result: the documented Phase 0 files are present and `git diff --check` produces no output.

## Run the local platform

Copy the local environment template and start the complete stack:

```powershell
Copy-Item .env.example .env
docker compose config
docker compose build
docker compose up -d
.\scripts\verify-local-stack.ps1
```

Open the web application at `http://localhost:3000`.

Stop the stack:

```powershell
docker compose down
```

See [`docs/deployment.md`](docs/deployment.md) for health checks, logs, port overrides, and destructive volume cleanup.

## Continuous integration

GitHub Actions validates repository structure, the Java control plane, Python intelligence service, React web application, container images, and Docker Compose configuration.

The aggregate required check is:

```text
Phase 1 quality gate
```

See [`docs/ci.md`](docs/ci.md) for workflow behaviour, local parity commands, artifacts, dependency automation, and branch-protection recommendations.

## Phase 2 local verification

Run the complete Phase 2 local quality gate from PowerShell:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\verify-phase-2.ps1
```

For a faster application-only pass that skips container builds:

```powershell
.\scripts\verify-phase-2.ps1 -SkipDocker
```

The full release verification requires repository checks, Java tests with PostgreSQL Testcontainers, Python quality gates, frontend formatting/lint/tests/build, Playwright, Docker Compose validation, and application image builds.
