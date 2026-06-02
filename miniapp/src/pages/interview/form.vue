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
      <view class="field">
        <text class="label">期望面试时间</text>
        <input v-model="form.preferredTime" placeholder="例如：本周六上午" />
      </view>
      <view class="field">
        <text class="label">面试方式</text>
        <view class="segmented">
          <text
            v-for="item in interviewForms"
            :key="item"
            :class="{ active: form.interviewForm === item }"
            @tap="form.interviewForm = item"
          >
            {{ item }}
          </text>
        </view>
      </view>
      <view class="field">
        <text class="label">面试地址</text>
        <input v-model="form.address" placeholder="线下面试可填写大致地址" />
      </view>
      <view class="field">
        <text class="label">服务重点</text>
        <textarea v-model="form.workContent" maxlength="200" placeholder="简单写下希望重点沟通的服务内容" />
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
      interviewForms: ['电话沟通', '视频面试', '线下面试'],
      form: {
        contactName: '',
        contactPhone: '',
        preferredTime: '',
        interviewForm: '电话沟通',
        address: '',
        workContent: '',
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
          preferredTime: this.form.preferredTime,
          interviewForm: this.form.interviewForm,
          address: this.form.address,
          workContent: this.form.workContent,
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
  background: #f7f4ef;
}

.panel {
  margin-bottom: 24rpx;
  padding: 30rpx;
  border: 1px solid rgba(31, 37, 43, 0.05);
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 12rpx 30rpx rgba(32, 38, 44, 0.05);
}

.title {
  display: block;
  color: #222832;
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
  color: #222832;
  font-size: 28rpx;
}

input {
  height: 72rpx;
  padding: 0 20rpx;
  border-radius: 18rpx;
  background: #f7f8f5;
  font-size: 28rpx;
}

textarea {
  width: 100%;
  min-height: 150rpx;
  box-sizing: border-box;
  padding: 18rpx 20rpx;
  border-radius: 18rpx;
  background: #f7f8f5;
  font-size: 28rpx;
  line-height: 1.5;
}

.segmented {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
}

.segmented text {
  height: 68rpx;
  line-height: 68rpx;
  border-radius: 18rpx;
  background: #f7f8f5;
  color: #68717a;
  font-size: 25rpx;
  text-align: center;
}

.segmented .active {
  background: #4c3b37;
  color: #fff;
}

.submit {
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 22rpx;
  background: #ef4f5f;
  color: #fff;
  font-size: 30rpx;
  box-shadow: 0 12rpx 26rpx rgba(232, 77, 100, 0.18);
}
</style>
