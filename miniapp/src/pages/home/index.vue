<template>
  <view class="page">
    <view class="hero">
      <text class="title">到家服务</text>
      <text class="subtitle">月嫂、保姆、育婴师、养老与清洁服务</text>
    </view>

    <swiper v-if="banners.length" class="banner" circular autoplay indicator-dots>
      <swiper-item v-for="item in banners" :key="item.id">
        <view class="banner-item">
          <image v-if="item.imageUrl" class="banner-image" :src="item.imageUrl" mode="aspectFill" />
          <view class="banner-text">
            <text class="banner-title">{{ item.title }}</text>
            <text v-if="item.content" class="banner-desc">{{ item.content }}</text>
          </view>
        </view>
      </swiper-item>
    </swiper>

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
        <text class="more" @tap="openGroupList">更多</text>
      </view>
      <view v-for="item in groupProducts" :key="item.id" class="product" @tap="openGroupDetail(item.id)">
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
      banners: [],
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
      this.banners = res.data.banners || []
      this.categories = res.data.categories || []
      this.tips = res.data.signSuccessTips || []
      this.groupProducts = res.data.groupProducts || []
    },
    openCategory(id) {
      uni.setStorageSync('staffCategoryId', id)
      uni.switchTab({ url: '/pages/staff/index' })
    },
    openGroupList() {
      uni.navigateTo({ url: '/pages/group/list' })
    },
    openGroupDetail(id) {
      uni.navigateTo({ url: `/pages/group/detail?id=${id}` })
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  background: #f4f5f2;
  padding: 24rpx 24rpx 36rpx;
}

.hero {
  padding: 38rpx;
  border-radius: 28rpx;
  background: #20252b;
  box-shadow: 0 18rpx 42rpx rgba(32, 37, 43, 0.16);
}

.banner {
  height: 240rpx;
  margin-top: 24rpx;
  border-radius: 26rpx;
  overflow: hidden;
  background: #20242c;
}

.banner-item {
  position: relative;
  height: 240rpx;
}

.banner-image {
  width: 100%;
  height: 240rpx;
}

.banner-text {
  position: absolute;
  left: 24rpx;
  right: 24rpx;
  bottom: 24rpx;
  color: #fff;
}

.banner-title,
.banner-desc {
  display: block;
}

.banner-title {
  font-size: 32rpx;
  font-weight: 700;
}

.banner-desc {
  margin-top: 8rpx;
  font-size: 24rpx;
}

.title {
  display: block;
  color: #fff;
  font-size: 44rpx;
  font-weight: 700;
}

.subtitle {
  display: block;
  margin-top: 12rpx;
  color: rgba(255, 255, 255, 0.72);
  font-size: 26rpx;
}

.section {
  margin-top: 24rpx;
  padding: 28rpx;
  border: 1px solid rgba(31, 37, 43, 0.05);
  border-radius: 26rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 30rpx rgba(32, 38, 44, 0.05);
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

.more {
  color: #e84d64;
  font-size: 25rpx;
  font-weight: 600;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}

.grid-item {
  display: flex;
  min-height: 118rpx;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 10rpx;
  border-radius: 20rpx;
  background: #f7f8f5;
  color: #3b414c;
  font-size: 24rpx;
}

.dot {
  display: flex;
  width: 52rpx;
  height: 52rpx;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #e84d64;
  color: #fff;
  font-size: 24rpx;
}

.notice {
  margin-top: 14rpx;
  padding: 18rpx 20rpx;
  border-top: 1px solid #edf0f3;
  border-radius: 18rpx;
  background: #f7f8f5;
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
  margin-top: 14rpx;
  padding: 22rpx;
  border-radius: 20rpx;
  background: #f7f8f5;
}

.product:first-of-type {
  margin-top: 0;
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
  color: #e84d64;
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
  border-radius: 18rpx;
  background: #20242c;
  color: #fff;
  font-size: 24rpx;
  box-shadow: 0 12rpx 26rpx rgba(32, 37, 43, 0.18);
}
</style>
