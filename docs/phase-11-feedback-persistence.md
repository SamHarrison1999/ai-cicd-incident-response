# Phase 11 Batch 2: feedback persistence and aggregation

This batch adds immutable feedback signals and deterministic aggregate projections. Signals preserve the organisation, project, recommendation, review action, policy version, and event time. Aggregates report bounded counts and a suppression reason when the sample is too small.

The implementation is advisory and provider-neutral. It does not train models or execute operational changes.
