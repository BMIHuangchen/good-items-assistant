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
          <el-alert title="用户与 AI 总览" type="info" :closable="false" />
          <div class="metric-grid compact">
            <div class="metric"><span>注册用户</span><strong>{{ analytics?.totalUsers || 0 }}</strong></div>
            <div class="metric"><span>今日登录</span><strong>{{ analytics?.todayLogins || 0 }}</strong></div>
            <div class="metric"><span>今日活跃</span><strong>{{ analytics?.todayActiveUsers || 0 }}</strong></div>
            <div class="metric"><span>AI Token</span><strong>{{ analytics?.totalAiTokens || 0 }}</strong></div>
            <div class="metric"><span>AI 估算费用</span><strong>¥{{ money(analytics?.totalAiEstimatedCost) }}</strong></div>
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

      <template v-if="activeTab === 'analytics'">
        <div class="metric-grid">
          <div class="metric"><span>注册用户</span><strong>{{ analytics?.totalUsers || 0 }}</strong></div>
          <div class="metric"><span>今日登录</span><strong>{{ analytics?.todayLogins || 0 }}</strong></div>
          <div class="metric"><span>今日活跃用户</span><strong>{{ analytics?.todayActiveUsers || 0 }}</strong></div>
          <div class="metric"><span>行为事件</span><strong>{{ analytics?.totalBehaviorEvents || 0 }}</strong></div>
          <div class="metric"><span>AI 调用</span><strong>{{ analytics?.totalAiCalls || 0 }}</strong></div>
          <div class="metric"><span>估算费用</span><strong>¥{{ money(analytics?.totalAiEstimatedCost) }}</strong></div>
        </div>
        <div class="panel-grid">
          <div class="panel">
            <h3>登录趋势</h3>
            <div v-for="point in analytics?.loginTrend || []" :key="point.date" class="bar-row">
              <span>{{ point.date }}</span><div><i :style="{ width: barWidth(point.value, analytics?.loginTrend || []) }"></i></div><b>{{ point.value }}</b>
            </div>
          </div>
          <div class="panel">
            <h3>AI 调用趋势</h3>
            <div v-for="point in analytics?.aiTrend || []" :key="point.date" class="bar-row">
              <span>{{ point.date }}</span><div><i :style="{ width: barWidth(point.value, analytics?.aiTrend || []) }"></i></div><b>{{ point.value }}</b>
            </div>
          </div>
          <div class="panel">
            <h3>功能使用排行</h3>
            <div v-for="item in analytics?.eventRanking || []" :key="item.name" class="rank-row">
              <span>{{ item.name }}</span><strong>{{ item.value }}</strong>
            </div>
          </div>
          <div class="panel">
            <h3>模型使用排行</h3>
            <div v-for="item in analytics?.modelUsage || []" :key="item.name" class="rank-row">
              <span>{{ item.name }}</span><strong>{{ item.value }}</strong>
            </div>
          </div>
          <div class="panel">
            <h3>内容浏览热度</h3>
            <div v-for="item in analytics?.hotItems || []" :key="item.name" class="rank-row">
              <span>{{ item.name }}</span><strong>{{ item.value }}</strong>
            </div>
          </div>
        </div>
      </template>

      <template v-if="activeTab === 'users'">
        <div class="panel">
          <el-button type="primary" @click="loadUserData">刷新用户数据</el-button>
          <el-table :data="users" style="margin-top: 12px">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="openid" label="OpenID" min-width="220" show-overflow-tooltip />
            <el-table-column prop="nickname" label="昵称" width="140" />
            <el-table-column prop="status" label="状态" width="100" />
            <el-table-column prop="loginCount" label="登录次数" width="100" />
            <el-table-column prop="firstLoginAt" label="首次登录" width="180" />
            <el-table-column prop="lastLoginAt" label="最近登录" width="180" />
          </el-table>
        </div>
        <div class="panel" style="margin-top: 16px">
          <h3>用户 AI Token 与费用</h3>
          <el-table :data="userAiUsage" style="margin-top: 12px">
            <el-table-column prop="userId" label="用户 ID" width="90" />
            <el-table-column prop="openidMask" label="OpenID" width="150" />
            <el-table-column prop="callCount" label="调用" width="90" />
            <el-table-column prop="successCount" label="成功" width="90" />
            <el-table-column prop="failedCount" label="失败" width="90" />
            <el-table-column prop="totalTokens" label="Token" width="120" />
            <el-table-column label="估算费用" width="120">
              <template #default="{ row }">¥{{ money(row.estimatedCost) }}</template>
            </el-table-column>
            <el-table-column prop="avgDurationMs" label="平均耗时 ms" width="120" />
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

      <template v-if="activeTab === 'aiSettings'">
        <div class="panel">
          <el-alert title="AI Key 不在后台保存，只读取服务器环境变量。开启自动入库前建议先保持自动发布关闭。" type="warning" :closable="false" />
          <el-form :model="aiSettingsForm" label-position="top" style="margin-top: 16px; max-width: 760px">
            <el-form-item label="AI 功能总开关"><el-switch v-model="aiSettingsForm.aiEnabled" /></el-form-item>
            <el-form-item label="自动入库"><el-switch v-model="aiSettingsForm.autoIngestEnabled" /></el-form-item>
            <el-form-item label="自动发布"><el-switch v-model="aiSettingsForm.autoPublishEnabled" /></el-form-item>
            <el-form-item label="低置信度转人工"><el-switch v-model="aiSettingsForm.lowConfidenceReviewEnabled" /></el-form-item>
            <el-form-item label="自动入库置信度阈值"><el-input-number v-model="aiSettingsForm.confidenceThreshold" :min="0" :max="1" :step="0.05" /></el-form-item>
            <el-form-item label="每日调用上限"><el-input-number v-model="aiSettingsForm.dailyCallLimit" :min="1" :max="1000" /></el-form-item>
            <el-form-item label="单图大小限制 MB"><el-input-number v-model="aiSettingsForm.maxImageSizeMb" :min="1" :max="10" /></el-form-item>
            <el-button type="primary" @click="saveAiSettings">保存 AI 开关</el-button>
          </el-form>
        </div>
        <div class="panel" style="margin-top: 16px">
          <el-table :data="aiModels">
            <el-table-column prop="displayName" label="模型" width="150" />
            <el-table-column prop="providerCode" label="标识" width="100" />
            <el-table-column prop="modelName" label="模型名" min-width="210" />
            <el-table-column prop="apiKeyEnv" label="Key 环境变量" width="150" />
            <el-table-column prop="enabled" label="启用" width="90" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }"><el-button link type="primary" @click="openAiModel(row)">编辑</el-button></template>
            </el-table-column>
          </el-table>
        </div>
      </template>

      <template v-if="activeTab === 'aiTasks'">
        <div class="panel">
          <el-button type="primary" @click="loadAiData">刷新 AI 任务</el-button>
          <el-table :data="aiTasks" style="margin-top: 12px">
            <el-table-column label="图片" width="88">
              <template #default="{ row }"><img class="cover" :src="row.mediaUrl" /></template>
            </el-table-column>
            <el-table-column prop="itemTitle" label="AI 标题" min-width="160" />
            <el-table-column prop="providerCode" label="模型" width="90" />
            <el-table-column prop="openidMask" label="用户" width="130" />
            <el-table-column prop="status" label="状态" width="130" />
            <el-table-column prop="matchedCategoryName" label="推荐分类" width="130" />
            <el-table-column prop="newCategoryName" label="新增分类" width="130" />
            <el-table-column prop="confidence" label="置信度" width="100" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <template v-if="['PENDING_REVIEW', 'PENDING_USER_CONFIRM'].includes(row.status)">
                  <el-button link type="primary" @click="openAiTask(row)">确认</el-button>
                  <el-button link type="danger" @click="rejectAiTask(row.id)">驳回</el-button>
                </template>
                <el-tag v-else-if="row.status === 'CONFIRMED'" type="success">已入库</el-tag>
                <el-tag v-else-if="row.status === 'REJECTED'" type="info">已驳回</el-tag>
                <el-tag v-else type="info">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="panel" style="margin-top: 16px">
          <el-table :data="aiCallLogs">
            <el-table-column prop="createdAt" label="时间" width="180" />
            <el-table-column prop="providerCode" label="模型" width="90" />
            <el-table-column prop="openidMask" label="用户" width="130" />
            <el-table-column prop="status" label="状态" width="90" />
            <el-table-column prop="totalTokens" label="Token" width="90" />
            <el-table-column prop="estimatedCost" label="估算费用" width="110" />
            <el-table-column prop="durationMs" label="耗时 ms" width="100" />
            <el-table-column prop="errorMessage" label="错误" />
          </el-table>
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
          <el-option v-for="category in categories" :key="category.id" :label="categoryLabel(category)" :value="category.id" />
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

  <el-dialog v-model="aiModelDialog" title="AI 模型配置" width="680px">
    <el-form :model="aiModelForm" label-position="top">
      <el-form-item label="显示名称"><el-input v-model="aiModelForm.displayName" /></el-form-item>
      <el-form-item label="模型名"><el-input v-model="aiModelForm.modelName" /></el-form-item>
      <el-form-item label="Base URL"><el-input v-model="aiModelForm.baseUrl" /></el-form-item>
      <el-form-item label="API Key 环境变量名"><el-input v-model="aiModelForm.apiKeyEnv" /></el-form-item>
      <el-form-item label="启用"><el-switch v-model="aiModelForm.enabled" /></el-form-item>
      <el-form-item label="输入单价/千 token"><el-input-number v-model="aiModelForm.promptPricePer1k" :min="0" :step="0.001" /></el-form-item>
      <el-form-item label="输出单价/千 token"><el-input-number v-model="aiModelForm.completionPricePer1k" :min="0" :step="0.001" /></el-form-item>
    </el-form>
    <template #footer><el-button type="primary" @click="saveAiModel">保存模型</el-button></template>
  </el-dialog>

  <el-dialog v-model="aiTaskDialog" title="确认 AI 图片分析" width="760px">
    <el-form :model="aiTaskForm" label-position="top">
      <img v-if="selectedAiTask?.mediaUrl" class="preview" :src="selectedAiTask.mediaUrl" />
      <el-form-item label="使用现有分类">
        <el-select v-model="aiTaskForm.categoryId" style="width: 100%" :disabled="aiTaskForm.createNewCategory">
          <el-option v-for="category in categories" :key="category.id" :label="categoryLabel(category)" :value="category.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="新建分类"><el-switch v-model="aiTaskForm.createNewCategory" /></el-form-item>
      <el-form-item v-if="aiTaskForm.createNewCategory" label="新分类名称"><el-input v-model="aiTaskForm.newCategoryName" /></el-form-item>
      <el-form-item label="标题"><el-input v-model="aiTaskForm.itemTitle" /></el-form-item>
      <el-form-item label="摘要"><el-input v-model="aiTaskForm.summary" /></el-form-item>
      <el-form-item label="体验心得"><el-input v-model="aiTaskForm.experience" type="textarea" :rows="4" /></el-form-item>
      <el-form-item label="标签，逗号分隔"><el-input v-model="aiTaskTagText" /></el-form-item>
      <el-form-item label="确认后直接发布"><el-switch v-model="aiTaskForm.publish" /></el-form-item>
      <el-alert v-if="selectedAiTask?.reviewReason" :title="selectedAiTask.reviewReason" type="warning" :closable="false" />
    </el-form>
    <template #footer>
      <el-button type="primary" :disabled="!['PENDING_REVIEW', 'PENDING_USER_CONFIRM'].includes(selectedAiTask?.status || '')" @click="confirmAiTask">确认入库</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
