# Phase 6 evidence model

## Goal

Store and present bounded technical evidence that helps a human inspect an
incident without exposing secrets or implying unsupported causal conclusions.

## Evidence item

Each evidence item is owned by one organisationId and one projectId. The
planned projection contains id, organisationId, projectId, kind, sourceSystem,
sourceReference, occurredAt, ingestedAt, contentHash, content, retentionClass,
and createdAt.

The model distinguishes when an observation occurred from when it was ingested.
It does not claim that an observation proves causality.

## Initial classifications

- LOG_EXCERPT
- TRACE_OBSERVATION
- DEPLOYMENT_RECORD
- EVENT_SNAPSHOT
- STATUS_CHANGE

Unsupported classifications are rejected at the boundary. Provider payloads
remain untrusted input and are mapped into typed fields only after validation
and redaction.

## Links and viewer contract

Evidence may link to an incident, normalised CI/CD event, pipeline run, or
source reference. Every link carries the same organisation and project ownership.

The viewer may show evidence kind, source label, timestamps, bounded redacted
content, content hash, provenance reference, and authorised linked identifiers.
Raw webhook signatures, secret values, credentials, access tokens, unbounded
payloads, and hidden policy internals are excluded.

## Retention and exclusions

Retention class is explicit and auditable. Redaction occurs before indexing.
Content size, line count, and source-reference limits are enforced before storage.
A rejected item is not partially indexed.

Phase 6 does not introduce AI diagnosis, retrieval ranking, causal certainty,
production provider integrations, autonomous remediation, or a claim that
evidence storage improves operational reliability.