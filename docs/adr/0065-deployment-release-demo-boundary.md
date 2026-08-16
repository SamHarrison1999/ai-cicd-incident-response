# ADR 0065: Deployment, release, and portfolio demo boundary

## Status

Accepted for Phase 14.

## Decision

Phase 14 makes the platform reproducibly deployable for local demonstration
and documents release evidence suitable for a portfolio case study.

The canonical demo uses Docker Compose, existing health checks, deterministic
intelligence behaviour, and existing tenant-scoped application contracts.

This phase does not claim production readiness. TLS, managed PostgreSQL,
secret-manager integration, registry publication, backup automation, and
Kubernetes production deployment remain separate concerns.

Screenshots must not contain credentials, tokens, private data, database
contents, or local filesystem paths.
