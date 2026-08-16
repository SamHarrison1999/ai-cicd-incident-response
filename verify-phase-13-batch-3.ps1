$ErrorActionPreference = "Stop"
$repoRoot = (git rev-parse --show-toplevel 2>$null | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($repoRoot)) {
    throw "Run this script from inside the repository."
}
Set-Location $repoRoot

Write-Host "==> Repository verification"
& (Join-Path $repoRoot "scripts\verify-repository.ps1")

$required = @(
    "docs\adr\0062-global-coverage-boundary.md",
    "docs\phase-13-coverage.md",
    "scripts\verify-phase-13-coverage.ps1"
)
foreach ($relative in $required) {
    if (-not (Test-Path (Join-Path $repoRoot $relative))) {
        throw "Required Phase 13 Batch 3 file is missing: $relative"
    }
}

$progress = [System.IO.File]::ReadAllText((Join-Path $repoRoot "docs\progress\phase-13.md"))
foreach ($requiredText in @("strict coverage", "100%", "meaningful tests")) {
    if (-not $progress.Contains($requiredText)) {
        throw "Phase 13 Batch 3 progress content is missing: $requiredText"
    }
}

$readme = [System.IO.File]::ReadAllText((Join-Path $repoRoot "README.md"))
foreach ($requiredText in @("Phase 12 local verification", "Phase 13 local verification", "Strict coverage verification")) {
    if (-not $readme.Contains($requiredText)) {
        throw "README coverage content is missing: $requiredText"
    }
}

Write-Host "`n==> Git whitespace validation"
git diff --check
if ($LASTEXITCODE -ne 0) {
    throw "Git whitespace validation failed."
}

Write-Host "`nPhase 13 Batch 3 coverage contract verification completed."
Write-Host "The strict coverage command is expected to fail until all uncovered production paths have meaningful tests."
