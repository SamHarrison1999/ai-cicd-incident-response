# Identity and Tenancy Design

## Purpose

Phase 2 introduces the identity and multi-tenant boundaries that every later event, pipeline, incident, recommendation, and audit feature will depend on.

The Java control plane remains the source of truth for users, organisations, memberships, projects, authentication sessions, and authorisation decisions.

## Identity model

A user is a global account identified by a normalised email address. A user may belong to multiple organisations through organisation memberships.

### User lifecycle

| Status | Meaning |
|---|---|
| `ACTIVE` | The user may authenticate and use authorised resources. |
| `DISABLED` | Authentication and refresh are rejected. |

Email is stored in display form and normalised lowercase form. Only a Spring Security password hash is persisted.

## Organisation model

An organisation is the top-level tenant boundary. All projects and later tenant-owned resources resolve to exactly one organisation.

### Membership roles

| Role | Capabilities |
|---|---|
| `OWNER` | Full organisation administration. |
| `ADMIN` | Manage projects and memberships except ownership transfer and deletion. |
| `MEMBER` | Use organisation projects. |
| `VIEWER` | Read-only access. |

Version 1 uses organisation-level roles. Project-specific memberships are deferred.

## Project model

A project belongs to exactly one organisation. Project slugs are unique within an organisation through `(organisation_id, slug)`.

| Status | Meaning |
|---|---|
| `ACTIVE` | The project accepts operational configuration. |
| `ARCHIVED` | The project remains readable but cannot accept new configuration. |

Hard deletion is not exposed in Phase 2.

## Authentication model

### Access tokens

- Signed JWT access tokens with a default 15-minute lifetime.
- Claims identify the user and session, not authoritative organisation permissions.
- Membership is re-evaluated for protected tenant operations.

### Refresh tokens

- Opaque high-entropy values with a default seven-day lifetime.
- Stored only as SHA-256 hashes.
- Rotated after each successful refresh.
- Reuse of a replaced token revokes its token family.

The refresh token uses an `HttpOnly`, `Secure`, `SameSite=Strict` cookie outside local development. The access token remains in frontend memory, never `localStorage`.

## Authentication API

| Method and path | Behaviour |
|---|---|
| `POST /api/v1/auth/register` | Create an active user. |
| `POST /api/v1/auth/login` | Verify credentials, create a session, return an access token, and set the refresh cookie. |
| `POST /api/v1/auth/refresh` | Rotate the refresh token and return a new access token. |
| `POST /api/v1/auth/logout` | Revoke the current token family and clear the cookie. |
| `GET /api/v1/auth/me` | Return the current user and accessible organisation summaries. |

Registration does not automatically create an organisation.

## Organisation API

| Method and path | Required access |
|---|---|
| `POST /api/v1/organisations` | Authenticated user; caller becomes `OWNER`. |
| `GET /api/v1/organisations` | Lists caller memberships only. |
| `GET /api/v1/organisations/{organisationId}` | Any active membership. |
| `GET /api/v1/organisations/{organisationId}/members` | `OWNER` or `ADMIN`. |
| `POST /api/v1/organisations/{organisationId}/members` | `OWNER` or `ADMIN`; adds an existing user by email. |

## Project API

| Method and path | Required access |
|---|---|
| `POST /api/v1/organisations/{organisationId}/projects` | `OWNER` or `ADMIN`. |
| `GET /api/v1/organisations/{organisationId}/projects` | Any active membership. |
| `GET /api/v1/projects/{projectId}` | Membership in the owning organisation. |
| `PATCH /api/v1/projects/{projectId}` | `OWNER` or `ADMIN`. |

## Tenant-isolation rules

1. Client-supplied organisation identifiers are never proof of access.
2. Every tenant-owned query includes an authorised organisation boundary.
3. Controllers do not call unrestricted repositories directly.
4. Services verify current membership using the authenticated user identifier.
5. Cross-tenant access returns `404 Not Found` to conceal resource existence.
6. Tenant-scoped uniqueness includes the tenant identifier.
7. Integration tests use two organisations and prove read and write isolation.
8. Audit events record actor, organisation, action, target, and correlation identifier.

## Error model

| HTTP status | Use |
|---|---|
| `400` | Structurally invalid request. |
| `401` | Missing, invalid, or expired authentication. |
| `403` | Authenticated caller lacks permission for a visible organisation operation. |
| `404` | Missing resource or concealed cross-tenant resource. |
| `409` | Uniqueness or lifecycle conflict. |
| `422` | Domain-rule violation. |
| `429` | Rate limit exceeded. |

Errors retain the existing correlation identifier and a stable machine-readable code.

## Non-goals

- Social login or external identity providers.
- Password-reset email delivery.
- Multi-factor authentication.
- Project-specific roles.
- Ownership transfer or hard deletion.
- Invitation email delivery.
- SAML, OIDC federation, or SCIM.
- PostgreSQL row-level security.
- Automatic destructive remediation.

## Risks

| Risk | Mitigation |
|---|---|
| Cross-tenant data exposure | Scoped services, repository predicates, and two-tenant integration tests. |
| Stolen refresh token | Hashed storage, rotation, reuse detection, and family revocation. |
| Stale JWT authorisation | Short lifetime and current membership checks. |
| User enumeration | Generic login errors and concealed cross-tenant `404` responses. |
| Brute-force login | Rate limiting and audit events before Phase 2 completion. |
| Credential leakage in logs | Never log passwords, access tokens, refresh tokens, or credential bodies. |