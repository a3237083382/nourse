<template>
  <view class="page" v-if="product">
    <view class="cover">
      <text>{{ product.title.slice(0, 4) }}</text>
    </view>

    <view class="panel">
      <view class="row">
        <text class="title">{{ product.title }}</text>
        <text class="sold">已售 {{ product.soldCount || 0 }}</text>
      </view>
      <view class="prices">
        <text class="group-price">¥{{ money(product.groupPrice) }}</text>
        <text class="single-price">单买 ¥{{ money(product.singlePrice) }}</text>
        <text class="original">原价 ¥{{ money(product.originalPrice) }}</text>
      </view>
      <text class="meta">{{ product.groupSize }} 人成团 · {{ product.validDays }} 天有效</text>
    </view>

    <view class="panel" v-if="product.activeTeams && product.activeTeams.length">
      <text class="section-title">正在拼团</text>
      <view v-for="team in product.activeTeams" :key="team.id" class="team">
        <view>
          <text class="team-name">{{ team.leaderNickname || '用户' }} 的团</text>
          <text class="meta">还差 {{ team.remainingCount }} 人成团</text>
        </view>
        <button @tap="join(team.id)">去拼团</button>
      </view>
    </view>

    <view class="panel">
      <text class="section-title">消费须知</text>
      <text class="paragraph">{{ product.notice || '请提前预约服务时间。' }}</text>
    </view>

    <view class="panel">
      <text class="section-title">服务保障</text>
      <text class="paragraph">{{ product.guarantee || '平台客服协助处理服务过程问题。' }}</text>
    </view>

    <view class="panel">
      <text class="section-title">商品介绍</text>
      <text class="paragraph">{{ product.description || '暂无介绍' }}</text>
    </view>

    <view class="actions">
      <button open-type="contact">客服</button>
      <button class="group" @tap="startGroup">发起拼团 ¥{{ money(product.groupPrice) }}</button>
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
    }
  },
  onLoad(options) {
    this.id = options.id
    this.loadDetail()
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
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  padding-bottom: 130rpx;
  background: #f4f5f2;
}

.cover {
  display: flex;
  height: 360rpx;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e84d64, #f3b35b);
  color: #fff;
  font-size: 52rpx;
  font-weight: 800;
}

.panel {
  margin: 22rpx 24rpx;
  padding: 28rpx;
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 12rpx 30rpx rgba(32, 38, 44, 0.05);
}

.row,
.team,
.actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.title {
  flex: 1;
  color: #20242c;
  font-size: 36rpx;
  font-weight: 800;
  line-height: 1.3;
}

.sold,
.meta,
.paragraph {
  color: #68717a;
  font-size: 26rpx;
  line-height: 1.6;
}

.prices {
  display: flex;
  align-items: baseline;
  gap: 18rpx;
  margin-top: 18rpx;
}

.group-price {
  color: #e84d64;
  font-size: 42rpx;
  font-weight: 900;
}

.single-price,
.original {
  color: #8a8f99;
  font-size: 24rpx;
}

.original {
  text-decoration: line-through;
}

.section-title {
  display: block;
  margin-bottom: 14rpx;
  color: #20242c;
  font-size: 30rpx;
  font-weight: 700;
}

.team {
  padding: 18rpx 0;
  border-top: 1px solid #edf0f3;
}

.team-name {
  display: block;
  color: #20242c;
  font-size: 28rpx;
  font-weight: 700;
}

.team button {
  width: 150rpx;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 16rpx;
  background: #20252b;
  color: #fff;
  font-size: 24rpx;
}

.actions {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 18rpx 20rpx calc(24rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -10rpx 28rpx rgba(32, 38, 44, 0.08);
}

.actions button {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 18rpx;
  background: #f3f4f1;
  color: #3b414c;
  font-size: 24rpx;
}

.actions .group {
  background: #20252b;
  color: #fff;
}

.actions .single {
  background: #e84d64;
  color: #fff;
}
</style>
