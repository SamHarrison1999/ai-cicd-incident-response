# Phase 3 Progress

## Goal

Add secure, replay-resistant, idempotent CI/CD event ingestion, provider-neutral event normalisation, pipeline-run tracking, and the foundations required for later incident correlation.

## Batches

| Batch | Scope | Status |
|---|---|---|
| 1 | Event-ingestion architecture, schemas, and security decisions | COMPLETE_VERIFIED |
| 2 | Event-source and webhook-delivery persistence model | COMPLETE_VERIFIED |
| 3 | Signed webhook verification and idempotent ingestion | COMPLETE_VERIFIED |
| 4 | Provider adapters and normalised event processing | COMPLETE_VERIFIED |
| 5 | Pipeline-run APIs, frontend workspace, and simulations | NOT_STARTED |
| 6 | Security tests, documentation, observability, and Phase 3 verification | NOT_STARTED |

## Batch 1 verification record

Developer-supplied output confirmed on 12 August 2026:

- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories were tracked.
- Git whitespace validation passed.
- `git diff --check` produced no output.
- Seven documentation files were committed in `fed1275` and pushed.

## Batch 2 acceptance criteria

- Flyway V3 creates event sources, webhook deliveries, pipeline runs, and normalised CI events.
- Composite foreign keys enforce organisation and project ownership at the database boundary.
- The signing secret is represented only by an opaque secret reference.
- Exact-payload SHA-256 hashes are stored without raw payloads or signatures.
- `(event_source_id, provider_delivery_id)` enforces webhook idempotency.
- `(event_source_id, external_run_id, attempt)` uniquely identifies pipeline attempts.
- One delivery can produce at most one normalised event.
- JPA entities, enums, and tenant-scoped repositories match the migration.
- Domain tests cover invalid secret references, invalid payload hashes, and terminal-run regression.
- Testcontainers tests cover persistence, uniqueness, graph relationships, and tenant-scoped queries.
- The data-model documentation records constraints, trust boundaries, and retention decisions.
- Phase 1 and Phase 2 tests remain green.
- Developer-supplied verification output is recorded before Batch 2 is marked verified.

## Batch 2 verification record

Developer-supplied output confirmed on 13 August 2026:

- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories were tracked.
- Git whitespace validation passed.
- Google Java formatting completed successfully.
- Flyway V3 applied and Hibernate validated the persistence model.
- All 32 Java tests passed, including 7 new Batch 2 tests.
- The executable Spring Boot JAR was built successfully.
- `git diff --check` produced no output.

## Batch 3 acceptance criteria

- The webhook endpoint is public only because the request is authenticated by its HMAC signature.
- Delivery ID, event type, exact timestamp header, and exact payload bytes are bound into a versioned signature.
- HMAC-SHA-256 comparison uses a constant-time operation.
- Timestamp tolerance rejects stale and implausibly future requests.
- Event-source secrets are resolved from opaque references and are not persisted, returned, or logged.
- Disabled and unknown event sources use the same not-found response.
- Content type, header length, control characters, payload size, empty bodies, and JSON syntax are validated.
- Payload bytes are bounded before full request-body consumption.
- The first verified delivery is inserted atomically at the database idempotency boundary.
- Retries with the same delivery ID, event type, and exact payload bytes return the original deterministic acceptance response without another insert.
- Delivery-ID reuse with a different event type or payload returns a conflict.
- Raw payloads and supplied signatures are not stored.
- Security, API, setup, event-schema, and ingestion documentation reflect the implemented contract.
- Unit and Testcontainers integration tests cover valid signatures, metadata tampering, stale requests, duplicates, conflicts, size limits, malformed JSON, and disabled sources.
- Developer-supplied verification output is recorded before Batch 3 is marked verified.

## Batch 3 verification record

Developer-supplied output confirmed on 13 August 2026:

- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories were tracked.
- Git whitespace validation passed.
- Java formatting completed successfully.
- All 49 Java tests passed, including the Batch 3 webhook tests.
- `check` and `bootJar` completed successfully.
- Docker Compose configuration validation passed.
- Webhook signature, metadata binding, timestamp tolerance, payload limits, JSON validation, idempotency, and conflict handling all passed.

## Phase 3 completion criteria

Phase 3 is complete only when all six batches are `COMPLETE_VERIFIED`, local and GitHub Actions quality gates pass, signed webhook simulations are repeatable, and the final Phase 3 pull request is merged into `main`.

## Batch 4 implementation record

Batch 4 implementation has been applied and remains `IN_PROGRESS` until developer-supplied verification output confirms the provider adapter, normalisation, persistence, and regression test criteria.

## Batch 4 acceptance criteria

- GitHub Actions and Jenkins payloads are mapped through provider-specific adapters.
- Provider adapters return provider-neutral pipeline candidates without persisting raw payloads.
- Pipeline runs are upserted by event source, external run ID, and attempt.
- Terminal pipeline states cannot regress from later or duplicate events.
- Normalised events retain safe source-field names and evidence summaries.
- Unsupported provider event types are recorded as processed without fabricating a normalised event.
- Existing signed-ingestion and persistence tests remain green.

## Batch 4 verification record

Developer-supplied output confirmed on 13 August 2026:

- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories were tracked.
- Git whitespace validation passed.
- Java formatting passed.
- All Java tests passed, including provider adapter tests and existing regression tests.
- Checkstyle passed.
- `clean check bootJar` completed successfully.
- Docker Compose configuration validation passed.
- GitHub Actions and Jenkins provider adapter tests passed.
- Webhook ingestion and persistence regression tests passed.