# 好物展示小助手

一个面向微信小程序审核和正式上线的个人生活好物内容展示项目。

当前干净主线目录：`D:\cursor\codex\good-items-assistant`。旧工作区仅作历史备份，后续开发、封版、分支和提交都从本目录开始。

## 文档快速入口

- `PROJECT_OVERVIEW_AND_STATUS.md`：项目功能、线上状态、待处理事项。
- `DEVELOPMENT_HANDOVER_20260603.md`：本次审核版重写交接记录。
- `DEPLOYMENT_AND_TROUBLESHOOTING.md`：部署、灰度、切换、回滚和网络/TLS 排错。
- `API.md`：接口文档。
- `AGENTS.md`：长期项目规则和安全约束。

## 当前能力

- 小程序：浏览首页、分类、搜索、详情、本地收藏、网络排查信息。
- 后端：公开内容接口、后台内容管理接口、统一 requestId、健康检查。
- 管理后台：内容、分类、Banner、上线诊断和角色把控。
- 数据库：只保留内容展示需要的数据表。
- 部署：`deploy/nginx-good-items.conf` 提供 HTTPS、反代、超时和 requestId 透传建议。
- 服务：`deploy/good-items-api.service` 是 systemd 模板，部署前必须替换其中的生产环境变量。
- 脚本：`deploy/deploy-linux.sh` 可发布 jar 和后台静态文件，`deploy/server-smoke-test.sh` 可在服务器本机检查 API。

## 上线原则

1. 图片统一来自腾讯 COS/CDN。
2. 所有接口返回和响应头都包含 `requestId`。
3. 正式验证时小程序不应显示“已启用本地兜底”；如出现兜底提示，先查 HTTPS、合法域名、API 和 COS。
4. 正式环境配置必须通过环境变量提供，不在仓库保存生产密钥。
5. 每次上线前运行在线一致性检查。

## 当前上线状态

- 2026-06-11：备案完成后，`https://zanzanai.top` 正式 HTTPS API 已恢复可用。
- 在线一致性检查已通过：ready、Banner、分类、好物列表、COS 配置均返回成功。
- 下一步是确认微信公众平台合法域名，并上传体验版做真机验证。
