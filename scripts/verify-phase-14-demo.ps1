[CmdletBinding()]
param(
    [switch]$SkipStack
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = (git rev-parse --show-toplevel).Trim()
Set-Location $repoRoot

$requiredFiles = @(
    "docs/phase-14-demo-evidence.md",
    "docs/phase-14-portfolio-case-study.md",
    "docs/phase-14-final-verification.md",
    "scripts/run-phase-14-demo.ps1",
    "scripts/verify-phase-14.ps1",
    "scripts/verify-local-stack.ps1"
)

$missing = @(
    $requiredFiles | Where-Object { -not (Test-Path $_) }
)

if ($missing.Count -gt 0) {
    throw "Phase 14 demo files are missing: $($missing -join ', ')"
}

$requiredText = @(
    "http://localhost:3000",
    "http://localhost:8080/swagger-ui.html",
    "http://localhost:8000/docs",
    "https://samharrison1999.github.io/resume/",
    "Remove browser chrome"
)

$evidenceText = Get-Content "docs/phase-14-demo-evidence.md" -Raw
foreach ($marker in $requiredText) {
    if ($evidenceText -notmatch [Regex]::Escape($marker)) {
        throw "Phase 14 demo evidence is missing: $marker"
    }
}

if (-not $SkipStack) {
    & ".\scripts\verify-local-stack.ps1"
    if ($LASTEXITCODE -ne 0) {
        throw "Local stack verification failed."
    }
}

git diff --check
if ($LASTEXITCODE -ne 0) {
    throw "Git whitespace validation failed."
}

Write-Host "Phase 14 demo verification completed." -ForegroundColor Green
