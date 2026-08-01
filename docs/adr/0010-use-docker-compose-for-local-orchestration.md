# ADR 0010: Use Docker Compose for local orchestration

- Status: Accepted
- Date: 2026-08-01
- Decision owners: Project maintainer
- Related phase: Phase 1

## Context

The platform contains PostgreSQL, a Java control plane, a Python intelligence service, and a React/nginx web application.

Developers and reviewers need a repeatable way to start the complete platform without manually installing PostgreSQL or coordinating service startup order.

The local environment must remain distinct from the eventual production deployment model.

## Decision

Use Docker Compose for local orchestration with:

- One service per deployable component.
- A named PostgreSQL volume.
- A private bridge network.
- Explicit health checks.
- `depends_on` health conditions for startup ordering.
- Root `.env` configuration for non-secret local overrides.
- Host ports defined in `docker-compose.override.yml`.
- Flyway as the sole application-schema migration mechanism.

## Consequences

### Positive

- The complete platform starts with one command.
- Local versions match containerised CI and release builds more closely.
- PostgreSQL is reproducible and disposable.
- Service health and startup dependencies are visible.
- Reviewers can run the portfolio demonstration without installing language runtimes.

### Negative

- Initial image builds are relatively slow.
- Docker Desktop consumes significant local resources.
- Health-based startup ordering does not replace runtime retry and resilience.
- Compose networking differs from hosted production infrastructure.
- Exposed local ports can conflict with existing developer services.

## Guardrails

- Committed credentials are local-development defaults only.
- `.env` remains ignored.
- No production secrets are stored in Compose files.
- Database schema creation remains in Flyway.
- The browser does not call the intelligence service directly.
- Containers run application processes as non-root users where supported.
- Automatic remediation is not introduced.
- Production deployment decisions require a separate ADR.

## Alternatives considered

### Run every service directly on the host

Rejected because it requires more manual setup and produces greater environment drift.

### Kubernetes for local development

Deferred because it adds operational complexity before the platform needs Kubernetes-specific behaviour.

### Embed PostgreSQL only in application tests

Rejected because reviewers need a persistent, integrated local environment in addition to Testcontainers-based tests.
