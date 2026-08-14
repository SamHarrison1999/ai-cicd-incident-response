# Phase 8 historical retrieval model

Phase 8 retrieves relevant prior incident-response context for human review.
It consumes the Phase 5 incident, Phase 6 evidence, and Phase 7 sanitisation
boundaries.

## Retrieval contract

Retrieval accepts a tenant and project scope plus bounded query dimensions such
as diagnosis category, provider, pipeline, environment, branch, commit, and
time range. Results are ordered deterministically and include a bounded match
explanation, source identifiers, timestamps, and provenance metadata.

## Safety boundary

Retrieval never crosses organisation boundaries, returns secrets or signatures,
or claims that a historical incident caused the current incident. It is a
read-only context operation. Empty and ambiguous matches remain explicit so a
reviewer can decide whether historical context is useful.

## Phase 8 batches

1. Contract, ranking, and tenant boundary.
2. Retrieval persistence and query service.
3. Historical retrieval API, filters, and pagination.
4. Retrieval workspace and incident context presentation.
5. Security, end-to-end scenarios, documentation, and verification.
