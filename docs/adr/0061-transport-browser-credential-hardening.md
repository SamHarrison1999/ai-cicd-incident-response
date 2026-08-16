# ADR 0061: Transport, browser, and credential hardening

## Status

Accepted for Phase 13 Batch 2.

## Decision

The control plane writes explicit browser and transport security headers at the response boundary. The policy is additive, preserves headers supplied by earlier filters, emits HSTS only for secure requests, and keeps authentication failures bounded without returning credentials or raw request values.

## Verification boundary

Tests cover HTTP and HTTPS behaviour, header preservation, content-security policy, browser capability restrictions, authentication error shaping, and duplicate-account responses. JaCoCo reporting remains part of the cumulative verification command.

## Explicit exclusions

This batch does not introduce autonomous remediation, production-changing actions, client-side secret storage, or a claim that local HTTP development is equivalent to production TLS.
