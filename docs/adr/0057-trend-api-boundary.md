# ADR 0057: Bounded operational trend API

## Status

Accepted for Phase 12 Batch 3.

## Decision

Expose read-only operational trend projections through an authenticated organisation and project boundary. The API supports bounded dimension, key, and observation-window filters, deterministic ordering, an opaque cursor, and a comparison response for adjacent persisted projections.

## Safety boundary

Responses contain aggregate counts, windows, dimension metadata, provenance references, and suppression state only. Raw evidence, comments, secrets, provider prompts, credentials, training payloads, policy mutation, and remediation actions are excluded.
