<template>
  <view class="page">
    <view class="profile-band">
      <view class="profile-row">
        <view class="avatar">
          <image v-if="avatarUrl" class="avatar-img" :src="avatarUrl" mode="aspectFill" />
          <view v-else class="avatar-heart"></view>
        </view>
        <view class="profile-main">
          <text class="nickname">{{ loggedIn ? nickname : '未登录用户' }}</text>
          <text v-if="!loggedIn" class="login-tip">登录后查看订单、需求和收藏</text>
          <button v-if="!loggedIn" class="phone-login" open-type="getPhoneNumber" @getphonenumber="quickPhoneLogin">
            手机号快捷登录
          </button>
        </view>
        <button class="setting-btn" @tap="openProfile">
          <text class="setting-dot"></text>
          <text>设置</text>
        </button>
      </view>
    </view>

    <view class="order-card">
      <text class="card-title">我的订单</text>
      <view class="order-grid">
        <view class="order-entry" @tap="openGroupOrders">
          <view class="order-icon group-icon">团</view>
          <text>团购订单</text>
        </view>
        <view class="order-entry" @tap="openServiceOrders">
          <view class="order-icon service-icon">服</view>
          <text>服务订单</text>
        </view>
      </view>
    </view>

    <view class="menu-card">
      <view v-for="item in menuItems" :key="item.title" class="menu-item" @tap="handleMenu(item)">
        <view class="menu-left">
          <view class="menu-icon" :class="item.iconClass">
            <text>{{ item.icon }}</text>
          </view>
          <text class="menu-title">{{ item.title }}</text>
        </view>
        <text class="chevron">›</text>
      </view>
    </view>

    <button v-if="loggedIn" class="logout" @tap="logout">退出登录</button>
  </view>
</template>

<script>
import { getFavoriteStaff, getUserProfile } from '@/services/api'
import { ensureLogin, getToken, phoneLogin, setToken } from '@/services/request'

