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
| 5 | Security, end-to-end, documentation, and Phase 4 verification | IN_PROGRESS |

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
## Batch 2 implementation record

Batch 2 adds the persistence indexes and monotonic aggregation behaviour required for deterministic timeline reads. Older evidence is retained as history but cannot move a pipeline projection backwards.

## Batch 2 acceptance criteria

- Timeline ordering has database support for organisation, project, occurred-at, received-at, and stable event identity.
- Older events do not regress the pipeline-run projection or fail the transaction.
- Later non-terminal events cannot regress terminal pipeline runs.
- Pipeline-run attempt identity remains source-scoped and deterministic.
- Regression tests cover late evidence and terminal-state protection.
## Batch 3 implementation record

Batch 3 adds the tenant-scoped pipeline timeline endpoint with allow-listed filters, deterministic cursor pagination, stable ordering, and malformed-query rejection.

## Batch 3 acceptance criteria

- Timeline reads require active organisation membership and a project belonging to that organisation.
- Status, branch, commit, environment, event-type, and time-range filters are supported.
- Results are ordered by occurred-at, received-at, and event ID without duplicate page boundaries.
- Cursor values are opaque, contain the complete ordering position, and reject malformed input.
- The API returns bounded safe event summaries and never raw payloads or signing material.
- Cursor and filter behaviour has unit coverage and remains compatible with the Phase 3 pipeline-run API.
## Batch 4 implementation record

Batch 4 adds the accessible pipeline timeline workspace, filter controls, run summaries, event cards, loading/error/empty states, and cursor-based loading of additional events.

## Batch 4 acceptance criteria

- Users can enter an organisation and project scope before data is queried.
- Timeline filters are represented by accessible controls and update the read-model query.
- Pipeline runs and canonical events are presented separately but share the same tenant scope.
- Loading, error, empty, and pagination states are visible and actionable.
- The frontend never displays raw provider payloads or signing material.
- API query construction has frontend test coverage and the production build remains green.
## Batch 5 implementation record

Batch 5 provides the cumulative Phase 4 verification boundary. It confirms deterministic ordering, tenant-scoped filters, cursor pagination, frontend query behaviour, documentation, and the absence of raw secrets or generated artefacts.

## Batch 5 acceptance criteria

- All Phase 4 Java, frontend, repository, and Compose quality gates pass with individual command exit checks.
- Timeline API filters, cursor pagination, tenant boundaries, and safe response fields are documented and tested.
- The frontend timeline workspace passes formatting, linting, tests, and production build checks.
- No raw payloads, signatures, signing material, generated dependency directories, or report directories are tracked.
- The final Phase 4 verification output is recorded before all five batches are marked `COMPLETE_VERIFIED`.