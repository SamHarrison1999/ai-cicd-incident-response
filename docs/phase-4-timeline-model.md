# Phase 4 timeline model

## Canonical event shape

The implementation must expose a versioned provider-neutral representation with these conceptual fields:

| Field | Purpose |
|---|---|
| `eventId` | Stable internal identity |
| `organisationId` / `projectId` | Tenant and project boundary |
| `eventSourceId` / `provider` | Source provenance |
| `providerDeliveryId` | External idempotency identity |
| `externalRunId` / `attempt` | Pipeline-run identity |
| `eventType` | Allow-listed canonical event type |
| `status` | Canonical lifecycle status |
| `occurredAt` / `receivedAt` | Domain and ingestion time |
| `orderingKey` | Stable timeline ordering component |
| `summary` | Bounded safe display metadata |
| `schemaVersion` | Contract evolution boundary |

The exact Java names and persistence mapping are implementation decisions for Batch 2, but the externally visible semantics must remain equivalent.

## Ordering

Timeline results are ordered ascending or descending by a compound key derived from:

1. `occurredAt`;
2. `receivedAt`;
3. `eventId`.

The cursor must encode the complete compound position. A cursor is invalid if it cannot be decoded or if its direction/filter scope does not match the request.

## Filters

The timeline API must support optional filters for:

- pipeline status;
- branch;
- commit SHA;
- environment;
- canonical event type;
- occurred-at time range.

Filters are applied within the requested organisation and project boundary. Unknown filter values return a stable validation error rather than silently broadening the query.

## Run aggregation

Events with the same project, source, external run identity, and attempt contribute to one pipeline-run projection. Duplicate deliveries do not create duplicate timeline events. Late events may enrich a run but cannot move a terminal run backwards. A new attempt remains distinguishable from an earlier attempt.

## Safety boundary

The timeline is descriptive. It does not infer causality, assign incidents, generate recommendations, or execute remediation. Those responsibilities begin in later phases and must consume this contract through explicit interfaces.