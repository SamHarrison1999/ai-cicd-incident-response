# Repository Structure

## Target monorepo

```text
.
├── .github/
│   ├── dependabot.yml
│   └── workflows/
│       ├── backend-ci.yml
│       ├── ai-service-ci.yml
│       ├── frontend-ci.yml
│       ├── integration-ci.yml
│       └── security.yml
├── backend/
│   ├── src/main/java/.../
│   ├── src/main/resources/
│   ├── src/test/java/.../
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── Dockerfile
├── ai-service/
│   ├── src/incident_ai/
│   ├── tests/
│   ├── evaluation/
│   ├── pyproject.toml
│   └── Dockerfile
├── frontend/
│   ├── src/
│   ├── tests/
│   ├── package.json
│   ├── vite.config.ts
│   └── Dockerfile
├── event-generator/
│   ├── src/
│   ├── scenarios/
│   ├── tests/
│   └── pyproject.toml
├── contracts/
│   ├── events/
│   ├── ai/
│   └── openapi/
├── docs/
│   ├── adr/
│   ├── progress/
│   ├── architecture.md
│   ├── product-scope.md
│   ├── product-backlog.md
│   ├── repository-structure.md
│   ├── risk-register.md
│   ├── setup.md
│   ├── api.md
│   ├── data-model.md
│   ├── event-schemas.md
│   ├── incident-correlation.md
│   ├── ai-system-card.md
│   ├── evaluation.md
│   ├── security-threat-model.md
│   ├── testing-strategy.md
│   ├── deployment.md
│   ├── demo-script.md
│   └── limitations.md
├── infra/
│   ├── compose/
│   ├── observability/
│   └── kubernetes/
├── scripts/
├── .editorconfig
├── .gitignore
├── compose.yaml
├── LICENSE
├── Makefile
└── README.md
```

## Phase 0 files

Phase 0 creates only planning and governance artefacts:

```text
README.md
docs/architecture.md
docs/product-scope.md
docs/product-backlog.md
docs/repository-structure.md
docs/risk-register.md
docs/testing-strategy.md
docs/limitations.md
docs/progress/ledger.md
docs/adr/0001-use-a-modular-monolith-control-plane.md
docs/adr/0002-separate-java-control-plane-and-python-intelligence-service.md
docs/adr/0003-use-postgresql-as-system-of-record.md
docs/adr/0004-evidence-grounded-human-reviewed-ai.md
docs/adr/0005-use-a-versioned-signed-event-envelope.md
.editorconfig
.gitignore
LICENSE
```

Executable service directories are intentionally deferred to Phase 1 so the generated skeletons, build files, formatter configuration, and CI workflows form one coherent and testable change.

## Naming conventions

- Java base package: `com.samuelharrison.incidentresponse`.
- Python package: `incident_ai`.
- React application name: `incident-response-web`.
- Database identifiers: lowercase `snake_case`.
- REST resources: plural lowercase kebab-case only when multiple words are needed.
- Event types: dot-separated lowercase names, for example `pipeline.run.failed`.
- Event schema IDs: stable URI-like identifiers under `io.github.samharrison1999.incident-response`.
- Correlation IDs and aggregate IDs: UUIDs represented canonically as lowercase strings at boundaries.

## Repository rules

- No service imports another service's source tree.
- Cross-service contracts live under `contracts/` and are versioned.
- Generated artefacts are reproducible and not committed unless needed by consumers.
- Secrets, private keys, provider credentials, and real incident logs are never committed.
- Database changes are forward-only migrations after a release tag.
- Every phase updates the progress ledger and relevant documentation.
