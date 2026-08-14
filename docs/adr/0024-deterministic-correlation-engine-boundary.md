# ADR 0024: Keep correlation policy deterministic and audit-safe

## Status

Accepted for Phase 5 Batch 3.

## Context

Batch 2 provides tenant-owned incidents and a primary event-link constraint. Correlation now needs to select a candidate consistently while preserving enough bounded metadata for an engineer to understand the result.

## Decision

1. Policy `incident-correlation-v1` scores explicit dimensions with fixed weights and a fixed threshold.
2. Organisation and project equality are hard gates before scoring.
3. Only failure, cancellation, and timeout pipeline statuses are eligible in this batch.
4. Ties are resolved by descending score, ascending incident detection time, and ascending incident ID.
5. Each event produces at most one persisted decision, enforced by a unique event constraint.
6. Decisions store policy, score, threshold, matched dimensions, and candidate IDs. Raw payloads and signature material are excluded.
7. An audit event records the result, but the engine makes no causal or remediation claim.

## Consequences

- Synthetic fixtures can reproduce every decision branch.
- Policy changes are visible as version changes rather than silent behaviour changes.
- The engine can be wired to event processing without coupling it to provider payloads.
- Later batches can expose decisions through tenant-scoped APIs and incident views.