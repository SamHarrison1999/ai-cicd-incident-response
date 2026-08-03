# ADR 0016: Enforce idempotency at the webhook-delivery boundary

## Status

Accepted

## Context

CI/CD providers retry deliveries when acknowledgements are delayed or unavailable. Processing the same delivery more than once could duplicate normalised events, regress pipeline state, and later create duplicate incidents.

## Decision

The database will enforce uniqueness on `(event_source_id, delivery_id)`.

The first valid request stores a delivery record and owns subsequent side effects. A repeated request with the same identifier and payload digest returns the existing deterministic result without reprocessing. Reuse of the identifier with different bytes is rejected as a conflict.

The idempotency record is tenant-scoped through the event source.

## Consequences

### Positive

- Provider retries are safe.
- Duplicate processing does not depend on in-memory locks.
- Results remain deterministic across application restarts and multiple instances.
- Delivery history is auditable.

### Negative

- Delivery records require retention and cleanup policy.
- Payload digests must be retained to detect conflicting reuse.
- Transactions must coordinate delivery creation, event creation, and pipeline projection carefully.

## Rejected alternatives

- Deduplicate only by normalised event fields.
- Use a process-local cache.
- Treat every retry as a new event.
- Rely solely on provider timestamps for uniqueness.
