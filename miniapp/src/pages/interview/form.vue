<template>
  <view class="page">
    <view v-if="staff" class="panel">
      <text class="title">预约 {{ staff.name }}</text>
      <text class="meta">{{ staff.categoryName }} · {{ staff.city || '-' }} {{ staff.district || '' }}</text>
    </view>

    <view class="panel">
      <view class="field">
        <text class="label">联系人</text>
        <input v-model="form.contactName" placeholder="请输入联系人" />
      </view>
      <view class="field">
        <text class="label">联系电话</text>
        <input v-model="form.contactPhone" type="number" placeholder="请输入联系电话" />
      </view>
      <view class="tip">提交后平台工作人员会线下联系你确认面试安排。</view>
    </view>

    <button class="submit" :loading="submitting" @tap="submit">提交预约</button>
  </view>
</template>

<script>
import { createInterview, getStaffDetail } from '@/services/api'
import { ensureLogin } from '@/services/request'

export default {
  data() {
    return {
      staffId: undefined,
      demandId: undefined,
      staff: null,
      submitting: false,
      form: {
        contactName: '',
        contactPhone: '',
      },
    }
  },
  async onLoad(options) {
    this.staffId = options.staffId
    this.demandId = options.demandId
    await ensureLogin()
    this.loadStaff()
  },
  methods: {
    async loadStaff() {
      const res = await getStaffDetail(this.staffId)
      this.staff = res.data
    },
    async submit() {
      if (!this.form.contactName || !this.form.contactPhone) {
        uni.showToast({ title: '请填写联系人和电话', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        const payload = {
          staffId: Number(this.staffId),
          contactName: this.form.contactName,
          contactPhone: this.form.contactPhone,
        }
        if (this.demandId) {
          payload.demandId = Number(this.demandId)
        }
        const res = await createInterview(payload)
        uni.showToast({ title: '已提交预约', icon: 'success' })
        setTimeout(() => {
          uni.redirectTo({ url: `/pages/interview/detail?id=${res.data}` })
        }, 500)
      } finally {
        this.submitting = false
      }
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
  margin-bottom: 24rpx;
  padding: 30rpx;
  border-radius: 18rpx;
  background: #fff;
}

.title {
  display: block;
  color: #20242c;
  font-size: 34rpx;
  font-weight: 700;
}

.meta,
.tip {
  display: block;
  margin-top: 12rpx;
  color: #6d7480;
  font-size: 26rpx;
  line-height: 1.7;
}

.field {
  padding: 18rpx 0;
  border-bottom: 1px solid #edf0f3;
}

.label {
  display: block;
  margin-bottom: 12rpx;
  color: #20242c;
  font-size: 28rpx;
}

input {
  height: 72rpx;
  font-size: 28rpx;
}

.submit {
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 16rpx;
  background: #ef3f5f;
  color: #fff;
  font-size: 30rpx;
}
</style>
