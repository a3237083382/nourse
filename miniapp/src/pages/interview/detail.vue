<template>
  <view class="page" v-if="detail">
    <view class="panel">
      <view class="row">
        <text class="title">{{ detail.staffName }}</text>
        <text class="status">{{ statusText(detail.status) }}</text>
      </view>
      <text class="meta">{{ detail.categoryName }}</text>
      <text v-if="detail.demandTitle" class="meta">来源需求：{{ detail.demandTitle }}</text>
    </view>

    <view class="panel">
      <text class="section-title">联系信息</text>
      <text class="line">联系人：{{ detail.contactName }}</text>
      <text class="line">电话：{{ detail.contactPhone }}</text>
      <text class="line">提交时间：{{ detail.createdAt }}</text>
    </view>

    <view class="panel">
      <text class="section-title">处理说明</text>
      <text class="meta">{{ detail.adminNote || '平台工作人员会线下联系你确认面试安排。' }}</text>
    </view>
  </view>
</template>

<script>
import { getInterviewDetail } from '@/services/api'
import { ensureLogin } from '@/services/request'

export default {
  data() {
    return {
      id: undefined,
      detail: null,
    }
  },
  async onLoad(options) {
    this.id = options.id
    await ensureLogin()
    this.loadDetail()
  },
  methods: {
    statusText(status) {
      return {
        PENDING: '待处理',
        CONTACTED: '已联系',
        ARRANGED: '已安排',
        COMPLETED: '已完成',
        CANCELED: '已取消',
      }[status] || status || '-'
    },
    async loadDetail() {
      const res = await getInterviewDetail(this.id)
      this.detail = res.data
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

.panel {
  margin-bottom: 22rpx;
  padding: 28rpx;
  border: 1px solid rgba(31, 37, 43, 0.05);
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 12rpx 30rpx rgba(32, 38, 44, 0.05);
}

.row {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.title,
.section-title {
  color: #20242c;
  font-weight: 700;
}

.title {
  font-size: 34rpx;
}

.section-title {
  display: block;
  margin-bottom: 12rpx;
  font-size: 30rpx;
}

.status {
  flex: 0 0 auto;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #fff0f2;
  color: #d93f58;
  font-size: 26rpx;
}

.meta,
.line {
  display: block;
  margin-top: 12rpx;
  color: #6d7480;
  font-size: 26rpx;
  line-height: 1.6;
}
</style>
