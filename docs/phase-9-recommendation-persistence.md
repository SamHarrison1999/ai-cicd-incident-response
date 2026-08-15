# Phase 9 Batch 2: recommendation persistence and evidence bundles

This batch adds the durable boundary for evidence-grounded recommendations.

## Persisted projection

`recommendations` stores bounded summaries, confidence, status, abstention reasons, and immutable generation provenance. `recommendation_citations` stores links to evidence or historical retrieval records plus a bounded claim.

## Evidence bundle

`EvidenceBundleAssembler` validates tenant and project ownership before returning sanitised evidence summaries and historical retrieval projections. It does not expose signatures, secrets, raw provider requests, or unbounded content to a future provider adapter.

## Safety

Persistence is explanatory only. This batch does not execute actions, change incident state, or infer that a recommendation is a confirmed cause.
