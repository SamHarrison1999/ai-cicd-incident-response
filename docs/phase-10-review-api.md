# Phase 10 Batch 3: review and resolution API

This batch exposes bounded review history, review submission, and incident-resolution creation. All mutations are authenticated, tenant-scoped, and recorded with the actor identity. Review responses contain governance metadata only; they do not return raw evidence or executable remediation instructions.

Endpoints:

- `GET /api/v1/organisations/{organisationId}/projects/{projectId}/recommendations/{recommendationId}/reviews`
- `POST /api/v1/organisations/{organisationId}/projects/{projectId}/recommendations/{recommendationId}/reviews`
- `POST /api/v1/organisations/{organisationId}/projects/{projectId}/incidents/{incidentId}/resolutions`
