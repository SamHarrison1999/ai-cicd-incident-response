# ADR 0012: Use Short-Lived JWT Access Tokens and Rotating Refresh Tokens

- Status: Accepted
- Date: 2026-08-03

## Context

The React frontend needs authenticated access to the Spring Boot control plane. The platform must support revocation, user disabling, multiple sessions, tenant-membership changes, and auditable session lifecycle events.

Long-lived JWTs make revocation and membership changes slow to enforce. Database-backed opaque tokens on every request add a session lookup to all API calls.

## Decision

Use a signed JWT access token with a default lifetime of 15 minutes and an opaque refresh token with a default lifetime of seven days.

Only SHA-256 refresh-token hashes are stored. Refresh tokens rotate after every successful refresh. Reuse of a replaced token revokes the whole token family.

The refresh token is transported in an HTTP-only cookie. The access token is returned in the response body and retained in frontend memory.

JWT claims identify the user and session but are not the authoritative source of organisation permissions. Protected tenant operations re-evaluate membership.

## Consequences

### Positive

- Access-token verification avoids a token lookup on every request.
- Short expiry limits stale authorisation.
- Rotation supports revocation and reuse detection.
- Session records provide audit history.
- Browser JavaScript cannot read the refresh token.

### Negative

- Rotation and reuse detection add persistence complexity.
- Cookie settings vary between local and deployed environments.
- Multiple token types increase test scope.
- Access tokens remain valid until expiry unless later deny-listing is added.

## Rejected alternatives

- Long-lived JWT only: insufficient revocation behaviour.
- Server-side session cookie only: requires a state lookup on every request.
- Browser local storage: exposes credentials to script compromise.