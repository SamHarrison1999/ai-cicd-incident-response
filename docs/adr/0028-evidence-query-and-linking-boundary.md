# ADR 0028: Evidence query and linking boundary

## Status

Accepted for Phase 6 Batch 3.

## Decision

Evidence search is tenant-scoped by organisation and project, uses bounded
metadata filters, and returns deterministic cursor pagination. Search responses
are metadata projections; raw evidence content remains behind the later viewer
boundary.

Evidence may be linked to one incident and one normalised CI event through
explicit tenant-owned link records. Link operations validate both sides of the
relationship inside the requested organisation and project before persisting
an audit event.

## Constraints

- Organisation and project identifiers are required on every query and link
  route.
- Search limits are bounded to 1--100 items.
- Ordering is `occurredAt DESC, id DESC` and cursor values are opaque.
- Search responses do not expose raw evidence content.
- Cross-project and cross-organisation links are rejected as not found.
- Link operations require an active incident-writer role.
- No causal or AI-generated interpretation is introduced by this batch.