export default {
  data() {
    return {
      loggedIn: false,
      nickname: '新用户B2V4475A7',
      avatarUrl: '',
      phone: '',
      favorites: [],
      menuItems: [
        { title: '我的需求', icon: '需', iconClass: 'need', action: 'openDemands' },
        { title: '我的邀约', icon: '约', iconClass: 'phone', action: 'openInterviews' },
        { title: '我的收藏', icon: '收', iconClass: 'star', action: 'openFavorites' },
        { title: '我的推荐', icon: '荐', iconClass: 'mail', action: 'openRecommendations' },
        { title: '常见问题', icon: '?', iconClass: 'help', action: 'openFaq' },
        { title: '用户协议', icon: '议', iconClass: 'doc', action: 'openAgreement' },
        { title: '隐私政策', icon: '隐', iconClass: 'safe', action: 'openPrivacy' },
        { title: '联系客服', icon: '客', iconClass: 'chat', action: 'contact' },
        { title: '关于我们', icon: 'i', iconClass: 'about', action: 'openAbout' },
      ],
    }
  },
  onShow() {
    this.loggedIn = !!getToken()
    this.loadProfile()
    this.loadFavorites()
  },
  methods: {
    async loadProfile() {
      if (!getToken()) {
        this.avatarUrl = ''
        this.phone = ''
        return
      }
      try {
        const res = await getUserProfile()
        const profile = res.data || {}
        this.nickname = profile.nickname || this.nickname
        this.avatarUrl = profile.avatarUrl || ''
        this.phone = profile.phone || ''
        uni.setStorageSync('appUserProfile', profile)
      } catch (error) {
        const cached = uni.getStorageSync('appUserProfile') || {}
        this.nickname = cached.nickname || this.nickname
        this.avatarUrl = cached.avatarUrl || ''
        this.phone = cached.phone || ''
      }
    },
    async loadFavorites() {
      if (!getToken()) {
        this.favorites = []
        return
      }
      const res = await getFavoriteStaff({ pageNum: 1, pageSize: 20 })
      this.favorites = res.rows || []
    },
    async requireLogin() {
      await ensureLogin()
      this.loggedIn = !!getToken()
    },
    async quickPhoneLogin(event) {
      try {
        const data = await phoneLogin({
          code: event.detail && event.detail.code,
          phone: event.detail && event.detail.phoneNumber,
        })
        this.nickname = data.nickname || '手机号用户'
        this.phone = data.phone || ''
        this.loggedIn = true
        uni.setStorageSync('appUserProfile', data)
        uni.showToast({ title: '登录成功', icon: 'success' })
        this.loadProfile()
        this.loadFavorites()
      } catch (error) {
        uni.showToast({ title: '登录失败，请稍后再试', icon: 'none' })
      }
    },
    handleMenu(item) {
      this[item.action]()
    },
    async openDemands() {
      await this.requireLogin()
      uni.navigateTo({ url: '/pages/demand/list' })
    },
    async openInterviews() {
      await this.requireLogin()
      uni.navigateTo({ url: '/pages/interview/list' })
    },
    async openFavorites() {
      await this.requireLogin()
      if (!this.favorites.length) {
        uni.showToast({ title: '暂无收藏', icon: 'none' })
        return
      }
      if (this.favorites.length === 1) {
        this.openDetail(this.favorites[0].id)
        return
      }
      uni.showActionSheet({
        itemList: this.favorites.slice(0, 6).map((item) => item.name),
        success: (res) => {
          const item = this.favorites[res.tapIndex]
          if (item) this.openDetail(item.id)
        },
      })
    },
    async openRecommendations() {
      await this.requireLogin()
      uni.navigateTo({ url: '/pages/demand/list' })
    },
    openFaq() {
      this.openContent('faq')
    },
    openAgreement() {
      this.openContent('agreement')
    },
    openPrivacy() {
      this.openContent('privacy')
    },
    openAbout() {
      this.openContent('about')
    },
    contact() {
      uni.showToast({ title: '请使用右侧联系客服按钮', icon: 'none' })
    },
    async openGroupOrders() {
      await this.requireLogin()
      uni.navigateTo({ url: '/pages/group/orders' })
    },
    async openServiceOrders() {
      await this.requireLogin()
      uni.navigateTo({ url: '/pages/order/service' })
    },
    openDetail(id) {
      uni.navigateTo({ url: `/pages/staff/detail?id=${id}` })
    },
    openContent(type) {
      uni.navigateTo({ url: `/pages/content/index?type=${type}` })
    },
    async openProfile() {
      await this.requireLogin()
      uni.navigateTo({ url: '/pages/mine/user-profile' })
    },
    logout() {
      setToken('')
      uni.removeStorageSync('appToken')
      uni.removeStorageSync('appUserProfile')
      this.loggedIn = false
      this.favorites = []
      this.avatarUrl = ''
      this.phone = ''
      uni.showToast({ title: '已退出登录', icon: 'none' })
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  padding: 0 24rpx 148rpx;
  background: linear-gradient(180deg, #fff1ed 0, #fff8f4 330rpx, #f7f4ef 760rpx);
}

.profile-band {
  margin: 0 -24rpx;
  padding: 86rpx 24rpx 40rpx;
  background:
    radial-gradient(circle at 22% 30%, rgba(255, 255, 255, 0.92) 0 78rpx, transparent 80rpx),
    radial-gradient(circle at 78% 20%, rgba(239, 79, 95, 0.12) 0 116rpx, transparent 118rpx),
    linear-gradient(135deg, #fff1ee 0%, #fff8f4 58%, #ffffff 100%);
}

.profile-row {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.avatar {
  position: relative;
  display: flex;
  width: 118rpx;
  height: 118rpx;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  border: 6rpx solid #fff;
  border-radius: 50%;
  background: #fff7f2;
  box-shadow: 0 12rpx 30rpx rgba(239, 79, 95, 0.14);
}

.avatar-heart {
  position: relative;
  width: 54rpx;
  height: 50rpx;
  transform: rotate(-45deg);
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.avatar-heart::before,
.avatar-heart::after {
  position: absolute;
  width: 54rpx;
  height: 54rpx;
  border-radius: 50%;
  background: #ef4f5f;
  content: '';
}

.avatar-heart::before {
  top: -27rpx;
  left: 0;
}

.avatar-heart::after {
  top: 0;
  left: 27rpx;
}

.profile-main {
  flex: 1;
  min-width: 0;
}

.nickname {
  display: block;
  color: #222832;
  font-size: 38rpx;
  font-weight: 900;
  line-height: 1.2;
}

.login-tip {
  display: block;
  margin-top: 8rpx;
  color: #8a6161;
  font-size: 24rpx;
}

.phone-login {
  width: 230rpx;
  height: 56rpx;
  line-height: 56rpx;
  margin: 18rpx 0 0;
  border-radius: 999rpx;
  background: #ef4f5f;
  color: #fff;
  font-size: 24rpx;
  font-weight: 800;
}

.setting-btn {
  display: flex;
  width: 128rpx;
  height: 62rpx;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  flex: 0 0 auto;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #ff9478, #ef4f5f);
  color: #fff;
  font-size: 27rpx;
  font-weight: 800;
  box-shadow: 0 10rpx 22rpx rgba(255, 91, 102, 0.22);
}

.setting-dot {
  width: 18rpx;
  height: 18rpx;
  border: 6rpx solid #fff;
  border-radius: 50%;
}

.order-card,
.menu-card {
  border: 1rpx solid #f3e5dc;
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 12rpx 28rpx rgba(80, 45, 40, 0.05);
}

.order-card {
  margin-top: -10rpx;
  padding: 30rpx 32rpx 34rpx;
}

.card-title {
  display: block;
  color: #222832;
  font-size: 34rpx;
  font-weight: 900;
}

.order-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  margin-top: 26rpx;
}

.order-entry {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 14rpx;
  color: #222832;
  font-size: 29rpx;
}

.order-icon {
  display: flex;
  width: 74rpx;
  height: 74rpx;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  color: #fff;
  font-size: 30rpx;
  font-weight: 900;
}

.group-icon {
  background: linear-gradient(135deg, #ef4f5f, #ff9478);
}

.service-icon {
  background: linear-gradient(135deg, #d9893d, #f2c987);
}

.menu-card {
  margin-top: 22rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  min-height: 92rpx;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx 0 30rpx;
  border-bottom: 1rpx solid #edf0f3;
}

.menu-item:last-child {
  border-bottom: 0;
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.menu-icon {
  display: flex;
  width: 42rpx;
  height: 42rpx;
  align-items: center;
  justify-content: center;
  border: 2rpx solid #eadbd2;
  border-radius: 10rpx;
  color: #643b34;
  font-size: 22rpx;
  font-weight: 900;
}

.menu-icon.phone,
.menu-icon.star,
.menu-icon.help,
.menu-icon.chat,
.menu-icon.about {
  border-radius: 50%;
}

.menu-icon.need,
.menu-icon.mail,
.menu-icon.doc,
.menu-icon.safe {
  box-shadow: inset 0 8rpx 0 rgba(255, 79, 94, 0.12);
}

.menu-title {
  color: #222832;
  font-size: 29rpx;
}

.chevron {
  color: #b7a9a2;
  font-size: 56rpx;
  line-height: 1;
}

.logout {
  height: 72rpx;
  line-height: 72rpx;
  margin-top: 28rpx;
  border-radius: 999rpx;
  border: 1rpx solid #ef4f5f;
  background: #fff1ee;
  color: #ef4f5f;
  font-size: 28rpx;
  font-weight: 800;
}
</style>
