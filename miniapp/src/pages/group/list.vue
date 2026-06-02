<template>
  <view class="page">
    <view class="hero">
      <text class="title">团购服务</text>
      <text class="subtitle">单独购买或邀请好友拼团，第一版使用模拟支付</text>
    </view>
    <view v-if="products.length === 0" class="empty">暂无团购商品</view>
    <view v-for="item in products" :key="item.id" class="product" @tap="openDetail(item.id)">
      <view class="cover">
        <image v-if="validCover(item.coverUrl)" :src="item.coverUrl" mode="aspectFill" />
        <text v-else>{{ item.title.slice(0, 2) }}</text>
      </view>
      <view class="info">
        <text class="name">{{ item.title }}</text>
        <text class="meta">{{ groupMeta(item) }}</text>
        <view class="price-row">
          <text class="price">¥{{ money(item.groupPrice) }}</text>
          <text class="single">单买 ¥{{ money(item.singlePrice) }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getGroupProductList } from '@/services/api'

export default {
  data() {
    return {
      products: [],
      nowTime: Date.now(),
      countdownTimer: null,
    }
  },
  onLoad() {
    this.countdownTimer = setInterval(() => {
      this.nowTime = Date.now()
    }, 1000)
  },
  onUnload() {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
      this.countdownTimer = null
    }
  },
  onShow() {
    this.loadProducts()
  },
  methods: {
    async loadProducts() {
      const res = await getGroupProductList({ pageNum: 1, pageSize: 50 })
      this.products = res.rows || []
    },
    openDetail(id) {
      uni.navigateTo({ url: `/pages/group/detail?id=${id}` })
    },
    money(value) {
      return Number(value || 0).toFixed(2).replace(/\.00$/, '')
    },
    validCover(value) {
      const url = String(value || '')
      return !!url && !url.includes('/static/logo.png')
    },
    groupMeta(item) {
      const seconds = this.remainingSeconds(item.activeTeamExpireAt)
      if (seconds > 0) {
        return `拼团时间剩余 ${this.formatDuration(seconds)}`
      }
      return `已售 ${item.soldCount || 0}`
    },
    remainingSeconds(value) {
      if (!value) return 0
      const time = new Date(String(value).replace(/-/g, '/')).getTime()
      if (!time) return 0
      return Math.max(0, Math.floor((time - this.nowTime) / 1000))
    },
    formatDuration(seconds) {
      const hours = String(Math.floor(seconds / 3600)).padStart(2, '0')
      const minutes = String(Math.floor((seconds % 3600) / 60)).padStart(2, '0')
      const secs = String(seconds % 60).padStart(2, '0')
      return `${hours}:${minutes}:${secs}`
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  padding: 24rpx;
  background: linear-gradient(180deg, #fff1ed 0, #fff8f4 300rpx, #f7f4ef 720rpx);
}

.hero {
  padding: 34rpx;
  border: 1rpx solid #ffe1d7;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #ef4f5f 0%, #ff9478 62%, #ffe2c8 100%);
  box-shadow: 0 16rpx 34rpx rgba(176, 70, 78, 0.16);
}

.title,
.subtitle,
.name,
.meta {
  display: block;
}

.title {
  color: #fff;
  font-size: 40rpx;
  font-weight: 800;
}

.subtitle {
  margin-top: 10rpx;
  color: rgba(255, 255, 255, 0.72);
  font-size: 25rpx;
}

.product,
.empty {
  display: flex;
  gap: 22rpx;
  margin-top: 22rpx;
  padding: 24rpx;
  border: 1rpx solid #f3e5dc;
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 12rpx 28rpx rgba(80, 45, 40, 0.05);
}

.empty {
  display: block;
  color: #8a8f99;
  text-align: center;
  font-size: 28rpx;
}

.cover {
  display: flex;
  width: 180rpx;
  height: 150rpx;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #ef4f5f, #f6a05f);
  color: #fff;
  font-size: 34rpx;
  font-weight: 800;
  overflow: hidden;
}

.cover image {
  width: 100%;
  height: 100%;
}

.info {
  flex: 1;
  min-width: 0;
}

.name {
  color: #222832;
  font-size: 30rpx;
  font-weight: 700;
}

.meta {
  margin-top: 12rpx;
  color: #8a8f99;
  font-size: 24rpx;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 18rpx;
  margin-top: 16rpx;
}

.price {
  color: #ef4f5f;
  font-size: 34rpx;
  font-weight: 800;
}

.single {
  color: #8a8f99;
  font-size: 23rpx;
  text-decoration: line-through;
}
</style>
