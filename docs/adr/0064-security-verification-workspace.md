# ADR 0064: dependency, image, and security verification workspace

## Status

Accepted for Phase 13 Batch 4.

## Decision

Supply-chain and security checks are kept in a dedicated local verifier and a
GitHub Actions workflow. The workspace checks lockfiles, repository secret
exclusions, container non-root declarations, Compose rendering, CodeQL analysis,
and dependency review. It does not store generated reports or credentials in
the repository.

## Verification boundary

The verifier is deterministic and safe to run from a clean checkout. Docker
Compose validation may be skipped explicitly when Docker is unavailable; the
full CI workflow remains the authoritative container and code-scanning gate.

## Explicit exclusions

The workflow does not upload application secrets, does not run containers with
host credentials, and does not treat a static scan as proof of runtime safety.
