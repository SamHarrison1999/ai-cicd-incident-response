# ADR 0015: Use HMAC-signed webhooks with timestamp replay protection

## Status

Accepted

## Context

Phase 3 must demonstrate realistic webhook authentication without depending on live GitHub or Jenkins installations. Payloads and headers are untrusted, and captured valid requests must not remain reusable indefinitely.

## Decision

Each configured event source uses HMAC-SHA-256 over the exact request body bytes. Requests also carry an RFC 3339 delivery timestamp that must fall within a configured tolerance.

Signature comparison uses a constant-time operation. Secrets are never returned by APIs or written to logs. Verification occurs before JSON parsing and before any normalised event or pipeline-run mutation.

## Consequences

### Positive

- The simulator can produce realistic signed requests.
- Payload tampering is detected.
- Timestamp validation limits replay windows.
- Exact-byte verification avoids canonicalisation ambiguity.

### Negative

- Secret distribution and rotation must be managed.
- Clock skew requires a documented tolerance.
- HMAC authenticates possession of a shared secret rather than a named human or machine identity.

## Rejected alternatives

- Unsigned local-only webhooks.
- Signatures calculated over re-serialised JSON.
- Basic authentication embedded in webhook URLs.
- Passing raw payloads to an AI model to decide whether they are trustworthy.
