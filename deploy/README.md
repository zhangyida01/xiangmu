# 部署快速开始（CentOS 7.9 + Docker）

最短路径。**完整说明、排错、验收清单见 `docs/04-部署手册.md`**，括号里的章节号对应手册。

前提：CentOS 7.9 x86_64，2 核 / 2 GB 内存 / 20 GB 空闲磁盘，能访问外网（否则走手册第十四章离线部署）。

## 1. 装 Docker（手册第二、三章）

```bash
# CentOS 7 已 EOL，先修 yum 源
mkdir -p /etc/yum.repos.d/bak && mv /etc/yum.repos.d/CentOS-*.repo /etc/yum.repos.d/bak/
cat > /etc/yum.repos.d/CentOS-Base.repo <<'EOF'
[base]
name=CentOS-7 - Base
baseurl=https://mirrors.aliyun.com/centos-vault/7.9.2009/os/$basearch/
gpgcheck=0
[updates]
name=CentOS-7 - Updates
baseurl=https://mirrors.aliyun.com/centos-vault/7.9.2009/updates/$basearch/
gpgcheck=0
[extras]
name=CentOS-7 - Extras
baseurl=https://mirrors.aliyun.com/centos-vault/7.9.2009/extras/$basearch/
gpgcheck=0
EOF
yum clean all && yum makecache

# 装 Docker（指定 24.x，CentOS 7 内核 3.10 支持的最后一个大版本）
yum install -y yum-utils device-mapper-persistent-data lvm2
yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
yum install -y docker-ce-24.0.7 docker-ce-cli-24.0.7 containerd.io docker-compose-plugin

# 日志上限，防止磁盘被吃满
mkdir -p /etc/docker
cat > /etc/docker/daemon.json <<'EOF'
{ "log-driver": "json-file", "log-opts": { "max-size": "50m", "max-file": "3" } }
EOF

systemctl enable --now docker
timedatectl set-timezone Asia/Shanghai && timedatectl set-ntp true && systemctl enable --now chronyd
docker version && docker compose version
```

## 2. 目录与主密钥（手册第四章）

```bash
mkdir -p /data/pdm/{mysql,uploads,keys,logs,backup/db,backup/files} /opt/pdm/src
openssl rand -base64 32 > /data/pdm/keys/master.key
chown 1000:1000 /data/pdm/keys/master.key && chmod 600 /data/pdm/keys/master.key
chown -R 1000:1000 /data/pdm/uploads /data/pdm/logs /data/pdm/backup
chown -R 999:999 /data/pdm/mysql
cat /data/pdm/keys/master.key      # 抄下来！丢了所有已存密码永久无法解密
```

## 3. 放代码（手册第五章）

把整个仓库放到 `/opt/pdm/src`，三种方式任选：`git clone`、Windows 上 `tar` 打包后 `scp`、或 WinSCP 拖拽。

```bash
cd /opt/pdm/src && ls      # 应看到 delivery-server delivery-web deploy docs
file deploy/backup.sh      # 不能出现 CRLF；有就 dos2unix deploy/backup.sh
```

## 4. 配置 .env（手册第六章）

```bash
cd /opt/pdm/src/deploy
cp .env.example .env && vi .env && chmod 600 .env
```

必须改的 4 项：`MYSQL_ROOT_PASSWORD`、`MYSQL_PASSWORD`、`JWT_SECRET`、`INIT_ADMIN_PASSWORD`。
生成随机值：`openssl rand -base64 24 | tr -d '/+=' | cut -c1-24`
值不要加引号，不要用 `$` `#` 和空格。

## 5. 构建并启动（手册第八章）

```bash
cd /opt/pdm/src/deploy
docker compose build 2>&1 | tee /tmp/pdm-build.log   # 首次 5-15 分钟
docker compose up -d
docker compose ps                                    # 两个服务都应为 healthy
docker compose logs -f app
```

日志里依次看到这几行就是成功：`凭据加密主密钥已加载` → `Successfully applied 3 migrations`（数字等于 `db/migration` 下的脚本数）→ `Tomcat started on port 8080` → `已创建默认管理员账号 admin，初始密码：xxx`。

## 6. 访问与验收（手册第九章）

浏览器打开 `http://虚拟机IP:8080`，账号 `admin`，密码取 `.env` 的 `INIT_ADMIN_PASSWORD`，首次登录强制改密。

照着手册第九章走一遍验收：建客户 → 建项目 → 存凭据 → 看明文（应留审计）→ 传文档 → 升版回滚 → 看临期预警 → 重启验证数据不丢。

## 7. 防火墙（手册第十章）

Docker 发布的端口走 iptables FORWARD 链，**会绕过 firewalld**，限源必须用 `DOCKER-USER`：

```bash
iptables -I DOCKER-USER -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
iptables -I DOCKER-USER -p tcp --dport 8080 ! -s 192.168.1.0/24 -j DROP   # 网段按实际改
yum install -y iptables-services
iptables-save > /etc/sysconfig/iptables && systemctl enable iptables
```

3306 绑在 127.0.0.1，外部连不上。想让 Web 也只本机可达：`.env` 里 `APP_LISTEN=127.0.0.1`，配合 SSH 隧道，或启用 compose 里注释掉的 nginx 走 443。

## 8. 备份（手册第十一章）

`deploy/backup.sh` 原地运行，会自动读同目录 `.env`：

```bash
chmod +x /opt/pdm/src/deploy/backup.sh
/opt/pdm/src/deploy/backup.sh            # 手动跑一次，看 /data/pdm/backup/db 有产物
crontab -e
# 0 2 * * * /opt/pdm/src/deploy/backup.sh >> /data/pdm/logs/backup-cron.log 2>&1
```

数据库每日全备、附件每周日全备。`master.key` 不在备份范围内，必须单独离线保管。

## 9. 升级（手册第十三章）

```bash
cd /opt/pdm/src && git pull              # 或重新拷贝代码，注意保留 deploy/.env
cd deploy
./backup.sh                              # 升级前必备
docker compose build app && docker compose up -d app
docker compose logs -f app | grep -iE 'flyway|Started PdmApplication'
```

数据库结构变更由 Flyway 自动执行，新增脚本放 `delivery-server/src/main/resources/db/migration/V{n}__xxx.sql`，**不要改动已执行过的历史脚本（V1/V2/V3…）**。

## 10. 本地开发（开发机需 JDK 17 + Maven + Node 20）

```bash
# 起一个本地 MySQL 8
docker run -d --name pdm-mysql-dev -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=pdm mysql:8.0.32

# 开发用主密钥与上传目录（仓库根目录下，已被 .gitignore 忽略）
mkdir -p data/keys data/uploads
openssl rand -base64 32 > data/keys/master.key

# 后端（application.yml 默认读 ./data/keys/master.key 与 ./data/uploads）
cd delivery-server && mvn spring-boot:run

# 前端（vite 已把 /api 代理到 127.0.0.1:8080）
cd delivery-web && npm install && npm run dev
# 浏览器打开 http://localhost:5173
```

Swagger（仅 dev profile 开启）：`http://localhost:8080/swagger-ui.html`

## 出问题先看这里

| 症状 | 去哪查 |
| --- | --- |
| 构建失败 | `grep -inE 'error\|fail\|timeout' /tmp/pdm-build.log \| tail -30` → 手册 15.1 |
| 容器反复重启 | `docker compose logs --tail 200 app` → 手册 15.2 |
| 浏览器打不开 | 手册 15.3 第一条，按 5 步顺序排 |
| 看明文报解密失败 | `master.key` 不对 → 手册 11.5 |
| 其他 | 手册第十五章 FAQ |
