<template>
  <view class="page">
    <view v-if="!item" class="empty">
      <text>网络连接失败，请返回后稍后重试</text>
    </view>
    <image v-else class="cover" :src="item.coverImage" mode="aspectFill" @error="onImageError(item.coverImage, 'item-detail-cover')" />
    <view v-if="item" class="body">
      <text class="category">{{ item.categoryName }}</text>
      <text class="title">{{ item.title }}</text>
      <text class="summary">{{ item.summary }}</text>
      <view class="tags"><text v-for="tag in item.tags" :key="tag">{{ tag }}</text></view>
      <view class="section">
        <text class="section-title">体验心得</text>
        <text class="experience">{{ item.experience }}</text>
      </view>
      <button @click="favorite">{{ favorited ? '已收藏' : '收藏这条记录' }}</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getFavorites, getItem, recordImageIssue, toggleFavorite } from '../../utils/api'

const item = ref(null)
const favorited = ref(false)

onLoad(async (query) => {
  item.value = await getItem(query.id)
  favorited.value = item.value ? getFavorites().some((saved) => saved.id === item.value.id) : false
})

function favorite() {
  if (!item.value) return
  favorited.value = toggleFavorite(item.value)
  uni.showToast({ title: favorited.value ? '已收藏' : '已取消', icon: 'none' })
}

function onImageError(url, source) {
  recordImageIssue(url, source)
}
</script>

<style>
.page { min-height: 100vh; background: #f6f7f4; }
.empty { min-height: 100vh; display: flex; align-items: center; justify-content: center; color: #65736d; font-size: 28rpx; padding: 40rpx; box-sizing: border-box; text-align: center; }
.cover { width: 100%; height: 560rpx; background: #e8ece5; }
.body { background: #fff; margin-top: -32rpx; border-radius: 28rpx 28rpx 0 0; padding: 34rpx 28rpx 60rpx; position: relative; }
.category { color: #2f6b4f; font-size: 24rpx; }
.title { display: block; font-size: 42rpx; font-weight: 800; margin-top: 10rpx; color: #1d2522; }
.summary { display: block; font-size: 28rpx; color: #65736d; line-height: 1.55; margin-top: 14rpx; }
.tags { display: flex; gap: 10rpx; flex-wrap: wrap; margin: 22rpx 0; }
.tags text { background: #edf5e8; color: #2f6b4f; border-radius: 999rpx; padding: 6rpx 14rpx; font-size: 22rpx; }
.section { border-top: 1rpx solid #e6ebe3; padding-top: 26rpx; }
.section-title { display: block; font-size: 30rpx; font-weight: 700; margin-bottom: 12rpx; }
.experience { display: block; color: #394640; font-size: 28rpx; line-height: 1.7; }
button { margin-top: 34rpx; background: #2f6b4f; color: #fff; border-radius: 12rpx; }
</style>
