[CmdletBinding()]
param(
    [string]$WebBaseUrl = "http://localhost:3000",
    [string]$ControlPlaneBaseUrl = "http://localhost:8080",
    [string]$IntelligenceBaseUrl = "http://localhost:8000"
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Assert-Status {
    param(
        [Parameter(Mandatory)]
        [string]$Name,

        [Parameter(Mandatory)]
        [string]$Url,

        [Parameter(Mandatory)]
        [scriptblock]$Assertion
    )

    Write-Host "Checking $Name at $Url..." -ForegroundColor Cyan
    $response = Invoke-RestMethod -Uri $Url -Method Get

    if (-not (& $Assertion $response)) {
        throw "$Name returned an unexpected response."
    }

    Write-Host "$Name is healthy." -ForegroundColor Green
}

Assert-Status `
    -Name "Control plane" `
    -Url "$ControlPlaneBaseUrl/actuator/health" `
    -Assertion { param($response) $response.status -eq "UP" }

Assert-Status `
    -Name "Intelligence service" `
    -Url "$IntelligenceBaseUrl/health/ready" `
    -Assertion { param($response) $response.status -eq "UP" }

Assert-Status `
    -Name "Control-plane system endpoint" `
    -Url "$ControlPlaneBaseUrl/api/v1/system/status" `
    -Assertion {
        param($response)
        $response.service -eq "control-plane" -and $response.status -eq "UP"
    }

Assert-Status `
    -Name "Intelligence-service system endpoint" `
    -Url "$IntelligenceBaseUrl/api/v1/system/status" `
    -Assertion {
        param($response)
        $response.service -eq "intelligence-service" -and $response.status -eq "UP"
    }

Write-Host "Checking web application at $WebBaseUrl..." -ForegroundColor Cyan
$webResponse = Invoke-WebRequest -Uri $WebBaseUrl -UseBasicParsing

if ($webResponse.StatusCode -ne 200) {
    throw "Web application returned HTTP $($webResponse.StatusCode)."
}

if ($webResponse.Content -notmatch "CI/CD Incident Response") {
    throw "Web application response did not contain the expected title."
}

Write-Host "Web application is healthy." -ForegroundColor Green
Write-Host ""
Write-Host "All local platform checks passed." -ForegroundColor Green
