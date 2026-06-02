<template>
  <view class="page" v-if="product">
    <view class="hero">
      <image v-if="validCover(product.coverUrl)" class="hero-image" :src="product.coverUrl" mode="aspectFill" />
      <view v-else class="hero-fallback">
        <text class="hero-kicker">到家服务</text>
        <text class="hero-title">{{ product.title }}</text>
      </view>
    </view>

    <view class="deal-bar">
      <view class="deal-price">
        <text class="group-price">¥{{ money(product.groupPrice) }}</text>
        <text class="original">原价：¥{{ money(product.originalPrice) }}</text>
      </view>
      <view class="countdown">
        <text class="count-title">拼团时间剩余</text>
        <view class="time-row">
          <text class="time-box">{{ countdownParts.hours }}</text>
          <text class="colon">:</text>
          <text class="time-box">{{ countdownParts.minutes }}</text>
          <text class="colon">:</text>
          <text class="time-box">{{ countdownParts.seconds }}</text>
        </view>
      </view>
    </view>

    <view class="title-card">
      <text class="title">{{ product.title }}</text>
      <text class="sold">已售：{{ product.soldCount || 0 }}</text>
    </view>

    <view class="notice-card">
      <text class="muted-title">消费须知：</text>
      <text class="notice-line">{{ product.notice || '请提前预约服务时间。' }}</text>
      <text class="notice-line">有效期：购买{{ product.validDays || 30 }}天内有效</text>
      <text class="notice-line">服务保障：{{ product.guarantee || '平台客服协助处理服务过程问题。' }}</text>
      <text class="notice-line">成团人数：{{ product.groupSize || 2 }}人成团</text>
    </view>

    <view class="service-banner">
      <view class="service-copy">
        <text class="service-title">到家服务</text>
        <text class="service-desc">专业人员上门，按约服务</text>
        <text class="service-badge">✓ 平台服务保障</text>
      </view>
      <view class="service-visual">
        <view class="sun"></view>
        <view class="person person-a"></view>
        <view class="person person-b"></view>
      </view>
    </view>

    <view class="panel">
      <view class="decor-title">
        <text class="decor">▲</text>
        <text class="section-title">产品介绍/说明</text>
        <text class="decor">▲</text>
      </view>
      <text class="paragraph">{{ product.description || product.guarantee || '暂无介绍' }}</text>
    </view>

    <view class="panel">
      <view class="decor-title">
        <text class="decor">▲</text>
        <text class="section-title">服务流程</text>
        <text class="decor">▲</text>
      </view>
      <view class="flow">
        <view v-for="item in flowItems" :key="item.index" class="flow-item">
          <view class="flow-icon">
            <text>{{ item.index }}</text>
          </view>
          <text class="flow-text">{{ item.label }}</text>
        </view>
      </view>
    </view>

    <view class="panel" v-if="product.activeTeams && product.activeTeams.length">
      <text class="plain-title">正在拼团</text>
      <view v-for="team in product.activeTeams" :key="team.id" class="team">
        <view>
          <text class="team-name">{{ team.leaderNickname || '用户' }}的团</text>
          <text class="meta">还差 {{ team.remainingCount }} 人成团</text>
        </view>
        <button @tap="join(team.id)">去拼团</button>
      </view>
    </view>

    <view class="review-block">
      <text class="review-title">用户评价</text>
      <text class="empty-review">暂无评价~</text>
    </view>

    <view class="actions">
      <button class="service-btn" open-type="contact">客服</button>
      <button class="group" @tap="startGroup">拼团购 ¥{{ money(product.groupPrice) }}</button>
      <button class="single" @tap="singleBuy">单独购买 ¥{{ money(product.singlePrice) }}</button>
    </view>
  </view>
</template>

<script>
import { createSingleGroupOrder, getGroupProductDetail, joinGroupOrder, startGroupOrder } from '@/services/api'
import { ensureLogin } from '@/services/request'

