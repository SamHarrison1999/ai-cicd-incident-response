# ADR 0036: Historical retrieval boundary

## Status

Accepted for Phase 8 Batch 1.

## Decision

Phase 8 provides tenant-scoped historical retrieval over authorised incidents,
pipeline timelines, and sanitised evidence. Retrieval is bounded,
deterministic, explainable, and read-only. It may rank prior records using
explicit metadata and signal overlap, but it must not invent similarity,
causality, or resolution effectiveness.

Historical retrieval must preserve organisation and project boundaries, exclude
raw secrets and signatures, retain provenance references, and return an
explicit empty result when no authorised historical match is available.
Model-backed retrieval and recommendations remain outside this initial
contract and belong to later phases.

## Consequences

- Retrieval queries have explicit limits, filters, and stable ordering.
- Each result includes why it matched and which authorised record supplied it.
- Cross-tenant records cannot influence a result.
- Historical context is advisory evidence for human review, not an automatic action.
