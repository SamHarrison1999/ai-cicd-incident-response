# Product Backlog

## Prioritisation

- **P0:** required for a credible, safe end-to-end version 1 demonstration.
- **P1:** required for portfolio completeness but can follow the first vertical slice.
- **P2:** valuable extension after the core release.

## Epics and acceptance criteria

### E0 — Architecture and delivery foundation (P0)

- Document scope, non-goals, risks, architecture, ADRs, and phase plan.
- Create reproducible local development commands.
- Establish independent CI jobs for Java, Python, frontend, contracts, and integration tests.
- Reject formatting, linting, test, dependency, or secret-scanning failures.

### E1 — Identity and tenant model (P0)

- A user can register, log in, and log out.
- Passwords are stored using an adaptive one-way hash.
- A user can create an organisation and project.
- Membership roles are `OWNER`, `ADMIN`, `ENGINEER`, and `VIEWER`.
- Requests cannot read or mutate resources belonging to another organisation.
- Authentication and authorization failures use stable problem-details responses.

### E2 — Event-source management (P0)

- An authorized user can create, disable, and rotate a simulated event source.
- A signing secret is shown only when created or rotated; only a verifier-safe representation is stored where practical.
- Source configuration changes create audit events.

### E3 — Secure event ingestion (P0)

- A signed request includes source ID, timestamp, event ID, schema version, and payload digest.
- Invalid, expired, oversized, malformed, or unsupported requests are rejected.
- Re-delivering the same source/event ID returns the original processing outcome without duplicating effects.
- Raw and validation status are persisted before asynchronous/domain processing.
- Every accepted event has a correlation ID.

### E4 — Pipeline runs and timelines (P0)

- Version-specific adapters map incoming events into one canonical event model.
- Events aggregate into a pipeline run identified by project and external run identity.
- Users can filter runs by project, status, branch, commit, environment, and time.
- A timeline is stable, paginated, tenant-scoped, and searchable.

### E5 — Incident correlation and lifecycle (P0)

- Failure events are correlated using project, commit, environment, event type, and configurable time windows.
- Every decision records the correlation-policy version and scored dimensions.
- An event belongs to zero or one primary incident in version 1.
- Valid incident transitions follow the documented state machine.
- Reopening preserves the original incident and timeline.

### E6 — Logs and evidence (P0)

- Bounded log fragments can be attached to incoming or normalised events.
- Users can search and filter logs without exposing another tenant's data.
- Original and sanitised content are distinguishable.
- Evidence citations point to immutable event or log-fragment versions and line ranges.

### E7 — Deterministic intelligence (P0)

- Sanitisation removes configured secret classes while preserving diagnostic meaning.
- Prompt-injection indicators are labelled and excluded from instruction channels.
- Deterministic rules classify fixed benchmark cases and expose matched evidence.
- Weak evidence produces an abstained result instead of an unsupported diagnosis.
- The complete demo works with all external providers disabled.

### E8 — Historical retrieval (P0)

- Only resolved incidents with approved resolutions are eligible for retrieval.
- Retrieval is scoped to an organisation by default.
- Results include similarity factors, not only an opaque score.
- A user can inspect the source incident before relying on its resolution.

### E9 — AI recommendations (P0)

- Providers implement one internal interface and return a strict versioned schema.
- Recommendation output includes summary, likely cause, confidence, citations, alternatives, next checks, abstention, and review status.
- The control plane rejects nonexistent or cross-tenant citations.
- Provider, model, prompt template, rule set, retrieval set, and schema versions are audited.
- Provider failure falls back safely or returns a retryable non-diagnostic result.

### E10 — Human review and resolution (P0)

- Authorized engineers can accept, edit, or reject a pending recommendation.
- Edits preserve the original generated content and create a new reviewed version.
- Rejection captures a bounded reason category and optional comment.
- Only human-reviewed content can become the final incident resolution.
- Every action is attributable and timestamped.

### E11 — Evaluation (P0)

- The repository contains synthetic, non-sensitive golden cases.
- Evaluation measures deterministic classification accuracy, citation validity, groundedness, abstention behaviour, and provider-schema validity.
- Product metrics include recommendation acceptance and edit rates.
- Evaluation runs are repeatable and versioned.

### E12 — Observability and demos (P1)

- All services emit structured logs with trace and correlation identifiers.
- HTTP, ingestion, correlation, recommendation, and review operations emit metrics.
- Distributed traces cross Java-to-Python requests.
- A scenario generator produces reproducible failure sequences.
- Dashboards show platform health and product outcomes without leaking log content.

### E13 — Security hardening (P0)

- Publish a STRIDE-oriented threat model.
- Apply rate limits to authentication, ingestion, search, and recommendation endpoints.
- Test tenant isolation at repository, service, and API levels.
- Run dependency, container, static analysis, and secret scans in CI.
- Test malicious log instructions, encoded secrets, replayed signatures, and citation forgery.

### E14 — Release and portfolio presentation (P1)

- Build versioned container images from clean checkouts.
- Provide local Docker Compose deployment and optional hosted-demo guidance.
- Publish final architecture, setup, API, data model, evaluation, threat model, limitations, and demo documents.
- Provide screenshots, a five-minute demonstration, CV bullets, and a technical case study.

## Deferred backlog (P2)

- Real read-only GitHub Actions, Jenkins, GitLab CI, and Kubernetes connectors.
- PostgreSQL row-level security after application-level isolation is proven.
- OpenSearch or a dedicated vector database when PostgreSQL search no longer meets measured needs.
- SSO/OIDC federation and SCIM.
- Background workflow orchestration for very large analyses.
- Multi-region operation and disaster recovery.
- Carefully constrained non-destructive runbook execution in a later major version.
