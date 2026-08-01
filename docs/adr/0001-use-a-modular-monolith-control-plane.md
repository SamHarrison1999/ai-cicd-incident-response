# ADR 0001: Use a modular monolith for the Java control plane

- Status: Accepted
- Date: 2026-07-30

## Context

The platform contains identity, tenancy, ingestion, pipeline, incident, recommendation, evaluation, and audit capabilities. Splitting these into independently deployed Java services would demonstrate microservices, but would also add distributed transactions, message infrastructure, service discovery, more CI pipelines, and substantially greater local operational cost before the core product is proven.

## Decision

Implement one Spring Boot deployable control plane with explicit internal modules and dependency rules. Modules own their domain models and persistence interfaces. Cross-module behaviour uses application services, ports, or in-process domain events rather than direct repository access.

The Python intelligence service remains separately deployed because its runtime, dependency ecosystem, scaling characteristics, and safety boundary are materially different.

## Consequences

### Positive

- One transactional boundary for event ingestion, workflow, and audit records.
- Faster local startup and simpler debugging.
- Architectural boundaries remain visible and testable.
- Lower risk of leaving the portfolio project incomplete.

### Negative

- Java modules cannot be scaled or deployed independently.
- Poor discipline could create a tightly coupled codebase.
- Extraction to services later would require contract and data-ownership work.

## Guardrails

- Package/module dependency tests will enforce allowed directions.
- Repositories remain module-private.
- Public module APIs are explicit.
- No shared catch-all domain package is allowed.
