<template>
  <view class="page">
    <view class="form">
      <view class="field">
        <text class="label">需求标题</text>
        <input v-model="form.title" placeholder="例如：急需月嫂到家服务" />
      </view>
      <view class="field">
        <text class="label">服务类型</text>
        <picker :range="categories" range-key="name" @change="onCategoryChange">
          <view class="picker">{{ selectedCategoryName || '请选择服务类型' }}</view>
        </picker>
      </view>
      <view v-if="selectedCategoryName === '月嫂'" class="field">
        <text class="label">月嫂周期</text>
        <picker :range="maternityPeriods" @change="onMaternityChange">
          <view class="picker">{{ form.maternityPeriod || '请选择周期' }}</view>
        </picker>
      </view>
      <view class="field">
        <text class="label">联系人</text>
        <input v-model="form.contactName" placeholder="请输入联系人姓名" />
      </view>
      <view class="field">
        <text class="label">联系电话</text>
        <input v-model="form.contactPhone" type="number" placeholder="请输入联系电话" />
      </view>
      <view class="field">
        <text class="label">是否住家</text>
        <switch :checked="form.liveIn" @change="form.liveIn = $event.detail.value" />
      </view>
      <view class="field">
        <text class="label">薪资待遇</text>
        <input v-model="form.expectedSalary" placeholder="例如：8000-10000/月" />
      </view>
      <view class="field">
        <text class="label">城市</text>
        <input v-model="form.city" placeholder="请输入城市" />
      </view>
      <view class="field">
        <text class="label">区域</text>
        <input v-model="form.district" placeholder="请输入区域" />
      </view>
      <view class="field">
        <text class="label">详细地址</text>
        <input v-model="form.address" placeholder="请输入详细地址" />
      </view>
      <view class="field">
        <text class="label">补充说明</text>
        <textarea v-model="form.remark" placeholder="可填写服务时间、家庭情况等" />
      </view>
    </view>
    <button class="submit" :loading="submitting" @tap="submit">提交需求</button>
  </view>
</template>

<script>
import { createDemand, getCategories } from '@/services/api'
import { ensureLogin } from '@/services/request'

export default {
  data() {
    return {
      categories: [],
      maternityPeriods: ['26天', '42天', '52天', '78天'],
      selectedCategoryName: '',
      submitting: false,
      form: {
        title: '',
        categoryId: undefined,
        maternityPeriod: '',
        contactName: '',
        contactPhone: '',
        gender: '',
        liveIn: false,
        expectedSalary: '',
        city: '',
        district: '',
        address: '',
        remark: '',
      },
    }
  },
  async onLoad() {
    await ensureLogin()
    const res = await getCategories()
    this.categories = res.data || []
  },
  methods: {
    onCategoryChange(event) {
      const item = this.categories[event.detail.value]
      this.form.categoryId = item.id
      this.selectedCategoryName = item.name
      if (item.name !== '月嫂') {
        this.form.maternityPeriod = ''
      }
    },
    onMaternityChange(event) {
      this.form.maternityPeriod = this.maternityPeriods[event.detail.value]
    },
    validate() {
      if (!this.form.title || !this.form.categoryId || !this.form.contactName || !this.form.contactPhone || !this.form.address) {
        uni.showToast({ title: '请填写必填信息', icon: 'none' })
        return false
      }
      return true
    },
    async submit() {
      if (!this.validate() || this.submitting) return
      this.submitting = true
      try {
        const res = await createDemand(this.form)
        uni.showToast({ title: '提交成功', icon: 'success' })
        setTimeout(() => {
          uni.redirectTo({ url: `/pages/demand/detail?id=${res.data}` })
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
  background: #f4f5f2;
}

.form {
  padding: 28rpx;
  border: 1px solid rgba(31, 37, 43, 0.05);
  border-radius: 26rpx;
  background: #fff;
  box-shadow: 0 12rpx 30rpx rgba(32, 38, 44, 0.05);
}

.field {
  padding: 20rpx 0;
  border-bottom: 1px solid #edf0f3;
}

.field:last-child {
  border-bottom: 0;
}

.label {
  display: block;
  margin-bottom: 14rpx;
  color: #20242c;
  font-size: 28rpx;
  font-weight: 600;
}

input,
textarea,
.picker {
  width: 100%;
  min-height: 70rpx;
  padding: 0 20rpx;
  border-radius: 18rpx;
  background: #f7f8f5;
  color: #3b414c;
  font-size: 28rpx;
}

textarea {
  height: 160rpx;
}

.picker {
  display: flex;
  align-items: center;
  color: #6d7480;
}

.submit {
  margin-top: 28rpx;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 22rpx;
  background: #e84d64;
  color: #fff;
  font-size: 30rpx;
  box-shadow: 0 12rpx 26rpx rgba(232, 77, 100, 0.18);
}
</style>
