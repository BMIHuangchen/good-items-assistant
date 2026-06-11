# 开发交接记录 2026-06-03

## 背景

本项目从旧的经营性项目重写为“好物展示小助手”。目标是服务微信小程序审核和正式上线，定位为个人生活好物内容展示，不包含交易、下单、支付、库存、价格结算等经营性能力。

## 本次重写范围

### 后端

- 将旧后端包名和业务边界重写为 `com.gooditems`。
- 新增统一响应结构 `ApiResult` 和分页结构 `PageResult`。
- 新增 requestId 追踪、慢请求日志、全局异常处理。
- 新增公开小程序接口：
  - `/api/mini/config`
  - `/api/mini/banners`
  - `/api/mini/categories`
  - `/api/mini/items`
  - `/api/mini/items/{id}`
  - `/api/mini/cos`
- 新增后台管理接口：
  - `/api/admin/auth/login`
  - `/api/admin/auth/me`
  - `/api/admin/mini-config`
  - `/api/admin/dashboard`
  - `/api/admin/items`
  - `/api/admin/categories`
  - `/api/admin/banners`
- 新增诊断接口：
  - `/api/diagnostics/ready`

### 后台 Web

- 重写为“好物展示小助手”内容管理后台。
- 保留登录、总览、内容、分类、Banner、上线诊断和角色把控。
- 修复页面标题，去掉旧“电商后台管理系统”痕迹。
- 构建产物已上传到 `/var/www/good-items-admin/`。

### 小程序

- 重写首页、分类、搜索、详情、收藏、我的页面。
- 接口默认指向 `https://zanzanai.top/api`。
- 图片统一使用腾讯 COS 域名。
- 保留弱网兜底内容，避免接口异常时出现空白页。

### 数据库

- 新增 `database/schema.sql` 和 `database/seed.sql`。
- 数据库名为 `good_items_assistant`。
- 仅保留内容展示相关表：
  - `content_categories`
  - `content_items`
  - `content_banners`
  - `operation_audit_logs`

### 部署与脚本

- 新增部署配置和脚本目录：
  - `deploy/nginx-good-items.conf`
  - `deploy/tls-minimal-demo.conf`
  - `deploy/good-items-api.service`
  - `deploy/deploy-linux.sh`
  - `deploy/server-smoke-test.sh`
  - `scripts/online-check.ps1`
  - `scripts/local-check.ps1`
  - `scripts/start-local-api.ps1`
  - `scripts/tls-minimal-check.ps1`

## 构建验证

- 后端使用 `D:\cursor\apache-maven-3.9.15\bin\mvn.cmd package -DskipTests` 构建通过。
- 后台使用 `npm.cmd run build` 构建通过。
- 小程序 `manifest.json` 和 `pages.json` 已做 JSON 结构校验。
- 正式线上接口、页面、后台登录链路已验证。

## 线上切换记录

- 新后端先以灰度形式运行在 `18080`。
- Nginx 先配置 `/gray-api/` 指向新后端。
- 灰度验证通过后，将正式 `/api/` 切换到 `127.0.0.1:18080`。
- 将 `/` 指向后台静态文件目录 `/var/www/good-items-admin/`。
- 新后端最终纳入 systemd，服务名 `good-items-api.service`。

## 重要判断

本次最难的问题不是业务代码，而是 HTTPS/TLS、Nginx、证书、客户端兼容性和旧项目残留混在一起。后续排错必须先做分层验证：

1. `/tls-ping` 判断 HTTPS/Nginx。
2. `/api/diagnostics/ready` 判断后端、数据库、COS 配置。
3. `/api/mini/items` 判断业务接口。
4. 后台登录判断管理链路。

## 遗留说明

- 新项目目录 `D:\cursor\codex\good-items-assistant` 已剥离旧原型目录 `High-Fidelity E-Commerce App` 和 `ui-figma`；旧工作区可仅作为历史备份，不再作为后续开发主线。
- 生产密码和密钥不写入仓库；线上配置保存在服务器环境文件中。
- 后续如继续开发，必须先读 `PROJECT_OVERVIEW_AND_STATUS.md`、`DEPLOYMENT_AND_TROUBLESHOOTING.md` 和 `AGENTS.md`。
