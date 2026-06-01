<template>
  <view class="page">
    <scroll-view class="tabs" scroll-x>
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab"
        :class="{ active: query.status === tab.value }"
        @tap="switchStatus(tab.value)"
      >
        {{ tab.label }}
      </view>
    </scroll-view>

    <view v-if="orders.length === 0" class="empty">暂无团购订单</view>
    <view v-for="item in orders" :key="item.id" class="order" @tap="openDetail(item)">
      <view class="top">
        <text class="title">{{ item.productTitle }}</text>
        <text class="status">{{ statusText(item.status) }}</text>
      </view>
      <view class="meta">
        <text>{{ item.buyType === 'GROUP' ? '拼团' : '单买' }}</text>
        <text>订单号 {{ item.orderNo }}</text>
      </view>
      <view class="bottom">
        <text class="amount">¥{{ money(item.amount) }}</text>
        <text v-if="item.teamStatus === 'GROUPING'" class="team">还差 {{ item.remainingCount || 0 }} 人</text>
        <text v-else class="team">{{ teamText(item.teamStatus) }}</text>
      </view>
    </view>

    <view v-if="detail" class="mask" @tap="detail = null">
      <view class="sheet" @tap.stop>
        <view class="sheet-head">
          <text class="sheet-title">订单详情</text>
          <text class="close" @tap="detail = null">关闭</text>
        </view>
        <view class="line"><text>商品</text><text>{{ detail.productTitle }}</text></view>
        <view class="line"><text>订单号</text><text>{{ detail.orderNo }}</text></view>
        <view class="line"><text>购买方式</text><text>{{ detail.buyType === 'GROUP' ? '拼团购买' : '单独购买' }}</text></view>
        <view class="line"><text>订单状态</text><text>{{ statusText(detail.status) }}</text></view>
        <view class="line"><text>实付金额</text><text>¥{{ money(detail.amount) }}</text></view>
        <view class="line"><text>有效期至</text><text>{{ detail.validUntil || '-' }}</text></view>
        <view v-if="detail.buyType === 'GROUP'" class="team-card">
          <text class="team-title">拼团状态：{{ teamText(detail.teamStatus) }}</text>
          <text class="team-desc">当前 {{ detail.joinedCount || 1 }}/{{ detail.groupSize || 2 }} 人，满员后自动成团。</text>
        </view>
        <view class="flow">
          <view v-for="step in groupFlow" :key="step.value" class="flow-step" :class="{ done: isFlowDone(step.value) }">
            <view class="flow-dot">{{ step.index }}</view>
            <text>{{ step.label }}</text>
          </view>
        </view>
        <view v-if="detail.reviewId" class="review">
          <text class="review-title">用户评价</text>
          <text class="stars">{{ stars(detail.reviewRating) }}</text>
          <text class="review-content">{{ detail.reviewContent || '用户未填写文字评价' }}</text>
        </view>
        <view v-else-if="detail.status === 'USED'" class="review">
          <text class="review-title">评价本次团购服务</text>
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
            placeholder="写下团购服务体验，方便平台持续改进"
          />
          <button class="review-button" @tap="submitReview">提交评价</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getGroupOrderDetail, getGroupOrderList, reviewGroupOrder } from '@/services/api'
import { ensureLogin } from '@/services/request'

