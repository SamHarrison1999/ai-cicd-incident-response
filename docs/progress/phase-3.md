# Phase 3 Progress

## Goal

Add secure, replay-resistant, idempotent CI/CD event ingestion, provider-neutral event normalisation, pipeline-run tracking, and the foundations required for later incident correlation.

## Batches

| Batch | Scope | Status |
|---|---|---|
| 1 | Event-ingestion architecture, schemas, and security decisions | COMPLETE_UNVERIFIED |
| 2 | Event-source and webhook-delivery persistence model | NOT_STARTED |
| 3 | Signed webhook verification and idempotent ingestion | NOT_STARTED |
| 4 | Provider adapters and normalised event processing | NOT_STARTED |
| 5 | Pipeline-run APIs, frontend workspace, and simulations | NOT_STARTED |
| 6 | Security tests, documentation, observability, and Phase 3 verification | NOT_STARTED |

## Batch 1 acceptance criteria

- The event-source, webhook-delivery, normalised-event, and pipeline-run boundaries are explicitly documented.
- Provider payloads are treated as untrusted input.
- GitHub Actions-style and Jenkins-style source contracts are documented.
- A provider-neutral normalised event envelope is defined.
- Event identifiers, delivery identifiers, timestamps, and tenant ownership rules are defined.
- HMAC verification, timestamp tolerance, constant-time comparison, replay protection, and secret handling are documented.
- Idempotency behaviour is defined independently from provider retry behaviour.
- Duplicate deliveries return a deterministic accepted response without repeating side effects.
- Payload-size and content-type limits are defined.
- Pipeline-run lifecycle and status transitions are defined.
- Raw payload retention is minimised and sensitive fields are not copied into normalised records.
- Deterministic ingestion rules are clearly separated from future model-generated analysis.
- ADRs record the normalised event model, webhook verification, and idempotency decisions.
- Phase 3 risks, non-goals, and failure handling are documented.
- Repository whitespace and unfinished-marker checks produce no errors.
- Developer-supplied output is recorded before Batch 1 is marked verified.

## Batch 1 verification record

No verification output has been supplied yet for Batch 1.

## Phase 3 completion criteria

Phase 3 is complete only when all six batches are `COMPLETE_VERIFIED`, local and GitHub Actions quality gates pass, signed webhook simulations are repeatable, and the final Phase 3 pull request is merged into `main`.
