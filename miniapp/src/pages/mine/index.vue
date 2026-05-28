<template>
  <view class="page">
    <view class="profile">
      <view class="avatar"></view>
      <view>
        <text class="name">未登录用户</text>
        <text class="phone">手机号快捷登录</text>
      </view>
    </view>
    <view class="menu">
      <view v-for="item in menus" :key="item" class="menu-item">{{ item }}</view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      menus: ['团购订单', '服务订单', '我的需求', '我的邀约', '我的收藏', '常见问题', '用户协议', '隐私政策', '联系客服', '关于我们'],
    }
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  background: #f6f7f9;
  padding: 24rpx;
}

.profile {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 32rpx;
  border-radius: 18rpx;
  background: #ffffff;
}

.avatar {
  width: 108rpx;
  height: 108rpx;
  border-radius: 50%;
  background: #f5c7d1;
}

.name,
.phone {
  display: block;
}

.name {
  color: #20242c;
  font-size: 32rpx;
  font-weight: 700;
}

.phone {
  margin-top: 8rpx;
  color: #8a8f99;
  font-size: 24rpx;
}

.menu {
  margin-top: 24rpx;
  border-radius: 18rpx;
  background: #ffffff;
}

.menu-item {
  padding: 30rpx 32rpx;
  border-bottom: 1px solid #edf0f3;
  color: #20242c;
  font-size: 28rpx;
}

.menu-item:last-child {
  border-bottom: 0;
}
</style>
