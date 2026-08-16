# Phase 13 coverage model

Phase 13 treats coverage as a security control: untested authorization,
validation, error, and bounded-output paths are not accepted as verified.

The target is 100% of measured production application code. JaCoCo measures
backend instructions, lines, branches, methods, and classes. Vitest measures
frontend statements, lines, functions, and branches.

Coverage must be achieved with meaningful tests. Tests should cover both
allowed and rejected requests, tenant boundaries, authentication failures,
secret-safe error responses, empty and ambiguous results, bounded limits,
security headers, and UI error/loading/empty states. Generated files, test
files, build output, and type declarations are excluded from the target.

Run `scripts/verify-phase-13-coverage.ps1` from the repository root. The
command is expected to fail while uncovered production paths remain; that
failure is the work list for the next coverage-test batches.
