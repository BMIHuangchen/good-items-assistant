# 好物展示小助手 UniApp 小程序

这是个人生活好物内容展示小程序，仅用于浏览日用好物、生活小用品图文与体验心得。

## 页面

- `pages/home/home`：首页，展示 Banner 和精选内容。
- `pages/category/category`：按分类浏览内容。
- `pages/search/search`：关键词搜索内容。
- `pages/item-detail/item-detail`：内容详情和体验心得。
- `pages/favorites/favorites`：本地收藏。
- `pages/me/me`：项目说明和网络排查信息。

## 数据与图片

- API 基础地址：`https://zanzanai.top/api`。
- 图片统一使用腾讯 COS/CDN 域名：`https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com`。
- 正式验证时不应依赖本地兜底；如果首页出现“已启用本地兜底”，优先检查 HTTPS、微信合法域名、API、COS 和体验版缓存。
- 每次请求都会带 `X-Request-Id`，失败时写入本地 `lastNetworkIssue`，可在“我的”页面查看。

## 禁止事项

小程序不提供购买、支付、订单、购物车、收货地址、售后、商业推广或经营性活动能力。
