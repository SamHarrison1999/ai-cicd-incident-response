# ADR 0037: Historical retrieval persistence boundary

## Status

Accepted for Phase 8 Batch 2.

## Decision

Historical retrieval uses a tenant-scoped, sanitised retrieval-record projection.
The projection stores bounded metadata and provenance rather than raw evidence,
secrets, signatures, or model-generated embeddings. Records are immutable from
the retrieval service's perspective and are ordered by occurred time and ID.

## Consequences

- Historical queries remain read-only and deterministic.
- Organisation and project ownership are represented in every record.
- Source identity and provenance remain available for human review.
- Later ranking strategies can be added without changing the incident or
  evidence persistence boundaries.
