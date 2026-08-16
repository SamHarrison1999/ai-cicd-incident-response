[CmdletBinding()]
param(
    [string]$RepositoryRoot = (Get-Location).Path
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$root = (Resolve-Path $RepositoryRoot).Path
Set-Location $root

$requiredFiles = @(
    ".editorconfig",
    ".gitattributes",
    ".gitignore",
    ".java-version",
    ".python-version",
    ".nvmrc",
    "README.md",
    "docker-compose.yml",
    "docker-compose.override.yml",
    "services/control-plane/build.gradle.kts",
    "services/control-plane/gradlew",
    "services/intelligence-service/pyproject.toml",
    "services/intelligence-service/uv.lock",
    "web/package.json",
    "web/package-lock.json",
    "docs/architecture.md",
    "docs/testing-strategy.md",
    "docs/deployment.md",
    "docs/progress/phase-1.md"
)

$missingFiles = @(
    $requiredFiles |
        Where-Object { -not (Test-Path (Join-Path $root $_)) }
)

if ($missingFiles.Count -ne 0) {
    throw "Required repository files are missing: $($missingFiles -join ', ')"
}

$searchRoots = @(
    "services/control-plane/src",
    "services/control-plane/config",
    "services/intelligence-service/src",
    "services/intelligence-service/tests",
    "web/src",
    "web/tests",
    "infrastructure",
    "scripts"
)

$currentScriptPath = [System.IO.Path]::GetFullPath($PSCommandPath)

$implementationFiles = @(
    foreach ($searchRoot in $searchRoots) {
        Get-ChildItem `
            -Path (Join-Path $root $searchRoot) `
            -Recurse `
            -File |
            Where-Object {
                [System.IO.Path]::GetFullPath($_.FullName) -ne $currentScriptPath
            }
    }
)

$unfinishedMatches = @(
    $implementationFiles |
        Select-String `
            -Pattern '\b(TODO|TBD|FIXME|PLACEHOLDER)\b' `
            -CaseSensitive:$false
)

if ($unfinishedMatches.Count -ne 0) {
    $locations = $unfinishedMatches |
        ForEach-Object { "$($_.Path):$($_.LineNumber)" }
    throw "Unresolved implementation markers found: $($locations -join ', ')"
}

$composeFiles = @(
    Join-Path $root "docker-compose.yml"
    Join-Path $root "docker-compose.override.yml"
)

$composeMatches = @(
    Select-String `
        -Path $composeFiles `
        -Pattern '\b(TODO|TBD|FIXME|PLACEHOLDER)\b' `
        -CaseSensitive:$false
)

if ($composeMatches.Count -ne 0) {
    throw "Unresolved markers found in Docker Compose files."
}

$trackedGeneratedPaths = @(
    git ls-files |
        Where-Object {
            ($_ -match '(^|/)(node_modules|\.venv|dist|build|coverage|htmlcov|playwright-report|test-results)(/|$)') -and
            ($_ -notmatch '^services/control-plane/src/test/java/com/samharrison/incidentresponse/coverage/ProductionSurfaceCoverageSupport\.java$')
        }
)

if ($trackedGeneratedPaths.Count -ne 0) {
    throw "Generated or dependency directories are tracked: $($trackedGeneratedPaths -join ', ')"
}

git diff --check
if ($LASTEXITCODE -ne 0) {
    throw "Git whitespace validation failed."
}

Write-Host "Repository structure validation passed." -ForegroundColor Green
Write-Host "No unresolved implementation markers were found." -ForegroundColor Green
Write-Host "No generated dependency or report directories are tracked." -ForegroundColor Green
Write-Host "Git whitespace validation passed." -ForegroundColor Green
