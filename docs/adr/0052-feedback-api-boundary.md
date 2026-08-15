# ADR 0052: bounded feedback API boundary

## Decision

Expose tenant-scoped, read-only feedback aggregate responses through the control-plane API. The API accepts bounded policy and time-window filters, caps result size, and returns counts, provenance windows, and suppression state only.

The API does not expose raw review comments, evidence payloads, provider prompts, credentials, or model-training controls. Membership is required before any aggregate query is evaluated.

## Consequences

- Consumers receive deterministic, bounded analytics rather than raw feedback material.
- Cross-tenant identifiers are rejected without revealing whether another tenant has data.
- Suppressed aggregates remain visible as suppressed metadata and cannot be treated as a provider directive.
