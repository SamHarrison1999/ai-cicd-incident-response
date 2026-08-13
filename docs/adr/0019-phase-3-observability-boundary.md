# ADR 0019: Keep ingestion observability low-cardinality and secret-free

## Status

Accepted for Phase 3 Batch 6.

## Decision

Expose low-cardinality Micrometer counters for normalised and unsupported verified deliveries. Do not place delivery IDs, project IDs, provider payload values, signatures, or secret references in metric labels.

## Rationale

High-cardinality or untrusted labels can damage metric systems and disclose sensitive operational data. Counters provide useful health signals while leaving detailed provenance in the tenant-scoped persistence model.

## Consequence

Dashboards can show ingestion health and adapter coverage. Detailed debugging continues through authenticated delivery and normalised-event records.
