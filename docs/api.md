# API Reference

## Conventions

- Base path: `/api/v1`
- Media type: `application/json`
- Protected endpoints require `Authorization: Bearer <access-token>`.
- Refresh tokens are transported only in an HTTP-only, SameSite Strict cookie.
- Refresh tokens are never returned in JSON.
- Error responses use stable machine-readable codes and include a correlation identifier when available.
- Tenant-owned resources are always addressed through an organisation boundary.

## Authentication

### Register

```http
POST /api/v1/auth/register
```

Request:

```json
{
  "email": "sam@example.com",
  "displayName": "Sam Harrison",
  "password": "a-long-example-password"
}
```

Security behaviour:

- Email is normalised before uniqueness checks.
- Passwords are stored only as BCrypt hashes.
- Passwords must contain at least 12 characters.
- Duplicate accounts return a stable conflict response.

### Login

```http
POST /api/v1/auth/login
```

Request:

```json
{
  "email": "sam@example.com",
  "password": "a-long-example-password"
}
```

Successful response:

```json
{
  "accessToken": "<signed-jwt>",
  "tokenType": "Bearer",
  "expiresInSeconds": 900,
  "user": {
    "userId": "00000000-0000-0000-0000-000000000001",
    "email": "sam@example.com",
    "displayName": "Sam Harrison"
  }
}
```

The response also sets the refresh-token cookie. Invalid credentials use a generic failure message so callers cannot distinguish unknown emails from incorrect passwords.

### Refresh

```http
POST /api/v1/auth/refresh
```

The browser sends the HTTP-only refresh-token cookie. A successful refresh rotates the opaque token, replaces its stored SHA-256 hash, and returns a new access token.

Reusing a replaced refresh token revokes the token family.

### Logout

```http
POST /api/v1/auth/logout
```

Logout revokes the refresh-token family and expires the browser cookie.

## Organisations

### Create organisation

```http
POST /api/v1/organisations
Authorization: Bearer <access-token>
```

Request:

```json
{
  "name": "Platform Engineering",
  "slug": "platform-engineering"
}
```

The authenticated user becomes the organisation owner.

### List organisations

```http
GET /api/v1/organisations
Authorization: Bearer <access-token>
```

Returns organisations for which the current user has an active membership.

### Get organisation

```http
GET /api/v1/organisations/{organisationId}
Authorization: Bearer <access-token>
```

Users without an active membership receive a not-found response so tenant existence is not disclosed.

### Rename organisation

```http
PATCH /api/v1/organisations/{organisationId}
Authorization: Bearer <access-token>
```

Request:

```json
{
  "name": "Developer Platform"
}
```

Allowed roles: `OWNER`, `ADMIN`.

## Projects

All project routes include the organisation identifier.

### Create project

```http
POST /api/v1/organisations/{organisationId}/projects
Authorization: Bearer <access-token>
```

Request:

```json
{
  "name": "Payments API",
  "slug": "payments-api",
  "description": "CI/CD incident monitoring for the payments service"
}
```

Allowed roles: `OWNER`, `ADMIN`, `MEMBER`.

### List projects

```http
GET /api/v1/organisations/{organisationId}/projects
Authorization: Bearer <access-token>
```

Allowed roles: any active organisation member.

### Get project

```http
GET /api/v1/organisations/{organisationId}/projects/{projectId}
Authorization: Bearer <access-token>
```

The repository lookup is constrained by both `organisationId` and `projectId`.

### Update project

```http
PATCH /api/v1/organisations/{organisationId}/projects/{projectId}
Authorization: Bearer <access-token>
```

Request:

```json
{
  "name": "Payments Platform",
  "description": "Updated project description"
}
```

Allowed roles: `OWNER`, `ADMIN`, `MEMBER`.

### Archive project

```http
POST /api/v1/organisations/{organisationId}/projects/{projectId}/archive
Authorization: Bearer <access-token>
```

