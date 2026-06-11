param(
  [string]$BaseUrl = "http://localhost:8080"
)

$checks = @(
  "/api/diagnostics/ready",
  "/api/mini/banners",
  "/api/mini/categories",
  "/api/mini/items?pageSize=3",
  "/api/mini/cos"
)

foreach ($path in $checks) {
  $requestId = [guid]::NewGuid().ToString()
  $url = "$BaseUrl$path"
  Write-Host "CHECK $url requestId=$requestId"
  try {
    $response = Invoke-WebRequest -Uri $url -Headers @{ "X-Request-Id" = $requestId } -UseBasicParsing -TimeoutSec 10
    Write-Host "STATUS $($response.StatusCode)"
    Write-Host $response.Content
  } catch {
    Write-Host "RESULT failed requestId=$requestId"
    Write-Host $_.Exception.Message
  }
}
