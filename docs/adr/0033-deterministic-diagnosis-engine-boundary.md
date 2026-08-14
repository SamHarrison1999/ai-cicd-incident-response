# ADR 0033: Deterministic diagnosis engine boundary

## Status

Accepted for Phase 7 Batch 3.

## Decision

Diagnosis is implemented as a versioned, deterministic rule engine over
sanitised signals. It returns bounded hypotheses for human review and never
receives raw evidence, secrets, signatures, or credentials.

The engine uses explicit category rules, stable ordering, bounded confidence,
and safe abstention. Equal sanitised signals and the same rule version produce
the same result. Ambiguous or unsupported evidence produces `UNKNOWN` or
`INSUFFICIENT_EVIDENCE` rather than an invented explanation.

## Consequences

- Rule versions are visible in every result.
- Results contain signal identifiers and warnings, never raw content.
- Confidence is constrained to the interval 0.0 through 1.0.
- Tie cases abstain instead of being resolved by incidental collection order.
- Model providers and remediation remain outside this boundary.
