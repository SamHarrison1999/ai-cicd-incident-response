[CmdletBinding()]
param(
    [switch]$SkipDocker
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = (git rev-parse --show-toplevel).Trim()
Set-Location $repoRoot

$requiredFiles = @(
    "docs/adr/0065-deployment-release-demo-boundary.md",
    "docs/phase-14-deployment-release-demo.md",
    "docs/phase-14-demo-evidence.md",
    "docs/phase-14-portfolio-case-study.md",
    "docs/phase-14-final-verification.md",
    "docs/progress/phase-14.md",
    "scripts/run-phase-14-demo.ps1",
    "scripts/verify-phase-14-demo.ps1"
)

$missing = @(
    $requiredFiles | Where-Object { -not (Test-Path $_) }
)

if ($missing.Count -gt 0) {
    throw "Phase 14 files are missing: $($missing -join ', ')"
}

if (-not $SkipDocker) {
    docker compose config --quiet
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose configuration failed."
    }
}

git diff --check
if ($LASTEXITCODE -ne 0) {
    throw "Git whitespace validation failed."
}

Write-Host "Phase 14 verification completed." -ForegroundColor Green
