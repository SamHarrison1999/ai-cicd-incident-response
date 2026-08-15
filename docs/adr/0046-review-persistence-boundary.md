# ADR 0046: Human review persistence boundary

## Status

Accepted for Phase 10 Batch 2.

## Decision

Persist human review actions as tenant-scoped governance records. Accept, edit, and reject actions are immutable facts. An edit creates a new reviewed recommendation version; it never overwrites generated content. Rejection requires a reason category. Optional comments and edited fields are bounded before persistence.

Incident resolutions may reference a reviewed recommendation version from the same organisation and project. The persistence boundary records the decision and resolution context only; it does not execute remediation or mutate provider systems.

## Consequences

- Review history is auditable and append-only.
- Tenant and project ownership are carried on every record and enforced by database keys and service checks.
- Version numbers are deterministic within a recommendation scope.
- Raw evidence, secrets, provider credentials, and executable instructions are not copied into review records.
