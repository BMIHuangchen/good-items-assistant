<template>
  <view v-if="aiEnabled" class="page">
    <view class="panel">
      <text class="title">AI 图片分析</text>
      <text class="desc">选择 Kimi 或豆包上传生活好物图片，分析通常需要 20-60 秒，完成后可自行确认生成内容、新建分类并公开展示。</text>
    </view>

    <view class="panel">
      <text class="label">选择 AI 大模型</text>
      <view class="model-row">
        <button
          v-for="model in models"
          :key="model.providerCode"
          class="model"
          :class="{ active: providerCode === model.providerCode }"
          @click="providerCode = model.providerCode"
        >
          {{ model.displayName }}
        </button>
      </view>
      <view v-for="usage in modelUsages" :key="usage.providerCode" class="usage-row">
        <text>{{ usage.providerCode }}</text>
        <text>{{ usage.totalTokens }} Token / ¥{{ money(usage.estimatedCost) }}</text>
      </view>
    </view>

    <view class="panel">
      <image v-if="imagePath" class="preview" :src="imagePath" mode="aspectFit" />
      <button class="action-button" :disabled="submitting" @click="chooseImage">选择图片</button>
      <button class="action-button primary" :disabled="!canSubmit" @click="submit">
        <text v-if="submitting" class="loading-dot"></text>
        <text>{{ submitting ? '分析中...' : '提交分析' }}</text>
      </button>
      <text v-if="submitting" class="hint">图片已提交，AI 正在分析，请稍等 20-60 秒。</text>
    </view>

    <view v-if="result" class="panel">
      <text class="label">分析结果</text>
      <text class="desc">状态：{{ result.status }}</text>
      <text class="desc">标题：{{ result.itemTitle || '待后台确认' }}</text>
      <text class="desc">分类：{{ result.matchedCategoryName || result.newCategoryName || '待判断' }}</text>
      <text class="desc">说明：{{ result.reason || result.reviewReason || '已提交后台处理' }}</text>
      <input v-model="confirmForm.itemTitle" class="input" placeholder="标题" />
      <input v-model="confirmForm.summary" class="input" placeholder="摘要" />
      <textarea v-model="confirmForm.experience" class="textarea" placeholder="体验心得" />
      <input v-model="tagText" class="input" placeholder="标签，逗号分隔" />
      <view class="switch-row">
        <text>新建分类</text>
        <switch :checked="confirmForm.createNewCategory" @change="confirmForm.createNewCategory = $event.detail.value" />
      </view>
      <input v-if="confirmForm.createNewCategory" v-model="confirmForm.newCategoryName" class="input" placeholder="新分类名称" />
      <picker v-else :range="categories" range-key="name" @change="selectCategory">
        <view class="picker">使用分类：{{ selectedCategoryName }}</view>
      </picker>
      <view class="switch-row">
        <text>确认后公开展示</text>
        <switch :checked="confirmForm.publish" @change="confirmForm.publish = $event.detail.value" />
      </view>
      <button class="action-button primary" :disabled="confirming" @click="confirmResult">{{ confirming ? '确认中...' : '确认生成内容' }}</button>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { analyzeImage, confirmAiImageTask, ensureLogin, getAiSettings, getAiUsage, getCategories, recordBehavior } from '../../utils/api'

const models = ref([])
const categories = ref([])
const modelUsages = ref([])
const aiEnabled = ref(false)
const providerCode = ref('')
const imagePath = ref('')
const result = ref(null)
const submitting = ref(false)
const confirming = ref(false)
const tagText = ref('')
const confirmForm = ref({})

const canSubmit = computed(() => providerCode.value && imagePath.value && !submitting.value)
const selectedCategoryName = computed(() => categories.value.find((item) => item.id === confirmForm.value.categoryId)?.name || '请选择')

onLoad(async () => {
  await ensureLogin()
  const settings = await getAiSettings()
  if (!settings.aiEnabled) {
    setTimeout(() => uni.navigateBack(), 800)
    return
  }
  aiEnabled.value = true
  models.value = settings.models || []
  providerCode.value = models.value[0]?.providerCode || ''
  categories.value = await getCategories()
  const usage = await getAiUsage().catch(() => ({}))
  modelUsages.value = usage.models || []
})

function chooseImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success(res) {
      imagePath.value = res.tempFilePaths[0]
      result.value = null
      recordBehavior('AI_CHOOSE_IMAGE', 'AI', providerCode.value, '/pages/ai-image/ai-image')
    }
  })
}

