$ErrorActionPreference = "Stop"
$repoRoot = (git rev-parse --show-toplevel 2>$null | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($repoRoot)) { throw "Run this script from inside the repository." }
Set-Location $repoRoot
Write-Host "==> Phase 13 repository verification"
& (Join-Path $repoRoot "scripts\verify-repository.ps1")
$required = @(
  "README.md",
  "docs\adr\0060-security-hardening-boundary.md",
  "docs\phase-13-security-model.md",
  "docs\progress\ledger.md",
  "docs\progress\phase-13.md",
  "docs\security-threat-model.md",
  "docs\testing-strategy.md",
  "docs\adr\0062-global-coverage-boundary.md",
  "docs\phase-13-coverage.md",
  "docs\testing-strategy.md",
  "scripts\verify-phase-13-coverage.ps1",
  "scripts\verify-phase-13.ps1"
)
foreach ($relative in $required) {
  if (-not (Test-Path (Join-Path $repoRoot $relative))) { throw "Required Phase 13 file is missing: $relative" }
}
$progress = [System.IO.File]::ReadAllText((Join-Path $repoRoot "docs\progress\phase-13.md"))
foreach ($requiredText in @("Security-hardening contract", "transport", "browser", "credential", "adversarial", "supply-chain", "fail closed", "strict coverage")) {
  if (-not $progress.Contains($requiredText)) { throw "Phase 13 content is missing: $requiredText" }
}
$readme = [System.IO.File]::ReadAllText((Join-Path $repoRoot "README.md"))
foreach ($requiredText in @("Current phase:** Phase 13", "Phase 13 local verification", "Security hardening")) {
  if (-not $readme.Contains($requiredText)) { throw "README Phase 13 content is missing: $requiredText" }
}
$ledger = [System.IO.File]::ReadAllText((Join-Path $repoRoot "docs\progress\ledger.md"))
if (-not $ledger.Contains("Security hardening") -or -not $ledger.Contains("IN_PROGRESS")) { throw "Phase 13 ledger entry is missing." }
Write-Host "`n==> Git whitespace validation"
git diff --check
if ($LASTEXITCODE -ne 0) { throw "Git whitespace validation failed." }
Write-Host "`nPhase 13 Batch 1 kickoff verification completed. Paste the complete output before staging or committing."
