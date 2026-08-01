# AI-Assisted CI/CD Incident Response Platform

A portfolio-grade platform-engineering system that ingests simulated CI/CD and infrastructure events, correlates related failures into incidents, and produces evidence-grounded recommendations for human review.

## Project status

**Current phase:** Phase 0 — scope, architecture, risks, backlog, repository structure, and initial ADRs.

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
