# 项目功能与当前状态

更新时间：2026-06-11  
项目定位：好物展示小助手，面向微信小程序审核与正式上线的个人生活好物内容展示项目。

## 一句话结论

备案完成后，`https://zanzanai.top` 的正式 HTTPS 入口已经恢复可用。当前后端公开 API、数据库、COS 图片、后台管理和小程序数据接口已完成线上联通验证，项目进入“微信合法域名配置 + 小程序体验版验证”的阶段。

## 当前功能

### 微信小程序

- 首页：Banner、推荐内容、最新好物；首页文案和精选标题由后台“小程序页面”配置管理。
- 分类：按内容分类浏览；左侧分类、右侧卡片内容由后台“分类”和“好物内容”管理。
- 搜索：按关键词查找内容；搜索提示语和热词由后台“小程序页面”配置管理。
- 详情：展示好物图文、体验心得、标签和图片。
- 收藏：本地收藏，不涉及交易、下单、支付。
- 我的：展示项目说明、网络排查信息和本地状态；项目说明由后台“小程序页面”配置管理。
- 兜底状态：接口异常时保留错误提示和排查信息；正式验证时不应显示“已启用本地兜底”。

### 后端 API

- 公开内容接口：Banner、分类、内容列表、内容详情、COS 配置。
- 小程序页面配置接口：首页文案、精选标题、搜索提示语、搜索热词、我的页说明。
- 管理接口：后台登录、当前用户、看板、内容、分类、Banner 管理。
- 诊断接口：`/api/diagnostics/ready` 检查数据库、公开域名、COS 配置。
- 统一响应：所有接口包含 `requestId`，便于快速排查。
- 日志：请求日志包含方法、路径、状态、耗时、来源和 UA。

### 后台 Web 页面

- 登录页：内容管理后台入口。
- 总览：展示内容数量、分类、Banner、浏览和收藏统计。
- 内容管理：查看、创建和更新好物内容。
- 分类管理：维护内容分类。
- Banner 管理：维护首页 Banner。
- 小程序页面：维护首页、搜索页和我的页的可配置文案。
- 上线诊断：辅助确认接口、数据库和 COS 状态。
- 角色把控：展示产品、后端、前端、运维、审核等职责。

### 数据库

数据库名：`good_items_assistant`

核心表：

- `content_categories`
- `content_items`
- `content_banners`
- `operation_audit_logs`

数据库只保留内容展示需要的结构，不包含交易、订单、支付、库存等经营性能力。

## 当前线上状态

- 正式域名：`https://zanzanai.top`
- 服务器 IP：`119.91.118.183`
- Nginx 配置：`/etc/nginx/conf.d/zanzanai.conf`
- 正式 API：`https://zanzanai.top/api/`
- 后台页面：`https://zanzanai.top/`
- 诊断接口：`https://zanzanai.top/api/diagnostics/ready`
- 新后端端口：`18080`
- systemd 服务：`good-items-api.service`
- 后端 jar：`/opt/good-items/api/good-items-assistant-api.jar`
- 后台静态文件：`/var/www/good-items-admin/`
- 生产环境变量：`/etc/good-items-api.env`
- 腾讯 COS 域名：`https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com`
- 生产数据库：服务器本机 MariaDB 10.11，`127.0.0.1:3306/good_items_assistant`
- 数据目录：`/var/lib/mysql/`
- 最近备份：`/root/good_items_assistant_backup_20260613_031301.sql`

## 已完成验证

- 2026-06-11 复查：`https://zanzanai.top/tls-ping` 返回 200，HTTPS/TLS 入口可用。
- 2026-06-11 复查：`scripts/online-check.ps1 -BaseUrl https://zanzanai.top` 全部通过。
- 正式 `/api/diagnostics/ready` 返回 `database=ok`。
- 正式 `/api/mini/categories` 返回 7 个启用分类。
- 正式 `/api/mini/items?pageSize=30` 返回 7 条已发布好物内容。
- 正式 `/api/mini/banners` 返回云端 Banner 数据。
- 代表性 COS 图片返回 200。
- 后端 Maven 构建成功：`D:\cursor\apache-maven-3.9.15\bin\mvn.cmd package -DskipTests`。
- 后台 Vite 构建成功：`npm.cmd run build`。
- 后端已按当前审核版移除旧 `/product/list`、`/category/list`、`/banner/list` 兼容路由。
- 2026-06-11 体验版验证：后台修改好物内容后，体验版小程序前端同步显示正常。
- 2026-06-13 服务器复查：`good-items-api.service` 运行中，生产数据库连接指向本机 MariaDB，当前表包括 `content_categories`、`content_items`、`content_banners`、`mini_program_config`、`operation_audit_logs`。
- 2026-06-13 线上数据库统计：`content_categories=9`、`content_items=9`、`content_banners=3`、`mini_program_config=1`、`operation_audit_logs=0`，库大小约 `0.17MB`。

## 待处理事项

- 生产后台管理密码需要尽快修改，不能长期使用临时密码。
- 服务器 root 密码曾在对话中暴露，确认系统稳定后应立即修改，并改为 SSH 密钥登录。
- 微信公众平台需要确认 request 合法域名已配置：`https://zanzanai.top`。
- 微信公众平台需要确认 downloadFile 合法域名已配置：`https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com`。
- 上传体验版后，需要在真机确认：首页不显示“已启用本地兜底”，分类为 7 个，好物内容为 7 条，且“笔记本小小台灯”“迷你清洁喷瓶”等后台内容可见。
- 新项目目录 `D:\cursor\codex\good-items-assistant` 已剥离旧原型目录 `High-Fidelity E-Commerce App` 和 `ui-figma`，后续以该目录作为干净主线。
- `www.zanzanai.top` 当前证书覆盖情况仍建议后续确认；正式主域 `zanzanai.top` 已可用。

## 快速判断

如果只想判断线上是否活着，优先看：

```text
https://zanzanai.top/api/diagnostics/ready
```

如果返回 `database=ok`，说明 HTTPS、Nginx、后端、数据库、COS 配置至少基本链路可用。
