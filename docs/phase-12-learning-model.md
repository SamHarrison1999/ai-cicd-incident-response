# Phase 12 learning and trend model

Phase 12 builds on the governed feedback signals from Phase 11. It defines a provider-neutral model for operational learning: recurring failure themes, diagnosis and recommendation outcomes, review effort, resolution patterns, and bounded trend changes over explicit observation windows.

## Contract

- Every output is scoped to one organisation and project.
- Source references, aggregation version, observation window, sample size, and suppression state are retained.
- Ordering is deterministic and stable across repeated reads.
- Raw evidence, review comments, secrets, provider prompts, and credentials are not part of a learning response.
- Trend output is advisory and cannot mutate incidents, policy, providers, or production systems.
- Insufficient, ambiguous, stale, or conflicting samples are suppressed or clearly qualified.

## Initial work packages

1. Operational-learning contract, trend dimensions, and tenant boundary.
2. Trend persistence and deterministic observation windows.
3. Bounded trend API and comparison responses.
4. Operational-learning workspace.
5. Security, end-to-end scenarios, documentation, and Phase 12 verification.
