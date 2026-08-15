$ErrorActionPreference = "Stop"
$repoRoot = (git rev-parse --show-toplevel 2>$null | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($repoRoot)) { throw "Run this script from inside the repository." }
Set-Location $repoRoot
function Invoke-NativeCommand {
  param([string]$FilePath, [string[]]$Arguments = @())
  & $FilePath @Arguments
  if ($LASTEXITCODE -ne 0) { throw "Command failed with exit code $LASTEXITCODE`: $FilePath $($Arguments -join ' ')" }
}
Write-Host "==> Phase 11 repository verification"
Invoke-NativeCommand (Join-Path $repoRoot "scripts\verify-repository.ps1")
if (-not (Test-Path (Join-Path $repoRoot "docs\progress\phase-11.md"))) { throw "Phase 11 progress file is missing." }
$wrapperCandidates = @("gradlew.bat", "gradlew", "services\control-plane\gradlew.bat", "services\control-plane\gradlew") | ForEach-Object { Join-Path $repoRoot $_ } | Where-Object { Test-Path $_ }
if ($wrapperCandidates.Count -eq 0) { throw "The Gradle wrapper was not found." }
$wrapper = $wrapperCandidates | Select-Object -First 1
Invoke-NativeCommand $wrapper @("-p", "services/control-plane", "spotlessApply")
Invoke-NativeCommand $wrapper @("-p", "services/control-plane", "clean", "check", "bootJar", "--no-daemon")
Push-Location (Join-Path $repoRoot "web")
try {
  Invoke-NativeCommand "npm" @("run", "format:check")
  Invoke-NativeCommand "npm" @("run", "lint")
  Invoke-NativeCommand "npm" @("run", "test", "--", "--run")
  Invoke-NativeCommand "npm" @("run", "build")
} finally { Pop-Location }
Invoke-NativeCommand "docker" @("compose", "config", "--quiet")
Invoke-NativeCommand "git" @("diff", "--check")
Write-Host "Phase 11 local verification completed successfully."
