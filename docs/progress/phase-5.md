# Phase 5 Progress

## Goal

Correlate related normalised CI/CD failure events into tenant-scoped incidents and expose a deterministic, auditable incident state machine.

Phase 5 consumes the Phase 4 canonical timeline. It does not introduce production CI/CD integrations, autonomous remediation, unsupported causal claims, or AI-generated conclusions.

## Batches

| Batch | Scope | Status |
|---|---|---|
| 1 | Incident correlation contract, policy, and state machine | IN_PROGRESS |
| 2 | Incident persistence and lifecycle domain model | NOT_STARTED |
| 3 | Deterministic correlation engine and audit decisions | NOT_STARTED |
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