# Phase 13 Batch 4: dependency, image, and security verification

The security verification workspace provides a repeatable local and CI
boundary for supply-chain checks.

## Local checks

From the repository root in PowerShell:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\verify-phase-13-security.ps1
```

When Docker is unavailable, run the repository, lockfile, secret-exclusion,
Dockerfile, and whitespace checks without Compose rendering:

```powershell
.\scripts\verify-phase-13-security.ps1 -SkipDocker
```

The script never creates tracked reports or dependency directories. It checks
for accidental `.env`, key, certificate, and secrets-path tracking; validates
the Gradle build configuration plus the uv and npm lockfiles; checks the
production container user boundaries; and renders Docker Compose when
requested.

GitHub Actions adds CodeQL analysis and dependency review. Those checks are
defence in depth and do not replace the application-level tenant, redaction,
validation, and adversarial tests.
