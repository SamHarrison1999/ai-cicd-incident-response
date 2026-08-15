$ErrorActionPreference = "Stop"
$repoRoot = (git rev-parse --show-toplevel 2>$null | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($repoRoot)) { throw "Run this script from inside the repository." }
Set-Location $repoRoot
function Invoke-NativeCommand {
  param([string]$FilePath, [string[]]$Arguments = @())
  & $FilePath @Arguments
  if ($LASTEXITCODE -ne 0) { throw "Command failed with exit code $LASTEXITCODE`: $FilePath $($Arguments -join ' ')" }
}
Write-Host "==> Phase 12 repository verification"
Invoke-NativeCommand (Join-Path $repoRoot "scripts\verify-repository.ps1")
$progressPath = Join-Path $repoRoot "docs\progress\phase-12.md"
if (-not (Test-Path $progressPath)) { throw "Phase 12 progress file is missing." }
$progress = [System.IO.File]::ReadAllText($progressPath)
foreach ($requiredText in @("| 1 | Operational-learning contract, trend dimensions, and tenant boundary | COMPLETE_VERIFIED |", "| 2 | Trend persistence and deterministic observation windows | COMPLETE_VERIFIED |", "| 3 | Bounded trend API and comparison responses | COMPLETE_VERIFIED |", "| 4 | Operational-learning workspace | COMPLETE_VERIFIED |", "| 5 | Security, end-to-end, documentation, and Phase 12 verification | COMPLETE_VERIFIED |")) {
  if (-not $progress.Contains($requiredText)) { throw "Phase 12 completion content is missing: $requiredText" }
}
$wrapperCandidates = @("gradlew.bat", "gradlew", "services\control-plane\gradlew.bat", "services\control-plane\gradlew") | ForEach-Object { Join-Path $repoRoot $_ } | Where-Object { Test-Path $_ }
if ($wrapperCandidates.Count -eq 0) { throw "The Gradle wrapper was not found." }
$wrapper = $wrapperCandidates | Select-Object -First 1
Write-Host "`n==> Java formatting, cumulative checks, and executable JAR"
Invoke-NativeCommand $wrapper @("-p", "services/control-plane", "spotlessApply")
Invoke-NativeCommand $wrapper @("-p", "services/control-plane", "clean", "check", "bootJar", "--no-daemon")
Push-Location (Join-Path $repoRoot "web")
try {
  Write-Host "`n==> Frontend formatting, lint, tests, and build"
  Invoke-NativeCommand "npm.cmd" @("run", "format:check")
  Invoke-NativeCommand "npm.cmd" @("run", "lint")
  Invoke-NativeCommand "npm.cmd" @("run", "test", "--", "--run")
  Invoke-NativeCommand "npm.cmd" @("run", "build")
} finally { Pop-Location }
Write-Host "`n==> Docker Compose configuration validation"
Invoke-NativeCommand "docker" @("compose", "config", "--quiet")
Write-Host "`n==> Git whitespace validation"
Invoke-NativeCommand "git" @("diff", "--check")
Write-Host "`nPhase 12 cumulative verification completed successfully."
