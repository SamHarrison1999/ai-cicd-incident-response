# ADR 0034: Diagnosis API and human-review workspace

## Status

Accepted for Phase 7 Batch 4.

## Decision

Expose diagnosis through a tenant-scoped read-only API and a human-readable
workspace. The API returns only the deterministic diagnosis projection: rule
version, category, confidence, supporting signal identifiers, warnings, and
abstention information. Raw evidence and secrets remain behind the Phase 6
viewer boundary.

The endpoint requires an active organisation membership and validates the
project belongs to the organisation. The workspace labels results as suspected
hypotheses and shows abstention reasons when evidence is insufficient.

## Consequences

- Diagnosis is available to authorised reviewers without granting remediation permissions.
- The UI cannot silently turn a bounded hypothesis into a confirmed cause.
- Every API response remains tenant-scoped and bounded.
