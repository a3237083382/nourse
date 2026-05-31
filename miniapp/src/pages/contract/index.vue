<template>
  <view class="page">
    <view class="tabs">
      <view v-for="item in tabs" :key="item.value" class="tab" :class="{ active: status === item.value }" @tap="switchStatus(item.value)">
        {{ item.label }}
      </view>
    </view>

    <view v-if="contracts.length === 0" class="empty">暂无合同信息</view>
    <view v-for="item in contracts" :key="item.id" class="card" @tap="openDetail(item)">
      <view class="row">
        <text class="title">{{ item.title }}</text>
        <text class="badge" :class="item.status">{{ statusText(item.status) }}</text>
      </view>
      <text class="meta">合同编号：{{ item.contractNo }}</text>
      <text class="meta">服务人员：{{ item.staffName || '-' }}</text>
      <text class="meta">签署时间：{{ item.signedAt || '-' }}</text>
    </view>

    <view v-if="detail" class="sheet">
      <view class="sheet-card">
        <view class="row">
          <text class="title">{{ detail.title }}</text>
          <text class="close" @tap="detail = null">关闭</text>
        </view>
        <text class="meta">合同编号：{{ detail.contractNo }}</text>
        <text class="meta">服务人员：{{ detail.staffName || '-' }}</text>
        <text class="meta">服务类型：{{ detail.categoryName || '-' }}</text>
        <text class="meta">关联需求：{{ detail.demandTitle || '-' }}</text>
        <text class="meta">关联订单：{{ detail.orderNo || '-' }}</text>
        <button class="primary" @tap="openFile(detail.fileUrl)">查看合同文件</button>
      </view>
    </view>
  </view>
</template>

<script>
import { getContractDetail, getContractList } from '@/services/api'
import { ensureLogin } from '@/services/request'

export default {
  data() {
    return {
      status: 'SIGNED',
      contracts: [],
      detail: null,
      tabs: [
        { label: '已签署', value: 'SIGNED' },
        { label: '已终止', value: 'TERMINATED' },
      ],
    }
  },
  async onShow() {
    await ensureLogin()
    this.loadContracts()
  },
  methods: {
    async loadContracts() {
      const res = await getContractList({ status: this.status, pageNum: 1, pageSize: 50 })
      this.contracts = res.rows || []
    },
    switchStatus(status) {
      this.status = status
      this.detail = null
      this.loadContracts()
    },
    async openDetail(item) {
      const res = await getContractDetail(item.id)
      this.detail = res.data
    },
    openFile(url) {
      if (!url) {
        uni.showToast({ title: '暂无文件', icon: 'none' })
        return
      }
      if (/\.(png|jpg|jpeg|webp)$/i.test(url)) {
        uni.previewImage({ urls: [url] })
        return
      }
      uni.showModal({
        title: '合同文件',
        content: url,
        showCancel: false,
      })
    },
    statusText(value) {
      return { SIGNED: '已签署', TERMINATED: '已终止' }[value] || value
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
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14rpx;
  margin-bottom: 22rpx;
}

.tab {
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 18rpx;
  background: #fff;
  color: #68717a;
  text-align: center;
  font-size: 28rpx;
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
  flex: 1;
  min-width: 0;
  color: #20242c;
  font-size: 32rpx;
  font-weight: 700;
  line-height: 1.35;
}

.badge {
  flex: 0 0 auto;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #e9f7ef;
  color: #218956;
  font-size: 23rpx;
}

.badge.TERMINATED {
  background: #f1f2f3;
  color: #68717a;
}

.meta {
  display: block;
  margin-top: 14rpx;
  color: #68717a;
  font-size: 26rpx;
  line-height: 1.55;
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

.primary {
  margin-top: 24rpx;
  height: 78rpx;
  line-height: 78rpx;
  border-radius: 18rpx;
  background: #e84d64;
  color: #fff;
  font-size: 28rpx;
}
</style>
