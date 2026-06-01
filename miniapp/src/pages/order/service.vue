<template>
  <view class="page">
    <view class="tabs">
      <view v-for="item in tabs" :key="item.value" class="tab" :class="{ active: status === item.value }" @tap="switchStatus(item.value)">
        {{ item.label }}
      </view>
    </view>

    <view v-if="orders.length === 0" class="empty">暂无服务订单</view>
    <view v-for="item in orders" :key="item.id" class="card" @tap="openDetail(item)">
      <view class="row">
        <text class="title">{{ item.categoryName || '家政服务' }}</text>
        <text class="badge">{{ statusText(item.status) }}</text>
      </view>
      <text class="meta">订单号：{{ item.orderNo }}</text>
      <text class="meta">服务人员：{{ item.staffName || '-' }}</text>
      <text class="meta">服务日期：{{ item.startDate || '-' }} 至 {{ item.endDate || '-' }}</text>
      <text class="amount">¥{{ money(item.amount) }}</text>
    </view>

    <view v-if="detail" class="sheet">
      <view class="sheet-card">
        <view class="row">
          <text class="title">{{ detail.categoryName || '服务订单' }}</text>
          <text class="close" @tap="detail = null">关闭</text>
        </view>
        <text class="meta">订单号：{{ detail.orderNo }}</text>
        <text class="meta">服务人员：{{ detail.staffName || '-' }}</text>
        <text class="meta">关联需求：{{ detail.demandTitle || '-' }}</text>
        <text class="meta">服务日期：{{ detail.startDate || '-' }} 至 {{ detail.endDate || '-' }}</text>
        <text class="meta">后台备注：{{ detail.adminNote || '-' }}</text>
        <view class="flow">
          <view v-for="step in serviceFlow" :key="step.value" class="flow-step" :class="{ done: isFlowDone(step.value) }">
            <view class="flow-dot">{{ step.index }}</view>
            <text>{{ step.label }}</text>
          </view>
        </view>
        <view v-if="detail.reviewId" class="review">
          <text class="review-title">用户评价</text>
          <text class="stars">{{ stars(detail.reviewRating) }}</text>
          <text class="review-content">{{ detail.reviewContent || '用户未填写文字评价' }}</text>
        </view>
        <view v-else-if="detail.status === 'COMPLETED'" class="review">
          <text class="review-title">评价本次服务</text>
          <view class="star-row">
            <text
              v-for="score in [1, 2, 3, 4, 5]"
              :key="score"
              class="star"
              :class="{ active: reviewForm.rating >= score }"
              @tap="reviewForm.rating = score"
            >★</text>
          </view>
          <textarea
            v-model="reviewForm.content"
            class="review-input"
            maxlength="512"
            placeholder="写下这次服务体验，方便平台持续改进"
          />
          <button class="review-button" @tap="submitReview">提交评价</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getServiceOrderDetail, getServiceOrderList, reviewServiceOrder } from '@/services/api'
import { ensureLogin } from '@/services/request'

