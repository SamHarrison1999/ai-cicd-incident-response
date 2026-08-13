# ADR 0017: Bind webhook metadata into the signed envelope

## Status

Accepted

## Context

ADR 0015 originally selected HMAC-SHA-256 over only the exact request body while
validating a separate delivery timestamp. A captured body and signature could
otherwise be replayed with a fresh timestamp or a different delivery identifier
because those headers were not cryptographically bound to the request.

Phase 3 requires replay resistance and database idempotency to reinforce one
another. The delivery identifier, provider event type, timestamp, and exact
payload bytes all affect how the request is authenticated and stored.

## Decision

Version 1 signs this byte sequence:

~~~text
CICD-WEBHOOK-V1\n
<delivery-id>\n
<event-type>\n
<exact-timestamp-header>\n
<exact-payload-bytes>
~~~

Header values are length-bounded and reject control characters before the
signing input is constructed. The timestamp is parsed for tolerance validation,
but its exact received text is used in the HMAC input. The payload is never
parsed or re-serialised before verification.

The supplied signature remains:

~~~text
sha256=<64 lowercase hexadecimal characters>
~~~

Comparison uses a constant-time operation.

## Consequences

### Positive

- A captured signature cannot be paired with a fresh timestamp.
- Delivery identifiers and event types cannot be changed without invalidating the signature.
- Exact payload bytes remain authenticated without JSON canonicalisation.
- The prefix versions the signed-input contract for future evolution.

### Negative

- Simulators and future provider adapters must reproduce the canonical prefix and newline separators exactly.
- This project-owned simulator contract is not byte-for-byte compatible with every real CI/CD provider.

## Rejected alternatives

- Continue signing only the request body.
- Sign parsed or re-serialised JSON.
- Trust unsigned replay and idempotency headers.
- Include secret material in webhook URLs.
