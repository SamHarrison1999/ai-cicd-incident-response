# ADR 0038: Historical retrieval API boundary

## Status

Accepted for Phase 8 Batch 3.

## Decision

Historical retrieval is exposed as a read-only, tenant-scoped API. Filters are
explicit, page sizes are bounded, and continuation cursors contain only the
stable ordering key. The response exposes bounded summaries, match explanations,
source identity, and provenance references.

## Consequences

- API callers cannot request raw evidence or cross-project records.
- Invalid filters and cursors fail closed with a bounded client error.
- Stable cursor ordering prevents duplicate or skipped records between pages.
