# ADR 0011: Use GitHub Actions for Phase 1 quality gates

- Status: Accepted
- Date: 2026-08-01
- Decision owners: Project maintainer
- Related phase: Phase 1

## Context

The monorepo contains independently built Java, Python, React, and containerised components. Local verification is necessary but insufficient because pull requests require repeatable, visible, repository-hosted quality evidence.

The CI design must avoid claiming success when one component fails, preserve useful reports, validate lockfiles, build release artifacts, and prevent dependency or infrastructure changes from bypassing review.

## Decision

Use GitHub Actions with one primary CI workflow containing independent jobs for:

- Repository structure and unfinished-marker validation.
- Java formatting, analysis, tests, coverage, and executable JAR creation.
- Python lockfile, formatting, linting, strict typing, tests, and branch coverage.
- React formatting, linting, unit tests, production build, and Playwright browser testing.
- Container builds for each deployable application.
- Docker Compose model validation.
- A final aggregate quality-gate job.

Also use:

- Dependency Review on pull requests.
- Dependabot for Gradle, Python, npm, Docker, and GitHub Actions.
- Pull-request and issue templates that expose verification and AI-safety expectations.

## Consequences

### Positive

- Each technology boundary fails independently and visibly.
- Required branch checks can target one aggregate quality-gate job.
- Lockfiles and wrappers are validated in CI.
- Test and coverage reports remain downloadable after failures.
- Container definitions are continuously build-tested.
- Dependency changes receive automated review and update proposals.
- Contributors must describe evidence, safety, and verification.

### Negative

- The first workflow run downloads several language and browser toolchains.
- Playwright and container builds make CI slower.
- Dependabot can create multiple maintenance pull requests.
- GitHub-hosted CI does not prove production deployment readiness.
- Compose configuration validation does not replace a full integration environment.

## Guardrails

- Workflows use least-privilege read permissions unless an action requires more.
- CI never receives model-provider secrets during Phase 1.
- Dependency Review fails on newly introduced high-severity vulnerabilities.
- Builds use committed Gradle, uv, and npm lock state.
- Generated reports are artifacts, not committed files.
- No deployment or remediation occurs from the CI workflow.
- Required branch protection should include the aggregate quality-gate job.

## Alternatives considered

### One sequential CI job

Rejected because a slow early failure would hide results from unrelated components and reduce diagnostic clarity.

### Separate workflow for every component

A valid option, but deferred because a single aggregate workflow gives Phase 1 one explicit required quality gate while retaining parallel jobs.

### Self-hosted runners

Deferred because the foundation does not need private network access or specialised hardware and should minimise operational overhead.
