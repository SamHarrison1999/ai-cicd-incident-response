# Phase 5 Progress

## Goal

Correlate related normalised CI/CD failure events into tenant-scoped incidents and expose a deterministic, auditable incident state machine.

Phase 5 consumes the Phase 4 canonical timeline. It does not introduce production CI/CD integrations, autonomous remediation, unsupported causal claims, or AI-generated conclusions.

## Batches

| Batch | Scope | Status |
|---|---|---|
| 1 | Incident correlation contract, policy, and state machine | COMPLETE_VERIFIED |
| 2 | Incident persistence and lifecycle domain model | COMPLETE_VERIFIED |
| 3 | Deterministic correlation engine and audit decisions | IN_PROGRESS |
| 4 | Tenant-scoped incident API and frontend workspace | NOT_STARTED |
| 5 | Security, end-to-end, documentation, and Phase 5 verification | NOT_STARTED |

## Batch 1 acceptance criteria

- The correlation input boundary is limited to bounded normalised event fields.
- Organisation and project are mandatory candidate gates.
- Policy version, score dimensions, threshold, candidates, and result are auditable.
- Candidate selection and tie-breaking are deterministic.
- An event has at most one primary incident association in version 1.
- The incident state machine defines valid transitions and rejects invalid transitions.
- Raw payloads, signatures, signing material, causal certainty, production readiness, and remediation remain outside scope.
- Developer-supplied verification output is recorded before Batch 1 is marked verified.

## Phase 5 completion criteria

Phase 5 is complete only when all five batches are `COMPLETE_VERIFIED`, cumulative Java, frontend, repository, and Compose quality gates pass, tenant isolation and deterministic correlation are covered by tests, and the final Phase 5 pull request is merged into `main`.

## Batch 1 implementation record

Batch 1 establishes the policy and lifecycle boundaries for the implementation batches that follow. It contains no executable correlation implementation.
## Batch 1 verification record

Developer-supplied output confirmed on 14 August 2026:

- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories were tracked.
- Git whitespace validation passed.
- Incident correlation boundaries, policy versioning, deterministic selection, primary incident membership, state transitions, and safety exclusions were documented.
- Phase 5 Batch 1 kickoff verification completed successfully.
## Batch 2 implementation record

Batch 2 adds the tenant-owned incident aggregate, explicit lifecycle transitions, one-primary-incident event links, repositories, and migration V5. Correlation scoring remains reserved for Batch 3.

## Batch 2 acceptance criteria

- Incident rows are scoped to an organisation and project.
- Valid lifecycle transitions match the Phase 5 contract.
- Invalid transitions are rejected without changing state.
- Reopening preserves incident identity and clears the active resolution timestamp.
- A normalised event can be linked to at most one primary incident.
- Java tests cover valid lifecycle progression and invalid transition rejection.
- Database constraints reinforce tenant ownership and event-link uniqueness.
## Batch 2 verification record

Developer-supplied output confirmed on 14 August 2026:

- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories were tracked.
- Git whitespace validation passed.
- Java formatting passed.
- All Java tests passed, including incident lifecycle tests.
- `check` and `bootJar` completed successfully.
- Docker Compose configuration validation passed.
- Incident persistence, tenant ownership, lifecycle transitions, reopening, and primary event-link constraints were verified.
## Batch 3 implementation record

Batch 3 adds the deterministic policy v1 engine, tenant and project gates, stable candidate ordering, bounded decision persistence, and audit recording.

## Batch 3 acceptance criteria

- Failure eligibility and policy weights are explicit and tested.
- Candidate selection is deterministic for equal scores.
- Cross-tenant candidates are excluded before scoring.
- Resolved incidents are not eligible candidates.
- Decisions are idempotent on event identity and contain bounded metadata only.
- Correlation policy version and audit action are recorded.