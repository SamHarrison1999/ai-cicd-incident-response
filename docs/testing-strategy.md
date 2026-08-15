# Testing Strategy

## Principles

1. Test behaviour at the cheapest reliable layer.
2. Use pure functions for correlation, sanitisation, confidence, and state-transition policies.
3. Use real PostgreSQL through Testcontainers for persistence and isolation behaviour.
4. Test cross-service contracts independently from implementation.
5. Keep the full offline demonstration covered by an end-to-end test.
6. Treat security and AI-safety tests as release gates, not optional hardening.
7. Never claim a suite passes without recorded command output.

## Test layers

### Java control plane

- Unit tests: domain policies, state machine, signature canonicalisation, correlation scoring, authorization decisions.
- Slice tests: controllers, serialization, validation, and problem-details responses.
- Integration tests: repositories, Flyway migrations, transaction boundaries, idempotency, concurrency, and tenant isolation using Testcontainers.
- Contract tests: event envelopes and Python analysis request/response schemas.

### Python intelligence service

- Unit tests: redaction, prompt-injection detection, deterministic classifiers, confidence calculation, abstention, retrieval scoring, and citation validation.
- API tests: Pydantic validation, error semantics, request limits, provider timeouts, and deterministic fallback.
- Evaluation tests: fixed synthetic benchmark incidents with versioned expected properties.
- Adversarial tests: encoded secrets, misleading logs, instruction injection, unsupported causal claims, and forged evidence identifiers.

### React frontend

- Unit/component tests: forms, role-aware controls, state displays, evidence citations, and accessible labels.
- Integration tests: API mocks for loading, empty, unauthorized, error, and stale-data states.
- Playwright: authentication, project setup, incident investigation, recommendation review, resolution, evaluation, and tenant boundaries.
- Accessibility: automated checks plus keyboard-only workflows for critical screens.

### System tests

- Compose smoke test from a clean checkout.
- Signed synthetic event sequence creates the expected pipeline run and incident.
- Duplicate event delivery remains idempotent.
- Provider-disabled recommendation produces deterministic output or justified abstention.
- Human review preserves generated and edited versions.
- Metrics and traces include correlation identifiers without secret content.

## AI quality metrics

- Classification accuracy on golden cases.
- Citation precision: cited evidence actually supports the associated claim.
- Citation validity: every citation resolves to an authorized immutable evidence item.
- Grounded-claim rate: factual recommendation claims with supporting citations.
- Appropriate abstention rate on intentionally insufficient cases.
- Unsafe-compliance rate for prompt-injection cases; target is zero.
- Human acceptance, edit, and rejection rates, interpreted alongside groundedness.

## Phase 0 verification

Phase 0 contains no executable production code. Its checks are:

```bash
git diff --check
find docs -type f -maxdepth 3 | sort
```

A Markdown link and Mermaid renderer may be introduced as documentation CI in Phase 1.

## Continuous integration quality gate

Phase 1 CI runs independent Java, Python, frontend, container, Compose, and repository-quality jobs in parallel. A final aggregate job fails unless every required job succeeds.

This structure preserves component-specific failure evidence while giving branch protection one stable required check:

```text
Phase 1 quality gate
```

Detailed commands and artifact behaviour are documented in [`ci.md`](ci.md).

## Phase 2 release verification

Phase 2 adds these mandatory test categories:

- Authentication unit tests for normalisation, password policy, generic failures, and disabled accounts.
- Token tests for deterministic SHA-256 hashing and non-disclosure of raw token values.
- PostgreSQL Testcontainers tests for identity, membership, project, and refresh-session constraints.
- Tenant-policy tests for absent membership and insufficient roles.
- Project-service tests for organisation-scoped lookup and duplicate slug handling.
- Frontend tests for protected-route redirection and successful login.
- Playwright navigation using deterministic API interception.
- Full repository, Java, Python, frontend, container, and Compose verification through `scripts/verify-phase-2.ps1`.

A Phase 2 batch or phase is not marked verified until developer-supplied command output has been reviewed and recorded.

## Phase 3 cumulative verification

Phase 3 verification combines provider-adapter unit tests, persistence-backed webhook tests, tenant-boundary API tests, frontend tests, repository checks, Compose validation, and repeatable signed webhook simulations. A passing unit test is not evidence that an AI-generated root cause is correct; later AI features must cite retained evidence and abstain when it is insufficient.

## Phase 5 cumulative verification

Phase 5 verification combines deterministic correlation-engine tests, incident
lifecycle and API contract tests, tenant-boundary checks, bounded frontend API
tests, frontend interaction tests, repository checks, and Docker Compose
validation. The synthetic end-to-end scenarios document expected technical
flow without making production or human-outcome claims.
## Phase 6 evidence verification

Phase 6 verification covers bounded content, redaction, retention classes,
content hashes, tenant-scoped evidence links, search authorization, viewer
outputs, and synthetic evidence-to-incident flows. Tests must confirm that
secret material and raw webhook signatures do not cross persistence, indexing,
API, or frontend boundaries.
## Phase 6 final verification gate

The cumulative Phase 6 gate runs repository validation, Java formatting and
tests, frontend formatting/lint/tests/build, and Docker Compose configuration
validation. The evidence scenarios must demonstrate tenant isolation,
redaction before persistence, bounded search and viewer output, deterministic
links, duplicate safety, and safe rejection of invalid cross-tenant access.

The final record must cite the command output supplied for the exact branch
commit being verified. A green unit test alone is not evidence that an
incident cause has been established.
## Phase 7 cumulative scenarios

The cumulative suite covers sanitisation before diagnosis, deterministic rule evaluation, explicit abstention, tenant isolation, bounded API responses, and human-review presentation.
### Phase 8 historical retrieval

Verification covers deterministic ranking, cursor pagination, tenant isolation,
invalid-filter rejection, explicit empty and ambiguous results, bounded
responses, and the human-review workspace.
### Phase 9 provider-grounded recommendations

Verification covers provider-interface substitution, bounded evidence-bundle
assembly, schema validation, provenance completeness, citation authorization,
confidence and abstention behaviour, provider failure fallback, prompt-injection
resistance, and the non-executable recommendation boundary. External providers
remain optional; the deterministic offline path must remain safe and repeatable.
## Phase 9 close-out verification

- Verify tenant isolation, prompt-injection exclusion, bounded responses, abstention, provider fallback, citation provenance, and human-review labelling.
## Phase 10 review verification

- Verify accept, edit, reject, tenant isolation, actor attribution, immutable versioning, bounded feedback, and resolution eligibility.
## Phase 10 human review controls

Review mutations require authenticated tenant membership, preserve immutable versions, require rejection reasons, bound comments and resolution text, and never execute remediation. Tests must reject cross-tenant references and responses containing secrets or raw evidence.
## Phase 11 feedback controls

Feedback analytics are authenticated, tenant-scoped, bounded, suppression-aware, and advisory. Raw evidence, review comments, provider credentials, provider prompts, silent retraining, policy mutation, and remediation actions are excluded.