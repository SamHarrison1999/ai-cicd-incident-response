# ADR 0013: Enforce Tenant Isolation in Service and Query Boundaries

- Status: Accepted
- Date: 2026-08-03

## Context

Organisations are security boundaries. An authenticated user in one organisation must not infer, read, or mutate another organisation's projects or later incidents.

Controller-only checks are fragile because background jobs and internal services can bypass controllers.

## Decision

- Tenant-owned records contain an explicit organisation identifier directly or through a required project relationship.
- Services receive the authenticated user identity and verify membership.
- Tenant repositories expose organisation-scoped queries.
- Controllers call authorised services, not unrestricted repositories.
- Cross-tenant access returns `404 Not Found`.
- Integration tests use two tenants and prove read and write isolation.
- Foreign keys and tenant-scoped uniqueness reinforce ownership integrity.
- Audit records capture actor, tenant, target, action, and correlation identifier.

PostgreSQL row-level security is deferred from version 1.

## Consequences

### Positive

- Tenant decisions are explicit and testable in Java.
- Query scoping reduces accidental cross-tenant reads.
- Controls apply to HTTP and internal workflows.
- Concealed `404` responses reduce identifier enumeration.

### Negative

- Service and repository APIs require organisation context.
- Developers must consistently use scoped repository methods.
- Application controls do not provide database-enforced row-level security.

## Rejected alternatives

- Controller-only checks: internal code paths could bypass them.
- PostgreSQL row-level security in Phase 2: deferred due to connection-context and migration complexity.
- Database per organisation: operationally excessive for version 1.