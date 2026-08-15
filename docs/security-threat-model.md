# Security Threat Model

## Scope

This document covers the Phase 2 identity, session, organisation, project, audit, and frontend boundaries.

Later phases will extend the model for signed webhooks, untrusted logs, secret redaction, AI prompt injection, evidence authorization, and provider integrations.

## Assets

- User identities and password hashes.
- Signed access-token keys and configuration.
- Refresh-token sessions and token-family state.
- Organisation memberships and roles.
- Tenant-scoped project metadata.
- Audit events and correlation identifiers.
- Browser session state.
- PostgreSQL credentials and persisted data.

## Trust boundaries

1. Browser to web application.
2. Browser to control-plane API through the same-origin reverse proxy.
3. Control plane to PostgreSQL.
4. GitHub Actions and local build tooling to dependency registries.
5. Future control-plane to intelligence-service boundary.

## Threats and controls

| Threat | Control |
|---|---|
| Password disclosure | BCrypt hashes only; plaintext passwords are not persisted or logged |
| Account enumeration | Generic invalid-login response |
| Credential stuffing | Stateless authentication boundary prepared for rate limiting; rate limits remain a later hardening item |
| Access-token theft | Short-lived signed JWTs; token retained only in frontend memory |
| Refresh-token theft | Opaque value in HTTP-only, SameSite Strict cookie; database stores only SHA-256 hashes |
| Refresh-token replay | Rotation after refresh; reuse revokes the token family |
| Session persistence after logout | Logout revokes the family and expires the cookie |
| Cross-site request abuse | SameSite Strict refresh cookie and JSON API; explicit anti-CSRF review remains required before cross-site deployment |
| Cross-tenant data access | Active-membership checks plus organisation-scoped repository queries |
| Identifier probing | Inaccessible tenant resources use not-found semantics |
| Privilege escalation | Explicit role allow-lists in application services |
| Duplicate tenant identifiers | PostgreSQL uniqueness constraints and stable conflict responses |
| Audit repudiation | Authentication and tenant mutations create audit records with actor, target, action, time, and correlation identifier |
| Browser token persistence | Access token is kept in React state; no localStorage or sessionStorage token persistence |
| Secret leakage in logs | Authentication payloads and token values must not be logged |
| Mass assignment | Controllers accept explicit request records rather than binding entities |
| Destructive deletion | Projects transition to `ARCHIVED`; normal workflows do not physically delete tenant data |

## Tenant-isolation invariant

Every tenant-owned operation must satisfy both conditions:

1. The current user has an active membership in the requested organisation.
2. Repository access is constrained by the same organisation identifier.

A successful lookup by globally unique resource identifier alone is not sufficient.

## Role matrix

| Operation | OWNER | ADMIN | MEMBER | VIEWER |
|---|---:|---:|---:|---:|
| View organisation | Yes | Yes | Yes | Yes |
| Rename organisation | Yes | Yes | No | No |
| List/view projects | Yes | Yes | Yes | Yes |
| Create/update/archive projects | Yes | Yes | Yes | No |

## Frontend security boundary

- Refresh-token cookies are inaccessible to JavaScript.
- Authentication requests use `credentials: include`.
- Access tokens exist only in application memory.
- Protected routes wait for session restoration before rendering.
- Logout clears frontend state even if the remote request fails.
- The UI is not an authorization boundary. Backend role and tenant checks remain authoritative.

## Known limitations

- Rate limiting and login throttling are not implemented in Phase 2.
- Multi-factor authentication is not implemented.
- Password reset and email verification are not implemented.
- Key rotation and external secret management are not yet implemented.
- Database row-level security is deferred as defence in depth.
- Security headers and production TLS termination depend on the deployment platform.
- CSRF controls must be reassessed if SameSite policy or deployment topology changes.
- Phase 2 does not yet expose membership administration or invitations.

## Security verification evidence

Phase 2 verification must include:

- Password-policy and duplicate-registration tests.
- Generic invalid-login and disabled-user tests.
- Refresh-token hash uniqueness tests.
- Missing-membership and insufficient-role tests.
- Cross-tenant project lookup tests.
- Frontend unauthenticated redirect and login tests.
- Full Java, frontend, container, Compose, and repository quality gates.

## Known dependency risks

### React Router advisory

The frontend currently uses React Router 6.30.4. The npm audit performed during
Phase 2 reports two moderate-severity advisories in the React Router dependency
chain.

React Router 7.18.2 was evaluated as a remediation candidate. Although the
upgrade installed successfully, that version introduced a high-severity
dependency finding affecting React Router RSC mode. The application does not use
React Server Components or React Router server-side hydration, but retaining a
dependency with a higher-severity reported advisory would weaken the repository's
security posture and fail strict dependency review.

