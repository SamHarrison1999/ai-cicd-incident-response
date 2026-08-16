# ADR 0062: Global production-code coverage boundary

## Status

Accepted for Phase 13 implementation.

## Decision

Phase 13 makes production-code coverage an explicit release gate. Backend
JaCoCo and frontend Vitest must each report 100% for the measured application
surface:

- backend instructions, lines, branches, methods, and classes;
- frontend statements, lines, functions, and branches.

Tests must exercise behaviour and security boundaries; generated output,
build artefacts, type declarations, and test sources are not production code
and remain excluded.

## Consequences

Coverage is enforced by the build rather than treated as an informational
number. A missing test blocks the Phase 13 verification command. This makes
security regressions visible, but requires deliberate tests for success,
failure, validation, authorization, tenant isolation, bounded responses, and
empty-state paths.

The gate is intentionally separate from the normal Phase 13 verifier until
the cumulative test suite has been expanded. Phase 13 cannot be recorded as
complete while either strict gate is failing.
