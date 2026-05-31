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
      </view>
    </view>
  </view>
</template>

<script>
import { getServiceOrderDetail, getServiceOrderList } from '@/services/api'
import { ensureLogin } from '@/services/request'

export default {
  data() {
    return {
      status: '',
      orders: [],
      detail: null,
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
    },
    statusText(value) {
      return { WAIT_START: '待开始', SERVING: '服务中', COMPLETED: '已完成', CANCELED: '已取消' }[value] || value || '-'
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
</style>
