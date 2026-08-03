# Phase 2 Progress

## Goal

Add secure identity, organisation membership, projects, and tenant-isolation foundations to the control plane and web application.

## Batches

| Batch | Scope | Status |
|---|---|---|
| 1 | Identity, tenancy, and API design | COMPLETE_VERIFIED |
| 2 | Persistence model and Flyway migrations | COMPLETE_VERIFIED |
| 3 | Authentication and session lifecycle | COMPLETE_VERIFIED |
| 4 | Organisation and project APIs with tenant isolation | COMPLETE_VERIFIED |
| 5 | Frontend authentication and organisation shell | COMPLETE_VERIFIED |
| 6 | Security tests, documentation, and Phase 2 verification | COMPLETE_UNVERIFIED |

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
## Batch 3 acceptance criteria

- Registration normalises email addresses and stores only BCrypt password hashes.
- Login uses generic credential errors and rejects disabled users.
- Access tokens are signed JWTs with issuer, audience, user, session, issued-at, and expiry claims.
- Refresh tokens are opaque, stored only as SHA-256 hashes, and rotated after successful refresh.
- Reuse of a replaced refresh token revokes its token family.
- Logout revokes the token family and expires the browser cookie.
- Refresh tokens use an HTTP-only SameSite Strict cookie.
- The API never returns refresh tokens in JSON.
- Security is stateless and protected endpoints require a valid bearer token.
- Validation and authentication failures return stable error codes with correlation identifiers.
- Unit tests cover token hashing, email normalisation, duplicate registration, password policy, generic login failure, and disabled-user rejection.
- Existing persistence and Phase 1 tests continue to pass.
- Developer-supplied output is recorded before Batch 3 is marked verified.

## Batch 3 verification record

Verified on 2026-08-03 from developer-supplied PowerShell and Gradle output:

- Spotless formatting completed successfully.
- The Java control-plane clean, check, and bootJar tasks completed successfully.
- All 21 Java tests passed.
- The Spring Boot application context started successfully.
- JWT resource-server security configuration loaded successfully.
- Registration email normalisation and password hashing tests passed.
- Duplicate registration and password-policy tests passed.
- Generic invalid-login handling passed.
- Disabled-user login rejection passed.
- SHA-256 refresh-token hashing tests passed.
- Existing persistence, tenant-boundary, correlation-ID, and system-status tests continued to pass.
- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories are tracked.
- Git whitespace and staged-diff validation passed.
## Batch 4 acceptance criteria

- Authenticated users can create organisations and become their owner.
- Users can list and retrieve only organisations with an active membership.
- Organisation updates require an owner or administrator role.
- Active organisation members can list and retrieve tenant-scoped projects.
- Owners, administrators, and members can create, update, and archive projects.
- Viewers cannot perform project mutations.
- Project lookups always include the organisation identifier.
- Cross-tenant and unauthorised resource access does not disclose resource existence.
- Organisation and project slug conflicts return stable conflict responses.
- Organisation and project mutations create audit events.
- Request validation enforces identifier and field-length rules.
- Unit tests cover missing membership, insufficient role, cross-tenant project lookup, and duplicate project slug handling.
- Existing authentication, persistence, and Phase 1 tests continue to pass.
- Developer-supplied output is recorded before Batch 4 is marked verified.

## Batch 4 verification record

Verified on 2026-08-03 from developer-supplied PowerShell and Gradle output:

- Spotless formatting completed successfully.
- The Java control-plane clean, check, and bootJar tasks completed successfully.
- All 25 Java tests passed.
- The Spring Boot application context started successfully.
- Organisation membership absence was handled without disclosing tenant existence.
- Insufficient organisation-role enforcement passed.
- Cross-tenant project lookup isolation passed.
- Duplicate project-slug handling passed.
- Existing authentication and refresh-token tests continued to pass.
- Existing identity persistence and tenant-boundary tests continued to pass.
- Existing correlation-ID and system-status tests continued to pass.
- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories are tracked.
- Git whitespace validation passed.
- Git diff validation produced no errors.

## Batch 5 acceptance criteria

- The frontend provides accessible registration and login pages.
- Registration validates the 12-character password minimum before submission.
- The browser never receives or stores a refresh token in JavaScript.
- Refresh-token cookies are sent with credentials for login, refresh, and logout.
- Access tokens are held in React state and attached only to protected API requests.
- Application startup attempts a cookie-backed session refresh.
- Unauthenticated users are redirected to the login page.
- Successful login returns the user to the originally requested route.
- Logout clears frontend authentication state and returns the user to login.
- Authenticated users can list and create organisations.
- The top bar displays the authenticated user without exposing token data.
- Tests cover unauthenticated redirection and successful login into the protected workspace.
- Prettier, ESLint, Vitest, production build, and Playwright continue to pass.
- Developer-supplied output is recorded before Batch 5 is marked verified.

## Batch 5 verification record

Verified on 2026-08-03 from developer-supplied PowerShell, Gradle, npm, Vitest and Playwright output:

- Prettier formatting completed successfully.
- Prettier format verification passed.
- ESLint completed with zero warnings.
- TypeScript compilation completed successfully.
- Vite production build completed successfully.
- Vitest executed successfully.
- All 7 frontend tests passed.
- Playwright end-to-end navigation test passed.
- React Router future flags were enabled for MemoryRouter tests, removing upgrade warnings.
- Authentication flow successfully redirected unauthenticated users.
- Login flow successfully displayed the protected dashboard after authentication.
- Repository structure validation passed.
- No unresolved implementation markers were found.
- No generated dependency or report directories are tracked.
- Git whitespace and staged-diff validation passed.
## Batch 6 acceptance criteria

- API documentation describes authentication, organisation, project, and error contracts.
- The security threat model documents session, token, role, tenant, browser, audit, and known-limitation boundaries.
- Architecture documentation includes the Phase 2 authentication and tenant-authorization sequence.
- Testing documentation records the Phase 2 release gates.
- The README accurately describes the current project phase and links the Phase 2 documentation.
- A repeatable `verify-phase-2.ps1` command validates repository, Java, Python, frontend, Playwright, Compose, and container quality gates.
- Java formatting, analysis, tests, coverage, and executable JAR packaging pass.
- Python lockfile, Ruff, MyPy, Pytest, and coverage checks pass.
- Frontend Prettier, ESLint, Vitest, production build, and Playwright pass.
- Docker Compose configuration and all application container builds pass.
- GitHub Actions required checks pass on the final Phase 2 pull request.
- Repository whitespace and unfinished-marker validation pass.
- Developer-supplied local and GitHub Actions output is recorded before Batch 6 and Phase 2 are marked verified.

## Batch 6 verification record

No verification output has been supplied yet for Batch 6.
