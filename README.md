# 项目交付管理后台

个人 / 小团队使用的项目交付管理系统：统一项目台账、交付文档归档、账号凭据保险箱（AES-256-GCM 加密 + 全量访问审计）。

- 技术栈：Java 17 + Spring Boot 3.2 + MyBatis-Plus 3.5 + MySQL 8 + Flyway ｜ Vue 3 + Vite 5 + Element Plus + Pinia
- 部署：CentOS 7.9 宿主机 + Docker Compose，前后端打成**单容器单端口**，nginx 可选
- 当前状态：功能代码与部署脚本已完成，待在 VM 上首次构建验证

## 快速开始

最短路径见 `deploy/README.md`，完整运维说明见 `docs/04-部署手册.md`。

```bash
# 1) 宿主机一次性准备
mkdir -p /data/pdm/{mysql,uploads,keys,logs,backup} /opt/pdm/src
openssl rand -base64 32 > /data/pdm/keys/master.key && chown 1000:1000 /data/pdm/keys/master.key && chmod 600 /data/pdm/keys/master.key
chown -R 1000:1000 /data/pdm/uploads /data/pdm/logs /data/pdm/backup
chown -R 999:999 /data/pdm/mysql

# 2) 放代码并配置环境变量
cd /opt/pdm/src                       # 仓库根目录
cp deploy/.env.example deploy/.env
vi deploy/.env && chmod 600 deploy/.env

# 3) 构建并启动（首次约 5-15 分钟）
cd deploy && docker compose up -d --build
docker compose logs -f app
```

浏览器打开 `http://虚拟机IP:8080`，默认账号 `admin`，密码取 `.env` 的 `INIT_ADMIN_PASSWORD`（默认 `Admin@12345`），首次登录强制改密。

## 目录结构

    delivery-server/   Spring Boot 后端（前端产物在镜像构建阶段注入 resources/static）
    delivery-web/      Vue 3 前端
    deploy/            Dockerfile、docker-compose.yml、.env.example、nginx 配置、backup.sh
    docs/              需求、数据库、接口、部署四份文档

## 文档

| 文档 | 说明 |
| --- | --- |
| docs/01-功能清单与需求说明.md | 功能清单、权限模型、核心功能描述、技术选型、开发计划、决策记录 |
| docs/02-数据库设计.md | MySQL 表结构 DDL、临期预警视图、初始化数据、加密实现说明 |
| docs/03-接口设计.md | REST API 清单、统一响应与错误码、权限标识约定 |
| docs/04-部署手册.md | 16 章 + 2 附录的完整部署手册：环境准备、yum 源修复、Docker 安装、SELinux、代码上传、.env 逐项说明、构建与日志判读、端到端验收、防火墙、备份恢复与迁移、运维速查、升级回滚、离线部署、FAQ、安全清单、systemd 备选方案 |
| deploy/README.md | 最短路径快速开始，章节号与部署手册对应 |

## 核心模块（阶段一已实现）

- 项目基础信息：客户、合同编号与金额、相关人、关键日期、交付环境、产品 License、状态流转、归档/锁定、项目复制、项目 360° 主页
- 交付清单：按项目类型从模板实例化 Checklist、完成率自动计算、缺项提醒
- 交付文档：分类目录树、上传下载、版本管理与回滚、带 token 的下载鉴权
- 凭据保险箱：AES-256-GCM 加密、默认掩码、按权限查看明文、一键复制自动清剪贴板、密码轮换提醒、加密附件、变更历史、访问全程留痕
- 临期预警：证书 / 域名 / License / 密码轮换 / 质保 / 里程碑，站内统一视图 + 首页角标
- 审计与统计：操作日志、凭据访问记录、首页工作台看板
- 系统管理：用户与敏感能力开关、数据字典、系统参数

阶段二待做：里程碑与交付任务、我的待办、文档在线预览、下载水印、Excel 批量导入导出、独立统计报表页。
对应数据表（`biz_milestone`、`biz_task`、`biz_contract`）已在 DDL 中建好，接口与页面尚未实现。

## 安全设计要点

- 凭据字段级 AES-256-GCM 加密；主密钥来自宿主机 `/data/pdm/keys/master.key`，容器内**只读**挂载，不入库、不进镜像、不进备份
- 明文查看 / 导出 / 下载 / 删除项目由账号的敏感能力开关控制，所有敏感操作写入 `sys_audit_log`
- 可选二次密码校验（`cred.recheck.enabled`）、复制后自动清空剪贴板（`cred.clipboard.clearSec`）
- 附件上传做类型与大小校验、防路径穿越，下载走带 token 的临时链接
- 3306 只绑 `127.0.0.1`；8080 的限源方式见 `docs/04-部署手册.md` 第十章（Docker 会绕过 firewalld）

## 已知限制

- 开发机无 JDK 17 / Maven / Node / Docker，代码**未经本地编译验证**，首次 `docker compose build` 若报错按提示微调即可
- 在线预览、导出水印、Excel 批量导入导出、通知渠道（邮件 / 钉钉 / 企微）列为阶段二
- 文档与交付清单项的自动绑定：后端已支持（上传即自动置为「已提交」），v1.0 前端未提供绑定入口，完成率需在「交付清单」页签手工改状态