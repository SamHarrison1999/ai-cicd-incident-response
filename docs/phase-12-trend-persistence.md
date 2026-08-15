# Phase 12 trend persistence

Batch 2 adds the persistence contract for operational-learning projections.

## Persisted projection

An `OperationalTrend` is tenant-scoped and records a provider-neutral dimension, a bounded dimension key, an explicit UTC observation window, a deterministic aggregation version, a sample count, an observed count, a source reference, and a suppression reason.

## Observation rules

- Observations outside the requested organisation, project, dimension, or time window are excluded.
- A projection is ordered deterministically and can be rebuilt without changing the meaning of an earlier record.
- Small, ambiguous, or stale samples remain suppressed.
- Source references identify governed records without exposing raw evidence or review content.
- Persisted learning output is advisory and cannot mutate incidents, policies, providers, or production systems.
