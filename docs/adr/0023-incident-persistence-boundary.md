# ADR 0023: Persist incidents as tenant-owned lifecycle aggregates

## Status

Accepted for Phase 5 Batch 2.

## Context

Batch 1 defines deterministic correlation and a guarded incident state machine. The implementation needs a durable aggregate that remains tenant-scoped, can be reopened without replacing its identity, and can link normalised events without allowing one event to become evidence for multiple primary incidents.

## Decision

1. `Incident` is a tenant-owned aggregate identified by organisation, project, and incident ID.
2. Lifecycle state is an allow-listed enum and transitions are performed by the aggregate method rather than by direct status mutation.
3. `IncidentEventLink` is a separate persistence boundary. A unique event constraint enforces zero or one primary incident link for each normalised event.
4. Database foreign keys repeat the organisation and project scope where required to make cross-tenant links structurally invalid.
5. Reopening changes the existing incident state and preserves its ID and event history.
6. Correlation decisions and scoring metadata are added in Batch 3; this batch does not infer a grouping.

## Consequences

- Incident lifecycle rules can be tested without a database or provider.
- Tenant isolation is represented in both repository methods and database constraints.
- The primary-link rule is explicit and can later be extended with secondary relationships without changing the version 1 invariant.
- State transition and persistence concerns remain separate from correlation policy execution.