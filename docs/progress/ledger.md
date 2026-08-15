# Progress Ledger

## Status vocabulary

- `NOT_STARTED`
- `IN_PROGRESS`
- `BLOCKED`
- `COMPLETE_UNVERIFIED`
- `COMPLETE_VERIFIED`

A phase cannot be `COMPLETE_VERIFIED` until Samuel provides or records the required command output.

## Phase ledger

| Phase | Status | Deliverables | Verification evidence |
|---|---|---|---|
| 0 — Scope and architecture | COMPLETE_UNVERIFIED | Scope, architecture, repository plan, backlog, risk register, testing strategy, limitations, and ADRs 0001–0005 | Awaiting `git diff --check` and file-list output |
| 1 — Repository and CI foundation | COMPLETE_VERIFIED | Java, Python, React, Compose, CI, formatting, linting | — |
| 2 — Identity, organisations and projects | COMPLETE_VERIFIED | Authentication, roles, tenant model, frontend shell | — |
| 3 — Event ingestion | COMPLETE_VERIFIED | Event sources, signatures, schemas, idempotency | — |
| 4 — Pipeline runs and timeline | COMPLETE_VERIFIED | Normalisation, aggregation, timeline API/UI | — |
| 5 — Incident correlation | COMPLETE_VERIFIED | Correlation engine, state machine, incident UI | — |
| 6 — Logs and evidence | COMPLETE_VERIFIED | Log fragments, search, citations, viewer | — |
| 7 — Deterministic diagnosis | COMPLETE_VERIFIED | Redaction, injection checks, rules, tests | — |
| 8 — Historical retrieval | COMPLETE_VERIFIED | Reviewed resolution store and similarity retrieval | — |
| 9 - AI recommendations | COMPLETE_VERIFIED | Provider abstraction, evidence-grounded recommendations, and human-review safety | - |
| 10 - Human review | IN_PROGRESS | Accept, edit, reject, feedback, and resolution workflow | - |
| 11 — Evaluation | NOT_STARTED | Golden data, metrics, dashboard | — |
| 12 — Observability and simulation | NOT_STARTED | Generator, logs, metrics, traces, dashboards | — |
| 13 — Security hardening | NOT_STARTED | Threat model, limits, scans, adversarial tests | — |
| 14 — Deployment and release | NOT_STARTED | Images, deployment, demo, case study | — |

## Phase 0 decisions

- Use a Java modular monolith for the control plane.
- Isolate AI concerns in a Python service behind a versioned contract.
- Use PostgreSQL as the initial system of record and retrieval substrate.
- Require evidence-grounded, confidence-scored, human-reviewed recommendations.
- Use a signed, versioned event envelope with source-scoped idempotency.

## Next phase entry criteria

- Phase 0 files are committed.
- `git diff --check` reports no whitespace errors.
- Samuel confirms the chosen repository name or accepts `ai-cicd-incident-response-platform`.

## Phase 9 current boundary

Phase 9 is in progress. It introduces provider-neutral, evidence-grounded
recommendations with bounded inputs and outputs, auditable provenance,
confidence, abstention, and an explicit non-remediation boundary. The detailed
batch status is maintained in `docs/progress/phase-9.md`.
## Phase 10 current boundary

Phase 10 is in progress. It introduces attributable human review, immutable generated content, bounded feedback, reviewed versions, and final resolution eligibility without executing remediation. The detailed batch status is maintained in `docs/progress/phase-10.md`.