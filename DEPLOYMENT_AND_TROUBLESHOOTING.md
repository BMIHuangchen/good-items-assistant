# 部署与排错手册

## 核心原则

每次上线都按“前置条件确认、本地构建、线上一致性检查、体验版验证、正式发布、快速回滚”的顺序执行。不要直接覆盖正式入口后再排查。

## 小程序上线前置条件

上传体验版前必须全部确认：

- 域名 `zanzanai.top` 已完成 ICP 备案，并已接入腾讯云服务。
- `https://zanzanai.top/tls-ping` 在外部网络返回 200。
- `https://zanzanai.top/api/diagnostics/ready` 返回 `database=ok`。
- 微信公众平台 request 合法域名包含：`https://zanzanai.top`。
- 微信公众平台 downloadFile 合法域名包含：`https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com`。
- 小程序代码 `mini-code/utils/api.js` 中 `API_BASE_URL` 指向 `https://zanzanai.top/api`。
- 小程序代码 `mini-code/utils/api.js` 中 `COS_BASE_URL` 指向腾讯 COS 域名。
- 小程序预览不应显示“已启用本地兜底”。

## 本地构建

### 后端

```powershell
cd java-code
D:\cursor\apache-maven-3.9.15\bin\mvn.cmd package -DskipTests
```

产物：

```text
java-code/target/good-items-assistant-api-0.0.1-SNAPSHOT.jar
```

### 后台

```powershell
cd web-code
npm.cmd run build
```

产物：

```text
web-code/dist
```

### 小程序

用 HBuilderX 或微信开发者工具打开 `mini-code`。上传体验版前确认：

- 已重新编译当前代码，不使用旧体验版缓存。
- 首页、分类、搜索、详情、收藏、我的页面都能打开。
- 分类数、好物标题、Banner、图片与后台一致。
- 不出现 `http://119.91.118.183/api`、`fallback=none`、调试条或“已启用本地兜底”。

## 服务器部署结构

```text
/opt/good-items/api/good-items-assistant-api.jar
/opt/good-items/database/schema.sql
/opt/good-items/database/seed.sql
/var/www/good-items-admin/
/etc/nginx/conf.d/zanzanai.conf
/etc/good-items-api.env
/etc/systemd/system/good-items-api.service
```

## 正式验证命令

本地或外部网络执行：

```powershell
powershell -ExecutionPolicy Bypass -File scripts\online-check.ps1 -BaseUrl https://zanzanai.top
```

单项检查：

```bash
curl -vk https://zanzanai.top/tls-ping --max-time 10
curl -sk https://zanzanai.top/api/diagnostics/ready
curl -sk https://zanzanai.top/api/mini/config
curl -sk https://zanzanai.top/api/mini/categories
curl -sk 'https://zanzanai.top/api/mini/items?pageSize=30'
curl -sk https://zanzanai.top/api/mini/cos
```

2026-06-11 复查结论：上述正式域名 API 已通过，当前公开接口返回 7 个启用分类、7 条已发布好物内容，COS 配置可用。

## 灰度发布流程

1. 上传新 jar 到 `/opt/good-items/api/good-items-assistant-api.jar`。
2. 上传后台 dist 到 `/var/www/good-items-admin/`。
3. 确认数据库结构和线上数据。
4. 先让新服务运行在 `18080`。
5. Nginx 使用 `/gray-api/` 代理到 `http://127.0.0.1:18080/api/`。
6. 验证灰度接口。
7. 灰度通过后再切正式 `/api/`。

## 正式切换流程

1. 备份 Nginx 配置。
2. 将正式 `/api/` 代理到 `http://127.0.0.1:18080`。
3. 将 `/` 指向 `/var/www/good-items-admin/`。
4. 执行：

```bash
nginx -t
systemctl reload nginx
```

5. 执行在线一致性检查。
6. 上传小程序体验版并真机检查。

## systemd 托管

正式服务名：

```text
good-items-api.service
```

常用命令：

```bash
systemctl status good-items-api.service
systemctl restart good-items-api.service
systemctl enable good-items-api.service
journalctl -u good-items-api.service -n 80 --no-pager
```

环境变量文件：

```text
/etc/good-items-api.env
```

该文件包含生产数据库密码、后台密码、JWT 密钥等敏感配置，不得提交到仓库。

## 回滚方式

如果正式 `/api/` 切换后异常：

1. 将 Nginx `/api/` 的 `proxy_pass` 改回上一版服务端口。
2. 执行 `nginx -t && systemctl reload nginx`。
3. 用 `requestId` 查 Nginx 与 Java 日志。

## 网络与 TLS 排错顺序

1. 先查产品和平台前置条件：备案、微信合法域名、COS downloadFile 合法域名。
2. 再查网络入口：DNS、端口、安全组、云防护、HTTPS/TLS/SNI、证书链。
3. 再查 Nginx：静态页面、反向代理、请求头、日志。
4. 再查后端：systemd、端口、健康检查、`requestId`、接口响应。
5. 再查数据库：连接、表结构、线上数据状态。
6. 再查 COS：图片路径、公开访问、微信 downloadFile 域名。
7. 最后查小程序页面、缓存、业务代码、SQL 和数据过滤。

## 本次 TLS 经验

之前出现过 `ECONNRESET Client network socket disconnected before secure TLS connection was established`。最终确认这类问题必须先从备案、合法域名、HTTPS/SNI、云防护和 Nginx 入口排查，不能先反复修改业务代码。

关键经验：

- TLS 握手失败时，请求还没有到 Java 后端。
- 最小 Nginx Demo 比反复测业务接口更可靠。
- 未备案或备案接入未生效，会让小程序表现得像“网络连接失败”。
- 以后必须先分层验证，再进入业务代码。
