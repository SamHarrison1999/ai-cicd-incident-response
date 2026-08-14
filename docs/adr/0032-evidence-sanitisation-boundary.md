# ADR 0032: Evidence sanitisation boundary

## Status

Accepted for Phase 7 Batch 2.

## Decision

Evidence is sanitised immediately before it becomes an input to diagnosis.
Existing secret redaction and content bounds remain mandatory. In addition,
instruction-like text is treated as untrusted data and replaced with an
explicit safe marker rather than being interpreted as an instruction.

The sanitiser has a stable version identifier and emits deterministic warning
codes. The evidence service invokes it before content hashing and persistence,
so later consumers receive the same bounded representation.

## Constraints

- Sanitisation does not weaken tenant, project, or provenance checks.
- The sanitiser does not make causal claims or classify an incident.
- A warning is not evidence that an attack occurred; it records a boundary
  transformation applied to untrusted input.
- Raw content, signatures, credentials, access tokens, and secret values do
  not cross the persisted evidence boundary.
