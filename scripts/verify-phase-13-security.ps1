[CmdletBinding()]
param(
    [switch]$SkipDocker
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = (git rev-parse --show-toplevel 2>$null | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($repoRoot)) {
    throw "Run this script from inside the repository."
}
Set-Location $repoRoot

Write-Host "==> Phase 13 security workspace validation"

$required = @(
    ".github/dependabot.yml",
    ".github/workflows/dependency-review.yml",
    ".github/workflows/security-hardening.yml",
    "services/control-plane/build.gradle.kts",
    "services/control-plane/settings.gradle.kts",
    "services/intelligence-service/pyproject.toml",
    "services/intelligence-service/uv.lock",
    "web/package.json",
    "web/package-lock.json",
    "services/control-plane/Dockerfile",
    "services/intelligence-service/Dockerfile",
    "web/Dockerfile",
    "docker-compose.yml"
)
foreach ($relative in $required) {
    if (-not (Test-Path (Join-Path $repoRoot $relative))) {
        throw "Required Phase 13 security file is missing: $relative"
    }
}

$trackedSensitive = @(
    git ls-files | Where-Object {
        $_ -match '(^|/)\.env$' -or
        $_ -match '(^|/)secrets/' -or
        $_ -match '\.(pem|key|p12|pfx)$'
    }
)
if ($trackedSensitive.Count -ne 0) {
    throw "Sensitive files are tracked: $($trackedSensitive -join ', ')"
}

$controlDockerfile = [System.IO.File]::ReadAllText((Join-Path $repoRoot "services/control-plane/Dockerfile"))
$intelligenceDockerfile = [System.IO.File]::ReadAllText((Join-Path $repoRoot "services/intelligence-service/Dockerfile"))
foreach ($nameAndText in @(
        @("services/control-plane/Dockerfile", $controlDockerfile),
        @("services/intelligence-service/Dockerfile", $intelligenceDockerfile)
    )) {
    if ($nameAndText[1] -notmatch '(?m)^USER\s+appuser\s*$') {
        throw "Production container must declare a non-root appuser: $($nameAndText[0])"
    }
}

git diff --check
if ($LASTEXITCODE -ne 0) {
    throw "Git whitespace validation failed."
}

if (-not $SkipDocker) {
    Write-Host "==> Docker Compose security rendering"
    docker compose config --quiet
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose security rendering failed."
    }
}

Write-Host "Phase 13 security workspace verification completed."
