# ADR 0060: Phase 13 security-hardening boundary

## Status

Accepted for Phase 13 Batch 1.

## Decision

Phase 13 hardens the platform around its existing tenant, authentication, evidence, recommendation, review, and operational-learning boundaries. The work covers transport and browser security controls, credential and secret handling, abuse resistance, dependency and supply-chain checks, adversarial tests, and documented security verification.

Security hardening is additive defence in depth. It must not weaken the existing tenant-isolation invariant, expose raw evidence, or turn recommendations and learning output into executable actions.

## Explicit exclusions

Phase 13 does not introduce autonomous remediation, production-changing actions, unrestricted administrative access, silent policy mutation, or collection of raw secrets for testing.
