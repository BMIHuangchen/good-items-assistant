<template>
  <view class="page">
    <view class="hero">
      <view>
        <text class="eyebrow">{{ config.heroEyebrow }}</text>
        <text class="title">{{ config.heroTitle }}</text>
        <text class="sub">{{ config.heroSubtitle }}</text>
      </view>
    </view>

    <swiper class="banner" indicator-dots autoplay circular>
      <swiper-item v-for="banner in banners" :key="banner.id">
        <image :src="banner.imageUrl" mode="aspectFill" @error="onImageError(banner.imageUrl, 'home-banner')" />
        <text>{{ banner.title }}</text>
      </swiper-item>
    </swiper>

    <view class="section-head">
      <text>{{ config.featuredTitle }}</text>
      <text v-if="fallback" class="hint">已启用本地兜底</text>
    </view>

    <view class="grid">
      <view v-for="item in items" :key="item.id" class="card" @click="openDetail(item.id)">
        <image :src="item.coverImage" mode="aspectFill" @error="onImageError(item.coverImage, 'home-item')" />
        <view class="card-body">
          <text class="name">{{ item.title }}</text>
          <text class="summary">{{ item.summary }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getHomeData, recordImageIssue } from '../../utils/api'

const banners = ref([])
const items = ref([])
const fallback = ref(false)
const config = ref({})

onLoad(async () => {
  const data = await getHomeData()
  config.value = data.config
  banners.value = data.banners
  items.value = data.items
  fallback.value = !!data.fallback
})

function openDetail(id) {
  uni.navigateTo({ url: `/pages/item-detail/item-detail?id=${id}` })
}

function onImageError(url, source) {
  recordImageIssue(url, source)
}
</script>

<style>
.page { min-height: 100vh; background: #f6f7f4; padding: 48rpx 28rpx 32rpx; }
.hero { background: #24342e; color: #fff; border-radius: 16rpx; padding: 36rpx 30rpx; }
.eyebrow { display: block; font-size: 22rpx; color: #dceecf; }
.title { display: block; font-size: 44rpx; font-weight: 700; margin-top: 10rpx; }
.sub { display: block; font-size: 26rpx; color: rgba(255,255,255,.72); margin-top: 10rpx; }
.banner { height: 300rpx; margin: 28rpx 0; border-radius: 16rpx; overflow: hidden; }
.banner image { width: 100%; height: 100%; }
.banner text { position: absolute; left: 24rpx; bottom: 24rpx; color: #fff; font-size: 32rpx; font-weight: 600; }
.section-head { display: flex; justify-content: space-between; align-items: center; font-size: 34rpx; font-weight: 700; margin: 20rpx 0; }
.hint { font-size: 22rpx; color: #8a6d1d; font-weight: 400; }
.grid { display: grid; grid-template-columns: 1fr 1fr; gap: 18rpx; }
.card { background: #fff; border-radius: 14rpx; overflow: hidden; border: 1rpx solid #e1e6df; }
.card image { width: 100%; height: 220rpx; background: #e8ece5; }
.card-body { padding: 18rpx; }
.name { display: block; font-size: 28rpx; font-weight: 700; color: #1d2522; }
.summary { display: block; font-size: 23rpx; color: #67736e; line-height: 1.45; margin-top: 8rpx; }
</style>
