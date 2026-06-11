#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://127.0.0.1:8080}"

for path in \
  "/api/diagnostics/ready" \
  "/api/mini/banners" \
  "/api/mini/categories" \
  "/api/mini/items?pageSize=3" \
  "/api/mini/cos"; do
  request_id="$(date +%s)-$RANDOM"
  echo "CHECK $BASE_URL$path requestId=$request_id"
  curl --fail-with-body --show-error --max-time 12 --connect-timeout 5 \
    -H "X-Request-Id: $request_id" "$BASE_URL$path"
  echo
done
