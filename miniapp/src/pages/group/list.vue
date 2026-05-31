<template>
  <view class="page">
    <view class="hero">
      <text class="title">团购服务</text>
      <text class="subtitle">单独购买或邀请好友拼团，第一版使用模拟支付</text>
    </view>
    <view v-if="products.length === 0" class="empty">暂无团购商品</view>
    <view v-for="item in products" :key="item.id" class="product" @tap="openDetail(item.id)">
      <view class="cover">
        <text>{{ item.title.slice(0, 2) }}</text>
      </view>
      <view class="info">
        <text class="name">{{ item.title }}</text>
        <text class="meta">已售 {{ item.soldCount || 0 }} · {{ item.groupSize || 2 }} 人团</text>
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
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  padding: 24rpx;
  background: #f4f5f2;
}

.hero {
  padding: 34rpx;
  border-radius: 28rpx;
  background: #20252b;
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
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 12rpx 30rpx rgba(32, 38, 44, 0.05);
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
  border-radius: 20rpx;
  background: linear-gradient(135deg, #e84d64, #f3b35b);
  color: #fff;
  font-size: 34rpx;
  font-weight: 800;
}

.info {
  flex: 1;
  min-width: 0;
}

.name {
  color: #20242c;
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
  color: #e84d64;
  font-size: 34rpx;
  font-weight: 800;
}

.single {
  color: #8a8f99;
  font-size: 23rpx;
  text-decoration: line-through;
}
</style>
