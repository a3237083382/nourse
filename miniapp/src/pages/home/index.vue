<template>
  <view class="page">
    <view class="hero">
      <text class="title">到家服务</text>
      <text class="subtitle">月嫂、保姆、育婴师、养老与清洁服务</text>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-title">服务分类</text>
      </view>
      <view class="grid">
        <view v-for="item in categories" :key="item.id" class="grid-item" @tap="openCategory(item.id)">
          <view class="dot">{{ item.name.slice(0, 1) }}</view>
          <text>{{ item.name }}</text>
        </view>
      </view>
    </view>

    <view class="section" v-if="tips.length">
      <view class="section-head">
        <text class="section-title">签约动态</text>
      </view>
      <view v-for="tip in tips" :key="tip" class="notice">{{ tip }}</view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-title">团购推荐</text>
      </view>
      <view v-for="item in groupProducts" :key="item.id" class="product">
        <view>
          <text class="product-title">{{ item.title }}</text>
          <text class="product-sub">可单独购买，也可发起拼团</text>
        </view>
        <text class="price">¥{{ item.groupPrice }}</text>
      </view>
    </view>

    <button class="contact" open-type="contact">联系客服</button>
  </view>
</template>

<script>
import { getHome } from '@/services/api'

export default {
  data() {
    return {
      categories: [],
      tips: [],
      groupProducts: [],
    }
  },
  onLoad() {
    this.loadHome()
  },
  methods: {
    async loadHome() {
      const res = await getHome()
      this.categories = res.data.categories || []
      this.tips = res.data.signSuccessTips || []
      this.groupProducts = res.data.groupProducts || []
    },
    openCategory(id) {
      uni.setStorageSync('staffCategoryId', id)
      uni.switchTab({ url: '/pages/staff/index' })
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  background: #f6f7f9;
  padding: 24rpx;
}

.hero {
  padding: 38rpx;
  border-radius: 18rpx;
  background: #fff;
  border-left: 8rpx solid #ef3f5f;
}

.title {
  display: block;
  color: #20242c;
  font-size: 44rpx;
  font-weight: 700;
}

.subtitle {
  display: block;
  margin-top: 12rpx;
  color: #6d7480;
  font-size: 26rpx;
}

.section {
  margin-top: 24rpx;
  padding: 28rpx;
  border-radius: 16rpx;
  background: #ffffff;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22rpx;
}

.section-title {
  color: #20242c;
  font-size: 32rpx;
  font-weight: 700;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 22rpx;
}

.grid-item {
  display: flex;
  min-height: 112rpx;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 10rpx;
  color: #3b414c;
  font-size: 24rpx;
}

.dot {
  display: flex;
  width: 48rpx;
  height: 48rpx;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ef3f5f;
  color: #fff;
  font-size: 24rpx;
}

.notice {
  padding: 18rpx 0;
  border-top: 1px solid #edf0f3;
  color: #525a66;
  font-size: 26rpx;
}

.notice:first-of-type {
  border-top: 0;
}

.product {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-top: 1px solid #edf0f3;
}

.product:first-of-type {
  border-top: 0;
}

.product-title,
.product-sub {
  display: block;
}

.product-title {
  color: #20242c;
  font-size: 28rpx;
  font-weight: 600;
}

.product-sub {
  margin-top: 8rpx;
  color: #8a8f99;
  font-size: 22rpx;
}

.price {
  color: #ef3f5f;
  font-size: 30rpx;
  font-weight: 700;
}

.contact {
  position: fixed;
  right: 24rpx;
  bottom: 140rpx;
  width: 172rpx;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 999rpx;
  background: #20242c;
  color: #fff;
  font-size: 24rpx;
}
</style>
