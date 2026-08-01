# PostgreSQL Local Infrastructure

PostgreSQL is the system of record for the Java control plane.

## Schema ownership

The PostgreSQL container does not use ad hoc `/docker-entrypoint-initdb.d` schema scripts.

All application schema changes are owned by Flyway migrations in:

```text
services/control-plane/src/main/resources/db/migration/
```

This prevents local Compose startup from creating database objects through a second, competing migration mechanism.

## Local credentials

Docker Compose reads the following values from `.env` when present:

```text
POSTGRES_DB
POSTGRES_USER
POSTGRES_PASSWORD
POSTGRES_PORT
```

The committed defaults are suitable only for local development.

## Persistent volume

The named volume is:

```text
incident-response-postgres-data
```

Stop services without deleting data:

```powershell
docker compose down
```

Delete the local database volume:

```powershell
docker compose down --volumes
```

Deleting the volume is destructive and should only be used for disposable local data.
