<template>
  <section v-if="!token" class="login-page">
    <div class="login-box">
      <h1>好物展示小助手</h1>
      <p>内容管理后台</p>
      <el-form :model="loginForm" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="账号">
          <el-input v-model="loginForm.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin">登录</el-button>
      </el-form>
    </div>
  </section>

  <section v-else class="admin-shell">
    <aside class="side">
      <div class="brand">
        <h1>好物展示小助手</h1>
        <p>内容、图片与上线质量管理</p>
      </div>
      <button v-for="item in navItems" :key="item.key" class="nav-item" :class="{ active: activeTab === item.key }" @click="activeTab = item.key">
        <el-icon><component :is="item.icon" /></el-icon>
        {{ item.label }}
      </button>
    </aside>

    <main class="main">
      <header class="topbar">
        <h2 class="page-title">{{ currentTitle }}</h2>
        <div>
          <el-button @click="loadAll">刷新</el-button>
          <el-button type="danger" plain @click="logout">退出</el-button>
        </div>
      </header>

      <template v-if="activeTab === 'dashboard'">
        <div class="metric-grid">
          <div v-for="metric in metrics" :key="metric.label" class="metric">
            <span>{{ metric.label }}</span>
            <strong>{{ metric.value }}</strong>
          </div>
        </div>
        <div class="panel">
          <el-alert title="上线把控角色" type="success" :closable="false" />
          <el-table :data="roles" style="margin-top: 12px">
            <el-table-column prop="role" label="角色" width="160" />
            <el-table-column prop="focus" label="把控重点" />
            <el-table-column prop="gate" label="上线门禁" />
          </el-table>
        </div>
      </template>

      <template v-if="activeTab === 'items'">
        <div class="panel">
          <el-button type="primary" @click="openItem()">新增内容</el-button>
          <el-table :data="items" style="margin-top: 12px">
            <el-table-column label="封面" width="86">
              <template #default="{ row }"><img class="cover" :src="row.coverImage" /></template>
            </el-table-column>
            <el-table-column prop="title" label="标题" min-width="180" />
            <el-table-column prop="categoryName" label="分类" width="130" />
            <el-table-column prop="status" label="状态" width="120" />
            <el-table-column prop="viewCount" label="浏览" width="90" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }"><el-button link type="primary" @click="openItem(row)">编辑</el-button></template>
            </el-table-column>
          </el-table>
        </div>
      </template>

      <template v-if="activeTab === 'categories'">
        <div class="panel">
          <el-button type="primary" @click="openCategory()">新增分类</el-button>
          <el-table :data="categories" style="margin-top: 12px">
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="slug" label="标识" />
            <el-table-column prop="sortOrder" label="排序" width="90" />
            <el-table-column prop="enabled" label="启用" width="90" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }"><el-button link type="primary" @click="openCategory(row)">编辑</el-button></template>
            </el-table-column>
          </el-table>
        </div>
      </template>

      <template v-if="activeTab === 'banners'">
        <div class="panel">
          <el-button type="primary" @click="openBanner()">新增 Banner</el-button>
          <el-table :data="banners" style="margin-top: 12px">
            <el-table-column label="图片" width="86">
              <template #default="{ row }"><img class="cover" :src="row.imageUrl" /></template>
            </el-table-column>
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="targetType" label="跳转类型" width="120" />
            <el-table-column prop="enabled" label="启用" width="90" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }"><el-button link type="primary" @click="openBanner(row)">编辑</el-button></template>
            </el-table-column>
          </el-table>
        </div>
      </template>

      <template v-if="activeTab === 'miniConfig'">
        <div class="panel">
          <el-alert title="这里控制小程序页面文案和搜索热词；分类、卡片和 Banner 分别在对应菜单维护。" type="info" :closable="false" />
          <el-form :model="miniConfigForm" label-position="top" style="margin-top: 16px; max-width: 840px">
            <el-form-item label="首页英文小标题"><el-input v-model="miniConfigForm.heroEyebrow" /></el-form-item>
            <el-form-item label="首页主标题"><el-input v-model="miniConfigForm.heroTitle" /></el-form-item>
            <el-form-item label="首页副标题"><el-input v-model="miniConfigForm.heroSubtitle" /></el-form-item>
            <el-form-item label="首页内容区标题"><el-input v-model="miniConfigForm.featuredTitle" /></el-form-item>
            <el-form-item label="搜索框提示语"><el-input v-model="miniConfigForm.searchPlaceholder" /></el-form-item>
            <el-form-item label="搜索热词，逗号分隔"><el-input v-model="hotWordText" /></el-form-item>
            <el-form-item label="我的页标题"><el-input v-model="miniConfigForm.meTitle" /></el-form-item>
            <el-form-item label="我的页说明"><el-input v-model="miniConfigForm.meDescription" type="textarea" :rows="4" /></el-form-item>
            <el-button type="primary" @click="saveMiniConfig">保存小程序页面配置</el-button>
          </el-form>
        </div>
      </template>

      <template v-if="activeTab === 'diagnostics'">
        <div class="panel">
          <el-button type="primary" :loading="loading" @click="checkReady">运行在线一致性检查</el-button>
          <pre style="white-space: pre-wrap">{{ diagnosticsText }}</pre>
        </div>
      </template>
    </main>
  </section>

  <el-dialog v-model="itemDialog" title="好物内容" width="720px">
    <el-form :model="itemForm" label-position="top">
      <el-form-item label="标题"><el-input v-model="itemForm.title" /></el-form-item>
      <el-form-item label="分类">
        <el-select v-model="itemForm.categoryId" style="width: 100%">
          <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="摘要"><el-input v-model="itemForm.summary" /></el-form-item>
      <el-form-item label="体验心得"><el-input v-model="itemForm.experience" type="textarea" :rows="5" /></el-form-item>
      <el-form-item label="标签，逗号分隔"><el-input v-model="tagText" /></el-form-item>
      <el-form-item label="腾讯 COS 封面 URL"><el-input v-model="itemForm.coverImage" /></el-form-item>
      <el-form-item label="详情图库 URL，逗号分隔"><el-input v-model="galleryText" type="textarea" :rows="3" /></el-form-item>
      <el-form-item label="排序，数字越大越靠前"><el-input-number v-model="itemForm.sortOrder" :min="0" :max="9999" /></el-form-item>
      <el-form-item label="状态">
        <el-segmented v-model="itemForm.status" :options="['DRAFT', 'PUBLISHED']" />
      </el-form-item>
    </el-form>
    <template #footer><el-button type="primary" @click="saveItem">保存</el-button></template>
  </el-dialog>

  <el-dialog v-model="categoryDialog" title="分类" width="560px">
    <el-form :model="categoryForm" label-position="top">
      <el-form-item label="名称"><el-input v-model="categoryForm.name" /></el-form-item>
      <el-form-item label="标识"><el-input v-model="categoryForm.slug" /></el-form-item>
      <el-form-item label="说明"><el-input v-model="categoryForm.description" /></el-form-item>
      <el-form-item label="腾讯 COS 封面 URL"><el-input v-model="categoryForm.coverImage" /></el-form-item>
      <el-form-item label="排序，数字越大越靠前"><el-input-number v-model="categoryForm.sortOrder" :min="0" :max="9999" /></el-form-item>
      <el-form-item label="启用"><el-switch v-model="categoryForm.enabled" /></el-form-item>
    </el-form>
    <template #footer><el-button type="primary" @click="saveCategory">保存</el-button></template>
  </el-dialog>

  <el-dialog v-model="bannerDialog" title="Banner" width="560px">
    <el-form :model="bannerForm" label-position="top">
      <el-form-item label="标题"><el-input v-model="bannerForm.title" /></el-form-item>
      <el-form-item label="腾讯 COS 图片 URL"><el-input v-model="bannerForm.imageUrl" /></el-form-item>
      <el-form-item label="跳转类型"><el-input v-model="bannerForm.targetType" /></el-form-item>
      <el-form-item label="跳转值"><el-input v-model="bannerForm.targetValue" /></el-form-item>
      <el-form-item label="排序，数字越大越靠前"><el-input-number v-model="bannerForm.sortOrder" :min="0" :max="9999" /></el-form-item>
      <el-form-item label="启用"><el-switch v-model="bannerForm.enabled" /></el-form-item>
    </el-form>
    <template #footer><el-button type="primary" @click="saveBanner">保存</el-button></template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
