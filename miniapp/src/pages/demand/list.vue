<template>
  <view class="page">
    <button class="publish" @tap="openPublish">发布新需求</button>
    <view v-if="demands.length === 0" class="empty">暂无需求</view>
    <view v-for="item in demands" :key="item.id" class="card" @tap="openDetail(item.id)">
      <view class="row">
        <text class="title">{{ item.title }}</text>
        <text class="status">{{ auditText(item.auditStatus) }}</text>
      </view>
      <text class="meta">{{ item.categoryName }} · {{ item.city || '-' }} {{ item.district || '' }}</text>
      <text class="meta">跟进：{{ followText(item.followStatus) }} · 推荐 {{ item.recommendationCount || 0 }} 位</text>
    </view>
  </view>
</template>

<script>
import { getDemandList } from '@/services/api'
import { ensureLogin } from '@/services/request'

export default {
  data() {
    return {
      demands: [],
    }
  },
  async onShow() {
    await ensureLogin()
    this.loadList()
  },
  methods: {
    async loadList() {
      const res = await getDemandList({ pageNum: 1, pageSize: 50 })
      this.demands = res.rows || []
    },
    openPublish() {
      uni.navigateTo({ url: '/pages/demand/form' })
    },
    openDetail(id) {
      uni.navigateTo({ url: `/pages/demand/detail?id=${id}` })
    },
    auditText(status) {
      return { PENDING: '审核中', APPROVED: '已通过', REJECTED: '已拒绝', CANCELED: '已取消' }[status] || status || '-'
    },
    followText(status) {
      return { TO_FOLLOW: '待跟进', CONTACTED: '已联系', MATCHED: '已匹配', SIGNED: '已签约', CLOSED: '已关闭' }[status] || status || '-'
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

.publish {
  margin-bottom: 24rpx;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 22rpx;
  background: #e84d64;
  color: #fff;
  font-size: 30rpx;
  box-shadow: 0 12rpx 26rpx rgba(232, 77, 100, 0.18);
}

.card {
  margin-bottom: 20rpx;
  padding: 28rpx;
  border: 1px solid rgba(31, 37, 43, 0.05);
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 12rpx 30rpx rgba(32, 38, 44, 0.05);
}

.row {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.title {
  color: #20242c;
  font-size: 30rpx;
  font-weight: 700;
}

.status {
  flex: 0 0 auto;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #fff0f2;
  color: #d93f58;
  font-size: 24rpx;
}

.meta {
  display: block;
  margin-top: 12rpx;
  color: #6d7480;
  font-size: 26rpx;
}

.empty {
  padding: 90rpx 0;
  border-radius: 24rpx;
  background: #fff;
  color: #8a8f99;
  text-align: center;
  font-size: 28rpx;
}
</style>
