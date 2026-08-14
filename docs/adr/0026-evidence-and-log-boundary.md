# ADR 0026: Tenant-scoped evidence and log boundary

## Decision

Phase 6 introduces an evidence boundary between the canonical timeline,
incident correlation, and later diagnosis features. Evidence is tenant-owned,
append-oriented, provenance-aware, bounded in size, and treated as untrusted input.

An evidence item carries organisation and project scope, source classification,
occurred-at time, ingestion time, content hash, provenance reference, and
retention class.

## Security boundary

Evidence access requires active organisation membership and a project predicate.
Incident and normalised-event links are resolved inside the same tenant boundary.
A globally unique evidence identifier is not sufficient to authorise access.

Raw webhook signatures, secret values, provider credentials, access tokens,
unbounded payloads, and unredacted sensitive content are not viewer outputs.
Redaction and content-size limits apply before persistence or indexing.

## Consequences

- Later search and viewer work can rely on a stable typed contract.
- Evidence provenance remains distinguishable from causal interpretation.
- Retention decisions can be audited without retaining secrets.
- AI diagnosis remains a later phase and cannot silently redefine evidence.