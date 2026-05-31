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
      </view>
    </view>
  </view>
</template>

<script>
import { getGroupOrderDetail, getGroupOrderList } from '@/services/api'
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
</style>
