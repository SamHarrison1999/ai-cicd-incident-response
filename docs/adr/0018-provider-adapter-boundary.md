# ADR 0018: Keep provider adapters behind a deterministic normalisation boundary

## Status

Accepted for Phase 3 Batch 4.

## Decision

Provider-specific webhook payloads are mapped by small deterministic adapters into a provider-neutral candidate model before persistence. The adapter registry is selected from the configured event-source provider.

## Rationale

- Provider payloads evolve independently.
- Provider-specific parsing should not leak into pipeline, incident, or AI code.
- Deterministic mapping makes provenance and test assertions explicit.
- Unsupported events can be retained as authenticated deliveries without inventing fields.

## Consequences

The first version supports GitHub Actions `workflow_run` and Jenkins build payloads. Additional providers require a new adapter and focused mapping tests. The system intentionally does not infer root causes from provider fields.