import type { AiCallLog, AiFeatureSettings, AiImageAnalysisTask, AiModelConfig, AnalyticsOverview, Banner, Category, DashboardStats, GoodItem, MiniProgramConfig, MiniUser, UserAiUsage } from '@/types'

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
const aiSettings = ref<AiFeatureSettings | null>(null)
const aiModels = ref<AiModelConfig[]>([])
const aiTasks = ref<AiImageAnalysisTask[]>([])
const aiCallLogs = ref<AiCallLog[]>([])
const users = ref<MiniUser[]>([])
const userAiUsage = ref<UserAiUsage[]>([])
const analytics = ref<AnalyticsOverview | null>(null)

const itemDialog = ref(false)
const categoryDialog = ref(false)
const bannerDialog = ref(false)
const itemForm = reactive<Partial<GoodItem>>({})
const categoryForm = reactive<Partial<Category>>({})
const bannerForm = reactive<Partial<Banner>>({})
const miniConfigForm = reactive<Partial<MiniProgramConfig>>({})
const aiSettingsForm = reactive<Partial<AiFeatureSettings>>({})
const aiModelDialog = ref(false)
const aiModelForm = reactive<Partial<AiModelConfig>>({})
const aiTaskDialog = ref(false)
const selectedAiTask = ref<AiImageAnalysisTask | null>(null)
const aiTaskForm = reactive<Record<string, any>>({})
const aiTaskTagText = ref('')
const tagText = ref('')
const hotWordText = ref('')
const galleryText = ref('')

