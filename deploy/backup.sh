#!/usr/bin/env bash
# 项目交付管理后台 - 备份脚本
# 直接原地运行即可（会自动读取同目录下的 .env）：
#   /opt/pdm/src/deploy/backup.sh
# cron: 0 2 * * * /opt/pdm/src/deploy/backup.sh >> /data/pdm/logs/backup-cron.log 2>&1
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
if [ -z "${ENV_FILE:-}" ]; then
  for f in "$SCRIPT_DIR/.env" /opt/pdm/.env; do
    if [ -f "$f" ]; then ENV_FILE=$f; break; fi
  done
fi
if [ -z "${ENV_FILE:-}" ] || [ ! -f "$ENV_FILE" ]; then
  echo "找不到 .env，请用 ENV_FILE=/path/to/.env $0 指定" >&2
  exit 1
fi

BK=${BACKUP_DIR:-/data/pdm/backup}
DB_CONTAINER=${DB_CONTAINER:-pdm-mysql}
UPLOAD_DIR=${UPLOAD_DIR:-/data/pdm/uploads}
KEEP_DB_DAYS=${KEEP_DB_DAYS:-14}
KEEP_FILE_COPIES=${KEEP_FILE_COPIES:-8}

# shellcheck disable=SC1090
set -a; source "$ENV_FILE"; set +a
DB_NAME=${DB_NAME:-pdm}

DAY=$(date +%F)
mkdir -p "$BK/db" "$BK/files"

echo "$(date '+%F %T') 使用配置 $ENV_FILE，开始备份数据库 $DB_NAME"
docker exec "$DB_CONTAINER" sh -c "exec mysqldump -uroot -p'${MYSQL_ROOT_PASSWORD}' \
  --single-transaction --routines --triggers --default-character-set=utf8mb4 ${DB_NAME}" \
  | gzip > "$BK/db/${DB_NAME}_${DAY}.sql.gz"

if [ "$(date +%u)" = "7" ]; then
  echo "$(date '+%F %T') 周日，开始备份附件"
  tar -czf "$BK/files/uploads_${DAY}.tar.gz" -C "$(dirname "$UPLOAD_DIR")" "$(basename "$UPLOAD_DIR")"
fi

find "$BK/db" -name "${DB_NAME}_*.sql.gz" -mtime +"$KEEP_DB_DAYS" -delete
ls -1t "$BK/files"/uploads_*.tar.gz 2>/dev/null | tail -n +$((KEEP_FILE_COPIES + 1)) | xargs -r rm -f

echo "$(date '+%F %T') 备份完成：$BK/db/${DB_NAME}_${DAY}.sql.gz"
echo "提醒：master.key 不在本备份范围内，请单独离线保管。"