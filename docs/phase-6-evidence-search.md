# Phase 6 evidence search and linking

Batch 3 adds tenant-scoped evidence search, bounded metadata responses,
deterministic cursor pagination, and explicit evidence links to incidents and
normalised CI events.

Search filters include evidence kind, source system, bounded text matching,
and occurred-at ranges. Every query is constrained by organisation and project
before repository access. The search response intentionally excludes the raw
redacted content; Batch 4 owns the evidence viewer projection.

Link records retain organisation and project identifiers and use composite
foreign keys to prevent cross-tenant associations. The service records link
creation in the existing audit stream.
