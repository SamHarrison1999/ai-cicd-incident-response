# ADR 0031: Sanitisation and deterministic diagnosis boundary

## Status

Accepted for Phase 7 Batch 1 kickoff.

## Decision

Phase 7 introduces a bounded sanitisation and deterministic diagnosis boundary
between tenant-authorised evidence and future human-review outputs.

Sanitisation is applied to untrusted technical content before it can become a
diagnostic input. The boundary removes or marks secret-like material, limits
size and structure, preserves provenance, and treats log text as data rather
than instructions.

Deterministic diagnosis is a versioned rule boundary. It may classify observed
signals, identify missing evidence, calculate a bounded confidence value, and
abstain when evidence is insufficient. It must not invent facts, claim causal
certainty, or execute remediation.

## Security constraints

- Tenant and project ownership are revalidated before evidence is consumed.
- Raw webhook payloads, signatures, credentials, and access tokens remain out
  of diagnosis inputs and outputs.
- Prompt-injection-like instructions in technical content are inert data.
- Sanitisation and diagnosis versions are recorded with any persisted result.
- Inputs and outputs are bounded, deterministic, and auditable.

## Consequences

Later provider or model integrations must consume the sanitised contract rather
than raw evidence. A safe abstention is a successful outcome when the available
evidence cannot support a bounded classification.
