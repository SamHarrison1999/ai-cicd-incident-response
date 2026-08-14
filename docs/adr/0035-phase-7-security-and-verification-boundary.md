# ADR 0035: Phase 7 security and verification boundary

## Status

Accepted for Phase 7 Batch 5.

## Decision

Phase 7 diagnosis remains bounded human decision support. Cumulative
verification must demonstrate that sanitisation precedes diagnosis, tenant
checks precede evidence access, diagnosis results are deterministic and
auditable, and no raw content or secret-like values cross the diagnosis API.

The final phase verification covers security contracts, duplicate and
cross-tenant scenarios, abstention, frontend presentation, build artefacts,
and Docker Compose configuration. Production model providers and autonomous
remediation remain explicitly out of scope.
