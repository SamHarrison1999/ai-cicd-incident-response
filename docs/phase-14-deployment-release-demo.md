# Phase 14: Deployment, release, demo, and portfolio case study

Phase 14 packages the completed platform for reproducible local deployment,
release evidence, a portfolio demonstration, and an honest case study.

| Batch | Scope | Status |
| --- | --- | --- |
| 1 | Deployment contract and local runbook | COMPLETE_VERIFIED |
| 2 | Demo workflow and screenshot evidence | COMPLETE_VERIFIED |
| 3 | Portfolio case study and close-out | COMPLETE_VERIFIED |

The phase remains IN_PROGRESS until the commands in the final verification
checklist have been run on the reviewed commit and their output recorded.

## Local demonstration

From the repository root:

~~~powershell
.\scripts\run-phase-14-demo.ps1 -Rebuild -OpenEndpoints
~~~

The script validates Compose configuration, builds the local images when
requested, starts the stack, checks the health and system-status endpoints,
and opens the web application, control-plane Swagger UI, and intelligence
OpenAPI documentation.

Use the individual commands when a step needs to be inspected:

~~~powershell
Copy-Item .env.example .env -ErrorAction SilentlyContinue
docker compose config --quiet
docker compose build
docker compose up -d
.\scripts\verify-local-stack.ps1
Start-Process "http://localhost:3000"
Start-Process "http://localhost:8080/swagger-ui.html"
Start-Process "http://localhost:8000/docs"
~~~

## Screenshot evidence

The screenshot checklist and privacy rules are in Phase 14 demo evidence.
The portfolio-facing case study is in Phase 14 portfolio case study.

Capture only the application content. Crop browser chrome, taskbars, tokens,
credentials, private data, database contents, and local filesystem paths before
publishing any image.

## Scope boundary

This phase does not claim production readiness. It does not introduce cloud
credentials, public image publishing, TLS termination, managed PostgreSQL,
backup automation, Kubernetes production manifests, or autonomous remediation.
