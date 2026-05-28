<template>
  <view class="page" v-if="demand">
    <view class="panel">
      <view class="row">
        <text class="title">{{ demand.title }}</text>
        <text class="status">{{ auditText(demand.auditStatus) }}</text>
      </view>
      <text class="meta">{{ demand.categoryName }} · {{ demand.city || '-' }} {{ demand.district || '' }}</text>
      <text class="meta">跟进状态：{{ followText(demand.followStatus) }}</text>
    </view>

    <view class="panel">
      <text class="section-title">需求信息</text>
      <text class="line">联系人：{{ demand.contactName }}</text>
      <text class="line">联系电话：{{ demand.contactPhone }}</text>
      <text class="line">详细地址：{{ demand.address }}</text>
      <text class="line">薪资待遇：{{ demand.expectedSalary || '-' }}</text>
      <text class="line">是否住家：{{ demand.liveIn ? '是' : '否' }}</text>
      <text class="line">补充说明：{{ demand.remark || '-' }}</text>
    </view>

    <view class="panel">
      <view class="row">
        <text class="section-title">推荐阿姨</text>
        <text class="hint">{{ recommendations.length }} 位</text>
      </view>
      <view v-if="recommendations.length === 0" class="empty">
        {{ demand.auditStatus === 'APPROVED' ? '平台正在为你匹配合适阿姨' : '审核通过后将展示推荐阿姨' }}
      </view>
      <view v-for="item in recommendations" :key="item.id" class="recommend" @tap="openStaff(item.staffId)">
        <text class="recommend-name">{{ item.staffName }}</text>
        <text class="meta">{{ item.categoryName }} · {{ item.city || '-' }} · {{ item.age || '-' }}岁 · {{ item.experienceYears || 0 }}年经验</text>
        <text class="reason">推荐理由：{{ item.reason || '符合你的需求条件' }}</text>
      </view>
    </view>

    <button v-if="demand.auditStatus === 'PENDING'" class="cancel" @tap="cancel">取消需求</button>
  </view>
</template>

<script>
import { cancelDemand, getDemandDetail, getDemandRecommendations } from '@/services/api'
import { ensureLogin } from '@/services/request'

export default {
  data() {
    return {
      id: undefined,
      demand: null,
      recommendations: [],
    }
  },
  async onLoad(options) {
    await ensureLogin()
    this.id = options.id
    this.loadDetail()
  },
  methods: {
    async loadDetail() {
      const res = await getDemandDetail(this.id)
      this.demand = res.data
      this.recommendations = res.data.recommendations || []
      if (this.recommendations.length) {
        const recRes = await getDemandRecommendations(this.id)
        this.recommendations = recRes.data || []
      }
    },
    auditText(status) {
      return { PENDING: '审核中', APPROVED: '已通过', REJECTED: '已拒绝', CANCELED: '已取消' }[status] || status || '-'
    },
    followText(status) {
      return { TO_FOLLOW: '待跟进', CONTACTED: '已联系', MATCHED: '已匹配', SIGNED: '已签约', CLOSED: '已关闭' }[status] || status || '-'
    },
    openStaff(id) {
      uni.navigateTo({ url: `/pages/staff/detail?id=${id}&demandId=${this.id}` })
    },
    cancel() {
      uni.showModal({
        title: '确认取消',
        content: '只有审核中的需求可以取消',
        success: async (res) => {
          if (!res.confirm) return
          await cancelDemand(this.id)
          uni.showToast({ title: '已取消', icon: 'success' })
          this.loadDetail()
        },
      })
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

.panel {
  margin-bottom: 22rpx;
  padding: 28rpx;
  border-radius: 16rpx;
  background: #fff;
}

.row {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.title,
.section-title,
.recommend-name {
  color: #20242c;
  font-weight: 700;
}

.title {
  font-size: 34rpx;
}

.section-title,
.recommend-name {
  font-size: 30rpx;
}

.status,
.hint {
  color: #ef3f5f;
  font-size: 26rpx;
}

.meta,
.line,
.reason,
.empty {
  display: block;
  margin-top: 14rpx;
  color: #6d7480;
  font-size: 26rpx;
  line-height: 1.7;
}

.recommend {
  padding: 22rpx 0;
  border-top: 1px solid #edf0f3;
}

.empty {
  padding: 30rpx 0;
  text-align: center;
}

.cancel {
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 999rpx;
  background: #f1f3f6;
  color: #3b414c;
  font-size: 28rpx;
}
</style>
