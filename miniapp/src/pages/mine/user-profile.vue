<template>
  <view class="page">
    <view class="hero">
      <view class="avatar-row">
        <button class="avatar-button" @tap="chooseAvatar">
          <image v-if="form.avatarUrl" class="avatar-image" :src="form.avatarUrl" mode="aspectFill" />
          <view v-else class="avatar-mark">
            <view class="avatar-heart"></view>
          </view>
          <view class="edit-dot">✎</view>
        </button>
        <view class="avatar-copy">
          <text class="avatar-title">头像</text>
          <text class="avatar-tip">建议使用真实头像，提高成交率</text>
        </view>
      </view>
    </view>

    <view class="form-card">
      <view class="form-row">
        <text class="label">昵称：</text>
        <input
          class="field"
          v-model="form.nickname"
          maxlength="24"
          placeholder="请输入昵称"
          placeholder-class="placeholder"
        />
      </view>
      <view class="form-row no-border">
        <text class="label">联系电话：</text>
        <text class="phone">{{ maskedPhone }}</text>
      </view>
    </view>

    <button class="confirm" :loading="saving" @tap="saveProfile">确定</button>
  </view>
</template>

<script>
import { getUserProfile, updateUserProfile } from '@/services/api'
import { ensureLogin } from '@/services/request'

export default {
  data() {
    return {
      saving: false,
      form: {
        nickname: '',
        avatarUrl: '',
        phone: '',
      },
    }
  },
  computed: {
    maskedPhone() {
      const phone = this.form.phone || ''
      if (phone.length < 7) {
        return phone || '未绑定手机号'
      }
      return `${phone.slice(0, 3)}****${phone.slice(-4)}`
    },
  },
  async onLoad() {
    await this.loadProfile()
  },
  methods: {
    async loadProfile() {
      try {
        await ensureLogin()
        const res = await getUserProfile()
        const profile = res.data || {}
        this.form.nickname = profile.nickname || ''
        this.form.avatarUrl = profile.avatarUrl || ''
        this.form.phone = profile.phone || ''
      } catch (error) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        setTimeout(() => uni.navigateBack(), 500)
      }
    },
    chooseAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album'],
        success: (res) => {
          const avatarUrl = res.tempFilePaths && res.tempFilePaths[0]
          if (avatarUrl) {
            this.form.avatarUrl = avatarUrl
          }
        },
      })
    },
    async saveProfile() {
      const nickname = this.form.nickname.trim()
      if (!nickname) {
        uni.showToast({ title: '请输入昵称', icon: 'none' })
        return
      }
      this.saving = true
      try {
        const res = await updateUserProfile({
          nickname,
          avatarUrl: this.form.avatarUrl,
        })
        uni.setStorageSync('appUserProfile', res.data || { ...this.form, nickname })
        uni.showToast({ title: '保存成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 500)
      } catch (error) {
        uni.showToast({ title: error.message || '保存失败', icon: 'none' })
      } finally {
        this.saving = false
      }
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  padding: 0 28rpx 150rpx;
  background: linear-gradient(180deg, #fff1ed 0, #fff8f4 330rpx, #f7f4ef 760rpx);
}

.hero {
  margin: 0 -28rpx;
  padding: 58rpx 28rpx 42rpx;
  background:
    radial-gradient(circle at 15% 38%, rgba(255, 255, 255, 0.92) 0 88rpx, transparent 91rpx),
    radial-gradient(circle at 80% 16%, rgba(239, 79, 95, 0.12) 0 116rpx, transparent 118rpx),
    linear-gradient(135deg, #fff1ee 0%, #fff8f4 58%, #ffffff 100%);
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 34rpx;
}

.avatar-button {
  position: relative;
  width: 146rpx;
  height: 146rpx;
  margin: 0;
  padding: 0;
  border: 8rpx solid #fff;
  border-radius: 50%;
  background: #fff7f2;
  box-shadow: 0 16rpx 36rpx rgba(239, 79, 95, 0.14);
}

.avatar-button::after {
  border: 0;
}

.avatar-image,
.avatar-mark {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.avatar-mark {
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-heart {
  position: relative;
  width: 58rpx;
  height: 54rpx;
  transform: rotate(-45deg);
}

.avatar-heart::before,
.avatar-heart::after {
  position: absolute;
  width: 58rpx;
  height: 58rpx;
  border-radius: 50%;
  background: #ef4f5f;
  content: '';
}

.avatar-heart::before {
  top: -29rpx;
  left: 0;
}

.avatar-heart::after {
  top: 0;
  left: 29rpx;
}

.edit-dot {
  position: absolute;
  right: -4rpx;
  bottom: 6rpx;
  display: flex;
  width: 46rpx;
  height: 46rpx;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #d9893d;
  color: #fff;
  font-size: 27rpx;
  font-weight: 900;
}

.avatar-copy {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.avatar-title {
  color: #222832;
  font-size: 42rpx;
  font-weight: 900;
}

.avatar-tip {
  color: #9a8d8d;
  font-size: 27rpx;
}

.form-card {
  margin-top: 18rpx;
  padding: 22rpx 46rpx;
  border: 1rpx solid #f3e5dc;
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 12rpx 28rpx rgba(80, 45, 40, 0.05);
}

.form-row {
  display: flex;
  min-height: 118rpx;
  align-items: center;
  border-bottom: 1rpx solid #eceff2;
}

.no-border {
  border-bottom: 0;
}

.label {
  flex: 0 0 auto;
  color: #222832;
  font-size: 32rpx;
  font-weight: 900;
}

.field {
  flex: 1;
  min-width: 0;
  height: 72rpx;
  color: #222832;
  font-size: 32rpx;
  text-align: right;
}

.placeholder,
.phone {
  color: #9ba1aa;
}

.phone {
  flex: 1;
  font-size: 32rpx;
  text-align: right;
}

.confirm {
  position: fixed;
  right: 74rpx;
  bottom: 78rpx;
  left: 74rpx;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 999rpx;
  background: #ef4f5f;
  color: #fff;
  font-size: 34rpx;
  font-weight: 900;
  box-shadow: 0 14rpx 26rpx rgba(239, 79, 95, 0.22);
}

.confirm::after {
  border: 0;
}
</style>