import type { Banner, Category, DashboardStats, GoodItem, MiniProgramConfig } from '@/types'

const token = ref(localStorage.getItem('token') || '')
const activeTab = ref('dashboard')
const loading = ref(false)
const loginForm = reactive({ username: 'admin', password: '' })
const stats = ref<DashboardStats>({ publishedItems: 0, draftItems: 0, categories: 0, banners: 0, totalViews: 0, totalFavorites: 0 })
const items = ref<GoodItem[]>([])
const categories = ref<Category[]>([])
const banners = ref<Banner[]>([])
const miniConfig = ref<MiniProgramConfig | null>(null)
const diagnosticsText = ref('尚未运行检查')

const itemDialog = ref(false)
const categoryDialog = ref(false)
const bannerDialog = ref(false)
const itemForm = reactive<Partial<GoodItem>>({})
const categoryForm = reactive<Partial<Category>>({})
const bannerForm = reactive<Partial<Banner>>({})
const miniConfigForm = reactive<Partial<MiniProgramConfig>>({})
const tagText = ref('')
const hotWordText = ref('')
const galleryText = ref('')

const navItems = [
  { key: 'dashboard', label: '总览', icon: 'DataLine' },
  { key: 'items', label: '好物内容', icon: 'Collection' },
  { key: 'categories', label: '分类', icon: 'Grid' },
  { key: 'banners', label: 'Banner', icon: 'Picture' },
  { key: 'miniConfig', label: '小程序页面', icon: 'EditPen' },
  { key: 'diagnostics', label: '上线诊断', icon: 'Monitor' },
]

