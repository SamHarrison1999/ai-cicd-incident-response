# Control Plane

The control plane is the authoritative Java and Spring Boot application for the platform.

## Technology baseline

- Java 25.
- Spring Boot 4.0.7.
- Gradle 9.6.1.
- PostgreSQL.
- Flyway.
- Spring Security.
- Spring Data JPA.
- Spring Boot Actuator.
- Micrometer Prometheus registry.
- springdoc OpenAPI.
- JUnit, AssertJ, Mockito, and Testcontainers.

## Responsibilities

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

## Run quality checks

From this directory:

```powershell
.\gradlew.bat clean check
```

The integration test starts PostgreSQL through Testcontainers. Docker Desktop must be running.

## Run locally

A local PostgreSQL service is required. Docker Compose support is added in Phase 1 Batch 5.

With a compatible database available:

```powershell
$env:SPRING_PROFILES_ACTIVE = "local"
.\gradlew.bat bootRun
```

Endpoints:

- `GET http://localhost:8080/api/v1/system/status`
- `GET http://localhost:8080/actuator/health`
- `GET http://localhost:8080/actuator/info`
- `GET http://localhost:8080/actuator/prometheus`
- `GET http://localhost:8080/swagger-ui.html`
- `GET http://localhost:8080/v3/api-docs`

## Security baseline

Health, info, API documentation, and the system status endpoint are public during the foundation phase. All other requests require authentication. Domain authentication is implemented in Phase 2.

CSRF is disabled because the control plane is designed as a stateless REST API. This decision will be revisited alongside token authentication in Phase 2.
