#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APP_DIR="/opt/good-items/api"
WEB_DIR="/var/www/good-items-admin"
DB_DIR="/opt/good-items/database"

echo "Create directories"
sudo mkdir -p "$APP_DIR" "$WEB_DIR" "$DB_DIR"

echo "Install API jar"
sudo cp "$ROOT_DIR/java-code/target/good-items-assistant-api-0.0.1-SNAPSHOT.jar" "$APP_DIR/good-items-assistant-api.jar"

echo "Install admin web"
sudo rsync -a --delete "$ROOT_DIR/web-code/dist/" "$WEB_DIR/"

echo "Install database migration scripts"
sudo cp "$ROOT_DIR/database/schema.sql" "$DB_DIR/schema.sql"
sudo cp "$ROOT_DIR/database/seed.sql" "$DB_DIR/seed.sql"
if [ -f "$ROOT_DIR/database/migration_user_ai_usage_20260614.sql" ]; then
  sudo cp "$ROOT_DIR/database/migration_user_ai_usage_20260614.sql" "$DB_DIR/migration_user_ai_usage_20260614.sql"
fi

echo "Install systemd service template"
sudo cp "$ROOT_DIR/deploy/good-items-api.service" /etc/systemd/system/good-items-api.service

echo "Install nginx template"
sudo cp "$ROOT_DIR/deploy/nginx-good-items.conf" /etc/nginx/conf.d/good-items.conf

echo "Reload services"
sudo systemctl daemon-reload
sudo systemctl enable good-items-api
sudo systemctl restart good-items-api
sudo nginx -t
sudo systemctl reload nginx

echo "Done. Run scripts/online-check.ps1 from local machine after DNS/HTTPS is ready."
