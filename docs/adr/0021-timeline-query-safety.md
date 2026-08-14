# ADR 0021: Keep timeline queries bounded, tenant-scoped, and safe

## Status

Accepted for Phase 4 Batch 5.

## Decision

Timeline reads must validate the organisation and project boundary before querying. Filters are allow-listed, page limits are bounded, cursors are opaque and validated, and results expose only canonical event fields and bounded evidence summaries.

The timeline does not return raw provider payloads, request signatures, secret references, or arbitrary source fields. The API returns newest events first using occurred-at, received-at, and event-ID ordering. A cursor represents the complete ordering position so a subsequent request cannot silently skip or repeat an event because two events share a timestamp.

## Consequences

The query contract is predictable for the frontend and safe for later incident correlation. It requires explicit validation errors for malformed cursors, unsupported enum filters, invalid time ranges, and out-of-range limits. It also means that future richer search must preserve the same tenant and response-safety boundary.