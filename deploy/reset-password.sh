#!/usr/bin/env bash
# 项目交付管理后台 - 重置登录密码并解锁账号（默认 admin）
# 用法:
#   ./reset-password.sh admin 'NewPass@2026'   # 指定新密码（不强制首登改密）
#   ./reset-password.sh admin                  # 随机临时密码（首登强制改密）
#   ./reset-password.sh                        # 等同上一条
# 依赖: docker、同目录 .env（或 ENV_FILE 指定）；哈希工具自动探测 htpasswd / python3+bcrypt，
#       都没有时尝试 yum install -y httpd-tools
# 安全: 密码只回显一次，不写任何日志；只更新 sys_user 的密码与锁定字段
set -euo pipefail

USER_NAME=${1:-admin}
if ! printf '%s' "$USER_NAME" | grep -Eq '^[A-Za-z0-9_.-]{1,50}$'; then echo "账号名只允许字母数字下划线点和横杠" >&2; exit 1; fi
NEW_PWD=${2:-}
AUTO=0
if [ -z "$NEW_PWD" ]; then
  NEW_PWD="$(openssl rand -base64 24 | tr -d '/+=' | cut -c1-10)@1"
  AUTO=1
fi
if [ "${#NEW_PWD}" -lt 8 ]; then echo "密码需至少 8 位且同时包含字母和数字" >&2; exit 1; fi
if ! printf '%s' "$NEW_PWD" | grep -q '[A-Za-z]'; then echo "密码需至少 8 位且同时包含字母和数字" >&2; exit 1; fi
if ! printf '%s' "$NEW_PWD" | grep -q '[0-9]'; then echo "密码需至少 8 位且同时包含字母和数字" >&2; exit 1; fi
case "$NEW_PWD" in
  *\'*|*\\*) echo "密码不能包含单引号或反斜杠" >&2; exit 1 ;;
esac

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
ENV_FILE=${ENV_FILE:-$SCRIPT_DIR/.env}
if [ ! -f "$ENV_FILE" ]; then ENV_FILE=/opt/pdm/src/deploy/.env; fi
if [ ! -f "$ENV_FILE" ]; then echo "找不到 .env，请用 ENV_FILE=/path/to/.env 重试" >&2; exit 1; fi
# shellcheck disable=SC1090
set -a; source "$ENV_FILE"; set +a
DB_NAME=${DB_NAME:-pdm}
DB_CONTAINER=${DB_CONTAINER:-pdm-mysql}

EXIST=$(docker exec "$DB_CONTAINER" mysql -N -u"$DB_USER" -p"$MYSQL_PASSWORD" "$DB_NAME" \
  -e "select count(*) from sys_user where username='$USER_NAME' and deleted=0;" 2>/dev/null)
if [ "$EXIST" != "1" ]; then echo "账号不存在或已删除: $USER_NAME" >&2; exit 1; fi

hash_pwd() {
  if command -v htpasswd >/dev/null 2>&1; then
    htpasswd -bnBC 10 "" "$1" | tr -d ':\n'
    return 0
  fi
  if command -v python3 >/dev/null 2>&1 && python3 -c 'import bcrypt' >/dev/null 2>&1; then
    python3 -c 'import bcrypt,sys; print(bcrypt.hashpw(sys.argv[1].encode(), bcrypt.gensalt(10)).decode())' "$1"
    return 0
  fi
  return 1
}

HASH=$(hash_pwd "$NEW_PWD") || {
  echo "未找到 bcrypt 工具，尝试 yum install -y httpd-tools ..."
  yum install -y -q httpd-tools >/dev/null 2>&1 || { echo "安装 httpd-tools 失败，请手动安装后重试" >&2; exit 1; }
  HASH=$(hash_pwd "$NEW_PWD") || { echo "仍无法生成 BCrypt 哈希" >&2; exit 1; }
}

docker exec "$DB_CONTAINER" mysql -u"$DB_USER" -p"$MYSQL_PASSWORD" "$DB_NAME" -e \
  "update sys_user set password='$HASH', must_change_pwd=$AUTO, login_fail_count=0, lock_until=null where username='$USER_NAME' and deleted=0;" 2>/dev/null
echo "已重置账号 $USER_NAME 的密码并解除锁定"
echo "新密码: $NEW_PWD"
if [ "$AUTO" = "1" ]; then echo "该密码为随机临时密码，下次登录会强制修改"; fi
echo "完成"
