# ADR 0005: Use a versioned, signed event envelope

- Status: Accepted
- Date: 2026-07-30

## Context

CI/CD providers produce different payloads and retry delivery. The platform must distinguish trusted source configuration from untrusted payload data, prevent trivial forgery and replay, evolve schemas, and guarantee idempotent domain effects.

## Decision

Every simulated source sends a canonical event envelope containing at least:

- `eventId` — source-generated stable identifier;
- `eventType` — canonical dot-separated type;
- `schemaVersion` — supported major/minor schema version;
- `occurredAt` — source event time;
- `sentAt` — delivery time used for replay limits;
- `correlationId` — distributed trace/business correlation identifier;
- `projectExternalId` — source-side project identity;
- `payload` — type-specific versioned object.

The HTTP request includes source identity and an HMAC signature over a documented canonical representation containing the timestamp and raw request body. The server verifies source status, timestamp tolerance, body size, signature, schema support, and uniqueness of `(event_source_id, event_id)`.

Duplicate deliveries return the stored result and do not repeat domain effects.

## Consequences

### Positive

- Supports retries safely.
- Isolates provider-specific adapters from the canonical domain.
- Enables schema evolution and reproducible simulations.
- Produces clear security and integration tests.

### Negative

- Canonical signing rules must be implemented identically by clients and server.
- Secret rotation and clock skew require explicit handling.
- Idempotency records consume storage and need retention rules.

## Guardrails

- Constant-time signature comparison.
- Configurable but bounded timestamp tolerance.
- Secret rotation supports an overlap window without indefinite old-key acceptance.
- Raw payload parsing occurs only after signature and size checks.
- Schema incompatibility fails closed and is audited.
