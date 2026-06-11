param(
  [string]$BaseUrl = "https://zanzanai.top"
)

$ErrorActionPreference = "Stop"

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
    $curl = Get-Command curl.exe -ErrorAction SilentlyContinue
    if ($curl) {
      & curl.exe --fail-with-body --show-error --location --max-time 12 --connect-timeout 5 `
        --ssl-no-revoke --header "X-Request-Id: $requestId" $url
      if ($LASTEXITCODE -ne 0) {
        throw "curl exited with code $LASTEXITCODE"
      }
      Write-Host ""
    } else {
      $response = Invoke-WebRequest -Uri $url -Headers @{ "X-Request-Id" = $requestId } -UseBasicParsing -TimeoutSec 12
      Write-Host "STATUS $($response.StatusCode)"
      Write-Host $response.Content
    }
    Write-Host "RESULT ok requestId=$requestId"
  } catch {
    Write-Host "RESULT failed requestId=$requestId"
    Write-Host $_.Exception.Message
  }
}
