<template>
  <view class="page">
    <view class="panel">
      <text class="title">{{ config.meTitle }}</text>
      <text class="desc">{{ config.meDescription }}</text>
    </view>
    <view class="panel">
      <text class="label">登录状态</text>
      <text class="status" :class="{ ok: !!user }">{{ loginStatus }}</text>
      <text v-if="user" class="desc">微信身份：{{ userIdentity(user) }}</text>
      <text v-if="user?.tierCode" class="desc">会员等级：{{ user.tierCode }}</text>
      <text v-if="user" class="desc">用户 ID：{{ user.id }}；登录次数：{{ user.loginCount || 0 }}</text>
      <text v-if="user?.lastLoginAt" class="desc">最近登录：{{ user.lastLoginAt }}</text>
      <text v-if="loginErrorText" class="desc">登录提示：{{ loginErrorText }}</text>
      <button @click="loginAgain">重新登录</button>
    </view>
    <view v-if="aiEnabled" class="panel">
      <text class="label">AI 图片分析</text>
      <text class="desc">上传生活好物图片，可在 Kimi 和豆包两个大模型入口中选择。Token 和费用为按模型单价估算。</text>
      <button @click="openAiImage">打开 AI 图片分析</button>
    </view>
    <view v-if="aiEnabled" class="panel">
      <text class="label">我的 AI 用量</text>
      <view class="usage-grid">
        <view>
          <text class="metric">{{ usage.today?.callCount || 0 }}</text>
          <text class="desc">今日调用</text>
        </view>
        <view>
          <text class="metric">{{ usage.month?.totalTokens || 0 }}</text>
          <text class="desc">本月 Token</text>
        </view>
        <view>
          <text class="metric">¥{{ money(usage.month?.estimatedCost) }}</text>
          <text class="desc">本月估算费用</text>
        </view>
      </view>
      <view v-for="model in usage.models || []" :key="model.providerCode" class="usage-row">
        <text>{{ model.providerCode }} · {{ model.callCount }} 次</text>
        <text>{{ model.totalTokens }} Token / ¥{{ money(model.estimatedCost) }}</text>
      </view>
    </view>
    <view class="panel">
      <text class="label">网络排查信息</text>
      <text class="desc">{{ issueText }}</text>
      <button @click="refreshIssue">刷新排查信息</button>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { clearLogin, ensureLogin, getAiSettings, getAiUsage, getCurrentUser, getMiniConfig } from '../../utils/api'

const issueText = ref('暂无网络异常记录')
const config = ref({})
const aiEnabled = ref(false)
const usage = ref({})
const user = ref(null)
const loginErrorText = ref('')
const loggingIn = ref(false)

const loginStatus = computed(() => {
  if (loggingIn.value) return '登录中'
  return user.value ? '已微信登录' : '未登录'
})

async function loadProfile() {
  user.value = getCurrentUser()
  if (!user.value) {
    await loginNow()
  }
  config.value = await getMiniConfig()
  const aiSettings = await getAiSettings()
  aiEnabled.value = !!aiSettings.aiEnabled
  usage.value = aiEnabled.value ? await getAiUsage().catch(() => ({})) : {}
}

async function loginNow() {
  loggingIn.value = true
  loginErrorText.value = ''
  try {
    user.value = await ensureLogin()
  } catch (error) {
    user.value = null
    loginErrorText.value = error?.message || error?.errMsg || '微信登录失败'
    refreshIssue()
  } finally {
    loggingIn.value = false
  }
}

async function loginAgain() {
  clearLogin()
  user.value = null
  usage.value = {}
  await loginNow()
  if (user.value && aiEnabled.value) {
    usage.value = await getAiUsage().catch(() => ({}))
  }
}

onLoad(loadProfile)

onShow(() => {
  refreshIssue()
  loadProfile()
})

function refreshIssue() {
  const issue = uni.getStorageSync('lastNetworkIssue')
  issueText.value = issue ? `路径：${issue.path}；编号：${issue.requestId}；信息：${issue.message}` : '暂无网络异常记录'
}

function openAiImage() {
  uni.navigateTo({ url: '/pages/ai-image/ai-image' })
}

function money(value) {
  return Number(value || 0).toFixed(4)
}

function userIdentity(currentUser) {
  if (currentUser?.nickname) return currentUser.nickname
  const openid = currentUser?.openid || ''
  if (openid.length <= 8) return '微信用户'
  return `${openid.slice(0, 4)}****${openid.slice(-4)}`
}
</script>

<style>
.page { min-height: 100vh; background: #f6f7f4; padding: 24rpx; }
.panel { background: #fff; border-radius: 16rpx; padding: 28rpx; margin-bottom: 20rpx; border: 1rpx solid #e1e6df; }
.title { display: block; font-size: 38rpx; font-weight: 800; color: #1d2522; }
.label { display: block; font-size: 30rpx; font-weight: 700; color: #1d2522; }
.desc { display: block; color: #65736d; font-size: 26rpx; line-height: 1.6; margin-top: 12rpx; }
.status { display: inline-block; margin-top: 16rpx; padding: 8rpx 18rpx; border-radius: 999rpx; background: #f1f3ef; color: #65736d; font-size: 24rpx; }
.status.ok { background: #e6f2ea; color: #2f6b4f; font-weight: 700; }
button { margin-top: 24rpx; background: #2f6b4f; color: #fff; border-radius: 12rpx; }
.usage-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14rpx; margin-top: 18rpx; }
.usage-grid view { background: #f6f7f4; border-radius: 12rpx; padding: 16rpx; min-width: 0; }
.metric { display: block; font-size: 30rpx; font-weight: 800; color: #2f6b4f; word-break: break-all; }
.usage-row { display: flex; justify-content: space-between; gap: 16rpx; margin-top: 14rpx; color: #394640; font-size: 24rpx; }
</style>