The project therefore remains on React Router 6.30.4 until a stable release is
available that resolves the reported advisories without introducing a
higher-severity finding.

The following controls apply:

- npm audit fix --force must not be used because it performs uncontrolled
  breaking dependency changes.
- React Router upgrades must be performed deliberately and verified with
  formatting, linting, unit tests, production build, browser tests, and
  dependency scanning.
- This accepted risk must be reviewed when Dependabot proposes a patched stable
  release.
- The current application does not use React Router RSC mode or server-side
  hydration.

## Phase 3 ingestion controls

- Webhook requests are authenticated with metadata-bound HMAC-SHA-256 before persistence.
- Delivery timestamps are bounded to reduce replay exposure.
- Raw payload bytes and supplied signatures are not persisted.
- Secret references are opaque and resolved only at verification time.
- Provider payloads are untrusted input; adapters copy only allow-listed typed fields.
- Unsupported provider events are not converted into invented normalised facts.
- Metrics intentionally exclude request identifiers, payload values, signatures, and secrets.

## Phase 5 incident-correlation controls

- Incident reads require active organisation membership and project scoping.
- Lifecycle writes use an explicit role allow-list and domain transition policy.
- Correlation candidates are gated by organisation and project before scoring.
- Correlation decisions persist policy version, bounded dimensions, and result.
- Incident responses exclude raw payloads, signatures, secrets, and provider
  credentials.
- Synthetic end-to-end checks cover duplicate processing and tenant boundaries.
## Phase 6 evidence controls

- Evidence is authorised by active organisation membership and project scope.
- Content is bounded and redacted before persistence or indexing.
- Secret values, signatures, credentials, and access tokens are excluded from viewer outputs.
- Provenance, content hashes, retention classes, and links are auditable.
- Evidence is technical observation, not proof of causality or an AI conclusion.
## Phase 6 final security verification

The final evidence boundary is verified as a chain rather than at the viewer alone:

1. Tenant and project ownership are checked before repository access.
2. Evidence content is bounded and redacted before hashing, persistence, or indexing.
3. Search returns metadata only; viewer responses return persisted redacted content with bounded links.
4. Raw webhook payloads, signatures, credentials, access tokens, and hidden policy internals remain excluded.
5. Synthetic duplicate and cross-tenant scenarios must fail safely without creating a second projection or leaking identifiers.

Evidence remains an authorised technical observation. It is not proof of causality, an AI conclusion, or permission to perform remediation.
## Phase 7 diagnosis boundary

Diagnosis consumes only sanitised, tenant-authorised evidence. Results are bounded suspected hypotheses; raw content, credentials, signatures, and remediation actions are excluded.
## Phase 8 historical retrieval

Historical retrieval is tenant-scoped, read-only, bounded, provenance-linked,
and limited to sanitised metadata. Cross-tenant access, raw secret disclosure,
causal claims, and autonomous remediation remain outside the boundary.
## Phase 9 provider-recommendation controls

- Provider requests contain only bounded, sanitised, tenant-authorised evidence
  and historical projections.
- Provider output is untrusted, schema-validated, length-bounded, and cannot
  execute commands or mutate incidents, pipelines, deployments, or hosts.
- Provider, model or ruleset, prompt-template, retrieval, schema, and evidence
  provenance are retained with each recommendation.
- Insufficient, conflicting, unsafe, or low-confidence inputs produce explicit
  abstention rather than an unsupported recommendation.
- Cross-tenant evidence, secrets, signatures, raw payloads, and hidden policy
  instructions remain outside the provider boundary.
## Phase 9 close-out controls

- Recommendation providers receive only bounded, sanitised evidence bundles.
- Provider failures use deterministic fallback or abstention and never execute remediation.
- Recommendation responses and citations remain tenant scoped and provenance bound.
## Phase 10 review controls

- Review actions require an authorised tenant member and derive the actor from authenticated context.
- Generated recommendation versions are immutable; edits create attributable reviewed versions.
- Rejection reasons and comments are bounded, and only reviewed content can become a final resolution.
## Phase 10 human review controls

Review mutations require authenticated tenant membership, preserve immutable versions, require rejection reasons, bound comments and resolution text, and never execute remediation. Tests must reject cross-tenant references and responses containing secrets or raw evidence.
## Phase 11 feedback controls

Feedback analytics are authenticated, tenant-scoped, bounded, suppression-aware, and advisory. Raw evidence, review comments, provider credentials, provider prompts, silent retraining, policy mutation, and remediation actions are excluded.
## Phase 12 operational learning controls

Learning outputs are tenant-scoped, bounded, provenance-preserving, suppression-aware, and advisory. Raw evidence, comments, secrets, provider prompts, silent retraining, policy mutation, and remediation actions are excluded.