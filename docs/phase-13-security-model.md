# Phase 13 security-hardening model

Phase 13 strengthens the platform's security posture without changing the decision-support safety boundary.

## Security dimensions

- Transport and browser controls: security headers, safe cookie behaviour, same-origin assumptions, and explicit CORS/CSRF review.
- Credential protection: password, token, signing material, refresh-session, and secret-reference handling.
- Abuse resistance: bounded request bodies, rate-limit policy, replay handling, account-probing resistance, and safe error responses.
- Supply-chain protection: dependency review, image provenance, lockfile discipline, and vulnerability scanning.
- Adversarial verification: tenant-boundary attacks, malformed input, prompt-injection carry-over, secret leakage, and unsafe-action attempts.

## Invariants

Tenant membership and organisation-scoped repository queries remain mandatory. Security failures fail closed, responses remain bounded, and diagnostics never disclose credentials, raw evidence, provider prompts, or hidden policy instructions.
