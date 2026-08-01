# ADR 0008: Use uv, FastAPI, and a deterministic provider boundary

- Status: Accepted
- Date: 2026-08-01
- Decision owners: Project maintainer
- Related phase: Phase 1

## Context

The intelligence service needs an independently deployable Python API with validated contracts, reproducible dependencies, strong typing, fast tests, structured logging, and a safe abstraction for deterministic and model-backed analysis.

Logs are untrusted input. The service must not allow provider implementations to bypass response validation, invent unsupported causes, or trigger destructive remediation.

## Decision

Use:

- Python 3.14.
- FastAPI for the HTTP and OpenAPI layer.
- Pydantic 2 for strict request and response contracts.
- uv for project environments and the committed universal lockfile.
- Ruff for linting and formatting.
- MyPy strict mode.
- Pytest for tests.
- structlog for JSON logs.
- A typed `RecommendationProvider` protocol.
- A deterministic provider as the only enabled provider during the foundation phase.

The foundation deterministic provider abstains until reviewed classification rules are introduced in Phase 7.

## Consequences

### Positive

- The HTTP contract is generated from validated Python types.
- `uv.lock` records exact cross-platform dependency resolution.
- Provider implementations can change without changing control-plane workflow ownership.
- Abstention is enforced by model validation.
- Unsafe or unknown request fields are rejected.
- The initial service works without an external model provider.
- Request bodies and untrusted log content are excluded from access logs.

### Negative

- uv is an additional tool developers must install.
- Provider abstraction adds structure before multiple providers exist.
- Strict validation can reject partially formed upstream payloads.
- The foundation provider produces no diagnosis until Phase 7.

## Guardrails

- Every non-abstained response requires evidence citations.
- An abstained response cannot declare a likely cause.
- Logs are represented as explicitly untrusted evidence.
- HTTP middleware logs metadata but never bodies.
- Automatic remediation is not part of the API contract.
- Provider and version metadata are returned with every recommendation.
- The Java control plane remains authoritative for human review and incident state.

## Alternatives considered

### pip with unpinned requirements

Rejected because it does not provide the same committed universal lockfile workflow.

### Poetry

A valid alternative, but uv provides a smaller and faster toolchain for environments, locking, syncing, and command execution.

### Direct model SDK calls from route handlers

Rejected because this would couple transport, provider behaviour, safety validation, and audit metadata.

### Enable an external LLM immediately

Rejected because evidence validation, redaction, prompt-injection controls, evaluation, and provider audit records must exist first.
