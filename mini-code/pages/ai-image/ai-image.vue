<template>
  <view class="page">
    <view class="panel">
      <text class="title">AI 图片分析</text>
      <text class="desc">选择模型并上传一张生活好物图片，分析通常需要 20-60 秒，结果会进入后台确认或按后台开关自动入库。</text>
    </view>

    <view class="panel">
      <text class="label">选择模型</text>
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
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { analyzeImage, getAiSettings } from '../../utils/api'

const models = ref([])
const providerCode = ref('')
const imagePath = ref('')
const result = ref(null)
const submitting = ref(false)

const canSubmit = computed(() => providerCode.value && imagePath.value && !submitting.value)

onLoad(async () => {
  const settings = await getAiSettings()
  if (!settings.aiEnabled) {
    uni.showToast({ title: 'AI 功能暂未开启', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 800)
    return
  }
  models.value = settings.models || []
  providerCode.value = models.value[0]?.providerCode || ''
})

function chooseImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success(res) {
      imagePath.value = res.tempFilePaths[0]
      result.value = null
    }
  })
}

async function submit() {
  if (!canSubmit.value) return
  submitting.value = true
  try {
    result.value = await analyzeImage(providerCode.value, imagePath.value)
    uni.showToast({ title: '分析已提交', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '分析失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
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
@keyframes spin { to { transform: rotate(360deg); } }
</style>
