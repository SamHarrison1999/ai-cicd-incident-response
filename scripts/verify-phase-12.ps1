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
if (-not (Test-Path (Join-Path $repoRoot "docs\progress\phase-12.md"))) { throw "Phase 12 progress file is missing." }
$wrapperCandidates = @("gradlew.bat", "gradlew", "services\control-plane\gradlew.bat", "services\control-plane\gradlew") | ForEach-Object { Join-Path $repoRoot $_ } | Where-Object { Test-Path $_ }
if ($wrapperCandidates.Count -eq 0) { throw "The Gradle wrapper was not found." }
$wrapper = $wrapperCandidates | Select-Object -First 1
Write-Host "`n==> Java cumulative verification"
Invoke-NativeCommand $wrapper @("-p", "services/control-plane", "clean", "check", "bootJar", "--no-daemon")
$webPath = Join-Path $repoRoot "web"
Push-Location $webPath
try {
  Write-Host "`n==> Frontend cumulative verification"
  Invoke-NativeCommand "npm.cmd" @("run", "format")
  Invoke-NativeCommand "npm.cmd" @("run", "format:check")
  Invoke-NativeCommand "npm.cmd" @("run", "lint")
  Invoke-NativeCommand "npm.cmd" @("run", "test", "--", "--run")
  Invoke-NativeCommand "npm.cmd" @("run", "build")
} finally { Pop-Location }
Write-Host "`n==> Docker Compose configuration validation"
Invoke-NativeCommand "docker" @("compose", "config", "-q")
Write-Host "`n==> Git whitespace validation"
Invoke-NativeCommand "git" @("diff", "--check")
Write-Host "`nPhase 12 cumulative verification completed."
