# ADR 0040: Provider-neutral evidence-grounded recommendation boundary

## Status

Accepted for Phase 9.

## Context

The platform now has bounded, tenant-authorised evidence, deterministic
diagnosis, and historical retrieval. The next capability is to produce useful
recommendations for human review while keeping provider integrations replaceable
and preventing model output from becoming an operational command.

An external model or provider must not receive raw secrets, signatures,
unbounded payloads, hidden tenant data, or uncontrolled instructions. Provider
responses must be treated as untrusted data and must not bypass the existing
sanitisation, tenant, provenance, confidence, or abstention boundaries.

## Decision

Phase 9 introduces a provider-neutral recommendation boundary with these rules:

1. Recommendation requests are scoped to one authorised organisation and
   project and reference bounded evidence and retrieval projections only.
2. The provider interface accepts a versioned, sanitised evidence bundle and
   returns a bounded recommendation candidate rather than an executable action.
3. Every candidate records provider identity, model or ruleset version,
   request provenance, evidence references, confidence, and generation time.
4. Low-confidence, conflicting, incomplete, or unsafe results abstain with an
   explicit reason and remain reviewable.
5. Provider output is schema-validated, length-bounded, and treated as
   untrusted content. It cannot alter incidents, pipelines, deployments, or
   infrastructure.
6. Secrets, signatures, raw unsafe payloads, hidden cross-tenant records, and
   automatic remediation remain outside the provider boundary.

## Consequences

Provider adapters can be replaced without changing the recommendation domain
or frontend contract. The recommendation record remains auditable and can be
compared with later human feedback. The first implementation may use a
deterministic local provider; external model integrations are optional and must
conform to the same boundary.

## Rejected alternatives

- Calling a model directly from a controller, which would bypass provenance and
  response validation.
- Passing raw evidence or complete historical records to a provider.
- Treating confidence as proof of correctness or allowing recommendation text
  to trigger an operational action.