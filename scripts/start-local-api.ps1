param(
  [string]$AdminPassword = "local-test-password",
  [string]$JwtSecret = "local-test-secret-local-test-secret-123456"
)

$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$apiDir = Join-Path $root "java-code"
$jar = Join-Path $apiDir "target/good-items-assistant-api-0.0.1-SNAPSHOT.jar"

Set-Location -LiteralPath $apiDir
& java -jar $jar "--ADMIN_PASSWORD=$AdminPassword" "--JWT_SECRET=$JwtSecret"
