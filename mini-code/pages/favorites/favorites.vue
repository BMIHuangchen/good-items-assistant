<template>
  <view class="page">
    <view v-if="items.length === 0" class="empty">还没有收藏内容</view>
    <view v-for="item in items" :key="item.id" class="item" @click="openDetail(item.id)">
      <image :src="item.coverImage" mode="aspectFill" @error="onImageError(item.coverImage, 'favorite-item')" />
      <view>
        <text class="name">{{ item.title }}</text>
        <text class="summary">{{ item.summary }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getFavorites, recordImageIssue } from '../../utils/api'

const items = ref([])

onShow(() => {
  items.value = getFavorites()
})

function openDetail(id) {
  uni.navigateTo({ url: `/pages/item-detail/item-detail?id=${id}` })
}

function onImageError(url, source) {
  recordImageIssue(url, source)
}
</script>

<style>
.page { min-height: 100vh; background: #f6f7f4; padding: 24rpx; }
.empty { text-align: center; color: #7a847e; padding-top: 180rpx; }
.item { display: grid; grid-template-columns: 150rpx 1fr; gap: 18rpx; background: #fff; border-radius: 14rpx; padding: 16rpx; margin-bottom: 18rpx; }
.item image { width: 150rpx; height: 150rpx; border-radius: 12rpx; background: #e8ece5; }
.name { display: block; font-size: 30rpx; font-weight: 700; }
.summary { display: block; font-size: 24rpx; color: #66716c; line-height: 1.45; margin-top: 10rpx; }
</style>
