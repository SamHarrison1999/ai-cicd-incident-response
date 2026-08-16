# Phase 14: Deployment, release, demo, and portfolio case study

Phase 14 packages the completed platform for reproducible local deployment,
release evidence, a portfolio demonstration, and an honest case study.

| Batch | Scope | Status |
| --- | --- | --- |
| 1 | Deployment and release contract and local runbook | IN_PROGRESS |
| 2 | Demo workflow and screenshot evidence | NOT_STARTED |
| 3 | Portfolio case study and close-out | NOT_STARTED |

## Run the local demo

From the repository root:

    Copy-Item .env.example .env -ErrorAction SilentlyContinue
    docker compose config --quiet
    docker compose build
    docker compose up -d
    .\scripts\verify-local-stack.ps1
    Start-Process "http://localhost:3000"

Or use:

    .\scripts\run-phase-14-demo.ps1 -Rebuild -OpenEndpoints

Suggested screenshots:

1. Authenticated workspace.
2. Operational overview.
3. Incident, pipeline, or evidence view.
4. Diagnosis or recommendation view.
5. Human-review state.
6. Swagger UI and intelligence OpenAPI.

Remove secrets, tokens, private data, and local filesystem paths.
