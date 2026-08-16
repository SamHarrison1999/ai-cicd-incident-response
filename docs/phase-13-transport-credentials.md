# Phase 13 Batch 2: transport, browser, and credential hardening

Batch 2 adds defence in depth at the control-plane response boundary.

- Content Security Policy, frame, MIME-sniffing, referrer, and permissions headers are explicit.
- HSTS is emitted only when the request is secure, preventing local HTTP verification from pretending to be production TLS.
- Existing headers are preserved rather than overwritten by the hardening writer.
- Authentication and duplicate-account responses remain bounded and do not echo passwords, tokens, or email values.
- Security-focused Java tests run with cumulative JaCoCo reporting.

The existing tenant, authentication, evidence, recommendation, review, and operational-learning boundaries remain unchanged.
