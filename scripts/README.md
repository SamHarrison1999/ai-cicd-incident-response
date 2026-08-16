# Repository Scripts

This directory contains repeatable developer and demonstration scripts.

Scripts must:

- Fail on errors.
- Avoid embedding real credentials.
- Be safe to run repeatedly where practical.
- Print actionable validation output.
- Support the documented Windows PowerShell workflow.

Phase verification scripts are cumulative. Phase 13 provides:

- `verify-phase-13-security.ps1` for secret exclusions, dependency metadata,
  non-root production containers, Compose rendering, and whitespace.
- `verify-phase-13-coverage.ps1` for strict Java JaCoCo and frontend Vitest
  coverage gates.
- `verify-phase-13.ps1` for the complete Phase 13 close-out boundary.
