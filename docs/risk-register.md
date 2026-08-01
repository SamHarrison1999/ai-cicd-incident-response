# Risk Register

| ID | Risk | Likelihood | Impact | Mitigation | Evidence of control |
|---|---|---:|---:|---|---|
| R-01 | AI states an unsupported root cause | Medium | Critical | Mandatory citations, confidence thresholds, deterministic validation, abstention, human review | Citation-validation and weak-evidence evaluation tests |
| R-02 | Secrets or personal data are sent to a provider | Medium | Critical | Bounded inputs, layered redaction, provider-off default, audit hashes rather than raw prompts where appropriate | Redaction fixtures, canary-secret tests, provider-boundary tests |
| R-03 | Log-based prompt injection changes model behaviour | High | High | Treat logs as untrusted data, delimit content, fixed system instructions, detect injection patterns, forbid tool/remediation authority | Adversarial benchmark cases and provider adapter tests |
| R-04 | Cross-tenant data leakage | Medium | Critical | Organisation-scoped identifiers and queries, deny-by-default authorization, isolation tests, optional later RLS | Repository/service/API isolation test matrix |
| R-05 | Webhook replay or forgery | Medium | High | HMAC signature, timestamp tolerance, constant-time comparison, source status check, unique source/event identity | Replay, expiry, signature, and rotation integration tests |
| R-06 | Duplicate delivery creates duplicate incidents | High | High | Idempotency key and database uniqueness, transactional processing, replay of original outcome | Concurrency and duplicate-delivery tests |
| R-07 | Correlation merges unrelated failures | Medium | High | Deterministic scored dimensions, bounded windows, policy versioning, inspectable rationale, manual split deferred/documented | Golden correlation cases and decision audit records |
| R-08 | Correlation fragments one incident into many | Medium | Medium | Tunable windows, pipeline-run linkage, recurrence/reopen workflow, metrics for incident fragmentation | Evaluation scenarios and manual review |
| R-09 | Provider outage blocks incident response | Medium | High | Offline deterministic mode, timeouts, circuit breaker, no provider dependency for evidence viewing | Provider-disabled end-to-end test |
| R-10 | Historical retrieval repeats an incorrect resolution | Medium | High | Retrieve only reviewed resolved incidents, expose similarity factors, never auto-apply, cite source incident | Retrieval eligibility tests and UI source links |
| R-11 | Audit data is mutable or incomplete | Low | High | Append-only application API, transactional audit creation, restricted database role, integrity metadata | Transition/review integration tests assert audit records |
| R-12 | Raw logs create unbounded storage cost | High | Medium | Payload and fragment limits, retention policy, synthetic data only in demo, metrics and cleanup jobs | Limit tests and documented retention settings |
| R-13 | Monorepo CI becomes slow and discourages iteration | Medium | Medium | Path-filtered jobs, dependency caches, unit/integration separation, scheduled full scans | CI duration budget tracked in progress ledger |
| R-14 | Too many technologies prevent project completion | Medium | High | Modular monolith, one database, REST first, deterministic provider, phased vertical slices | ADRs and strict phase acceptance criteria |
| R-15 | Portfolio demo depends on paid or external services | Low | High | Fully local Compose deployment and deterministic golden scenarios | Clean-machine setup test and demo script |
| R-16 | Metrics expose tenant or secret content | Low | High | Low-cardinality identifiers, no raw log lines in labels, security review of telemetry | Telemetry tests and threat-model checklist |
| R-17 | Recommendation metrics incentivise unsafe acceptance | Medium | Medium | Track groundedness, abstention, edits, and rejection alongside acceptance; no single success metric | Evaluation dashboard definitions |
| R-18 | Dependency or container vulnerabilities undermine the demo | Medium | High | Automated update checks, SBOM, SCA, image scanning, pinned base images, release gate | Security workflow and release checklist |

## Review cadence

The register is reviewed at the end of every phase. New risks receive an owner, control, and verification method before the phase is marked complete.
