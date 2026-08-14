# ADR 0030: Phase 6 security and verification boundary

## Status

Accepted for Phase 6 close-out.

## Decision

Phase 6 treats evidence as bounded, tenant-owned technical observation. The
release boundary is verified across persistence, search, links, viewer output,
frontend rendering, and cumulative quality gates.

The final verification must demonstrate:

- organisation and project checks before evidence access;
- redaction before hashing, persistence, indexing, and presentation;
- bounded content and deterministic provenance fields;
- safe duplicate handling and cross-tenant rejection;
- role-aware incident and link operations; and
- repeatable Java, frontend, repository, and Compose checks.

## Explicit exclusions

Phase 6 does not claim causal certainty, add AI diagnosis, rank evidence with a
model, execute remediation, or connect production provider systems. Evidence
supports human inspection and remains subject to the existing authorization and
redaction boundaries.

## Consequences

The cumulative verifier is a release gate, while synthetic scenarios provide a
repeatable demonstration of the evidence-to-investigation path. Future AI
features must cite authorised immutable evidence and abstain when evidence is
insufficient.
