param(
    [Parameter(Mandatory = $true)]
    [ValidateRange(3, 10)]
    [int]$Phase
)

$ErrorActionPreference = "Stop"
$repoRoot = (git rev-parse --show-toplevel 2>$null | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($repoRoot)) {
    throw "Run this script from inside the repository."
}
Set-Location $repoRoot

function Invoke-NativeCommand {
    param(
        [Parameter(Mandatory = $true)] [string]$FilePath,
        [Parameter(Mandatory = $false)] [string[]]$Arguments = @()
    )
    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed with exit code $LASTEXITCODE`: $FilePath $($Arguments -join ' ')"
    }
}

Write-Host "==> Phase $Phase repository verification"
Invoke-NativeCommand (Join-Path $repoRoot "scripts\verify-repository.ps1")
$progressPath = Join-Path $repoRoot "docs\progress\phase-$Phase.md"
if (-not (Test-Path $progressPath)) {
    throw "Phase $Phase progress file is missing: $progressPath"
}

$wrapperCandidates = @("gradlew.bat", "gradlew", "services\control-plane\gradlew.bat", "services\control-plane\gradlew") |
    ForEach-Object { Join-Path $repoRoot $_ } | Where-Object { Test-Path $_ }
if ($wrapperCandidates.Count -eq 0) {
    throw "The Gradle wrapper was not found."
}
$wrapper = $wrapperCandidates | Select-Object -First 1
Write-Host "==> Phase $Phase Java verification"
Invoke-NativeCommand $wrapper @("-p", "services/control-plane", "spotlessApply")
Invoke-NativeCommand $wrapper @("-p", "services/control-plane", "clean", "check", "bootJar", "--no-daemon")

Push-Location (Join-Path $repoRoot "web")
try {
    Write-Host "==> Phase $Phase frontend verification"
    Invoke-NativeCommand "npm" @("run", "format:check")
    Invoke-NativeCommand "npm" @("run", "lint")
    Invoke-NativeCommand "npm" @("run", "test", "--", "--run")
    Invoke-NativeCommand "npm" @("run", "build")
} finally {
    Pop-Location
}

Write-Host "==> Docker Compose verification"
Invoke-NativeCommand "docker" @("compose", "config", "--quiet")
Write-Host "==> Git whitespace verification"
Invoke-NativeCommand "git" @("diff", "--check")
Write-Host "Phase $Phase local verification completed successfully."
