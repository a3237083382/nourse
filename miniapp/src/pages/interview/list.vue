<template>
  <view class="page">
    <view v-if="list.length === 0" class="empty">暂无邀约记录</view>
    <view v-for="item in list" :key="item.id" class="card" @tap="openDetail(item.id)">
      <view class="row">
        <text class="title">{{ item.staffName }}</text>
        <text class="status">{{ statusText(item.status) }}</text>
      </view>
      <text class="meta">{{ item.categoryName }} · {{ item.contactName }} · {{ item.contactPhone }}</text>
      <text class="meta">提交时间：{{ item.createdAt }}</text>
    </view>
  </view>
</template>

<script>
import { getInterviewList } from '@/services/api'
import { ensureLogin } from '@/services/request'

export default {
  data() {
    return {
      list: [],
    }
  },
  async onShow() {
    await ensureLogin()
    this.loadList()
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
    async loadList() {
      const res = await getInterviewList({ pageNum: 1, pageSize: 50 })
      this.list = res.rows || []
    },
    openDetail(id) {
      uni.navigateTo({ url: `/pages/interview/detail?id=${id}` })
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  padding: 24rpx;
  background: #f6f7f9;
}

.card,
.empty {
  margin-bottom: 22rpx;
  padding: 28rpx;
  border-radius: 18rpx;
  background: #fff;
}

.empty,
.meta {
  color: #6d7480;
  font-size: 26rpx;
}

.row {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.title {
  color: #20242c;
  font-size: 32rpx;
  font-weight: 700;
}

.status {
  color: #ef3f5f;
  font-size: 26rpx;
}

.meta {
  display: block;
  margin-top: 12rpx;
  line-height: 1.6;
}
</style>
