# 项目规则：好物展示小助手

## 项目目标

本项目是个人生活内容分享与好物展示小助手，仅用于展示日用好物、生活小用品图文与体验心得。项目不提供经营性能力，不恢复旧电商、交易、支付、订单、库存等业务。

## 技术栈

- 后端：Spring Boot 3、Java 21、JDBC、MySQL、JWT、Actuator、腾讯 COS 配置。
- 管理后台：Vue 3、Vite、TypeScript、Element Plus、Axios。
- 小程序：UniApp、Vue 3、微信小程序。
- 数据库：MySQL，脚本位于 `database/schema.sql` 和 `database/seed.sql`。
- 部署：腾讯云 CVM、Nginx、systemd、Let's Encrypt 证书、腾讯 COS。

## 常用命令

- 后端构建：在 `java-code` 中运行 `D:\cursor\apache-maven-3.9.15\bin\mvn.cmd package -DskipTests`。
- 后台构建：在 `web-code` 中运行 `npm.cmd run build`。
- 后台开发：在 `web-code` 中运行 `npm.cmd run dev`。
- 小程序：用 HBuilderX 或微信开发者工具打开 `mini-code`。
- 在线一致性检查：运行 `scripts/online-check.ps1`。

## 目录结构

- `java-code`：后端 API。
- `web-code`：内容管理后台。
- `mini-code`：微信小程序前端。
- `database`：上线数据库结构和内容种子数据。
- `scripts`：本地与线上一致性检查脚本。

## 文档入口

- `PROJECT_OVERVIEW_AND_STATUS.md`：项目功能、当前线上状态、待处理事项。
- `DEVELOPMENT_HANDOVER_20260603.md`：本次审核版重写的交接记录。
- `DEPLOYMENT_AND_TROUBLESHOOTING.md`：部署、灰度、切换、回滚和网络/TLS 排错手册。
- `API.md`：后端公开接口、后台接口和诊断接口。
- `README.md`：项目简要说明和快速入口。

## 当前上线结论

- 2026-06-11 已将稳定审核版迁移为全新干净主线仓库：`D:\cursor\codex\good-items-assistant`。后续开发、封版、分支和提交都以该目录为准。
- 旧工作区只作为历史备份，不再作为后续开发主线；不得把旧电商原型目录、旧经营性模块或临时排查目录重新带入本项目。
- 2026-06-11 复查：备案完成后，`https://zanzanai.top` 的 HTTPS/TLS 已恢复可用。
- 后台、数据库、公开 API、COS 图片、分类和好物内容管理逻辑已经完成线上联通。
- 正式 HTTPS 通道已验证后台可控制分类和内容：当前公开接口返回 7 个启用分类、7 条已发布好物内容。
- 正式小程序必须使用 HTTPS 合法域名，不允许使用 IP 或 HTTP。
- 上传体验版前仍必须确认微信公众平台的 request 合法域名包含 `https://zanzanai.top`，downloadFile 合法域名包含腾讯 COS 域名。

## 标准化开发流程

每次开发、修复和上线都必须按标准化流程推进：

1. 明确目标和当前环境。
2. 先读项目文档、`AGENTS.md`、现有代码和线上状态。
3. 识别前置条件，尤其是小程序上线前必须先确认：域名备案、微信合法域名、HTTPS、证书、DNS、服务器安全组、Nginx、后端、数据库、COS。
4. 本地实现和构建。
5. 本地或开发通道验证。
6. 线上灰度验证。
7. 正式切换。
8. 回滚预案和最终状态记录。

严禁跳过基础前置条件，严禁想当然认为域名、备案、HTTPS、证书、合法域名、服务器安全组、数据库、COS 已经可用。

## 排错优先级

排错必须由主到次、由外到内、由前置条件到业务代码：

1. 产品和上线前置条件：项目是否符合审核定位、域名是否备案、微信后台是否合法配置。
2. 网络入口：DNS、端口、安全组、云防护、HTTPS/TLS/SNI、证书链。
3. Nginx：静态页面、反向代理、请求头、灰度路径、日志。
4. 后端服务：systemd、端口、健康检查、`requestId`、接口响应。
5. 数据库：连接、表结构、种子数据、线上数据状态。
6. 对象存储：COS 域名、图片路径、公开访问、downloadFile 合法域名。
7. 前端和小程序：API 地址、兜底逻辑、页面状态、缓存、体验版/审核版差异。
8. 业务代码：SQL、数据过滤、状态发布、权限、页面渲染。

本项目的经验教训：`ECONNRESET Client network socket disconnected before secure TLS connection was established` 首先应按域名备案、微信合法域名、HTTPS/SNI、云防护和 Nginx 入口排查，不应先反复修改业务代码。

## 小程序上线前检查清单

上传体验版前必须全部通过：

- `zanzanai.top` 已完成 ICP 备案，并已接入腾讯云。
- 微信公众平台已配置 request 合法域名：`https://zanzanai.top`。
- 微信公众平台已配置 downloadFile 合法域名：腾讯 COS 域名。
- `https://zanzanai.top/api/diagnostics/ready` 在外部网络稳定返回。
- `https://zanzanai.top/api/mini/categories` 返回后台启用分类。
- `https://zanzanai.top/api/mini/items?pageSize=30` 返回后台已发布内容。
- 小程序不显示“已启用本地兜底”。
- 小程序分类数、好物标题、Banner、图片与后台一致。
- 小程序代码中没有临时 `http://IP`、调试条、旧电商文案和旧业务接口。

## 用户协作习惯

- 用户不希望陪跑式排查。Codex 应优先自己完成可由工具、日志、代码、服务器和控制台信息判断的问题。
- 只有需要用户权限、账号页面确认、人工上传、备案资料、短信核验、微信后台配置等必须用户参与的事项，才明确告诉用户需要做什么。
- 用户经常在多轮交互后忘记下一步。每次阶段性结束或最终回复，必须明确列出：当前结论、我已完成什么、用户现在需要做什么、下一步是什么、是否可以上传体验版/上线。
- 面对标准化开发任务，Codex 必须主动按成熟工程流程推进，不得凭感觉跳过基础检查。

## 角色把控

- 产品负责人：确认所有页面和文案都符合“内容展示，不交易”的定位。
- 后端负责人：把控 API、数据库、日志、`requestId`、健康检查。
- 前端负责人：把控后台、小程序页面、弱网兜底和腾讯 COS 图片域名。
- 运维负责人：把控备案、HTTPS、Nginx、带宽、安全组、健康检查、发布回滚。
- 审核负责人：把控微信审核材料、类目、隐私说明、合法域名和版本冻结。

## 安全规则

- 禁止批量删除、递归删除、目录删除、强制删除和通配符删除。
- 删除文件只能一次删除一个明确普通文件路径。
- 不允许恢复任何旧经营性业务模块。
- 配置中不得写入明文生产密钥。生产环境必须使用环境变量。
- 所有线上排查必须优先记录和使用 `requestId`。
- 正式发布必须先灰度验证，再切正式入口，并保留可回滚配置。
- 不允许恢复旧经营性项目命名、页面标题、接口路径、数据库表或业务文案。
