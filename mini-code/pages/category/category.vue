<template>
  <view class="page">
    <scroll-view scroll-y class="category-list">
      <view v-for="category in categories" :key="category.id" class="category" :class="{ active: activeId === category.id }" @click="selectCategory(category.id)">
        <text>{{ category.name }}</text>
      </view>
    </scroll-view>
    <scroll-view scroll-y class="content">
      <view v-if="items.length === 0" class="empty">
        <text>这个分类暂时还没有内容</text>
      </view>
      <view class="card-grid">
        <view v-for="item in items" :key="item.id" class="item-card" @click="openDetail(item.id)">
          <image :src="item.coverImage" mode="aspectFill" @error="onImageError(item.coverImage, 'category-item')" />
          <view class="card-body">
            <text class="name">{{ item.title }}</text>
            <text class="tag">{{ item.tags?.[1] || item.tags?.[0] || '图文心得' }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCategories, getItems, recordImageIssue } from '../../utils/api'

const categories = ref([])
const items = ref([])
const activeId = ref(0)

onShow(async () => {
  await loadCategories()
  await loadItems()
})

async function loadCategories() {
  const nextCategories = await getCategories()
  const activeExists = nextCategories.some((category) => category.id === activeId.value)
  categories.value = nextCategories
  if (!activeExists) {
    activeId.value = nextCategories[0]?.id || 0
  }
}

async function selectCategory(id) {
  activeId.value = id
  await loadItems()
}

async function loadItems() {
  if (!activeId.value) {
    items.value = []
    return
  }
  items.value = await getItems({ categoryId: activeId.value })
}

function openDetail(id) {
  uni.navigateTo({ url: `/pages/item-detail/item-detail?id=${id}` })
}

function onImageError(url, source) {
  recordImageIssue(url, source)
}
</script>

<style>
.page { display: grid; grid-template-columns: 190rpx 1fr; height: 100vh; background: #fff; }
.category-list { height: 100vh; background: #f3f3f3; border-right: 1rpx solid #e6e6e6; padding-top: 18rpx; box-sizing: border-box; }
.category { padding: 32rpx 20rpx; color: #666; font-size: 31rpx; text-align: center; line-height: 1.3; }
.category.active { color: #2f6b4f; background: #fff; font-weight: 700; border-left: 8rpx solid #2f8b70; }
.content { height: 100vh; padding: 28rpx; box-sizing: border-box; }
.empty { color: #8a948e; font-size: 28rpx; padding-top: 120rpx; text-align: center; }
.card-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24rpx; }
.item-card { background: #fff; border-radius: 14rpx; overflow: hidden; border: 1rpx solid #e1e6df; box-shadow: 0 4rpx 14rpx rgba(26, 40, 32, .06); }
.item-card image { width: 100%; height: 230rpx; background: #e8ece5; }
.card-body { padding: 22rpx 20rpx 24rpx; min-height: 132rpx; box-sizing: border-box; }
.name { display: block; font-size: 30rpx; font-weight: 700; color: #333; line-height: 1.35; }
.tag { display: block; font-size: 25rpx; color: #2f8b70; margin-top: 22rpx; }
</style>
