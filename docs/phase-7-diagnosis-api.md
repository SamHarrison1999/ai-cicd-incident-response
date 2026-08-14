# Phase 7 diagnosis API and workspace

Batch 4 exposes the deterministic diagnosis engine through:

`GET /api/v1/organisations/{organisationId}/projects/{projectId}/diagnosis`

The response contains the rule version, suspected or abstaining category,
bounded confidence, supporting signal identifiers, warnings, missing evidence,
and an abstention reason. It never returns raw evidence content, credentials,
signatures, or remediation actions.

The Diagnosis workspace uses the existing authenticated application shell and
clearly presents results as decision support for human review.