Archiving is a lifecycle transition. Normal product workflows do not physically delete projects.

## Signed webhook ingestion

~~~http
POST /api/v1/event-sources/{eventSourceId}/deliveries
Content-Type: application/json
X-CICD-Delivery-ID: provider-delivery-42
X-CICD-Event-Type: workflow_run
X-CICD-Delivery-Timestamp: 2026-08-13T12:00:00Z
X-CICD-Signature: sha256=<64 lowercase hexadecimal characters>
~~~

The webhook signature authenticates the request; a bearer token is not used.
The signature binds a version prefix, delivery ID, event type, exact timestamp
header, and exact payload bytes. The timestamp must fall within the configured
event-source tolerance.

Successful first delivery:

~~~json
{
  "deliveryId": "00000000-0000-0000-0000-000000000010",
  "duplicate": false,
  "status": "RECEIVED",
  "receivedAt": "2026-08-13T12:00:02Z"
}
~~~

A retry with the same provider delivery ID, event type, and exact payload bytes
returns HTTP 202 with the original delivery identifier and `duplicate: true`.
Reusing the provider delivery ID with a different event type or payload returns
HTTP 409. Batch 3 stores only the digest and safe metadata; raw bodies, signatures,
and secrets are not persisted.

## Error contract

Representative response:

```json
{
  "timestamp": "2026-08-03T15:30:00Z",
  "status": 403,
  "code": "INSUFFICIENT_ORGANISATION_ROLE",
  "message": "Your organisation role does not permit this operation.",
  "path": "/api/v1/organisations/00000000-0000-0000-0000-000000000001/projects",
  "correlationId": "e2c1861a-37c0-40d0-b7d7-8f594a33a596"
}
```

Important codes include:

| Code | Meaning |
|---|---|
| `AUTHENTICATION_REQUIRED` | No valid authenticated principal is available |
| `INVALID_ACCESS_TOKEN` | The token subject is invalid |
| `INVALID_CREDENTIALS` | Login failed without disclosing which credential was wrong |
| `ORGANISATION_NOT_FOUND` | Organisation is absent or inaccessible to the caller |
| `PROJECT_NOT_FOUND` | Project is absent from the requested organisation boundary |
| `INSUFFICIENT_ORGANISATION_ROLE` | Membership exists but the role cannot perform the operation |
| `ORGANISATION_SLUG_IN_USE` | Organisation slug is already used |
| `PROJECT_SLUG_IN_USE` | Project slug is already used in that organisation |
| `RESOURCE_CONFLICT` | A database uniqueness constraint rejected the operation |
| `EVENT_SOURCE_NOT_FOUND` | Event source is unknown or disabled |
| `WEBHOOK_HEADER_MISSING` | A required signed webhook header is absent |
| `WEBHOOK_HEADER_INVALID` | A webhook header exceeds its limit or contains unsafe characters |
| `WEBHOOK_SIGNATURE_MALFORMED` | Signature does not match the required wire format |
| `WEBHOOK_SIGNATURE_INVALID` | Constant-time HMAC comparison failed |
| `WEBHOOK_TIMESTAMP_INVALID` | Delivery timestamp is not valid RFC 3339 UTC |
| `WEBHOOK_TIMESTAMP_OUTSIDE_TOLERANCE` | Signed delivery is too old or too far in the future |
| `WEBHOOK_PAYLOAD_TOO_LARGE` | Body exceeds the event-source limit |
| `WEBHOOK_CONTENT_TYPE_UNSUPPORTED` | Request is not JSON |
| `WEBHOOK_JSON_INVALID` | Verified body is not syntactically valid JSON |
| `WEBHOOK_DELIVERY_PAYLOAD_CONFLICT` | Delivery ID was reused with a different event type or payload |
| `WEBHOOK_SECRET_UNAVAILABLE` | Configured signing material cannot be resolved |
