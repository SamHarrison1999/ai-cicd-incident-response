# ADR 0051: feedback persistence and deterministic aggregation

Feedback signals are persisted as immutable, tenant-scoped projections of human review outcomes. Aggregates are calculated deterministically from bounded source records and carry the aggregation policy version and sample window.

Small samples are suppressed rather than presented as reliable quality claims. Aggregation never mutates review records, recommendation state, provider configuration, or incident state.
