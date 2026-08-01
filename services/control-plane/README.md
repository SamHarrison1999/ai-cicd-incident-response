# Control Plane

The control plane is the authoritative Java and Spring Boot application for the platform.

## Planned responsibilities

- Authentication and authorisation.
- Organisation and project tenancy.
- Event-source configuration.
- Signed webhook ingestion.
- Idempotent event persistence.
- Pipeline-run aggregation.
- Incident correlation and lifecycle.
- Recommendation review workflow.
- Audit logging.
- REST and OpenAPI endpoints.
- Health checks and metrics.

## Ownership boundary

The control plane owns authoritative workflow state and the PostgreSQL schema. The Python intelligence service does not write directly to control-plane domain tables.

The executable Spring Boot project is added in Phase 1 Batch 2.
