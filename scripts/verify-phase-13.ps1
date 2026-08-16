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

$required = @(
    "README.md",
    "docs/adr/0060-security-hardening-boundary.md",
    "docs/adr/0061-transport-browser-credential-hardening.md",
    "docs/adr/0062-global-coverage-boundary.md",
    "docs/adr/0063-abuse-resistance-and-adversarial-verification.md",
    "docs/adr/0064-security-verification-workspace.md",
    "docs/phase-13-security-model.md",
    "docs/phase-13-transport-credentials.md",
    "docs/phase-13-abuse-resistance.md",
    "docs/phase-13-security-verification.md",
    "docs/phase-13-final-verification.md",
    "docs/phase-13-coverage.md",
    "docs/progress/ledger.md",
    "docs/progress/phase-13.md",
    "scripts/verify-phase-13-security.ps1",
    "scripts/verify-phase-13-coverage.ps1",
    "scripts/verify-phase-13.ps1",
    ".github/workflows/security-hardening.yml"
)
foreach ($relative in $required) {
    if (-not (Test-Path (Join-Path $repoRoot $relative))) {
        throw "Required Phase 13 close-out file is missing: $relative"
    }
}

$progress = [System.IO.File]::ReadAllText((Join-Path $repoRoot "docs/progress/phase-13.md"))
$progressForSearch = $progress.ToLowerInvariant()
foreach ($requiredText in @(
        "Security-hardening contract",
        "transport",
        "browser",
        "credential",
        "abuse resistance",
        "adversarial",
        "supply-chain",
        "fail closed",
        "strict coverage",
        "Batch 5 verification record"
    )) {
    if (-not $progressForSearch.Contains($requiredText.ToLowerInvariant())) {
        throw "Phase 13 progress content is missing: $requiredText"
    }
}

foreach ($status in @(
        "| 1 | Security-hardening contract, threat model, and verification boundary | COMPLETE_VERIFIED |",
        "| 2 | Transport, browser, credential, and secret hardening | COMPLETE_VERIFIED |",
        "| 3 | Abuse resistance, request limits, adversarial tests, and strict coverage | COMPLETE_VERIFIED |",
        "| 4 | Dependency, image, and security verification workspace | COMPLETE_VERIFIED |",
        "| 5 | Security, end-to-end, documentation, and Phase 13 verification | COMPLETE_VERIFIED |"
    )) {
    if (-not $progress.Contains($status)) {
        throw "Phase 13 batch is not marked COMPLETE_VERIFIED: $status"
    }
}

$ledger = [System.IO.File]::ReadAllText((Join-Path $repoRoot "docs/progress/ledger.md"))
if (-not $ledger.Contains("| 13 | Transport, credential, abuse-resistance, supply-chain, and adversarial security hardening | COMPLETE_VERIFIED |")) {
    throw "Phase 13 ledger is not marked COMPLETE_VERIFIED."
}

$readme = [System.IO.File]::ReadAllText((Join-Path $repoRoot "README.md"))
foreach ($requiredText in @(
        "Current phase:** Phase 13",
        "Phases 0 through 13 are complete",
        "Phase 13 security workspace verification",
        "Phase 13 final verification"
    )) {
    if (-not $readme.Contains($requiredText)) {
        throw "README Phase 13 close-out content is missing: $requiredText"
    }
}

Write-Host "==> Repository verification"
& (Join-Path $repoRoot "scripts/verify-repository.ps1")
if ($LASTEXITCODE -ne 0) {
    throw "Repository verification failed."
}

Write-Host "==> Security workspace verification"
if ($SkipDocker) {
    & (Join-Path $repoRoot "scripts/verify-phase-13-security.ps1") -SkipDocker
} else {
    & (Join-Path $repoRoot "scripts/verify-phase-13-security.ps1")
}
if ($LASTEXITCODE -ne 0) {
    throw "Security workspace verification failed."
}

Write-Host "==> Strict backend and frontend coverage verification"
& (Join-Path $repoRoot "scripts/verify-phase-13-coverage.ps1")
if ($LASTEXITCODE -ne 0) {
    throw "Strict Phase 13 coverage verification failed."
}

Write-Host "==> Git whitespace validation"
git diff --check
if ($LASTEXITCODE -ne 0) {
    throw "Git whitespace validation failed."
}

Write-Host "Phase 13 final verification completed successfully."
