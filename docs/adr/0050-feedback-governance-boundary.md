# ADR 0050: feedback governance and learning boundary

## Status

Accepted for Phase 11 Batch 1.

## Decision

Treat human review outcomes as bounded governance signals for product and reliability learning. Accepted, edited, rejected, and resolved recommendations may be aggregated for reporting and evaluation, but they must not silently retrain providers, change production policy, or execute remediation.

Feedback records remain tenant-scoped, attributable, versioned, and explainable. Aggregates must preserve source counts, time windows, policy versions, and abstention behaviour. Small or ambiguous samples remain suppressed or explicitly marked as insufficient evidence.

## Consequences

- Learning signals are separated from operational actions and model updates.
- Cross-tenant aggregation is prohibited unless an explicit, documented anonymisation boundary is introduced later.
- Human review remains authoritative for a decision, while feedback analytics remain advisory.
