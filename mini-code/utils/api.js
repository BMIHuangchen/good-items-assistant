const API_BASE_URL = 'https://zanzanai.top/api'
const COS_BASE_URL = 'https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com'

export const fallbackCategories = [
  { id: 4, name: '家居', slug: 'home', description: '适合家里日常使用的小物件', coverImage: `${COS_BASE_URL}/good-items/categories/home.jpg` },
  { id: 5, name: '收纳', slug: 'storage', description: '让空间更整齐的收纳用品', coverImage: `${COS_BASE_URL}/good-items/categories/storage.jpg` },
  { id: 6, name: '出行', slug: 'travel', description: '随身携带和外出使用的小物', coverImage: `${COS_BASE_URL}/good-items/categories/travel.jpg` },
  { id: 7, name: '数码', slug: 'digital', description: '日常使用的数码周边和小工具', coverImage: `${COS_BASE_URL}/good-items/categories/digital.jpg` },
  { id: 8, name: '清洁', slug: 'cleaning', description: '提升清洁效率的小用品', coverImage: `${COS_BASE_URL}/good-items/categories/cleaning.jpg` },
  { id: 9, name: '日用', slug: 'daily', description: '每天都能顺手用上的生活小物', coverImage: `${COS_BASE_URL}/good-items/categories/daily.jpg` }
]

export const fallbackItems = [
  {
    id: 1,
    categoryId: 9,
    categoryName: '日用',
    title: '便携纸巾盒',
    summary: '适合随身放纸巾和小包装湿巾。',
    experience: '它适合放在包里、车里或办公桌边。取用顺手，外观看起来也比散放纸巾更整洁。',
    tags: ['日用', '图文心得', '随身'],
    coverImage: `${COS_BASE_URL}/good-items/items/tissue-box-cover.jpg`,
    gallery: [`${COS_BASE_URL}/good-items/items/tissue-box-1.jpg`]
  },
  {
    id: 2,
    categoryId: 9,
    categoryName: '日用',
    title: '随手记事本',
    summary: '适合记录临时想法、清单和待办。',
    experience: '纸笔记录比手机更直接，适合放在书桌、包里或床头。平时写购物清单、灵感和提醒都方便。',
    tags: ['日用', '图文心得', '记录'],
    coverImage: `${COS_BASE_URL}/good-items/items/notebook-cover.jpg`,
    gallery: [`${COS_BASE_URL}/good-items/items/notebook-1.jpg`]
  },
  {
    id: 3,
    categoryId: 5,
    categoryName: '收纳',
    title: '桌面分格收纳盒',
    summary: '适合放数据线、便签和零碎小工具。',
    experience: '透明分格方便一眼找到东西，放在书桌角落不会显得突兀。实际使用后，桌面会更容易保持清爽。',
    tags: ['收纳', '桌面', '图文心得'],
    coverImage: `${COS_BASE_URL}/good-items/items/desk-organizer-cover.jpg`,
    gallery: [`${COS_BASE_URL}/good-items/items/desk-organizer-1.jpg`]
  },
  {
    id: 4,
    categoryId: 4,
    categoryName: '家居',
    title: '台灯',
    summary: '适合放在书桌、床头和阅读角。',
    experience: '小体积不会占太多桌面空间，光线集中，适合夜间阅读或整理桌面时使用。',
    tags: ['家居', '图文心得', '桌面'],
    coverImage: `${COS_BASE_URL}/good-items/items/lamp-cover.jpg`,
    gallery: [`${COS_BASE_URL}/good-items/items/lamp-1.jpg`]
  },
  {
    id: 5,
    categoryId: 6,
    categoryName: '出行',
    title: '杂物小包',
    summary: '适合放纸巾、卡片和零碎随身小物。',
    experience: '外出时把小物集中放在一个小包里，拿取会更顺手，也能减少包内翻找。',
    tags: ['出行', '图文心得', '随身'],
    coverImage: `${COS_BASE_URL}/good-items/items/travel-pouch-cover.jpg`,
    gallery: [`${COS_BASE_URL}/good-items/items/travel-pouch-1.jpg`]
  },
  {
    id: 6,
    categoryId: 7,
    categoryName: '数码',
    title: '笔记本台灯',
    summary: '适合记录设备清单、账号提示和临时想法。',
    experience: '放在电脑旁边，用来记临时任务、设备编号或待办事项，比打开应用更快。',
    tags: ['数码', '图文心得', '记录'],
    coverImage: `${COS_BASE_URL}/good-items/items/digital-desk-cover.jpg`,
    gallery: [`${COS_BASE_URL}/good-items/items/digital-desk-1.jpg`]
  },
  {
    id: 7,
    categoryId: 8,
    categoryName: '清洁',
    title: '迷你清洁喷瓶',
    summary: '适合分装少量清洁液，放在厨房、浴室或桌面边。',
    experience: '小瓶身拿取方便，适合处理局部污渍和台面清洁，日常补水或清洁都很顺手。',
    tags: ['清洁', '图文心得', '小物'],
    coverImage: `${COS_BASE_URL}/good-items/items/cleaning-spray-cover.jpg`,
    gallery: [`${COS_BASE_URL}/good-items/items/cleaning-spray-1.jpg`]
  }
]

