param(
  [string]$HostName = "zanzanai.top"
)

$ErrorActionPreference = "Continue"

Write-Host "DNS"
Resolve-DnsName $HostName | Select-Object Name, Type, IPAddress, NameHost

Write-Host "`nTCP 443"
Test-NetConnection $HostName -Port 443 | Select-Object ComputerName, RemoteAddress, RemotePort, TcpTestSucceeded

Write-Host "`nHTTP minimal"
try {
  curl.exe --show-error --max-time 10 "http://$HostName/tls-ping"
  Write-Host "`nHTTP_RESULT ok"
} catch {
  Write-Host "HTTP_RESULT failed"
  Write-Host $_.Exception.Message
}

Write-Host "`nHTTPS minimal"
try {
  curl.exe --show-error --max-time 10 --ssl-no-revoke "https://$HostName/tls-ping"
  if ($LASTEXITCODE -ne 0) {
    throw "curl exited with code $LASTEXITCODE"
  }
  Write-Host "`nHTTPS_RESULT ok"
} catch {
  Write-Host "HTTPS_RESULT failed"
  Write-Host $_.Exception.Message
}

Write-Host "`nNode HTTPS minimal"
node -e "const https=require('https'); const req=https.get('https://$HostName/tls-ping',{timeout:10000},res=>{let d='';res.on('data',c=>d+=c);res.on('end',()=>console.log('NODE_HTTPS_RESULT ok',res.statusCode,d));}); req.on('error',e=>console.log('NODE_HTTPS_RESULT failed',e.code,e.message)); req.on('timeout',()=>{console.log('NODE_HTTPS_RESULT timeout'); req.destroy();});"
