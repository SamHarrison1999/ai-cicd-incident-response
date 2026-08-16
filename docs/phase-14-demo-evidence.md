# Phase 14 demo evidence

This record defines the reproducible evidence for the local Docker Compose
demonstration. It is intentionally separate from production deployment claims.

## Run and verify

From the repository root:

~~~powershell
.\scripts\run-phase-14-demo.ps1 -Rebuild -OpenEndpoints
.\scripts\verify-phase-14-demo.ps1
~~~

For a documentation-only check on a machine without Docker:

~~~powershell
.\scripts\verify-phase-14-demo.ps1 -SkipStack
~~~

Expected checks:

- Docker Compose configuration renders successfully.
- PostgreSQL, the Java control plane, the Python intelligence service, and the
  React web application start in the expected dependency order.
- Control-plane health and system-status endpoints report UP.
- Intelligence health and system-status endpoints report UP.
- The web application returns HTTP 200 and its expected title.

## Screenshot sequence

Use a clean browser window at http://localhost:3000 after the stack passes
verification. Save working screenshots outside the repository or in a local
ignored evidence directory.

| Evidence | Route or endpoint | What it demonstrates |
| --- | --- | --- |
| 01 | / after sign-in | Authenticated engineering workspace |
| 02 | / | Operational readiness and service status |
| 03 | /organisations | Tenant boundary and organisation workflow |
| 04 | /pipelines | Pipeline timeline and bounded filters |
| 05 | /incidents | Incident workspace and lifecycle boundary |
| 06 | /evidence | Redacted evidence investigation |
| 07 | /diagnosis | Deterministic, bounded diagnosis |
| 08 | /recommendations | Evidence-grounded decision support |
| 09 | /review | Human review and governance boundary |
| 10 | /settings | Safety baseline and disabled automation |
| 11 | http://localhost:8080/swagger-ui.html | Control-plane API contract |
| 12 | http://localhost:8000/docs | Intelligence API contract |

The portfolio currently presents the overview screenshot as
img/AI CI-CD Incident Response Platform.png in the resume repository
(https://github.com/SamHarrison1999/resume) and links to the published
portfolio (https://samharrison1999.github.io/resume/).

## Publication rules

Before adding an image to a public portfolio:

- Remove browser chrome, operating-system taskbars, tokens, passwords, email
  addresses, database records, and local paths.
- Use synthetic or empty tenant data.
- Do not expose .env values or request headers.
- Prefer the overview and contract screenshots when a single image is needed.
- Treat the screenshots as product evidence, not proof of production readiness.