const navItems = [
  { key: 'dashboard', label: '总览', icon: 'DataLine' },
  { key: 'items', label: '好物内容', icon: 'Collection' },
  { key: 'categories', label: '分类', icon: 'Grid' },
  { key: 'banners', label: 'Banner', icon: 'Picture' },
  { key: 'miniConfig', label: '小程序页面', icon: 'EditPen' },
  { key: 'analytics', label: '数据看板', icon: 'TrendCharts' },
  { key: 'users', label: '用户管理', icon: 'User' },
  { key: 'aiSettings', label: 'AI 设置', icon: 'Setting' },
  { key: 'aiTasks', label: 'AI 图片分析', icon: 'Camera' },
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

function categoryLabel(category: Category) {
  return category.enabled ? category.name : `${category.name}（未启用）`
}

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
  await Promise.all([loadAiData(), loadUserData(), loadAnalytics()])
}

async function loadUserData() {
  const [userList, usageList] = await Promise.all([
    api.users({ pageSize: 100 }),
    api.userAiUsage({ pageSize: 100 }),
  ])
  users.value = userList
  userAiUsage.value = usageList
}

async function loadAnalytics() {
  analytics.value = await api.analyticsOverview()
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

async function loadAiData() {
  const [settings, models, tasks, logs] = await Promise.all([
    api.aiSettings(),
    api.aiModels(),
    api.aiImageTasks({ pageSize: 80 }),
    api.aiCallLogs({ pageSize: 80 }),
  ])
  aiSettings.value = settings
  Object.assign(aiSettingsForm, settings)
  aiModels.value = models
  aiTasks.value = tasks
  aiCallLogs.value = logs
}

function money(value: unknown) {
  return Number(value || 0).toFixed(4)
}

function barWidth(value: number, points: Array<{ value: number }>) {
  const max = Math.max(1, ...points.map((item) => Number(item.value || 0)))
  return `${Math.max(4, Math.round((Number(value || 0) / max) * 100))}%`
}

async function saveAiSettings() {
  await api.updateAiSettings(aiSettingsForm)
  ElMessage.success('AI 设置已保存')
  await loadAiData()
}

function openAiModel(row: AiModelConfig) {
  Object.assign(aiModelForm, row)
  aiModelDialog.value = true
}

async function saveAiModel() {
  if (!aiModelForm.id) return
  await api.updateAiModel(aiModelForm.id, aiModelForm)
  ElMessage.success('AI 模型配置已保存')
  aiModelDialog.value = false
  await loadAiData()
}

function openAiTask(row: AiImageAnalysisTask) {
  selectedAiTask.value = row
  Object.assign(aiTaskForm, {
    categoryId: row.matchedCategoryId || categories.value[0]?.id,
    createNewCategory: row.decision === 'NEW_CATEGORY',
    newCategoryName: row.newCategoryName,
    newCategorySlug: row.newCategorySlug,
    newCategoryDescription: row.newCategoryDescription,
    itemTitle: row.itemTitle,
    summary: row.summary,
    experience: row.experience,
    publish: false,
  })
  aiTaskTagText.value = row.tags?.join(',') || ''
  aiTaskDialog.value = true
}

async function confirmAiTask() {
  if (!selectedAiTask.value) return
  await api.confirmAiImageTask(selectedAiTask.value.id, {
    ...aiTaskForm,
    tags: aiTaskTagText.value.split(',').map((item) => item.trim()).filter(Boolean),
  })
  ElMessage.success('AI 分析结果已入库')
  aiTaskDialog.value = false
  await loadAll()
}

async function rejectAiTask(id: number) {
  await api.rejectAiImageTask(id, '后台驳回')
  ElMessage.success('已驳回')
  await loadAiData()
}

onMounted(loadAll)
</script>

<style scoped>
.compact {
  margin-top: 16px;
}

.panel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
}

.panel h3 {
  margin: 0 0 14px;
  font-size: 16px;
  color: #1f2933;
}

.bar-row {
  display: grid;
  grid-template-columns: 86px 1fr 48px;
  align-items: center;
  gap: 10px;
  margin: 10px 0;
  font-size: 13px;
  color: #52616b;
}

.bar-row div {
  height: 10px;
  background: #edf2f7;
  border-radius: 999px;
  overflow: hidden;
}

.bar-row i {
  display: block;
  height: 100%;
  background: #2f6b4f;
  border-radius: inherit;
}

.bar-row b,
.rank-row strong {
  color: #1f2933;
  font-weight: 700;
  text-align: right;
}

.rank-row {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 10px 0;
  border-bottom: 1px solid #edf0eb;
  color: #52616b;
}

.rank-row span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
