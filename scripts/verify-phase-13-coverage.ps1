$ErrorActionPreference = "Stop"
$repoRoot = (git rev-parse --show-toplevel 2>$null | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($repoRoot)) {
    throw "Run this script from inside the repository."
}
Set-Location $repoRoot

$controlPlaneGradle = Join-Path $repoRoot "services\control-plane\gradlew.bat"
$webRoot = Join-Path $repoRoot "web"
if (-not (Test-Path $controlPlaneGradle)) {
    throw "The control-plane Gradle wrapper was not found."
}
if (-not (Test-Path (Join-Path $webRoot "package.json"))) {
    throw "The web package manifest was not found."
}

Write-Host "==> Backend 100% coverage gate"
& $controlPlaneGradle -p (Join-Path $repoRoot "services\control-plane") clean test jacocoTestReport jacocoTestCoverageVerification
if ($LASTEXITCODE -ne 0) {
    throw "Backend coverage verification failed with exit code $LASTEXITCODE."
}

Write-Host "`n==> Frontend 100% coverage gate"
Push-Location $webRoot
try {
    & npm run test -- --run --coverage
    if ($LASTEXITCODE -ne 0) {
        throw "Frontend coverage verification failed with exit code $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}

Write-Host "`nPhase 13 strict coverage verification completed."