async function submit() {
  if (!canSubmit.value) return
  submitting.value = true
  try {
    result.value = await analyzeImage(providerCode.value, imagePath.value)
    fillConfirmForm(result.value)
    recordBehavior('AI_ANALYZE_SUBMIT', 'AI', providerCode.value, '/pages/ai-image/ai-image')
    uni.showToast({ title: '分析已提交', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '分析失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function fillConfirmForm(data) {
  confirmForm.value = {
    categoryId: data.matchedCategoryId || categories.value[0]?.id,
    createNewCategory: data.decision === 'NEW_CATEGORY',
    newCategoryName: data.newCategoryName || '',
    newCategorySlug: '',
    newCategoryDescription: '',
    itemTitle: data.itemTitle || '',
    summary: data.summary || '',
    experience: data.experience || '',
    publish: true
  }
  tagText.value = (data.tags || []).join(',')
}

function selectCategory(event) {
  const index = Number(event.detail.value)
  confirmForm.value.categoryId = categories.value[index]?.id
}

async function confirmResult() {
  if (!result.value?.taskId) return
  confirming.value = true
  try {
    const body = {
      ...confirmForm.value,
      tags: tagText.value.split(',').map((item) => item.trim()).filter(Boolean)
    }
    result.value = await confirmAiImageTask(result.value.taskId, body)
    recordBehavior('AI_CONFIRM_RESULT', 'AI_TASK', result.value.taskId, '/pages/ai-image/ai-image')
    uni.showToast({ title: '已生成内容', icon: 'success' })
    const usage = await getAiUsage().catch(() => ({}))
    modelUsages.value = usage.models || []
  } catch (error) {
    uni.showToast({ title: error.message || '确认失败', icon: 'none' })
  } finally {
    confirming.value = false
  }
}

function money(value) {
  return Number(value || 0).toFixed(4)
}
</script>

<style>
view, text, button, image { box-sizing: border-box; }
.page { width: 100%; min-height: 100vh; background: #f6f7f4; padding: 24rpx; overflow-x: hidden; }
.panel { width: 100%; background: #fff; border-radius: 16rpx; padding: 28rpx; margin-bottom: 20rpx; border: 1rpx solid #e1e6df; overflow: hidden; }
.title { display: block; font-size: 38rpx; font-weight: 800; color: #1d2522; }
.label { display: block; font-size: 30rpx; font-weight: 700; color: #1d2522; }
.desc { display: block; color: #65736d; font-size: 26rpx; line-height: 1.6; margin-top: 12rpx; }
.model-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(240rpx, 1fr)); gap: 16rpx; margin-top: 18rpx; }
.model { width: 100%; min-height: 84rpx; margin: 0; background: #edf0eb; color: #1d2522; border-radius: 12rpx; font-size: 26rpx; line-height: 84rpx; }
.model.active { background: #2f6b4f; color: #fff; }
.preview { display: block; width: 100%; height: 420rpx; border-radius: 14rpx; background: #edf0eb; margin-bottom: 20rpx; }
button { width: 100%; margin-top: 18rpx; border-radius: 12rpx; }
.action-button { height: 92rpx; line-height: 92rpx; font-size: 30rpx; display: flex; align-items: center; justify-content: center; }
.primary { background: #2f6b4f; color: #fff; }
.hint { display: block; margin-top: 18rpx; color: #65736d; font-size: 24rpx; line-height: 1.5; text-align: center; }
.loading-dot { width: 28rpx; height: 28rpx; margin-right: 12rpx; border: 4rpx solid rgba(255,255,255,0.45); border-top-color: #fff; border-radius: 50%; animation: spin 0.9s linear infinite; }
.usage-row { display: flex; justify-content: space-between; gap: 16rpx; margin-top: 16rpx; color: #65736d; font-size: 24rpx; }
.input, .textarea, .picker { width: 100%; min-height: 78rpx; background: #f6f7f4; border: 1rpx solid #e1e6df; border-radius: 12rpx; padding: 18rpx; margin-top: 18rpx; font-size: 26rpx; color: #1d2522; }
.textarea { min-height: 160rpx; line-height: 1.5; }
.switch-row { display: flex; align-items: center; justify-content: space-between; margin-top: 18rpx; font-size: 26rpx; color: #1d2522; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
