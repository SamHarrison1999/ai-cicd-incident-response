# ADR 0003: Use PostgreSQL as the initial system of record

- Status: Accepted
- Date: 2026-07-30

## Context

The platform needs relational tenancy, transactions, idempotency, timelines, audits, structured search, and historical retrieval. Introducing separate document, log-search, vector, and relational databases in version 1 would increase operational complexity before scale requirements are measured.

## Decision

Use PostgreSQL as the authoritative datastore for product data, event metadata, bounded log fragments, audit events, evaluation records, and initial historical retrieval.

Use Flyway for schema migrations. Prefer PostgreSQL full-text/trigram capabilities and transparent feature-based retrieval before adding vector infrastructure. A vector extension may be evaluated only when a benchmark demonstrates a need.

## Consequences

### Positive

- Strong transactions for idempotency and audits.
- One datastore for local development and Testcontainers.
- Familiar operational model and excellent query capabilities.
- Avoids premature infrastructure.

### Negative

- Not suitable for unlimited raw log ingestion.
- Text or similarity search may require future specialised storage.
- Large append-only event tables need retention and indexing discipline.

## Guardrails

- Enforce payload limits and retention policies.
- Keep raw, normalised, and sanitised representations explicit.
- Index only measured query paths.
- Introduce another datastore through an ADR supported by benchmark evidence.
