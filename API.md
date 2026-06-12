# 好物展示小助手 API 文档

版本：v1.0.0  
定位：个人生活好物内容展示平台，不包含经营性能力。

## 通用响应

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "requestId": "排查编号"
}
```

所有接口响应头包含 `X-Request-Id`。线上排查时优先用该编号定位 Nginx 与 Java 日志。

## 公开小程序接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/mini/banners` | 获取启用的 Banner |
| GET | `/api/mini/config` | 获取小程序页面配置：首页文案、精选标题、搜索热词、我的页说明 |
| GET | `/api/mini/categories` | 获取启用的分类 |
| GET | `/api/mini/items` | 分页获取已发布内容，支持 `categoryId`、`keyword`、`pageNum`、`pageSize` |
| GET | `/api/mini/items/{id}` | 获取内容详情 |
| GET | `/api/mini/cos` | 获取当前腾讯 COS 图片域名配置 |
| GET | `/api/mini/ai/settings` | 获取小程序 AI 入口开关和可选模型 |
| POST | `/api/mini/ai/analyze-image` | 上传图片并选择 `providerCode` 进行 AI 分析，使用 multipart/form-data |

## 管理后台接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/admin/auth/login` | 管理员登录 |
| GET | `/api/admin/auth/me` | 当前登录人 |
| GET | `/api/admin/dashboard` | 内容管理总览 |
| GET/PUT | `/api/admin/mini-config` | 小程序页面配置读取与更新 |
| GET/POST/PUT | `/api/admin/items` | 内容列表、新增、更新 |
| GET/POST/PUT | `/api/admin/categories` | 分类列表、新增、更新 |
| GET/POST/PUT | `/api/admin/banners` | Banner 列表、新增、更新 |
| GET/PUT | `/api/admin/ai/settings` | AI 总开关、自动入库、自动发布、限额和图片大小配置 |
| GET/PUT | `/api/admin/ai/models` | Kimi、豆包模型配置、模型名、Base URL、单价和启用状态 |
| GET | `/api/admin/ai/image-tasks` | AI 图片分析任务列表 |
| POST | `/api/admin/ai/image-tasks/{id}/confirm` | 后台确认 AI 分析结果，创建分类/内容草稿或发布内容 |
| POST | `/api/admin/ai/image-tasks/{id}/reject` | 驳回 AI 图片分析任务 |
| GET | `/api/admin/ai/call-logs` | AI 调用日志、token、耗时和估算费用 |

后台接口需要 `Authorization: Bearer {token}`。

## 诊断接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/diagnostics/ready` | 检查数据库、公开域名、COS 配置是否可读 |

## 图片策略

图片只使用腾讯 COS/CDN 地址。正式上线前必须确认微信公众平台 downloadFile 合法域名包含对应 COS/CDN 域名。

小程序 AI 图片分析上传链路为：小程序上传到后端 `/api/mini/ai/analyze-image`，后端转存腾讯 COS，数据库保存 COS URL 和媒体元数据。正式上线前还必须确认微信公众平台 uploadFile 合法域名包含 `https://zanzanai.top`。

## AI 密钥策略

Kimi、豆包和 COS 写入密钥只允许通过服务器环境变量注入：

```text
KIMI_API_KEY
DOUBAO_API_KEY
COS_SECRET_ID
COS_SECRET_KEY
```

后台模型配置只保存模型名、Base URL、环境变量名和单价，不保存真实 API Key。
