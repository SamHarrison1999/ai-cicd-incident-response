# ADR 0056: Trend persistence and observation-window boundary

## Status

Accepted for Phase 12 Batch 2.

## Decision

Persist only bounded operational trend projections derived from governed platform records. Each projection is scoped by organisation and project, records its dimension, explicit observation window, aggregation version, sample count, source reference, and suppression state.

Observation windows are immutable once persisted. Rebuilding a projection creates a new versioned record rather than mutating historical learning output. Ordering is deterministic by window end, dimension, key, and identifier.

## Safety boundary

Trend records contain aggregate metadata only. Raw evidence, review comments, secrets, provider prompts, credentials, model-training payloads, and remediation controls are excluded. Insufficient, ambiguous, or stale samples are persisted as suppressed metadata rather than exposed as actionable signals.
