#!/bin/bash
# Runs once, on first start of the MySQL container: one database and one restricted user per service.
set -euo pipefail

for svc in auth product supplier invoice; do
  var="$(echo "$svc" | tr '[:lower:]' '[:upper:]')_DB_PASSWORD"
  password="${!var}"
  mysql -uroot -p"$MYSQL_ROOT_PASSWORD" <<SQL
CREATE DATABASE IF NOT EXISTS ${svc}_db CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS '${svc}'@'%' IDENTIFIED BY '${password}';
GRANT ALL PRIVILEGES ON ${svc}_db.* TO '${svc}'@'%';
FLUSH PRIVILEGES;
SQL
done
