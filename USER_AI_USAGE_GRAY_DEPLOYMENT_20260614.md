# 用户 AI 用量分支灰度部署交接

更新时间：2026-06-14

当前分支：`codex/user-ai-usage-analytics`

当前 PR：<https://github.com/BMIHuangchen/good-items-assistant/pull/1>

当前提交：

- `295a28e fix: align deployment templates with production port`
- `f2232ee test: record local user ai usage verification`
- `87aba6e docs: add user ai usage gray deployment handoff`
- `0aa1bc9 chore: ignore local deployment bundles`
- `eb11ba2 feat: add mini user AI usage analytics`

## 当前结论

本分支已完成第一版“小程序用户登录 + 服务端收藏 + Kimi/豆包 AI 识图选择 + 用户 AI Token/费用估算 + 后台用户管理和数据看板”开发。

PR 当前是 Draft。不要在完成线上灰度、数据库迁移和小程序真机验证前合并到 `main`。

## 本地验证记录

已通过：

```powershell
git diff --check
cd java-code
D:\cursor\apache-maven-3.9.15\bin\mvn.cmd package -DskipTests
cd ..\web-code
npm.cmd run build
```

2026-06-14 已补充本地真实后台页面验证：

- 使用本地后端 `http://localhost:8080` 和后台 Vite `http://127.0.0.1:5173`。
- 管理员登录接口 `/api/admin/auth/login` 返回 200，登录后后台 shell 正常显示。
- “数据看板”页面可见注册用户、今日登录、今日活跃、行为事件、AI 调用、估算费用、登录趋势、AI 调用趋势、功能使用排行、模型使用排行和内容热度。
- “用户管理”页面可见小程序用户列表，以及每个用户的 AI 调用、成功、失败、Token、估算费用和平均耗时。
- “AI 设置”页面可见 AI 总开关、自动入库、自动发布、模型配置与 Key 环境变量名，不暴露密钥。
- “AI 图片分析”页面可见任务列表和调用日志，接口均返回 200。
- 页面验证期间未发现 4xx/5xx API 响应、网络失败或前端控制台错误。
- 已补充“小程序用户自行确认 AI 结果”链路验证：本地模拟 `PENDING_USER_CONFIRM` 任务后，使用小程序用户 token 调用 `/api/mini/ai/image-tasks/{id}/confirm`，任务变为 `USER_CONFIRMED`，并创建新分类与 `PUBLISHED` 内容；公开列表可搜索到该内容。该验证证明用户登录后可自行确认生成内容、新建分类并公开展示，不需要管理员批准。

已知非阻塞提示：

- 后台构建提示 Vite chunk 超过 500k。
- Sass legacy JS API 有 deprecation warning。
- `npm audit` 仍有 Vite/esbuild 相关漏洞提示，升级会涉及 breaking change，建议单独分支处理。

## 本地接口测试记录

2026-06-14 本地 MySQL 8 环境已完成接口级验证。测试环境使用 `good_items/change-me` 连接本地 `good_items_assistant`，后端运行在 `http://localhost:8080`。

已验证：

- `scripts/local-check.ps1 -BaseUrl http://localhost:8080` 通过，公开 ready、Banner、分类、内容、COS 接口返回 200。
- 未登录访问 `/api/mini/me/favorites` 返回 401，收藏功能未绕过登录。
- `/api/mini/auth/login` 在本地未配置微信 AppID/Secret 时使用 `dev_openid_*` 兜底并返回小程序 token。
- 登录后 `/api/mini/auth/me` 返回当前用户。
- 登录后服务端收藏新增和收藏列表查询通过。
- `/api/mini/me/ai-usage` 返回今日/本月 Token 和估算费用结构。
- `/api/mini/me/events` 可记录用户行为事件。
- 管理员登录通过。
- `/api/admin/users`、`/api/admin/users/ai-usage`、`/api/admin/analytics/overview` 返回 200。
- AI 总开关关闭时，登录用户上传图片到 `/api/mini/ai/analyze-image` 返回 403：`AI 图片分析入口暂未开启`。

本地测试额外发现并修复：

