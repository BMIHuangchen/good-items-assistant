#!/usr/bin/env bash
set -euo pipefail

HOST_NAME="${1:-zanzanai.top}"

echo "Local Nginx HTTP"
curl --fail-with-body --show-error --max-time 10 "http://127.0.0.1/tls-ping"
echo

echo "Local Nginx HTTPS with SNI"
curl --fail-with-body --show-error --max-time 10 --resolve "$HOST_NAME:443:127.0.0.1" "https://$HOST_NAME/tls-ping"
echo

echo "Public HTTPS from server"
curl --fail-with-body --show-error --max-time 10 "https://$HOST_NAME/tls-ping"
echo