export const fallbackBanners = [
  { id: 1, title: '日用好物灵感', imageUrl: `${COS_BASE_URL}/good-items/banners/daily-finds.jpg`, targetType: 'CATEGORY', targetValue: 'daily' },
  { id: 2, title: '小空间整理灵感', imageUrl: `${COS_BASE_URL}/good-items/banners/storage-ideas.jpg`, targetType: 'CATEGORY', targetValue: 'storage' }
]

export const fallbackConfig = {
  heroEyebrow: 'Good Finds',
  heroTitle: '好物展示小助手',
  heroSubtitle: '记录日常生活里真正顺手的小物件',
  featuredTitle: '精选好物',
  searchPlaceholder: '搜索好物、标签或体验关键词',
  hotWords: ['收纳', '日用', '清洁', '数码'],
  meTitle: '好物展示小助手',
  meDescription: '这里用于浏览个人生活好物记录，是非经营性的内容展示工具。'
}

export function request(path, data = {}) {
  return new Promise((resolve, reject) => {
    const requestId = `${Date.now()}-${Math.random().toString(16).slice(2)}`
    uni.request({
      url: `${API_BASE_URL}${path}`,
      method: 'GET',
      data,
      timeout: 8000,
      header: { 'X-Request-Id': requestId },
      success(res) {
        const body = res.data || {}
        if (res.statusCode === 200 && body.code === 200) {
          resolve(body.data)
          return
        }
        const error = { requestId: body.requestId || requestId, message: body.message || '接口返回异常', path }
        uni.setStorageSync('lastNetworkIssue', error)
        reject(error)
      },
      fail(err) {
        const error = { requestId, message: err.errMsg || '网络连接失败', path }
        uni.setStorageSync('lastNetworkIssue', error)
        reject(error)
      }
    })
  })
}

export function upload(path, filePath, formData = {}) {
  return new Promise((resolve, reject) => {
    const requestId = `${Date.now()}-${Math.random().toString(16).slice(2)}`
    uni.uploadFile({
      url: `${API_BASE_URL}${path}`,
      filePath,
      name: 'file',
      formData,
      timeout: 30000,
      header: { 'X-Request-Id': requestId },
      success(res) {
        let body = {}
        try {
          body = typeof res.data === 'string' ? JSON.parse(res.data) : (res.data || {})
        } catch (error) {
          body = {}
        }
        if (res.statusCode === 200 && body.code === 200) {
          resolve(body.data)
          return
        }
        const error = { requestId: body.requestId || requestId, message: body.message || '上传接口返回异常', path }
        uni.setStorageSync('lastNetworkIssue', error)
        reject(error)
      },
      fail(err) {
        const error = { requestId, message: err.errMsg || '图片上传失败', path }
        uni.setStorageSync('lastNetworkIssue', error)
        reject(error)
      }
    })
  })
}

export function recordImageIssue(url, source = 'unknown') {
  const issue = {
    url,
    source,
    time: new Date().toISOString()
  }
  uni.setStorageSync('lastImageIssue', issue)
  console.warn('[image-load-failed]', issue)
}

export async function getHomeData() {
  const fallbackModules = {
    config: false,
    banners: false,
    items: false
  }

  let config = fallbackConfig
  let banners = []
  let items = []
  const errors = []

  try {
    config = await request('/mini/config')
  } catch (error) {
    fallbackModules.config = true
    errors.push(`config:${error.message || error.errMsg || 'failed'}`)
  }

  try {
    banners = await request('/mini/banners')
  } catch (error) {
    fallbackModules.banners = true
    errors.push(`banners:${error.message || error.errMsg || 'failed'}`)
  }

  try {
    const page = await request('/mini/items', { pageSize: 10 })
    items = page.list || []
  } catch (error) {
    fallbackModules.items = true
    errors.push(`items:${error.message || error.errMsg || 'failed'}`)
  }

  return {
    config,
    banners,
    items,
    fallback: fallbackModules.config || fallbackModules.banners || fallbackModules.items,
    fallbackModules
  }
}

export async function getMiniConfig() {
  try {
    return await request('/mini/config')
  } catch (error) {
    return fallbackConfig
  }
}

export async function getAiSettings() {
  try {
    return await request('/mini/ai/settings')
  } catch (error) {
    return { aiEnabled: false, models: [] }
  }
}

export async function analyzeImage(providerCode, filePath) {
  return upload('/mini/ai/analyze-image', filePath, { providerCode })
}

export async function getCategories() {
  try {
    return await request('/mini/categories')
  } catch (error) {
    uni.showToast({ title: '网络连接失败', icon: 'none' })
    return []
  }
}

export async function getItems(params = {}) {
  try {
    const page = await request('/mini/items', params)
    return page.list || []
  } catch (error) {
    uni.showToast({ title: '网络连接失败', icon: 'none' })
    return []
  }
}

export async function getItem(id) {
  try {
    return await request(`/mini/items/${id}`)
  } catch (error) {
    uni.showToast({ title: '网络连接失败', icon: 'none' })
    return null
  }
}

export function getFavorites() {
  return uni.getStorageSync('favoriteItems') || []
}

export function toggleFavorite(item) {
  const list = getFavorites()
  const exists = list.some((saved) => saved.id === item.id)
  const next = exists ? list.filter((saved) => saved.id !== item.id) : [item, ...list]
  uni.setStorageSync('favoriteItems', next)
  return !exists
}
