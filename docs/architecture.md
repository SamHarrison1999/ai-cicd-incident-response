# Architecture

## Architectural style

The system is a modular monorepo containing three deployable applications and supporting infrastructure:

1. A Java control plane owns identity, tenancy, ingestion, workflow, persistence, APIs, and auditability.
2. A Python intelligence service owns sanitisation, classification, retrieval, recommendation generation, and evaluation.
3. A React web application provides operational and governance workflows.

The initial release deliberately uses a modular monolith for the Java control plane rather than many Java microservices. This keeps local operation and transactional consistency manageable while preserving clear module boundaries that can later be extracted.

## C4 context

```mermaid
C4Context
    title AI-Assisted CI/CD Incident Response Platform â€” System Context
    Person(engineer, "Engineer", "Investigates pipeline and deployment incidents")
    Person(manager, "Engineering Manager", "Reviews incident and recommendation trends")
    System(platform, "Incident Response Platform", "Correlates delivery failures and provides evidence-grounded assistance")
    System_Ext(cicd, "Simulated CI/CD Sources", "Emit signed pipeline, deployment, test, and infrastructure events")
    System_Ext(aiProvider, "Optional AI Provider", "Produces structured text from sanitised evidence")

    Rel(cicd, platform, "Sends signed, versioned webhook events", "HTTPS/JSON")
    Rel(engineer, platform, "Investigates incidents and reviews recommendations", "HTTPS")
    Rel(manager, platform, "Reviews metrics and evaluations", "HTTPS")
    Rel(platform, aiProvider, "Sends sanitised evidence only", "HTTPS/JSON")
```

## Container architecture

```mermaid
C4Container
    title Container Architecture
    Person(user, "Platform user", "Engineer or manager")
    System_Ext(source, "Failure Event Generator", "Synthetic CI/CD and infrastructure source")
    System_Ext(provider, "Optional AI Provider", "External or local model adapter")

    Container(web, "Web Application", "React, TypeScript", "Operational UI")
    Container(control, "Control Plane", "Java, Spring Boot", "Identity, tenancy, ingestion, correlation, workflow, API, audit")
    Container(ai, "Intelligence Service", "Python, FastAPI", "Sanitisation, deterministic classification, retrieval, recommendations, evaluation")
    ContainerDb(db, "PostgreSQL", "Relational database", "Tenant-scoped product and audit data")
    Container(obs, "Observability Stack", "OpenTelemetry-compatible", "Logs, metrics, and traces")

    Rel(user, web, "Uses", "HTTPS")
    Rel(web, control, "Calls", "REST/JSON")
    Rel(source, control, "Sends signed events", "HTTPS/JSON")
    Rel(control, db, "Reads and writes", "JDBC")
    Rel(control, ai, "Requests analysis", "HTTP/JSON")
    Rel(ai, db, "Reads approved retrieval/evaluation data through controlled interface", "Initially API; no shared-table writes")
    Rel(ai, provider, "Optionally requests structured output", "HTTPS/JSON")
    Rel(control, obs, "Emits telemetry", "OTLP")
    Rel(ai, obs, "Emits telemetry", "OTLP")
```

## Control-plane modules

- `identity`: users, credentials, authentication tokens, and account lifecycle.
- `tenancy`: organisations, memberships, roles, projects, and tenant access policies.
- `eventsource`: source configuration, signing-secret metadata, and source status.
- `ingestion`: signature verification, idempotency, schema dispatch, and raw event persistence.
- `pipeline`: normalised events, pipeline runs, stages, jobs, and searchable timelines.
- `incident`: correlation, incident lifecycle, linked evidence, and state transitions.
- `recommendation`: requests, structured results, citations, review status, and version history.
- `resolution`: final human-authored outcomes and reusable historical knowledge.
- `evaluation`: benchmark definitions, runs, results, and quality metrics.
- `audit`: append-only security and business audit events.
- `shared`: narrowly scoped technical primitives such as identifiers, clocks, pagination, and error contracts.

Modules must not directly access another module's repository implementation. They communicate through application services, explicit ports, or published in-process domain events.

## Data ownership

PostgreSQL is the system of record. Each tenant-owned row contains an `organisation_id` or is reachable only through an aggregate that contains it. Application-level authorization is mandatory for every query; database row-level security is evaluated in Phase 13 as defence in depth.

Raw event payloads and normalised fields are stored separately. Raw payload retention is limited and configurable. Sanitised log content is stored separately from original log content so the system can prove which representation was supplied to an AI provider.

The Python service does not write directly into control-plane domain tables. It receives a bounded analysis request and returns a validated structured response. The Java control plane validates evidence references and persists the recommendation and audit metadata transactionally.

## Event ingestion sequence

