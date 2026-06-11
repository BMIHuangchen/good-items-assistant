<template>
  <view class="page">
    <view class="panel">
      <text class="title">{{ config.meTitle }}</text>
      <text class="desc">{{ config.meDescription }}</text>
    </view>
    <view class="panel">
      <text class="label">网络排查信息</text>
      <text class="desc">{{ issueText }}</text>
      <button @click="refreshIssue">刷新排查信息</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getMiniConfig } from '../../utils/api'

const issueText = ref('暂无网络异常记录')
const config = ref({})

onLoad(async () => {
  config.value = await getMiniConfig()
})

onShow(refreshIssue)

function refreshIssue() {
  const issue = uni.getStorageSync('lastNetworkIssue')
  issueText.value = issue ? `路径：${issue.path}；编号：${issue.requestId}；信息：${issue.message}` : '暂无网络异常记录'
}
</script>

<style>
.page { min-height: 100vh; background: #f6f7f4; padding: 24rpx; }
.panel { background: #fff; border-radius: 16rpx; padding: 28rpx; margin-bottom: 20rpx; border: 1rpx solid #e1e6df; }
.title { display: block; font-size: 38rpx; font-weight: 800; color: #1d2522; }
.label { display: block; font-size: 30rpx; font-weight: 700; color: #1d2522; }
.desc { display: block; color: #65736d; font-size: 26rpx; line-height: 1.6; margin-top: 12rpx; }
button { margin-top: 24rpx; background: #2f6b4f; color: #fff; border-radius: 12rpx; }
</style>
