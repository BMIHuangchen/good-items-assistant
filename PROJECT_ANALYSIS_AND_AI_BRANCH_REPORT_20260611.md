# 项目整体分析与新功能分支报告

更新时间：2026-06-11  
当前工作区：`D:\cursor\codex\good-items-assistant`  
当前 Git 分支：`codex/ai-analytics-entry`  
稳定基线：`main` / `v1.0.0-audit-ready`

## 一句话结论

本项目已经形成一个干净、可构建、线上可访问的“个人生活好物内容展示”稳定基线。当前最重要的原则是：`main` 继续作为微信审核与线上稳定基线；大模型入口、后台模型管理和数据分析能力应在独立功能分支推进，避免影响当前审核版定位和上线稳定性。

## 当前项目状态

### 已完成能力

- 小程序：已具备首页、分类、搜索、详情、本地收藏、我的页和网络排查信息。
- 后端：已具备公开小程序 API、后台管理 API、统一响应、`requestId`、健康检查和 COS 配置读取。
- 管理后台：已具备内容、分类、Banner、小程序页面文案、上线诊断和角色把控。
- 数据库：只保留内容展示相关结构，包括分类、内容、Banner、小程序配置和操作审计日志。
- 部署：已具备 Nginx、systemd、部署脚本、服务器冒烟检查和在线一致性检查脚本。
- 线上：`https://zanzanai.top` 当前 HTTPS/API 链路可用，公开接口返回线上数据。

### 本次验证结果

- 后端构建成功：`D:\cursor\apache-maven-3.9.15\bin\mvn.cmd package -DskipTests`
- 后台构建成功：`npm.cmd run build`
- 在线一致性检查成功：`scripts/online-check.ps1 -BaseUrl https://zanzanai.top`
- Git 当前有稳定远端：`origin -> https://github.com/BMIHuangchen/good-items-assistant.git`
- 当前 Git 基线提交：`1d8a0bd chore: initialize good items assistant stable baseline`
- 当前标签：`v1.0.0-audit-ready`

后台构建存在 Vite chunk size warning，原因主要是 Element Plus、ECharts 等依赖打包后体积较大；这不是当前上线阻塞项，后续后台变复杂时可通过路由拆分或 manualChunks 优化。

## 架构梳理

### 后端

当前后端是轻量 Spring Boot 3 单体 API：

- Controller 分为公开小程序接口、后台内容管理接口、后台登录接口和健康诊断接口。
- 数据访问集中在 `ContentRepository`，使用 `JdbcTemplate` 直接访问 MySQL。
- 登录采用配置文件中的管理员账号密码和 JWT，不依赖用户表。
- 配置通过环境变量注入，生产密钥不应进入仓库。

优点：结构简单、部署轻、排查路径短，适合当前内容展示项目。  
限制：新增大模型、数据分析、调用日志、用量统计后，单个 `ContentRepository` 会变重，建议拆出独立 repository/service/controller。

### 管理后台

当前后台是 Vue 3 + Element Plus 的单页管理台：

- 主体逻辑集中在 `web-code/src/App.vue`。
- API 封装集中在 `web-code/src/api/index.ts`。
- 类型定义集中在 `web-code/src/types/index.ts`。

优点：开发速度快、维护入口少。  
限制：新增“大模型管理”“数据分析看板”“提示词配置”“调用日志”后，建议开始拆分模块组件，否则 `App.vue` 会过大。

### 小程序

当前小程序是 UniApp 微信小程序：

- 页面包括首页、分类、搜索、详情、收藏、我的。
- API 封装集中在 `mini-code/utils/api.js`。
- 当前正式 API 固定为 `https://zanzanai.top/api`。

优点：链路清晰，兜底逻辑明确。  
限制：大模型入口需要重点控制审核风险、隐私提示、内容安全和接口合法域名，不应直接做成开放聊天产品。

## 新功能方向判断

用户提出的新方向包括：

1. 小程序前端加入大模型入口。
2. 后端对大模型进行管理。
3. 增加相应的大数据分析。

建议将其定义为“内容助手与数据洞察”能力，而不是泛聊天、交易导购或商业推荐系统。这样更贴合当前项目的非经营性定位，也更容易通过微信审核。

## 推荐功能边界

### 小程序大模型入口

建议第一阶段只做低风险入口：

- “问问好物助手”：围绕已发布好物内容进行问答。
- “帮我找”：根据生活场景、关键词、分类，从已有内容中生成浏览建议。
- “整理心得”：把后台已有内容摘要成更容易读的清单。

不建议第一阶段做：

- 开放式陪聊。
- 医疗、金融、法律建议。
- 商品购买决策、价格比较、跳转交易。
- 用户上传敏感图片或个人隐私资料进行分析。