```mermaid
sequenceDiagram
    autonumber
    participant Source as Simulated Event Source
    participant API as Java Ingestion API
    participant Verify as Signature/Schema Verifier
    participant DB as PostgreSQL
    participant Normalise as Normalisation Handler
    participant Correlate as Correlation Engine

    Source->>API: POST /api/v1/event-sources/{id}/events
    API->>Verify: Verify timestamp, signature, schema version, size
    Verify-->>API: Verified envelope
    API->>DB: Insert incoming event using idempotency key
    alt duplicate delivery
        DB-->>API: Existing event and prior outcome
        API-->>Source: 200/202 idempotent response
    else new delivery
        API->>Normalise: Dispatch versioned payload
        Normalise->>DB: Persist normalised event and pipeline-run update
        Normalise->>Correlate: Publish committed event reference
        Correlate->>DB: Link to incident or create incident
        API-->>Source: 202 Accepted with event ID
    end
```

## Incident correlation sequence

```mermaid
sequenceDiagram
    autonumber
    participant Event as Normalised Failure Event
    participant Engine as Correlation Engine
    participant DB as PostgreSQL
    participant Audit as Audit Service

    Event->>Engine: event(project, commit, environment, occurredAt)
    Engine->>DB: Find open candidate incidents in time window
    DB-->>Engine: Candidate incidents
    Engine->>Engine: Score deterministic correlation dimensions
    alt candidate exceeds threshold
        Engine->>DB: Link event and update incident summary fields
        Engine->>Audit: Record correlation decision and rule version
    else no candidate
        Engine->>DB: Create DETECTED incident and link event
        Engine->>Audit: Record incident creation and rule version
    end
```

## AI recommendation sequence

```mermaid
sequenceDiagram
    autonumber
    participant User as Engineer
    participant Control as Java Control Plane
    participant AI as Python Intelligence Service
    participant Provider as Optional AI Provider
    participant DB as PostgreSQL

    User->>Control: Request recommendation
    Control->>DB: Load tenant-authorized incident evidence
    Control->>AI: Analysis request with bounded evidence IDs
    AI->>AI: Redact secrets and detect injection patterns
    AI->>AI: Run deterministic classification and retrieval
    alt provider enabled and safety gate passes
        AI->>Provider: Sanitised evidence and structured-output contract
        Provider-->>AI: Proposed structured recommendation
    else offline or unsafe/insufficient
        AI->>AI: Produce deterministic result or abstention
    end
    AI->>AI: Validate citations, confidence, and abstention rules
    AI-->>Control: Structured recommendation plus audit metadata
    Control->>Control: Revalidate evidence IDs and tenant ownership
    Control->>DB: Persist recommendation, citations, prompt/model/rule versions
    Control-->>User: Pending human review
```

## Incident state machine

```mermaid
stateDiagram-v2
    [*] --> DETECTED
    DETECTED --> TRIAGED: engineer begins investigation
    TRIAGED --> MITIGATING: mitigation started
    MITIGATING --> MONITORING: service/pipeline appears recovered
    MONITORING --> RESOLVED: resolution verified
    MONITORING --> MITIGATING: regression detected
    DETECTED --> RESOLVED: false positive or externally resolved
    TRIAGED --> RESOLVED: diagnosis completed without mitigation
    RESOLVED --> REOPENED: recurrence or incomplete recovery
    REOPENED --> TRIAGED
    RESOLVED --> [*]
```

State transitions are performed by a domain service, guarded by role and current-state checks, and recorded as incident timeline and audit events. Records are not physically deleted through normal product workflows.

## Trust boundaries

1. Browser to control-plane API.
2. Simulated webhook source to ingestion API.
3. Java control plane to Python intelligence service.
4. Python intelligence service to optional model provider.
5. Services to PostgreSQL and observability backends.

Untrusted log text is data, never instruction. It is delimited, size-limited, sanitised, and excluded from system/developer instruction channels in provider adapters.

## Principal quality attributes

- **Auditability:** every correlation, recommendation, review, and state transition has actor and version metadata.
- **Safety:** abstention is a successful outcome; no autonomous remediation exists.
- **Security:** signed events, tenant checks, rate limits, least privilege, and secret redaction.
- **Operability:** deterministic local mode, health endpoints, metrics, traces, and synthetic scenarios.
- **Testability:** pure correlation/classification policies, contract tests, Testcontainers, and fixed evaluation cases.
- **Evolvability:** versioned event schemas and provider interfaces isolate external changes.

## Phase 2 identity and tenancy architecture

```mermaid
sequenceDiagram
    autonumber
    participant Browser
    participant Web as React Web
    participant API as Spring Control Plane
    participant DB as PostgreSQL

    Browser->>Web: Open protected route
    Web->>API: POST /api/v1/auth/refresh with HTTP-only cookie
    API->>DB: Find SHA-256 token hash and active family
    alt valid refresh session
        API->>DB: Rotate refresh token session
        API-->>Web: Access JWT plus replacement cookie
        Web->>API: Protected request with Bearer JWT
        API->>DB: Active membership and tenant-scoped query
        API-->>Web: Authorized resource
    else invalid, expired, or replayed token
        API->>DB: Revoke family when replay is detected
        API-->>Web: Stable authentication error
        Web-->>Browser: Redirect to login
    end
```

The Java control plane remains the authoritative security boundary. The frontend controls navigation and user experience but cannot grant access. Organisation membership and repository scoping are both required for tenant-owned resource access.
