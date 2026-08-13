# ADR 0014: Use provider-neutral normalised CI/CD events

## Status

Accepted

## Context

GitHub Actions and Jenkins use different event names, payload shapes, status values, identifiers, and retry behaviour. Downstream incident correlation must not depend directly on provider-specific JSON.

## Decision

The control plane will verify and retain delivery metadata, then convert supported provider payloads into a versioned provider-neutral event envelope.

Provider adapters are deterministic. They map only fields supported by the received payload and use `UNKNOWN` or reject the event when evidence is insufficient.

Tenant identifiers are derived from the configured event source rather than from provider payload data.

## Consequences

### Positive

- Pipeline tracking and future incident correlation use one stable contract.
- Provider-specific parsing is isolated.
- Evidence provenance can identify which source fields supported each mapping.
- Unknown provider values cannot silently become invented internal facts.

### Negative

- Adapters require explicit maintenance as provider schemas evolve.
- Some provider-specific detail will not belong in the common envelope.
- Schema evolution requires compatibility discipline.

## Rejected alternatives

- Store and query only raw provider JSON.
- Build separate downstream pipelines for every provider.
- Ask an AI model to translate arbitrary webhook payloads.
