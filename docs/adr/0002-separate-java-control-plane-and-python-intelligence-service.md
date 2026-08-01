# ADR 0002: Separate the Java control plane and Python intelligence service

- Status: Accepted
- Date: 2026-07-30

## Context

The product needs enterprise workflow and tenancy features as well as Python-native AI, evaluation, text-processing, and provider libraries. Putting everything in Java would reduce deployment count but weaken the intended Python and AI-engineering demonstration. Putting the control plane in Python would duplicate the existing AI Life Coach portfolio project and underuse Samuel's Java/Spring experience.

## Decision

Use Java/Spring Boot for the authoritative control plane and Python/FastAPI for bounded intelligence operations. Communication uses a synchronous versioned HTTP/JSON contract initially.

The Java service authorizes the request, selects evidence, and persists final results. The Python service sanitises and analyses supplied evidence but cannot mutate control-plane domain tables.

## Consequences

### Positive

- Demonstrates appropriate use of both Java and Python.
- Creates a clear trust and data-ownership boundary.
- Allows deterministic and provider-backed analysis implementations behind one interface.
- Permits independent testing and scaling.

### Negative

- Adds network failures and cross-language contract maintenance.
- Requires distributed tracing and timeout handling.
- Synchronous analysis can become slow for large inputs.

## Guardrails

- Strict request and response schemas live under `contracts/ai/`.
- Requests are bounded by evidence count and byte size.
- Timeouts and safe fallback behaviour are mandatory.
- Python responses are untrusted until Java revalidates citation ownership and schema invariants.
