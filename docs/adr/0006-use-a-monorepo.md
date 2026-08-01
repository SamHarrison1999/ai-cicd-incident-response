# ADR 0006: Use a monorepo for the platform

- Status: Accepted
- Date: 2026-08-01
- Decision owners: Project maintainer
- Related phase: Phase 1

## Context

The platform contains a Java control plane, a Python intelligence service, a React web application, infrastructure configuration, shared documentation, and cross-service tests.

The components are deployed separately but form one portfolio product. Changes to event contracts, recommendation schemas, local infrastructure, or demonstration workflows often require coordinated updates across more than one component.

Separate repositories would add version coordination, duplicated repository administration, and fragmented documentation before independent release cycles are justified.

## Decision

Use one Git repository with these top-level component boundaries:

```text
services/control-plane/
services/intelligence-service/
web/
infrastructure/
scripts/
docs/
```

Each executable component owns its dependency manifests, tests, build configuration, and container definition. Root-level automation may orchestrate cross-component validation without merging component responsibilities.

## Consequences

### Positive

- One pull request can update contracts and all affected consumers.
- Architecture, security, evaluation, and demonstration documentation remain close to the code.
- Local development and CI orchestration are simpler.
- The portfolio presents one coherent product.
- Cross-service integration tests can be versioned with the services they validate.

### Negative

- CI must avoid rebuilding unaffected components unnecessarily.
- Repository-wide conventions must not erase language-specific standards.
- Access control cannot be separated by repository.
- The repository will grow larger than a single-service project.

## Guardrails

- Java does not import Python implementation code.
- Python does not write directly to Java-owned domain tables.
- The web application communicates through published APIs.
- Each component retains independent dependency management.
- Cross-component contracts are explicitly versioned.
- A future split requires measured organisational or release-management need, not preference alone.

## Alternatives considered

### Separate repository per component

Rejected for version 1 because it increases coordination overhead without providing meaningful team or deployment independence.

### One application with embedded Python execution

Rejected because it weakens runtime isolation, API boundaries, independent testing, and AI-provider abstraction.

### Git submodules

Rejected because they add operational friction while retaining most multi-repository coordination costs.
