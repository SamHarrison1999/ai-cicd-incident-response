# Infrastructure

This directory contains local-development and deployment infrastructure.

## Current contents

- PostgreSQL local-development documentation.
- Root Docker Compose orchestration.
- Service-specific Dockerfiles.
- Health checks and named volumes.
- Shared bridge networking.

## Start the full stack

From the repository root:

```powershell
Copy-Item .env.example .env
docker compose config
docker compose build
docker compose up -d
docker compose ps
```

Verify:

```powershell
.\scripts\verify-local-stack.ps1
```

Stop while retaining PostgreSQL data:

```powershell
docker compose down
```

Stop and delete the disposable local database:

```powershell
docker compose down --volumes
```
