$ErrorActionPreference = "Stop"
$repoRoot = (git rev-parse --show-toplevel 2>$null | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($repoRoot)) {
    throw "Run this script from inside the repository."
}
Set-Location $repoRoot
if ((git status --porcelain).Trim()) {
    throw "Working tree must be clean before applying Phase 13 Batch 3."
}

$sourceRoot = $PSScriptRoot
$files = @(
    "README.md",
    "docs/adr/0062-global-coverage-boundary.md",
    "docs/phase-13-coverage.md",
    "docs/progress/phase-13.md",
    "docs/testing-strategy.md",
    "scripts/verify-phase-13-coverage.ps1",
    "scripts/verify-phase-13.ps1",
    "services/control-plane/build.gradle.kts",
    "web/vite.config.ts"
)
foreach ($relative in $files) {
    $source = Join-Path $sourceRoot $relative
    $target = Join-Path $repoRoot ($relative -replace '/', '\')
    $parent = Split-Path -Parent $target
    if (-not (Test-Path $parent)) {
        New-Item -ItemType Directory -Path $parent -Force | Out-Null
    }
    Copy-Item -LiteralPath $source -Destination $target -Force
}

Write-Host "Phase 13 Batch 3 strict coverage contract applied."
Write-Host "No files were committed. Run verify-phase-13-batch-3.ps1, then run scripts/verify-phase-13-coverage.ps1."