export default {
  data() {
    return {
      tabs: [
        { label: '全部', value: '' },
        { label: '待分享', value: 'WAIT_SHARE' },
        { label: '待使用', value: 'WAIT_USE' },
        { label: '已使用', value: 'USED' },
        { label: '到期', value: 'EXPIRED' },
        { label: '售后', value: 'AFTER_SALE' },
      ],
      query: {
        status: '',
        pageNum: 1,
        pageSize: 50,
      },
      orders: [],
      detail: null,
      reviewForm: {
        rating: 5,
        content: '',
      },
      groupFlow: [
        { index: 1, label: '购买', value: 'PAID' },
        { index: 2, label: '成团', value: 'GROUPED' },
        { index: 3, label: '服务完成', value: 'USED' },
        { index: 4, label: '用户评价', value: 'REVIEWED' },
      ],
    }
  },
  async onShow() {
    await ensureLogin()
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      const res = await getGroupOrderList(this.query)
      this.orders = res.rows || []
    },
    switchStatus(status) {
      this.query.status = status
      this.loadOrders()
    },
    async openDetail(item) {
      const res = await getGroupOrderDetail(item.id)
      this.detail = res.data
      this.reviewForm = { rating: 5, content: '' }
    },
    async submitReview() {
      if (!this.detail) return
      await reviewGroupOrder(this.detail.id, this.reviewForm)
      uni.showToast({ title: '评价成功', icon: 'success' })
      const res = await getGroupOrderDetail(this.detail.id)
      this.detail = res.data
      this.loadOrders()
    },
    statusText(status) {
      return {
        WAIT_SHARE: '待分享',
        WAIT_USE: '待使用',
        USED: '已使用',
        EXPIRED: '到期',
        AFTER_SALE: '售后',
      }[status] || status || '-'
    },
    teamText(status) {
      return {
        GROUPING: '拼团中',
        SUCCESS: '拼团成功',
        FAILED: '拼团失败',
        CANCELED: '已取消',
      }[status] || '单买订单'
    },
    money(value) {
      return Number(value || 0).toFixed(2).replace(/\.00$/, '')
    },
    isFlowDone(value) {
      if (!this.detail) return false
      const grouped = this.detail.buyType !== 'GROUP' || this.detail.teamStatus === 'SUCCESS'
      const used = this.detail.status === 'USED'
      const reviewed = !!this.detail.reviewId
      return {
        PAID: ['WAIT_SHARE', 'WAIT_USE', 'USED'].includes(this.detail.status),
        GROUPED: grouped,
        USED: used,
        REVIEWED: reviewed,
      }[value]
    },
    stars(value) {
      const score = Number(value || 0)
      return '★★★★★'.slice(0, score) + '☆☆☆☆☆'.slice(0, Math.max(0, 5 - score))
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
  white-space: nowrap;
}

.tab {
  display: inline-flex;
  height: 64rpx;
  align-items: center;
  margin-right: 14rpx;
  padding: 0 24rpx;
  border-radius: 999rpx;
  background: #fff;
  color: #68717a;
  font-size: 25rpx;
}

.tab.active {
  background: #20252b;
  color: #fff;
}

.order,
.empty {
  margin-top: 22rpx;
  padding: 26rpx;
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 12rpx 30rpx rgba(32, 38, 44, 0.05);
}

.empty {
  color: #8a8f99;
  text-align: center;
  font-size: 28rpx;
}

.top,
.meta,
.bottom,
.sheet-head,
.line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.title {
  flex: 1;
  min-width: 0;
  color: #20242c;
  font-size: 30rpx;
  font-weight: 700;
}

.status {
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #fff0f2;
  color: #d93f58;
  font-size: 22rpx;
}

.meta {
  justify-content: flex-start;
  margin-top: 14rpx;
  color: #8a8f99;
  font-size: 24rpx;
}

.bottom {
  margin-top: 18rpx;
}

.amount {
  color: #e84d64;
  font-size: 34rpx;
  font-weight: 800;
}

.team {
  color: #68717a;
  font-size: 24rpx;
}

.mask {
  position: fixed;
  inset: 0;
  z-index: 20;
  display: flex;
  align-items: flex-end;
  background: rgba(0, 0, 0, 0.34);
}

.sheet {
  width: 100%;
  padding: 30rpx 28rpx calc(32rpx + env(safe-area-inset-bottom));
  border-radius: 30rpx 30rpx 0 0;
  background: #fff;
}

.sheet-title {
  color: #20242c;
  font-size: 32rpx;
  font-weight: 800;
}

.close {
  color: #8a8f99;
  font-size: 26rpx;
}

.line {
  padding: 22rpx 0;
  border-bottom: 1px solid #edf0f3;
  color: #3b414c;
  font-size: 26rpx;
}

.line text:first-child {
  color: #8a8f99;
}

.team-card {
  margin-top: 22rpx;
  padding: 22rpx;
  border-radius: 20rpx;
  background: #f7f8f5;
}

.team-title,
.team-desc {
  display: block;
}

.team-title {
  color: #20242c;
  font-size: 28rpx;
  font-weight: 700;
}

.team-desc {
  margin-top: 8rpx;
  color: #68717a;
  font-size: 24rpx;
  line-height: 1.6;
}

.flow {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
  margin-top: 24rpx;
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
