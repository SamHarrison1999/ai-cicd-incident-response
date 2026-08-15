# ADR 0058: Operational-learning workspace boundary

## Status

Accepted for Phase 12 Batch 4.

## Decision

Provide a protected workspace for viewing tenant-scoped operational trends and bounded comparisons. The workspace requires explicit organisation and project identifiers, presents suppression state and provenance, and keeps learning output separate from incident mutation and remediation controls.

## Safety boundary

The workspace displays aggregate trend metadata only. It does not render raw evidence, review comments, secrets, provider prompts, credentials, training controls, policy mutation, or production actions.
