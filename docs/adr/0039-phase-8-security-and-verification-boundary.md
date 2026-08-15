# ADR 0039: Phase 8 security and verification boundary

## Status

Accepted for Phase 8 Batch 5.

## Decision

Historical retrieval remains a read-only decision-support capability. Every
query is authorised against the organisation membership and project boundary;
every result is bounded, sanitised, provenance-linked, and explicitly marked as
historical context. The system does not infer causality, disclose raw material,
or trigger remediation from a retrieved record.

## Verification scenarios

- authorised retrieval returns only the requested tenant and project;
- cross-tenant and unknown-project queries fail closed;
- invalid filters, oversized limits, and malformed cursors are rejected;
- empty and ambiguous matches remain explicit;
- secrets, signatures, and raw unsafe evidence do not appear in responses;
- deterministic ordering is stable across repeated requests.
