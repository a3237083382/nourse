<template>
  <view class="page">
    <view class="profile">
      <view class="avatar">我</view>
      <view>
        <text class="name">{{ loggedIn ? '小程序用户' : '未登录用户' }}</text>
        <text class="phone">{{ loggedIn ? '已登录' : '收藏、发布需求等操作需要登录' }}</text>
      </view>
    </view>

    <view class="quick">
      <button class="primary" @tap="openPublish">发布需求</button>
      <button @tap="openDemands">我的需求</button>
    </view>

    <view class="section">
      <text class="section-title">我的收藏</text>
      <view v-if="favorites.length === 0" class="empty">暂无收藏服务人员</view>
      <view v-for="item in favorites" :key="item.id" class="favorite" @tap="openDetail(item.id)">
        <text>{{ item.name }}</text>
        <text class="favorite-meta">{{ item.city }} {{ item.district }}</text>
      </view>
    </view>

    <view class="menu">
      <view class="menu-item" @tap="openDemands">我的推荐</view>
      <view class="menu-item" @tap="openInterviews">我的邀约</view>
      <view class="menu-item" @tap="openGroupProducts">团购服务</view>
      <view class="menu-item" @tap="openGroupOrders">团购订单</view>
      <view class="menu-item" @tap="openServiceOrders">服务订单</view>
      <view class="menu-item" @tap="openContent('faq')">常见问题</view>
      <view class="menu-item" @tap="openContent('agreement')">用户协议</view>
      <view class="menu-item" @tap="openContent('privacy')">隐私政策</view>
      <view class="menu-item">联系客服</view>
      <view class="menu-item" @tap="openContent('about')">关于我们</view>
    </view>
  </view>
</template>

<script>
import { getFavoriteStaff } from '@/services/api'
import { ensureLogin, getToken } from '@/services/request'

export default {
  data() {
    return {
      loggedIn: false,
      favorites: [],
    }
  },
  onShow() {
    this.loggedIn = !!getToken()
    this.loadFavorites()
  },
  methods: {
    async loadFavorites() {
      if (!getToken()) {
        this.favorites = []
        return
      }
      const res = await getFavoriteStaff({ pageNum: 1, pageSize: 20 })
      this.favorites = res.rows || []
    },
    async openPublish() {
      await ensureLogin()
      uni.navigateTo({ url: '/pages/demand/form' })
    },
    async openDemands() {
      await ensureLogin()
      uni.navigateTo({ url: '/pages/demand/list' })
    },
    async openInterviews() {
      await ensureLogin()
      uni.navigateTo({ url: '/pages/interview/list' })
    },
    openGroupProducts() {
      uni.navigateTo({ url: '/pages/group/list' })
    },
    async openGroupOrders() {
      await ensureLogin()
      uni.navigateTo({ url: '/pages/group/orders' })
    },
    async openServiceOrders() {
      await ensureLogin()
      uni.navigateTo({ url: '/pages/order/service' })
    },
    openDetail(id) {
      uni.navigateTo({ url: `/pages/staff/detail?id=${id}` })
    },
    openContent(type) {
      uni.navigateTo({ url: `/pages/content/index?type=${type}` })
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  background: #f4f5f2;
  padding: 24rpx 24rpx 36rpx;
}

.profile {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 34rpx;
  border-radius: 28rpx;
  background: #20252b;
  box-shadow: 0 18rpx 42rpx rgba(32, 37, 43, 0.16);
}

.avatar {
  display: flex;
  width: 108rpx;
  height: 108rpx;
  align-items: center;
  justify-content: center;
  border-radius: 24rpx;
  background: #ffffff;
  color: #e84d64;
  font-size: 40rpx;
  font-weight: 800;
}

.profile .avatar {
  color: #fff;
  background: #e84d64;
}

.name,
.phone,
.section-title {
  display: block;
}

.name {
  color: #fff;
  font-size: 32rpx;
  font-weight: 700;
}

.phone {
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.68);
  font-size: 24rpx;
}

.quick {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx;
  margin-top: 24rpx;
}

.quick button {
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 20rpx;
  background: #fff;
  color: #3b414c;
  font-size: 28rpx;
  box-shadow: 0 10rpx 24rpx rgba(32, 38, 44, 0.05);
}

.quick .primary {
  background: #e84d64;
  color: #fff;
}

.section,
.menu {
  margin-top: 24rpx;
  border: 1px solid rgba(31, 37, 43, 0.05);
  border-radius: 26rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 30rpx rgba(32, 38, 44, 0.05);
}

.section {
  padding: 30rpx 32rpx;
}

.section-title {
  margin-bottom: 16rpx;
  color: #20242c;
  font-size: 30rpx;
  font-weight: 700;
}

.empty {
  color: #8a8f99;
  font-size: 26rpx;
}

.favorite {
  display: flex;
  justify-content: space-between;
  padding: 22rpx 0;
  border-top: 1px solid #edf0f3;
  color: #20242c;
  font-size: 28rpx;
}

.favorite-meta {
  color: #8a8f99;
  font-size: 24rpx;
}

.menu-item {
  position: relative;
  padding: 30rpx 54rpx 30rpx 32rpx;
  border-bottom: 1px solid #edf0f3;
  color: #20242c;
  font-size: 28rpx;
}

.menu-item::after {
  position: absolute;
  right: 32rpx;
  top: 50%;
  width: 14rpx;
  height: 14rpx;
  border-top: 2rpx solid #b7bec5;
  border-right: 2rpx solid #b7bec5;
  content: "";
  transform: translateY(-50%) rotate(45deg);
}

.menu-item:last-child {
  border-bottom: 0;
}
</style>
