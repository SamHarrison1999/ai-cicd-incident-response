# Phase 14 portfolio case study

## AI-Assisted CI/CD Incident Response Platform

View the project on GitHub:
https://github.com/SamHarrison1999/ai-cicd-incident-response

View the portfolio:
https://samharrison1999.github.io/resume/

### Problem

CI/CD failures are often investigated across disconnected pipeline logs, event
records, incident notes, and prior resolutions. The platform was designed as a
bounded engineering workspace for bringing those signals together without
turning an AI suggestion into an unattended production action.

### Solution

The platform combines:

- A Java and Spring Boot control plane for identity, tenant isolation, event
  ingestion, pipeline timelines, incidents, evidence, recommendations, review,
  feedback, and operational learning.
- A Python FastAPI intelligence service for deterministic diagnosis and
  provider-neutral decision support.
- A React and TypeScript web application for operational views, investigation,
  recommendations, human review, feedback, and learning.
- PostgreSQL persistence, Flyway migrations, Docker Compose orchestration, and
  GitHub Actions quality gates.

### Engineering decisions

The system treats logs as untrusted input, sanitises and redacts evidence,
keeps results tenant-scoped, records provenance, bounds recommendation inputs
and outputs, and requires human review. Deterministic behaviour is used for
the local demonstration so the same evidence produces repeatable results.

### Verification evidence

The repository records strict Java, Python, and frontend coverage gates,
security and adversarial checks, Docker Compose validation, container builds,
health checks, API contract inspection, and the Phase 14 screenshot checklist.

The local demonstration is started with:

~~~powershell
.\scripts\run-phase-14-demo.ps1 -Rebuild -OpenEndpoints
.\scripts\verify-phase-14-demo.ps1
~~~

### Honest limitations

This is a portfolio-grade hosted and local demonstration, not a production
deployment. The hosted environment is intended for synthetic portfolio
verification and does not claim production-grade availability, managed backups,
capacity guarantees, or autonomous remediation.
It does not claim managed infrastructure, TLS termination, secret-manager
integration, public image publication, backup and restore automation,
Kubernetes operations, measured capacity, or autonomous remediation.

### Portfolio presentation

The portfolio screenshot is deliberately cropped to the application content.
No credentials, tokens, private data, database contents, or local filesystem
paths belong in the published case study.
