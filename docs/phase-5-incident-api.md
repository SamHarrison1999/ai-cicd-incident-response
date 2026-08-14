# Phase 5 incident API

## List incidents

```text
GET /api/v1/organisations/{organisationId}/projects/{projectId}/incidents
Authorization: Bearer <access-token>
```

The caller must have active membership in the organisation. The project must belong to that organisation. The response is ordered by detection time descending and contains incident ID, status, title, bounded summary, and lifecycle timestamps.

## Get one incident

```text
GET /api/v1/organisations/{organisationId}/projects/{projectId}/incidents/{incidentId}
Authorization: Bearer <access-token>
```

An incident outside the requested tenant and project boundary is returned as not found.

## Transition status

```text
PATCH /api/v1/organisations/{organisationId}/projects/{projectId}/incidents/{incidentId}/status
Content-Type: application/json
Authorization: Bearer <access-token>

{
  "status": "TRIAGED",
  "occurredAt": "2026-08-14T12:00:00Z"
}
```

Only `OWNER`, `ADMIN`, and `MEMBER` roles may request a transition. The domain aggregate validates the current-to-next state pair and records the action in the audit stream. `VIEWER` members can read but cannot transition. Invalid transitions do not change the incident.

Responses never contain raw webhook payloads, signatures, signing material, or provider credentials.