export default {
  data() {
    return {
      status: '',
      orders: [],
      detail: null,
      reviewForm: {
        rating: 5,
        content: '',
      },
      serviceFlow: [
        { index: 1, label: '面试', value: 'INTERVIEW' },
        { index: 2, label: '签约', value: 'SIGNED' },
        { index: 3, label: '上户', value: 'SERVING' },
        { index: 4, label: '服务完成', value: 'COMPLETED' },
      ],
      tabs: [
        { label: '全部', value: '' },
        { label: '待开始', value: 'WAIT_START' },
        { label: '服务中', value: 'SERVING' },
        { label: '已完成', value: 'COMPLETED' },
        { label: '已取消', value: 'CANCELED' },
      ],
    }
  },
  async onShow() {
    await ensureLogin()
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      const res = await getServiceOrderList({ status: this.status || undefined, pageNum: 1, pageSize: 50 })
      this.orders = res.rows || []
    },
    switchStatus(status) {
      this.status = status
      this.detail = null
      this.loadOrders()
    },
    async openDetail(item) {
      const res = await getServiceOrderDetail(item.id)
      this.detail = res.data
      this.reviewForm = { rating: 5, content: '' }
    },
    async submitReview() {
      if (!this.detail) return
      await reviewServiceOrder(this.detail.id, this.reviewForm)
      uni.showToast({ title: '评价成功', icon: 'success' })
      const res = await getServiceOrderDetail(this.detail.id)
      this.detail = res.data
      this.loadOrders()
    },
    statusText(value) {
      return { WAIT_START: '待开始', SERVING: '服务中', COMPLETED: '已完成', CANCELED: '已取消' }[value] || value || '-'
    },
    isFlowDone(value) {
      if (!this.detail) return false
      const rank = { WAIT_START: 2, SERVING: 3, COMPLETED: 4, CANCELED: 0 }[this.detail.status] || 0
      const stepRank = { INTERVIEW: 1, SIGNED: 2, SERVING: 3, COMPLETED: 4 }[value] || 0
      return rank >= stepRank
    },
    stars(value) {
      const score = Number(value || 0)
      return '★★★★★'.slice(0, score) + '☆☆☆☆☆'.slice(0, Math.max(0, 5 - score))
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

.tabs {
  display: flex;
  gap: 12rpx;
  margin-bottom: 22rpx;
  overflow-x: auto;
  white-space: nowrap;
}

.tab {
  flex: 0 0 auto;
  padding: 18rpx 24rpx;
  border-radius: 999rpx;
  background: #fff;
  color: #68717a;
  font-size: 26rpx;
}

.tab.active {
  background: #20252b;
  color: #fff;
  font-weight: 700;
}

.card,
.empty,
.sheet-card {
  margin-bottom: 20rpx;
  padding: 28rpx;
  border: 1px solid rgba(31, 37, 43, 0.05);
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 12rpx 30rpx rgba(32, 38, 44, 0.05);
}

.empty {
  color: #8a8f99;
  text-align: center;
  font-size: 28rpx;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.title {
  color: #20242c;
  font-size: 32rpx;
  font-weight: 700;
}

.badge {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #fff0f2;
  color: #d93f58;
  font-size: 23rpx;
}

.meta,
.amount {
  display: block;
  margin-top: 14rpx;
  color: #68717a;
  font-size: 26rpx;
  line-height: 1.55;
}

.amount {
  color: #e84d64;
  font-size: 32rpx;
  font-weight: 800;
}

.sheet {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: flex-end;
  padding: 24rpx;
  background: rgba(32, 37, 43, 0.28);
}

.sheet-card {
  width: 100%;
  margin-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.close {
  color: #e84d64;
  font-size: 26rpx;
}

.flow {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
  margin-top: 28rpx;
  padding: 24rpx 0 8rpx;
  border-top: 1px solid #edf0f3;
}

.flow-step {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 10rpx;
  color: #9aa1aa;
  font-size: 23rpx;
}

.flow-dot {
  display: flex;
  width: 54rpx;
  height: 54rpx;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #eef0f2;
  color: #9aa1aa;
  font-size: 24rpx;
  font-weight: 800;
}

.flow-step.done {
  color: #ef3f5f;
  font-weight: 700;
}

.flow-step.done .flow-dot {
  background: #ffe9ee;
  color: #ef3f5f;
}

.review {
  margin-top: 24rpx;
  padding: 24rpx;
  border-radius: 20rpx;
  background: #f7f8f5;
}

.review-title,
.review-content,
.stars {
  display: block;
}

.review-title {
  color: #20242c;
  font-size: 28rpx;
  font-weight: 800;
}

.stars {
  margin-top: 12rpx;
  color: #ff9f1c;
  font-size: 32rpx;
  letter-spacing: 0;
}

.review-content {
  margin-top: 10rpx;
  color: #68717a;
  font-size: 25rpx;
  line-height: 1.6;
}

.star-row {
  display: flex;
  gap: 12rpx;
  margin-top: 12rpx;
}

.star {
  color: #d4d8dd;
  font-size: 44rpx;
  line-height: 1;
}

.star.active {
  color: #ff9f1c;
}

.review-input {
  width: 100%;
  height: 150rpx;
  box-sizing: border-box;
  margin-top: 18rpx;
  padding: 18rpx;
  border-radius: 16rpx;
  background: #fff;
  color: #3b414c;
  font-size: 25rpx;
}

.review-button {
  height: 72rpx;
  line-height: 72rpx;
  margin-top: 18rpx;
  border-radius: 16rpx;
  background: #ef3f5f;
  color: #fff;
  font-size: 26rpx;
  font-weight: 700;
}
</style>
