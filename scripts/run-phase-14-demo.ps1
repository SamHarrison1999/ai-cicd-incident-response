[CmdletBinding()]
param(
    [switch]$Rebuild,
    [switch]$OpenEndpoints
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = (git rev-parse --show-toplevel).Trim()
Set-Location $repoRoot

if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
}

docker compose config --quiet

if ($LASTEXITCODE -ne 0) {
    throw "Docker Compose configuration failed."
}

if ($Rebuild) {
    docker compose build

    if ($LASTEXITCODE -ne 0) {
        throw "Docker build failed."
    }
}

docker compose up -d

if ($LASTEXITCODE -ne 0) {
    throw "Docker Compose startup failed."
}

& ".\scripts\verify-local-stack.ps1"

Write-Host "Demo ready: http://localhost:3000" -ForegroundColor Green
Write-Host "Swagger: http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "Intelligence OpenAPI: http://localhost:8000/docs" -ForegroundColor Cyan

if ($OpenEndpoints) {
    Start-Process "http://localhost:3000"
    Start-Process "http://localhost:8080/swagger-ui.html"
    Start-Process "http://localhost:8000/docs"
}
