# AI-Assisted CI/CD Incident Response Platform

A portfolio-grade platform-engineering system that ingests simulated CI/CD and infrastructure events, correlates related failures into incidents, and produces evidence-grounded recommendations for human review.

## Project status

**Current phase:** Phase 10 - human review, feedback, and resolutions

Phases 0 through 9 are complete. The repository now contains a runnable Java
control plane, Python intelligence service, React web application, PostgreSQL
persistence, Docker Compose environment, continuous-integration quality gates,
authentication, organisations, projects, tenant isolation, pipeline timelines,
incident correlation, evidence persistence and search, sanitisation, and
bounded deterministic diagnosis for human review.

Phase 3 established secure event ingestion. Phase 4 added provider-neutral
pipeline timelines. Phase 5 added deterministic incident correlation and the
incident workspace. Phase 6 added bounded evidence persistence, redaction,
search, linking, and the investigation viewer. Phase 7 added sanitisation,
prompt-injection defence, deterministic diagnosis, bounded confidence,
abstention, and a human-review diagnosis workspace. Phase 8 added tenant-scoped
historical retrieval with deterministic ranking, filters, pagination, and
incident context.

Phase 9 completed provider abstraction and evidence-grounded AI recommendations. Phase 10 now adds
bounded recommendation requests, provider-neutral adapters, provenance,
confidence, abstention, and human-review safety. Automatic remediation and
production-changing actions remain outside this phase.

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
- [Phase 5 correlation model](docs/phase-5-correlation-model.md)
- [Phase 6 evidence model](docs/phase-6-evidence-model.md)
- [Phase 7 diagnosis model](docs/phase-7-diagnosis-model.md)
- [Phase 8 retrieval model](docs/phase-8-retrieval-model.md)
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


Phase 7 establishes bounded sanitisation and deterministic diagnosis outputs for human review.

## Project status through Phase 11

Phases 1-10 are complete and Phase 11 is now in progress. The platform includes secure CI/CD event ingestion, deterministic incident correlation, tenant-scoped evidence and historical retrieval, bounded diagnosis, provider-neutral recommendations, and human review with immutable versions and governed resolutions.

Phase 11 is focused on governed feedback analytics derived from human review. Feedback remains tenant-scoped, attributable, versioned, bounded, and advisory. It does not silently retrain providers, change production policy, or execute remediation.

## Phase 3 local verification

From the repository root in PowerShell:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\verify-phase-3.ps1
```

Expected result: repository, Java, frontend, Docker Compose, and Git whitespace checks pass.

## Phase 4 local verification

```powershell
.\scripts\verify-phase-4.ps1
```

Expected result: the pipeline timeline API, pagination, frontend workspace, and cumulative quality checks pass.

## Phase 5 local verification

```powershell
.\scripts\verify-phase-5.ps1
```

Expected result: incident persistence, deterministic correlation, incident API, frontend checks, and cumulative quality checks pass.

## Phase 6 local verification

```powershell
.\scripts\verify-phase-6.ps1
```

Expected result: evidence persistence, redaction, search, linking, viewer checks, frontend checks, and cumulative quality checks pass.

## Phase 7 local verification

```powershell
.\scripts\verify-phase-7.ps1
```

Expected result: sanitisation, prompt-injection defence, deterministic diagnosis, security checks, frontend checks, and cumulative quality checks pass.

## Phase 8 local verification

```powershell
.\scripts\verify-phase-8.ps1
```

Expected result: historical retrieval persistence, API filters, pagination, workspace checks, frontend checks, and cumulative quality checks pass.

## Phase 9 local verification

```powershell
.\scripts\verify-phase-9.ps1
```

Expected result: provider-neutral recommendations, persistence, security, workspace checks, frontend checks, and cumulative quality checks pass.

## Phase 10 local verification

```powershell
.\scripts\verify-phase-10.ps1
```

Expected result: human review persistence, review and resolution APIs, workspace checks, authentication isolation, frontend checks, and cumulative quality checks pass.

## Phase 11 local verification

```powershell
.\scripts\verify-phase-11.ps1
```

Expected result: the Phase 11 feedback governance contract and all cumulative repository, Java, frontend, Docker Compose, and Git checks pass.