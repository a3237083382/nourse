<template>
  <view class="page">
    <view class="hero">
      <text class="title">到家服务</text>
      <text class="subtitle">月嫂、保姆、育婴、养老与保洁服务</text>
    </view>

    <view class="section">
      <view class="section-title">服务分类</view>
      <view class="grid">
        <view v-for="item in categories" :key="item" class="grid-item">
          <view class="dot"></view>
          <text>{{ item }}</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-title">团购活动</view>
      <view class="product">
        <text class="product-title">深度保洁新人优惠</text>
        <text class="price">¥429.00</text>
      </view>
      <view class="product">
        <text class="product-title">老人陪护体验服务</text>
        <text class="price">¥368.00</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      categories: ['月嫂', '保姆', '育婴师', '居家养老', '保洁师', '钟点工', '成长陪伴师', '家电清洗师'],
    }
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
  padding: 36rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #fff1f3, #fff8e8);
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

.section-title {
  margin-bottom: 22rpx;
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
  width: 42rpx;
  height: 42rpx;
  border-radius: 50%;
  background: #ef3f5f;
}

.product {
  display: flex;
  justify-content: space-between;
  padding: 24rpx 0;
  border-top: 1px solid #edf0f3;
}

.product:first-of-type {
  border-top: 0;
}

.product-title {
  color: #20242c;
  font-size: 28rpx;
}

.price {
  color: #ef3f5f;
  font-size: 28rpx;
  font-weight: 700;
}
</style>
