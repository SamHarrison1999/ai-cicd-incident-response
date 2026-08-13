# Phase 4 Progress

## Goal

Complete the provider-neutral pipeline read model introduced in Phase 3 by defining a versioned canonical event contract, deterministic pipeline-run aggregation, and a tenant-scoped, searchable timeline experience.

Phase 4 is a read-model and product-surface phase. It does not introduce incident correlation, log storage, AI diagnosis, or remediation. Those capabilities remain in later phases.

## Batches

| Batch | Scope | Status |
|---|---|---|
| 1 | Canonical event and timeline contract | IN_PROGRESS |
| 2 | Timeline persistence and deterministic run aggregation | NOT_STARTED |
| 3 | Tenant-scoped timeline API, filters, and pagination | NOT_STARTED |
| 4 | Pipeline timeline frontend workspace | NOT_STARTED |
| 5 | Security, end-to-end, documentation, and Phase 4 verification | NOT_STARTED |

## Batch 1 acceptance criteria

- A versioned canonical event model defines identity, source metadata, project scope, run identity, lifecycle status, ordering, and safe summary fields.
- Timeline ordering is deterministic for equal timestamps and remains stable across repeated reads.
- Pipeline-run aggregation rules distinguish attempts, retries, lifecycle transitions, and terminal states.
- The timeline contract defines filtering dimensions for status, branch, commit, environment, event type, and time range.
- Cursor pagination is preferred for the timeline read model; page boundaries must not duplicate or omit events.
- Tenant ownership and the Phase 3 raw-payload retention boundary remain explicit.
- The Phase 4 scope excludes incident correlation and AI-generated conclusions.
- Developer-supplied verification output is recorded before Batch 1 is marked verified.

## Phase 4 completion criteria

Phase 4 is complete only when all five batches are `COMPLETE_VERIFIED`, the cumulative local and GitHub Actions quality gates pass, timeline filters and pagination are covered by tests, and the final Phase 4 pull request is merged into `main`.

## Batch 1 implementation record

Batch 1 establishes the canonical event, pipeline-run, and timeline boundaries for the implementation batches that follow.