### 后端大模型管理

建议增加独立模块：

- `ai_model_configs`：模型供应商、模型名、启用状态、温度、最大 token、系统提示词。
- `ai_prompt_templates`：小程序不同场景的提示词模板。
- `ai_call_logs`：请求场景、请求摘要、响应摘要、耗时、状态、错误码、token 估算、`requestId`。
- `ai_safety_rules`：敏感词、拒答规则、内容边界说明。

生产密钥必须继续走环境变量，例如：

- `AI_PROVIDER`
- `AI_API_BASE_URL`
- `AI_API_KEY`
- `AI_MODEL`

### 数据分析

建议从“可解释、可落地”的轻分析开始：

- 内容浏览量、详情打开量、收藏量、搜索词、分类访问热度。
- AI 入口点击量、AI 问答调用量、成功率、失败率、平均耗时。
- 高频问题、无结果问题、热门场景词。
- 内容缺口：用户问了但现有内容无法回答的问题。

暂不建议一开始引入复杂大数据平台。当前数据规模更适合 MySQL 聚合 + 后台 ECharts 看板；等访问量上来后再考虑 ClickHouse、ES、日志管道或离线分析。

## 建议实施路线

### 阶段 1：功能骨架

- 新增数据库表：AI 配置、提示词模板、AI 调用日志、用户行为事件。
- 新增后端模块：`AiController`、`AdminAiController`、`AnalyticsController`、对应 service/repository。
- 新增后台菜单：大模型配置、提示词管理、调用日志、数据分析。
- 新增小程序页面：AI 助手入口和问答页。

### 阶段 2：可用闭环

- 小程序提交问题，后端组合上下文，调用模型，返回答案。
- 后端记录调用日志和 `requestId`。
- 后台可查看调用结果、失败原因、耗时和基础统计。
- 增加内容安全边界：只围绕好物内容、生活小用品体验和站内数据回答。

### 阶段 3：上线灰度

- 后台总开关控制 AI 入口是否显示。
- 小程序入口默认可关闭，灰度验证后再公开。
- 线上先使用低频限流和错误兜底。
- 审核版说明中强调“内容整理与浏览辅助”，不表达经营、交易或医疗等高风险能力。

## 分支与版本建议

当前已从稳定 `main` 创建功能分支：

```text
codex/ai-analytics-entry
```

建议规则：

- `main`：只保留稳定审核版、线上修复和封版提交。
- `codex/ai-analytics-entry`：承载大模型入口、模型管理和分析能力开发。
- 若开发周期变长，可继续拆：
  - `codex/ai-backend-management`
  - `codex/mini-ai-entry`
  - `codex/admin-analytics-dashboard`

## Gitee 同步建议

当前 GitHub 远端为：

```text
origin https://github.com/BMIHuangchen/good-items-assistant.git
```

探测同名 Gitee 地址：

```text
https://gitee.com/BMIHuangchen/good-items-assistant.git
```

结果：404 not found，说明该地址当前不可直接推送，可能是 Gitee 命名空间不同或仓库尚未创建。

推荐操作：

1. 在 Gitee 创建空仓库，仓库名建议：`good-items-assistant`。
2. 不要勾选自动生成 README、.gitignore、LICENSE，避免和本地历史冲突。
3. 创建后提供 Gitee 仓库 HTTPS 地址或 SSH 地址。
4. 本地添加远端：

```powershell
git remote add gitee <你的Gitee仓库地址>
git push gitee main --tags
git push gitee codex/ai-analytics-entry
```

如果希望 Gitee 作为镜像，后续可固定流程：

```powershell
git push origin main --tags
git push gitee main --tags
git push origin codex/ai-analytics-entry
git push gitee codex/ai-analytics-entry
```

## 当前风险与注意事项

- AI 功能不能破坏“个人生活内容展示、非经营性”的项目定位。
- AI 接口密钥不得进入 `application.yaml`、前端代码、小程序代码或 Git 历史。
- 小程序 AI 入口上线前必须确认微信合法域名、隐私说明、内容安全和审核表达。
- 数据分析应先从 MySQL 事件表和聚合统计开始，避免过早引入复杂大数据基础设施。
- 后台当前是单文件应用，新功能开发时应开始拆组件，避免后续维护困难。
- `main` 上的 `v1.0.0-audit-ready` 是稳定审核基线，不应直接混入实验性 AI 功能。

## 下一步建议

最建议的下一步是先完成“AI 功能技术设计 + 数据库迁移草案”，再开始编码。这样可以在不影响当前审核稳定版的前提下，把模型配置、调用日志、行为分析和小程序入口一次性规划清楚。
