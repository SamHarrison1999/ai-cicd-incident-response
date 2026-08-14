# Phase 6 Progress

## Goal

Store, search, link, and present bounded technical evidence for tenant-scoped incident investigation while preserving provenance, redaction, retention, and authorization boundaries.

Phase 6 consumes the Phase 5 incident and timeline models. It does not introduce AI diagnosis, unsupported causal claims, autonomous remediation, or production provider integrations.

## Batches

| Batch | Scope | Status |
|---|---|---|
| 1 | Evidence and log model, provenance, retention, and viewer boundary | COMPLETE_VERIFIED |
| 2 | Evidence persistence, redaction, and retention enforcement | COMPLETE_VERIFIED |
| 3 | Tenant-scoped evidence search and incident/event linking | COMPLETE_VERIFIED |
| 4 | Evidence viewer and investigation workspace | COMPLETE_VERIFIED |
| 5 | Security, end-to-end, documentation, and Phase 6 verification | IN_PROGRESS |

## Batch 1 acceptance criteria

- Evidence ownership is constrained by organisation and project.
- Evidence kinds, provenance, timestamps, hashes, and retention classes are explicit.
- Content is bounded, redacted before indexing, and treated as untrusted input.
- Incident, event, and pipeline links preserve the tenant boundary.
- Viewer outputs exclude signatures, secrets, credentials, and unbounded payloads.
- Phase 6 remains evidence presentation and storage, not AI diagnosis or remediation.

## Phase 6 completion criteria

Phase 6 is complete only when all five batches are COMPLETE_VERIFIED, cumulative Java, frontend, repository, and Compose quality gates pass, tenant isolation and redaction are covered by tests, and the final Phase 6 pull request is merged into main.

## Batch 1 implementation record

Batch 1 establishes the typed evidence, provenance, retention, redaction, linking, and viewer boundaries for the executable implementation batches.

## Batch 1 verification record

Developer-supplied output confirmed on 14 August 2026:

- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories were tracked.
- Git whitespace validation passed.
- The tenant-scoped evidence model, provenance fields, retention classes, redaction boundary, evidence links, and viewer exclusions were documented.
- Phase 6 Batch 1 kickoff verification completed successfully.

## Batch 2 implementation record

Batch 2 adds the tenant-owned evidence item, bounded redaction before hashing, deterministic content hashes, retention classes, migration V7, and service-level tenant checks. Search and viewer APIs remain outside this batch.

## Batch 2 acceptance criteria

- Evidence is persisted only inside an organisation and project boundary.
- Secret-like values, bearer tokens, and signatures are redacted before storage.
- Content hashes are deterministic lowercase SHA-256 values.
- Content and line limits are enforced before persistence.
- Retention expiry is calculated deterministically from the ingested timestamp.

## Batch 2 verification record

Developer-supplied output confirmed on 14 August 2026:

- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories were tracked.
- Git whitespace validation passed.
- Java formatting, tests, analysis, coverage, and `bootJar` passed.
- Docker Compose configuration validation passed.
- Tenant-scoped evidence persistence, redaction before hashing, deterministic SHA-256 content hashing, content bounds, and retention-boundary calculation were verified.

## Batch 3 implementation record

Batch 3 adds tenant-scoped evidence metadata search, deterministic cursor pagination, and explicit evidence links to incidents and normalised CI events. Raw evidence content remains outside the search response and is reserved for Batch 4.

## Batch 3 acceptance criteria

- Search requires active membership and validates the requested project inside the organisation.
- Search filters are bounded and ordered by `occurredAt DESC, id DESC`.
- Cursor values are opaque, deterministic, and reject malformed input.
- Search responses exclude raw evidence content.
- Evidence-to-incident and evidence-to-event links enforce the same organisation and project boundary.
- Link creation requires an incident-writer role and records an audit event.

## Batch 3 verification record

Developer-supplied output confirmed on 14 August 2026:

- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories were tracked.
- Git whitespace validation passed.
- Java formatting passed.
- All Java tests passed, including evidence search, cursor, metadata contract, and linking tests.
- `clean check bootJar` completed successfully.
- Docker Compose configuration validation passed.
- Tenant-scoped evidence search, bounded metadata responses, deterministic cursor pagination, and incident/event link boundaries were verified.

## Batch 4 implementation record

Batch 4 adds the tenant-scoped evidence detail projection and frontend investigation workspace. The viewer returns only persisted, bounded, redacted content plus incident and event identifiers.

## Batch 4 acceptance criteria

- Viewer access requires active membership and validates organisation/project/evidence ownership.
- Viewer content is bounded and remains redacted before presentation.
- Viewer responses expose no raw webhook payload, signature, credential, or secret material.
- Evidence search and cursor pagination remain available from the workspace.
- Evidence selection loads one detail projection without changing tenant scope.
- The workspace remains evidence inspection, not AI diagnosis or remediation.

## Batch 4 verification record

Developer-supplied output confirmed on 14 August 2026:

- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories were tracked.
- Git whitespace validation passed.
- Java formatting, tests, analysis, coverage, and `bootJar` passed.
- Frontend formatting, ESLint, twelve frontend tests, and production build passed.
- Docker Compose configuration validation passed.
- Tenant-scoped evidence detail access and redacted viewer content were verified.
- The evidence investigation workspace passed route, search, selection, pagination, and viewer contract checks.
## Batch 5 implementation record

Batch 5 closes Phase 6 with explicit security controls, synthetic evidence-to-incident end-to-end scenarios, cumulative quality-gate instructions, and the final verification boundary. It does not introduce AI diagnosis, causal certainty, autonomous remediation, or production provider integrations.

## Batch 5 acceptance criteria

- Tenant isolation, role enforcement, redaction, bounded content, provenance, and retention controls are covered by the cumulative verification plan.
- Synthetic evidence flows cover persistence, metadata search, incident/event linking, viewer access, duplicate processing, and cross-tenant rejection.
- Security evidence excludes raw webhook payloads, signatures, credentials, access tokens, and unbounded content from persistence, indexing, API, and frontend outputs.
- Java, frontend, repository, and Docker Compose gates are repeatable from a clean checkout.
- Phase 6 remains evidence storage and presentation, not AI diagnosis or autonomous remediation.