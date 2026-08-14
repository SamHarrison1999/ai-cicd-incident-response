# Phase 7 Progress

## Goal

Sanitise tenant-authorised technical evidence and produce bounded,
deterministic diagnosis outputs for human review while preserving provenance,
tenant isolation, redaction, abstention, and auditability.

Phase 7 consumes the Phase 6 evidence boundary. It does not introduce
unsupported causal claims, autonomous remediation, or production model
provider integrations.

## Batches

| Batch | Scope | Status |
|---|---|---|
| 1 | Sanitisation and deterministic diagnosis contract | COMPLETE_VERIFIED |
| 2 | Evidence sanitisation and prompt-injection defence | COMPLETE_VERIFIED |
| 3 | Deterministic diagnosis rules, confidence, and abstention | COMPLETE_VERIFIED |
| 4 | Diagnosis API and human-readable workspace | NOT_STARTED |
| 5 | Security, end-to-end, documentation, and Phase 7 verification | NOT_STARTED |

## Phase 7 completion criteria

Phase 7 is complete only when all five batches are COMPLETE_VERIFIED, the
cumulative Phase 6 baseline remains green, sanitisation and tenant isolation
are covered by tests, deterministic diagnosis outputs are auditable, and the
final Phase 7 pull request is merged into main.

## Batch 1 implementation record

Batch 1 defines the bounded sanitisation, prompt-injection, deterministic
diagnosis, confidence, provenance, and abstention contracts for later
executable batches.

## Batch 1 acceptance criteria

- Sanitisation treats technical content as untrusted data rather than instructions.
- Secret-like material, credentials, signatures, and access tokens remain outside diagnosis inputs and outputs.
- Sanitised signals preserve authorised evidence provenance and explicit bounds.
- Deterministic diagnosis uses a versioned rule boundary and stable outputs.
- Confidence is bounded and insufficient evidence produces explicit abstention.
- Phase 7 remains human decision support, not causal proof or autonomous remediation.

## Batch 1 verification record

Developer-supplied output confirmed on 14 August 2026:

- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories were tracked.
- Git whitespace validation passed.
- The bounded sanitisation, prompt-injection, deterministic diagnosis, confidence, provenance, and abstention contracts were documented.
- Phase 7 Batch 1 kickoff verification completed successfully.
## Batch 2 implementation record

Batch 2 adds deterministic evidence sanitisation, prompt-injection-like instruction removal, warning codes, sanitiser versioning, and service integration before content hashing and persistence.

## Batch 2 acceptance criteria

- Technical evidence is treated as untrusted data rather than executable instruction.
- Prompt-injection-like lines are replaced with an explicit safe marker.
- Secret redaction and content bounds remain applied before hashing and persistence.
- Sanitisation warnings and version identifiers are deterministic.
- Existing tenant and provenance boundaries remain unchanged.
## Batch 2 verification record

Developer-supplied output confirmed on 14 August 2026:

- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories were tracked.
- Git whitespace validation passed.
- Java formatting, cumulative tests, analysis, coverage, and `bootJar` passed.
- Evidence sanitiser tests passed, including prompt-injection-like instruction removal, deterministic output, secret redaction, and blank-input rejection.
- Frontend formatting, ESLint, twelve frontend tests, and production build passed.
- Docker Compose configuration validation passed.
- EvidenceService sanitised content before hashing and persistence.
## Batch 3 implementation record

Batch 3 adds a versioned deterministic diagnosis engine over sanitised signals. It emits bounded suspected categories, confidence values, supporting signal identifiers, warnings, and explicit abstention reasons without exposing raw evidence.

## Batch 3 acceptance criteria

- Dependency, deployment, configuration, and resource hypotheses use explicit deterministic rules.
- Confidence remains bounded and equal inputs produce equal outputs.
- Empty, unmatched, tied, and over-bound inputs abstain safely.
- Results contain provenance identifiers and no raw content, secrets, or signatures.
## Batch 3 verification record

Developer-supplied output confirmed on 14 August 2026:

- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories were tracked.
- Git whitespace validation passed.
- Java formatting, cumulative tests, analysis, coverage, and `bootJar` passed.
- Determistic diagnosis engine tests passed.
- Deterministic rule selection, bounded confidence, signal provenance, tie abstention, empty-input abstention, and content bounds were verified.
- Frontend formatting, ESLint, twelve frontend tests, and production build passed.
- Docker Compose configuration validation passed.