- 原迁移脚本使用 `alter table ... add column if not exists`，MariaDB 10.11 可用，但 MySQL 8 不兼容；已改为 `information_schema + prepare/execute` 动态 SQL，提升迁移脚本兼容性。

## 本地灰度部署包

本地已生成部署包目录：

```text
D:\cursor\codex\good-items-assistant-user-ai-usage\deploy\user-ai-usage-20260614
```

包含：

- `good-items-assistant-api-0.0.1-SNAPSHOT.jar`
- `good-items-admin-dist.zip`
- `migration_user_ai_usage_20260614.sql`
- `README.md`

该目录已被 `.gitignore` 忽略，不进入仓库。

## 线上部署前置条件

必须先确认：

1. 生产数据库仍为 `good_items_assistant`。
2. 生产数据库写入前已重新备份。
3. 微信公众平台 request 合法域名包含 `https://zanzanai.top`。
4. 微信公众平台 uploadFile 合法域名包含 `https://zanzanai.top`。
5. 微信公众平台 downloadFile 合法域名包含腾讯 COS 域名。
6. `/etc/good-items-api.env` 已配置：

```text
WECHAT_MINI_APP_ID=微信小程序 AppID
WECHAT_MINI_APP_SECRET=微信小程序 AppSecret
MINI_JWT_SECRET=至少 32 位随机字符串
```

## 数据库迁移

迁移脚本：

```text
database/migration_user_ai_usage_20260614.sql
```

执行前先备份：

```bash
mysqldump -uroot -p good_items_assistant > /root/good_items_assistant_backup_$(date +%Y%m%d_%H%M%S).sql
```

执行迁移：

```bash
mysql -uroot -p < /opt/good-items/database/migration_user_ai_usage_20260614.sql
```

迁移新增：

- `mini_users`
- `user_login_events`
- `user_behavior_events`
- `user_favorites`
- `ai_image_analysis_tasks.user_id`
- `ai_call_logs.user_id`

## 灰度部署顺序

1. 上传迁移脚本到 `/opt/good-items/database/`。
2. 备份生产数据库。
3. 执行迁移脚本。
4. 上传后端 jar 到 `/opt/good-items/api/good-items-assistant-api.jar`。
5. 解压后台 dist 到 `/var/www/good-items-admin/`。
6. 确认 `/etc/good-items-api.env` 中微信和 JWT 环境变量存在。
7. 重启 `good-items-api.service`。
8. 运行线上一致性检查。
9. 上传小程序体验版。
10. 真机验证后再决定是否把 PR 标记 ready。

## 灰度验收清单

后端和公开接口：

- `https://zanzanai.top/api/diagnostics/ready` 返回 200。
- `https://zanzanai.top/api/mini/ai/settings` 返回 200。
- 后端日志能按 `requestId` 查到小程序登录和 AI 调用。

小程序真机：

- 首次启动能静默登录。
- 收藏必须登录后使用，收藏列表来自服务端。
- AI 图片分析页面能选择 Kimi 或豆包。
- AI 分析提交超时不低于 120 秒。
- AI 分析完成后，用户能自行确认生成内容。
- 我的页显示今日/本月 Token 和估算费用。
- 不出现旧电商、交易、支付、订单、价格比较、下单等表达。

后台：

- “用户管理”页面能看到小程序用户。
- “用户管理”中能看到每个用户 AI Token 和估算费用。
- “数据看板”能展示登录、行为、AI、模型使用和内容热度统计。
- “AI 图片分析”任务列表能看到用户掩码。
- “AI 调用日志”能看到用户掩码、Token、估算费用和耗时。

## 回滚策略

如果灰度异常：

1. 先切回上一版后端 jar。
2. 切回上一版后台 dist。
3. 保留新增表和 `user_id` 字段，不要直接删除线上已产生数据。
4. 使用 `requestId` 查 Java 与 Nginx 日志定位问题。
5. 如必须数据库回滚，先从备份恢复到独立库验证，再决定是否恢复生产库。

## 下一步

优先完成线上前置条件和灰度验证。灰度与真机验证通过后，再将 PR 从 Draft 改为 Ready，并考虑合并回 `main`。
