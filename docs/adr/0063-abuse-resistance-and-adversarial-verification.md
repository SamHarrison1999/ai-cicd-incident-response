# ADR 0063: Abuse resistance and adversarial verification

## Status

Accepted for Phase 13 Batch 3.

## Decision

Phase 13 treats bounded validation, generic authentication failures, tenant
checks, replay-safe ingestion, secret redaction, prompt-injection resistance,
and non-remediating recommendations as one adversarial verification boundary.
Existing domain limits remain the source of truth; the security suite exercises
their reject paths rather than introducing an unbounded second policy.

## Verification boundary

The cumulative tests cover malformed and oversized values, invalid cursors and
windows, duplicate delivery identifiers, cross-tenant references, redaction of
secrets and signatures, untrusted instruction removal, generic credential
errors, and the absence of production-changing actions.

## Explicit exclusions

This batch does not claim to provide a distributed rate limiter, a managed WAF,
or autonomous remediation. Production deployments must place the service behind
their approved gateway and rate-limit policy.