export default {
  data() {
    return {
      id: undefined,
      product: null,
      nowTime: Date.now(),
      countdownTimer: null,
      flowItems: [
        { index: 1, label: '下单预约' },
        { index: 2, label: '上门服务' },
        { index: 3, label: '订单结束' },
      ],
    }
  },
  computed: {
    countdownParts() {
      const seconds = this.remainingSeconds(this.activeExpireAt())
      return {
        hours: String(Math.floor(seconds / 3600)).padStart(2, '0'),
        minutes: String(Math.floor((seconds % 3600) / 60)).padStart(2, '0'),
        seconds: String(seconds % 60).padStart(2, '0'),
      }
    },
  },
  onLoad(options) {
    this.id = options.id
    this.loadDetail()
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
  methods: {
    async loadDetail() {
      const res = await getGroupProductDetail(this.id)
      this.product = res.data
    },
    async singleBuy() {
      await ensureLogin()
      await createSingleGroupOrder({ productId: this.id, quantity: 1 })
      uni.showToast({ title: '购买成功', icon: 'success' })
      setTimeout(() => uni.navigateTo({ url: '/pages/group/orders' }), 600)
    },
    async startGroup() {
      await ensureLogin()
      await startGroupOrder({ productId: this.id, quantity: 1 })
      uni.showToast({ title: '已发起拼团', icon: 'success' })
      setTimeout(() => uni.navigateTo({ url: '/pages/group/orders' }), 600)
    },
    async join(teamId) {
      await ensureLogin()
      await joinGroupOrder({ teamId, quantity: 1 })
      uni.showToast({ title: '参团成功', icon: 'success' })
      setTimeout(() => uni.navigateTo({ url: '/pages/group/orders' }), 600)
    },
    money(value) {
      return Number(value || 0).toFixed(2).replace(/\.00$/, '')
    },
    validCover(value) {
      const url = String(value || '')
      return !!url && !url.includes('/static/logo.png')
    },
    activeExpireAt() {
      const teams = (this.product && this.product.activeTeams) || []
      return teams.length ? teams[0].expireAt : ''
    },
    remainingSeconds(value) {
      if (!value) return 0
      const time = new Date(String(value).replace(/-/g, '/')).getTime()
      if (!time) return 0
      return Math.max(0, Math.floor((time - this.nowTime) / 1000))
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  padding-bottom: 154rpx;
  background: #f6f6f6;
}

.hero {
  position: relative;
  height: 300rpx;
  overflow: hidden;
  background: #fff;
}

.hero-image {
  width: 100%;
  height: 100%;
}

.hero-fallback {
  display: flex;
  height: 100%;
  justify-content: center;
  flex-direction: column;
  padding-left: 70rpx;
  background: linear-gradient(135deg, #fff7e8 0%, #fff 42%, #79aaf1 43%, #5d98e8 100%);
}

.hero-kicker {
  color: #ef4f5f;
  font-size: 30rpx;
  font-weight: 800;
}

.hero-title {
  display: block;
  width: 370rpx;
  margin-top: 12rpx;
  color: #222832;
  font-size: 46rpx;
  font-weight: 900;
  line-height: 1.16;
}

.deal-bar {
  display: flex;
  height: 150rpx;
  align-items: center;
  justify-content: space-between;
  padding: 0 34rpx 0 44rpx;
  background: linear-gradient(100deg, #ff2b18 0%, #ff6518 58%, #ff4f5f 100%);
  color: #fff;
}

.deal-price {
  display: flex;
  align-items: baseline;
  gap: 14rpx;
}

.group-price {
  color: #fff;
  font-size: 52rpx;
  font-weight: 900;
  line-height: 1;
}

.original {
  color: rgba(255, 255, 255, 0.82);
  font-size: 24rpx;
  text-decoration: line-through;
}

.countdown {
  position: relative;
  min-width: 220rpx;
  padding-left: 34rpx;
}

.countdown::before {
  position: absolute;
  left: 0;
  top: -22rpx;
  width: 2rpx;
  height: 108rpx;
  background: rgba(255, 255, 255, 0.68);
  content: '';
  transform: rotate(10deg);
}

.count-title {
  display: block;
  margin-bottom: 12rpx;
  font-size: 25rpx;
  font-weight: 800;
}

.time-row {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.time-box {
  min-width: 44rpx;
  height: 42rpx;
  padding: 0 8rpx;
  border-radius: 8rpx;
  background: #fff0ef;
  color: #ef4f5f;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 42rpx;
  text-align: center;
}

.colon {
  color: #fff;
  font-size: 24rpx;
  font-weight: 900;
}

.panel {
  margin: 20rpx 24rpx;
  padding: 30rpx 28rpx;
  border-radius: 8rpx;
  background: #fff;
  box-shadow: 0 10rpx 26rpx rgba(80, 45, 40, 0.04);
}

.title-card {
  display: flex;
  min-height: 94rpx;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
  margin: -32rpx 24rpx 20rpx;
  padding: 0 28rpx;
  border-radius: 8rpx;
  background: #fff;
  box-shadow: 0 10rpx 26rpx rgba(80, 45, 40, 0.06);
}

.team,
.actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.title {
  flex: 1;
  color: #222832;
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1.3;
}

.sold,
.meta,
.paragraph {
  color: #68717a;
  font-size: 28rpx;
  line-height: 1.75;
}

.sold {
  flex: 0 0 auto;
  color: #9aa0a6;
}

.notice-card {
  margin: 20rpx 24rpx;
  padding: 32rpx 28rpx;
  border-radius: 8rpx;
  background: #fff;
}

.muted-title,
.notice-line {
  display: block;
  color: #555f6b;
  font-size: 30rpx;
  line-height: 1.85;
}

.muted-title {
  margin-bottom: 14rpx;
  color: #9aa0a6;
  font-weight: 700;
}

.service-banner {
  position: relative;
  display: flex;
  height: 250rpx;
  align-items: center;
  margin: 24rpx 24rpx 20rpx;
  overflow: hidden;
  background: linear-gradient(105deg, #f9792a 0%, #fca06c 45%, #fff0df 100%);
}

.service-copy {
  position: relative;
  z-index: 2;
  width: 390rpx;
  padding-left: 58rpx;
  color: #fff;
}

.service-title {
  display: block;
  font-size: 46rpx;
  font-weight: 900;
  line-height: 1;
}

.service-desc,
.service-badge {
  display: block;
  margin-top: 14rpx;
  font-size: 24rpx;
  line-height: 1.45;
}

.service-badge {
  font-size: 22rpx;
  font-weight: 700;
}

.service-visual {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 300rpx;
  height: 250rpx;
}

.sun {
  position: absolute;
  right: 38rpx;
  top: 34rpx;
  width: 98rpx;
  height: 98rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.28);
}

.person {
  position: absolute;
  bottom: 28rpx;
  border-radius: 80rpx 80rpx 18rpx 18rpx;
}

.person-a {
  right: 118rpx;
  width: 86rpx;
  height: 130rpx;
  background: #fff6ea;
}

.person-b {
  right: 34rpx;
  width: 108rpx;
  height: 168rpx;
  background: #5f9ee9;
}

.section-title {
  display: block;
  color: #8d3f2a;
  font-size: 38rpx;
  font-weight: 900;
}

.decor-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 30rpx;
  margin-bottom: 24rpx;
}

.decor {
  color: #ff8f78;
  font-size: 30rpx;
  transform: rotate(35deg);
}

.plain-title,
.review-title {
  display: block;
  color: #222832;
  font-size: 32rpx;
  font-weight: 900;
}

.flow {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  padding: 6rpx 0 4rpx;
}

.flow-item {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 18rpx;
  color: #727982;
  font-size: 28rpx;
}

.flow-icon {
  position: relative;
  display: flex;
  width: 94rpx;
  height: 94rpx;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ffe1dc;
  color: #d9462f;
  font-size: 30rpx;
  font-weight: 900;
}

.flow-icon::after {
  position: absolute;
  right: 8rpx;
  bottom: 8rpx;
  width: 38rpx;
  height: 38rpx;
  border-radius: 50%;
  background: linear-gradient(180deg, #ff9d18, #ef4f5f);
  content: '';
}

.flow-text {
  display: block;
}

.team {
  padding: 18rpx 0;
  border-top: 1px solid #edf0f3;
}

.team-name {
  display: block;
  color: #222832;
  font-size: 28rpx;
  font-weight: 700;
}

.team button {
  width: 150rpx;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 999rpx;
  background: #ef4f5f;
  color: #fff;
  font-size: 24rpx;
}

.review-block {
  min-height: 210rpx;
  padding: 10rpx 24rpx 32rpx;
}

.empty-review {
  display: block;
  margin-top: 62rpx;
  color: #9aa0a6;
  font-size: 28rpx;
  text-align: center;
}

.actions {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 18rpx 20rpx calc(24rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -10rpx 28rpx rgba(80, 45, 40, 0.08);
}

.actions button {
  height: 80rpx;
  margin: 0;
  padding: 0;
  line-height: 80rpx;
  border-radius: 0;
  font-size: 28rpx;
}

.actions button::after {
  border: 0;
}

.service-btn {
  width: 120rpx;
  flex: 0 0 auto;
  background: #fff;
  color: #3b414c;
  font-size: 25rpx;
}

.actions .group {
  flex: 1;
  border-radius: 16rpx 0 0 16rpx;
  color: #fff;
  background: #ef4f5f;
}

.actions .single {
  flex: 1;
  border-left: 1rpx solid rgba(255, 255, 255, 0.38);
  border-radius: 0 16rpx 16rpx 0;
  background: #ef4f5f;
  color: #fff;
}
</style>
