# Local Setup

## Status

Phase 1 is establishing the executable project foundation. At the end of Batch 1, the repository contains the monorepo layout and runtime-version declarations but no runnable application services yet.

## Required tooling baseline

- Git.
- Docker Desktop with Docker Compose.
- Java 25.
- Python 3.14.
- Node.js 24 LTS.
- PowerShell 7 or Windows PowerShell 5.1.

Exact application dependencies are pinned in the service-specific lockfiles and build files introduced in later Phase 1 batches.

## Clone and select the working branch

```powershell
git clone https://github.com/SamHarrison1999/ai-cicd-incident-response.git
Set-Location .\ai-cicd-incident-response
git checkout chore/phase-1-project-skeleton
```

## Verify installed runtimes

```powershell
git --version
docker --version
docker compose version
java --version
python --version
node --version
npm --version
```

The `.java-version`, `.python-version`, and `.nvmrc` files document the repository runtime baseline. Developers may use any compatible version manager.

## Environment variables

`.env.example` contains non-secret local-development variable names and defaults. Do not commit a populated `.env` file.

Signed webhook simulations resolve secrets through an opaque event-source
reference. The built-in local reference is `local-simulator`; configure its
value before sending webhook requests:

~~~powershell
$env:WEBHOOK_SECRET_LOCAL_SIMULATOR = "replace-with-a-long-random-local-secret"
~~~

The secret value is not stored in PostgreSQL, returned by APIs, or written to
application logs.

Docker Compose support is introduced in Phase 1 Batch 5.

## Line endings

`.editorconfig` and `.gitattributes` provide repository-wide text-file conventions. Most source files use LF endings. Windows-native command files may use CRLF.

## Current limitation

There are no executable services or automated application tests after Batch 1. This is intentional; the Java, Python, and frontend skeletons are introduced in Batches 2 through 4.

## Docker Compose local platform

After installing Docker Desktop:

```powershell
Copy-Item .env.example .env
docker compose config
docker compose build
docker compose up -d
.\scripts\verify-local-stack.ps1
```

The default local endpoints are:

- Web: `http://localhost:3000`
- Control plane: `http://localhost:8080`
- Intelligence service: `http://localhost:8000`
- PostgreSQL: `localhost:5432`

Use `docker compose down` to stop services while preserving the database volume.
