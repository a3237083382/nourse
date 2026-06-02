<template>
  <view class="page">
    <view v-if="items.length === 0" class="empty">暂无内容</view>
    <view v-for="item in items" :key="item.id" class="card">
      <text class="title">{{ item.title }}</text>
      <image v-if="item.imageUrl" class="image" :src="item.imageUrl" mode="aspectFill" />
      <text class="content">{{ item.content || '' }}</text>
    </view>
  </view>
</template>

<script>
import { getContent } from '@/services/api'

const titleMap = {
  faq: '常见问题',
  agreement: '用户协议',
  privacy: '隐私政策',
  about: '关于我们',
}

export default {
  data() {
    return {
      type: 'faq',
      items: [],
    }
  },
  onLoad(options) {
    this.type = options.type || 'faq'
    uni.setNavigationBarTitle({ title: titleMap[this.type] || '内容' })
    this.loadContent()
  },
  methods: {
    async loadContent() {
      const res = await getContent(this.type)
      this.items = res.data || []
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  padding: 24rpx;
  background: #f7f4ef;
}

.card,
.empty {
  margin-bottom: 20rpx;
  padding: 30rpx;
  border: 1px solid rgba(31, 37, 43, 0.05);
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 12rpx 30rpx rgba(32, 38, 44, 0.05);
}

.title {
  display: block;
  color: #222832;
  font-size: 32rpx;
  font-weight: 700;
}

.image {
  width: 100%;
  height: 260rpx;
  margin-top: 18rpx;
  border-radius: 20rpx;
}

.content {
  display: block;
  margin-top: 18rpx;
  color: #4f5662;
  font-size: 28rpx;
  line-height: 1.7;
  white-space: pre-wrap;
}

.empty {
  color: #8a8f99;
  text-align: center;
  font-size: 28rpx;
}
</style>
