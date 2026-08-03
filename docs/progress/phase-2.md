# Phase 2 Progress

## Goal

Add secure identity, organisation membership, projects, and tenant-isolation foundations to the control plane and web application.

## Batches

| Batch | Scope | Status |
|---|---|---|
| 1 | Identity, tenancy, and API design | COMPLETE_VERIFIED |
| 2 | Persistence model and Flyway migrations | COMPLETE_VERIFIED |
| 3 | Authentication and session lifecycle | NOT_STARTED |
| 4 | Organisation and project APIs with tenant isolation | NOT_STARTED |
| 5 | Frontend authentication and organisation shell | NOT_STARTED |
| 6 | Security tests, documentation, and Phase 2 verification | NOT_STARTED |

## Batch 1 acceptance criteria

- Authentication and session boundaries are explicitly documented.
- Organisation roles and permissions are explicitly documented.
- Tenant-owned resources have a documented ownership rule.
- API contracts are defined before implementation.
- Database entities, uniqueness rules, and lifecycle states are defined.
- JWT and refresh-token trade-offs are recorded in an ADR.
- Tenant-isolation enforcement is recorded in an ADR.
- Phase 2 risks and non-goals are documented.
- Repository whitespace and unfinished-marker checks produce no errors.
- Developer-supplied command output is recorded before Batch 1 is marked verified.

## Batch 1 verification record

Verified on 2026-08-03 from developer-supplied PowerShell output:

- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories are tracked.
- Git whitespace validation passed.
- Git staged-diff validation produced no errors.
- No TODO, TBD, FIXME, or PLACEHOLDER markers were found in the Batch 1 documents.
- The five expected Phase 2 Batch 1 design documents were staged.

## Phase 2 completion criteria

Phase 2 is complete only when all six batches are `COMPLETE_VERIFIED`, all local and GitHub Actions quality gates pass, and the final Phase 2 pull request is merged into `main`.
## Batch 2 acceptance criteria

- Flyway creates users, organisations, memberships, projects, refresh-token sessions, and audit events.
- JPA entities map to the migration without Hibernate schema generation.
- Normalised email uniqueness is enforced by PostgreSQL.
- Organisation membership uniqueness is enforced by PostgreSQL.
- Project slugs are unique within an organisation and reusable across organisations.
- Refresh-token hashes are unique.
- Project repository lookups can require an organisation boundary.
- Repository and domain-model tests cover the principal constraints.
- Existing Phase 1 tests continue to pass.
- Formatting, static analysis, tests, coverage, and executable JAR packaging pass.
- Developer-supplied output is recorded before Batch 2 is marked verified.

## Batch 2 verification record

Verified on 2026-08-03 from developer-supplied PowerShell and Gradle output:

- Spotless formatting completed successfully.
- The Java control-plane clean, check, and bootJar tasks completed successfully.
- All 14 Java tests passed.
- Flyway applied the Phase 2 migration against PostgreSQL Testcontainers.
- User normalised-email uniqueness was verified.
- Organisation membership uniqueness was verified.
- Project slug uniqueness within an organisation was verified.
- Project slug reuse across different organisations was verified.
- Organisation-scoped project lookup isolation was verified.
- Refresh-token hash uniqueness was verified.
- Existing Phase 1 control-plane tests continued to pass.
- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories are tracked.
- Git whitespace and staged-diff validation passed.
