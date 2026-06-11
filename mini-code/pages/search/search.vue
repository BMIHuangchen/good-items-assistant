<template>
  <view class="page">
    <view class="search">
      <input v-model="keyword" :placeholder="config.searchPlaceholder" confirm-type="search" @confirm="runSearch" />
      <button @click="runSearch">搜索</button>
    </view>
    <view class="hot">
      <text v-for="word in hotWords" :key="word" @click="useWord(word)">{{ word }}</text>
    </view>
    <view v-for="item in items" :key="item.id" class="item" @click="openDetail(item.id)">
      <image :src="item.coverImage" mode="aspectFill" @error="onImageError(item.coverImage, 'search-item')" />
      <view>
        <text class="name">{{ item.title }}</text>
        <text class="summary">{{ item.summary }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getItems, getMiniConfig, recordImageIssue } from '../../utils/api'

const keyword = ref('')
const items = ref([])
const hotWords = ref([])
const config = ref({})

onLoad(async () => {
  config.value = await getMiniConfig()
  hotWords.value = config.value.hotWords || []
})

async function runSearch() {
  items.value = await getItems({ keyword: keyword.value })
}

function useWord(word) {
  keyword.value = word
  runSearch()
}

function openDetail(id) {
  uni.navigateTo({ url: `/pages/item-detail/item-detail?id=${id}` })
}

function onImageError(url, source) {
  recordImageIssue(url, source)
}
</script>

<style>
.page { min-height: 100vh; background: #f6f7f4; padding: 24rpx; }
.search { display: grid; grid-template-columns: 1fr 140rpx; gap: 14rpx; }
input { background: #fff; border-radius: 12rpx; padding: 0 22rpx; height: 78rpx; border: 1rpx solid #dfe6dc; }
button { height: 78rpx; line-height: 78rpx; background: #2f6b4f; color: #fff; border-radius: 12rpx; font-size: 28rpx; }
.hot { display: flex; gap: 12rpx; flex-wrap: wrap; margin: 22rpx 0; }
.hot text { background: #fff; border: 1rpx solid #dfe6dc; border-radius: 999rpx; padding: 10rpx 18rpx; font-size: 24rpx; color: #54615b; }
.item { display: grid; grid-template-columns: 150rpx 1fr; gap: 18rpx; background: #fff; border-radius: 14rpx; padding: 16rpx; margin-bottom: 18rpx; }
.item image { width: 150rpx; height: 150rpx; border-radius: 12rpx; background: #e8ece5; }
.name { display: block; font-size: 30rpx; font-weight: 700; }
.summary { display: block; font-size: 24rpx; color: #66716c; line-height: 1.45; margin-top: 10rpx; }
</style>
