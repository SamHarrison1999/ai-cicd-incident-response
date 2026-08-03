[CmdletBinding()]
param(
    [switch]$SkipDocker,
    [switch]$SkipPlaywright
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $repositoryRoot

function Invoke-Step {
    param(
        [Parameter(Mandatory)][string]$Name,
        [Parameter(Mandatory)][scriptblock]$Command
    )

    Write-Host ""
    Write-Host "==> $Name" -ForegroundColor Cyan
    & $Command

    if ($LASTEXITCODE -ne 0) {
        throw "$Name failed with exit code $LASTEXITCODE."
    }
}

Invoke-Step "Repository validation" {
    & ".\scripts\verify-repository.ps1"
}

Invoke-Step "Java control-plane quality gate" {
    Push-Location ".\services\control-plane"
    try {
        & ".\gradlew.bat" spotlessCheck clean check bootJar --no-configuration-cache
    }
    finally {
        Pop-Location
    }
}

Invoke-Step "Python intelligence-service quality gate" {
    Push-Location ".\services\intelligence-service"
    try {
        & python -m uv lock --check
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

        & python -m uv run ruff format --check .
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

        & python -m uv run ruff check .
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

        & python -m uv run mypy src tests
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

        & python -m uv run pytest
    }
    finally {
        Pop-Location
    }
}

Invoke-Step "Frontend quality gate" {
    Push-Location ".\web"
    try {
        & npm ci
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

        & npm run format:check
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

        & npm run lint
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

        & npm run test
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

        & npm run build
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

        if (-not $SkipPlaywright) {
            & npm run test:e2e
        }
    }
    finally {
        Pop-Location
    }
}

if (-not $SkipDocker) {
    Invoke-Step "Docker Compose configuration" {
        & docker compose config --quiet
    }

    Invoke-Step "Application container builds" {
        & docker compose build
    }
}

Invoke-Step "Git whitespace validation" {
    & git diff --check
}

Write-Host ""
Write-Host "Phase 2 local verification completed successfully." -ForegroundColor Green
Write-Host "Record the command output before marking Batch 6 and Phase 2 verified."
