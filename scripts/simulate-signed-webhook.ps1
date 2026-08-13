param(
  [Parameter(Mandatory = $true)][Guid] $EventSourceId,
  [Parameter(Mandatory = $true)][ValidateSet("GITHUB_ACTIONS", "JENKINS")][string] $Provider,
  [string] $BaseUrl = "http://localhost:8080",
  [string] $DeliveryId = ([Guid]::NewGuid().ToString()),
  [string] $Secret = $env:WEBHOOK_SECRET_LOCAL_SIMULATOR
)
$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($Secret)) { throw "Set WEBHOOK_SECRET_LOCAL_SIMULATOR or pass -Secret." }
$timestamp = [DateTime]::UtcNow.ToString("yyyy-MM-ddTHH:mm:ssZ")
if ($Provider -eq "GITHUB_ACTIONS") {
  $eventType = "workflow_run"
  $payload = '{"action":"completed","workflow_run":{"id":9001,"name":"demo-deployment","run_attempt":1,"status":"completed","conclusion":"failure","head_sha":"0123456789abcdef0123456789abcdef01234567","head_branch":"main","updated_at":"' + $timestamp + '"}}'
} else {
  $eventType = "build"
  $payload = '{"name":"demo-deployment","build":{"number":9001,"fullDisplayName":"demo-deployment #9001","result":"FAILURE","timestamp":' + [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds() + '},"scm":{"commitId":"0123456789abcdef0123456789abcdef01234567","branch":"main"}}'
}
$newline = [Environment]::NewLine
$signingInput = "CICD-WEBHOOK-V1$($newline)$DeliveryId$($newline)$eventType$($newline)$timestamp$($newline)$payload"
$hmac = [System.Security.Cryptography.HMACSHA256]::new([Text.Encoding]::UTF8.GetBytes($Secret))
try { $digest = [Convert]::ToHexString($hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($signingInput))).ToLowerInvariant() } finally { $hmac.Dispose() }
Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/event-sources/$EventSourceId/deliveries" -ContentType "application/json" -Headers @{
  "X-CICD-Delivery-ID" = $DeliveryId
  "X-CICD-Event-Type" = $eventType
  "X-CICD-Delivery-Timestamp" = $timestamp
  "X-CICD-Signature" = "sha256=$digest"
} -Body $payload
