# AI 大模型接入配置说明

更新时间：2026-06-13  
适用分支：`codex/ai-analytics-entry`

## 当前功能

本分支新增“小程序上传图片，后端调用 Kimi 或豆包分析，后台确认或自动入库”的能力。

默认策略：

- AI 功能总开关：关闭。
- 自动入库：关闭。
- 自动发布：关闭。
- 图片文件：上传到腾讯 COS。
- 数据库：只保存图片 URL、objectKey、大小、hash、AI 任务和调用日志。
- 小程序展示：只有 `content_items.status = 'PUBLISHED'` 的内容才展示。

## 需要你提供的信息

这些信息不要发到前端代码、不要写进 Git：

| 信息 | 从哪里获取 | 服务器环境变量 |
| --- | --- | --- |
| Kimi API Key | Kimi 开放平台控制台 | `KIMI_API_KEY` |
| 豆包/火山方舟 API Key | 火山方舟控制台 | `DOUBAO_API_KEY` |
| 腾讯云 SecretId | 腾讯云访问管理 CAM | `COS_SECRET_ID` |
| 腾讯云 SecretKey | 腾讯云访问管理 CAM | `COS_SECRET_KEY` |

如果暂时只测试一个模型，可以只配置其中一个模型的 Key，并在后台只启用这个模型。

## Kimi 配置步骤

1. 打开 Kimi 开放平台控制台。
2. 进入 API Key 管理页面。
3. 创建或复制一个可用于视觉模型的 API Key。
4. 不要把 Key 发到小程序、后台页面或 Git。
5. 在服务器环境文件 `/etc/good-items-api.env` 中增加：

```bash
KIMI_API_KEY=你的Kimi API Key
```

后台模型配置建议：

```text
providerCode: kimi
displayName: Kimi 视觉模型
modelName: moonshot-v1-8k-vision-preview
baseUrl: https://api.moonshot.cn/v1
apiKeyEnv: KIMI_API_KEY
```

## 豆包配置步骤

1. 打开火山方舟控制台。
2. 确认可用的豆包视觉/多模态模型。
3. 创建 API Key。
4. 记录需要调用的模型名。
5. 在服务器环境文件 `/etc/good-items-api.env` 中增加：

```bash
DOUBAO_API_KEY=你的火山方舟 API Key
```

后台模型配置建议：

```text
providerCode: doubao
displayName: 豆包视觉模型
modelName: 以火山方舟控制台实际开通的模型名为准
baseUrl: https://ark.cn-beijing.volces.com/api/v3
apiKeyEnv: DOUBAO_API_KEY
```

## 腾讯 COS 写入配置步骤

当前项目已经有 COS 公开访问域名，但 AI 上传图片还需要后端具备写入权限。

1. 打开腾讯云访问管理 CAM。
2. 创建一个最小权限子用户或访问密钥。
3. 只授予当前 COS Bucket 写入权限，避免给过大权限。
4. 在服务器环境文件 `/etc/good-items-api.env` 中增加：

```bash
COS_SECRET_ID=你的腾讯云 SecretId
COS_SECRET_KEY=你的腾讯云 SecretKey
```

已有配置仍需保留：

```bash
COS_BASE_URL=https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com
COS_BUCKET=ai-file-1409230880
COS_REGION=ap-guangzhou
```

## 后台开关顺序

上线或测试时按这个顺序开：

1. 先执行数据库新增表脚本。
2. 部署新版后端和后台。
3. 在服务器配置 `KIMI_API_KEY`、`DOUBAO_API_KEY`、`COS_SECRET_ID`、`COS_SECRET_KEY`。
4. 重启后端服务。
5. 登录后台，进入“AI 设置”。
6. 启用要测试的模型。
7. 打开“AI 功能总开关”。
8. 第一次测试建议保持“自动入库”和“自动发布”关闭。
9. 小程序“我的”页出现 AI 图片分析入口。
10. 上传测试图，确认后台“AI 图片分析”出现待确认任务。
11. 后台确认生成草稿。
12. 检查内容管理中草稿是否正确。
13. 手动发布后，小程序前端才展示。

## 自动入库策略

推荐初期配置：

```text
AI 功能总开关：开启
自动入库：关闭
自动发布：关闭
低置信度转人工：开启
置信度阈值：0.75
每日调用上限：50
单图大小限制：5MB
```

功能稳定后可以改为：

```text
AI 功能总开关：开启
自动入库：开启
自动发布：关闭
```

不建议一开始开启自动发布。自动发布开启后，小程序上传图片并被 AI 成功分析时，内容会直接进入公开展示列表。

## 微信后台域名要求

小程序正式体验版必须配置：

```text
request 合法域名：https://zanzanai.top
uploadFile 合法域名：https://zanzanai.top
downloadFile 合法域名：https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com
```

缺少 uploadFile 合法域名时，小程序会出现图片上传失败。