const roles = [
  { role: '产品负责人', focus: '确认只做内容展示，不出现经营性承诺。', gate: '提交前审核页面和文案。' },
  { role: '后端负责人', focus: 'API 可用性、请求日志、数据库和腾讯 COS 配置。', gate: 'ready 接口通过，日志可按 requestId 定位。' },
  { role: '前端负责人', focus: '小程序和后台页面可访问、弱网兜底、图片域名一致。', gate: '真机检查核心路径。' },
  { role: '运维负责人', focus: 'HTTPS、Nginx、带宽、健康检查、发布回滚。', gate: '线上同构检查通过。' },
  { role: '审核负责人', focus: '微信审核材料、类目、隐私和非交易定位。', gate: '审核前冻结版本。' },
]

const currentTitle = computed(() => navItems.find((item) => item.key === activeTab.value)?.label || '')
const metrics = computed(() => [
  { label: '已发布内容', value: stats.value.publishedItems },
  { label: '草稿内容', value: stats.value.draftItems },
  { label: '分类', value: stats.value.categories },
  { label: 'Banner', value: stats.value.banners },
  { label: '浏览', value: stats.value.totalViews },
  { label: '收藏', value: stats.value.totalFavorites },
])

async function handleLogin() {
  loading.value = true
  try {
    const res = await api.login(loginForm.username, loginForm.password)
    localStorage.setItem('token', res.token)
    token.value = res.token
    await loadAll()
  } finally {
    loading.value = false
  }
}

function logout() {
  localStorage.removeItem('token')
  token.value = ''
}

async function loadAll() {
  if (!token.value) return
  const [dashboard, itemPage, categoryList, bannerList, config] = await Promise.all([
    api.dashboard(),
    api.items({ pageSize: 50 }),
    api.categories(),
    api.banners(),
    api.miniConfig(),
  ])
  stats.value = dashboard
  items.value = itemPage.list
  categories.value = categoryList
  banners.value = bannerList
  miniConfig.value = config
  Object.assign(miniConfigForm, config)
  hotWordText.value = config.hotWords?.join(',') || ''
}

function openItem(row?: GoodItem) {
  Object.assign(itemForm, row || { status: 'DRAFT', sortOrder: 0, categoryId: categories.value[0]?.id })
  tagText.value = row?.tags?.join(',') || ''
  galleryText.value = row?.gallery?.join(',') || ''
  itemDialog.value = true
}

async function saveItem() {
  const body = {
    ...itemForm,
    tags: tagText.value.split(',').map((item) => item.trim()).filter(Boolean),
    gallery: galleryText.value.split(',').map((item) => item.trim()).filter(Boolean),
  }
  itemForm.id ? await api.updateItem(itemForm.id, body) : await api.createItem(body)
  ElMessage.success('内容已保存')
  itemDialog.value = false
  await loadAll()
}

function openCategory(row?: Category) {
  Object.assign(categoryForm, row || { enabled: true, sortOrder: 0 })
  categoryDialog.value = true
}

async function saveCategory() {
  categoryForm.id ? await api.updateCategory(categoryForm.id, categoryForm) : await api.createCategory(categoryForm)
  ElMessage.success('分类已保存')
  categoryDialog.value = false
  await loadAll()
}

function openBanner(row?: Banner) {
  Object.assign(bannerForm, row || { enabled: true, targetType: 'NONE', sortOrder: 0 })
  bannerDialog.value = true
}

async function saveBanner() {
  bannerForm.id ? await api.updateBanner(bannerForm.id, bannerForm) : await api.createBanner(bannerForm)
  ElMessage.success('Banner 已保存')
  bannerDialog.value = false
  await loadAll()
}

async function saveMiniConfig() {
  const body = {
    ...miniConfigForm,
    hotWords: hotWordText.value.split(',').map((item) => item.trim()).filter(Boolean),
  }
  const config = await api.updateMiniConfig(body)
  miniConfig.value = config
  Object.assign(miniConfigForm, config)
  hotWordText.value = config.hotWords?.join(',') || ''
  ElMessage.success('小程序页面配置已保存')
}

async function checkReady() {
  loading.value = true
  try {
    const res = await api.ready()
    diagnosticsText.value = JSON.stringify(res, null, 2)
  } finally {
    loading.value = false
  }
}

onMounted(loadAll)
</script>
