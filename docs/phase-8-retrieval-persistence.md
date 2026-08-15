# Phase 8 historical retrieval persistence

Batch 2 adds the bounded persistence projection used by historical retrieval.
Each record represents sanitised, reviewable context from an incident, pipeline
run, evidence item, or diagnosis result.

## Stored fields

Records retain tenant identifiers, source identity, occurred time, provider and
pipeline dimensions, diagnosis category, a bounded summary, a match explanation,
and a provenance reference. Raw payloads, signatures, credentials, and prompt
instructions are not stored in this projection.

## Query boundary

The query service accepts explicit filters and returns a deterministic slice
ordered by `occurred_at DESC, id DESC`. A maximum page size is enforced and
cursor pagination is added by the following API batch.
