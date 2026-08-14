# ADR 0025: Phase 5 security and verification boundary

## Decision

Phase 5 treats incident correlation as bounded, tenant-scoped decision support.
The backend remains authoritative for organisation membership, project
ownership, role checks, lifecycle transitions, and audit recording.

Incident API responses contain only identifiers, lifecycle status, titles,
bounded summaries, and timestamps. Raw webhook payloads, signatures, secret
references, provider credentials, correlation internals, and remediation
commands remain outside the response and frontend boundaries.

## Verification boundary

The cumulative gate combines repository checks, Java tests and analysis,
frontend formatting/lint/tests/build, and Docker Compose configuration
validation. The end-to-end scenario uses synthetic event data and validates
technical data flow only. It is not evidence of production reliability,
operational effectiveness, human participant behaviour, or autonomous action.

## Consequences

- Tenant isolation is tested at service and repository boundaries.
- Viewer members can read permitted incident projections but cannot change state.
- Lifecycle transitions remain explicit and invalid transitions are rejected.
- Later evidence and AI features must preserve these boundaries.