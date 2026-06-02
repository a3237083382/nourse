<template>
  <view class="page">
    <view class="hero">
      <view class="hero-copy">
        <text class="hero-question">#你想找什么样的阿姨?</text>
        <text class="hero-title">发布需求</text>
        <text class="hero-pill">阿姨主动找上门</text>
      </view>
      <view class="hero-art">
        <view class="nanny-head"></view>
        <view class="nanny-body"></view>
        <view class="baby-head"></view>
        <view class="baby-body"></view>
      </view>
    </view>

    <view class="card title-card">
      <view class="line-field">
        <text class="field-label required">标题：</text>
        <input v-model.trim="form.title" class="line-input" placeholder="请输入" placeholder-class="placeholder" />
      </view>
    </view>

    <view class="card service-card">
      <text class="block-label required">服务类型：</text>
      <view class="service-grid">
        <view
          v-for="item in categories"
          :key="item.id"
          class="service-option"
          :class="{ active: form.categoryId === item.id }"
          @tap="selectCategory(item)"
        >
          {{ item.name }}
        </view>
      </view>

      <view v-if="selectedCategoryName === '月嫂'" class="period-panel">
        <checkbox-group class="period-group" @change="onMaternityChange">
          <label v-for="item in maternityPeriods" :key="item.value" class="period-option">
            <checkbox :value="item.value" :checked="form.maternityPeriod === item.value" color="#ef4f5f" />
            <text>{{ item.label }}</text>
          </label>
        </checkbox-group>
      </view>

      <view v-else-if="serviceOptions.length" class="period-panel">
        <checkbox-group class="period-group" @change="onServiceOptionChange">
          <label v-for="item in serviceOptions" :key="item" class="period-option">
            <checkbox :value="item" :checked="form.serviceOptions.includes(item)" color="#ef4f5f" />
            <text>{{ item }}</text>
          </label>
        </checkbox-group>
      </view>
    </view>

    <view class="card">
      <view class="line-field">
        <text class="field-label required">您的姓名：</text>
        <input v-model.trim="form.contactName" class="line-input" placeholder="请输入" placeholder-class="placeholder" />
      </view>
      <view class="line-field">
        <text class="field-label required">您的电话：</text>
        <input v-model.trim="form.contactPhone" class="line-input" type="number" placeholder="请输入" placeholder-class="placeholder" />
      </view>
      <picker :range="genderOptions" @change="onGenderChange">
        <view class="line-field">
          <text class="field-label required">性别：</text>
          <text class="picker-value" :class="{ muted: !form.gender }">{{ form.gender || '请选择' }}</text>
        </view>
      </picker>
      <picker :range="liveInOptions" @change="onLiveInChange">
        <view class="line-field">
          <text class="field-label required">是否住家：</text>
          <text class="picker-value" :class="{ muted: form.liveIn === null }">{{ liveInText }}</text>
        </view>
      </picker>
      <picker :range="salaryOptions" @change="onSalaryChange">
        <view class="line-field">
          <text class="field-label required">薪资待遇（月/¥）：</text>
          <text class="picker-value" :class="{ muted: !form.expectedSalary }">{{ form.expectedSalary || '请选择' }}</text>
        </view>
      </picker>
      <picker :range="regionOptions" range-key="label" @change="onRegionChange">
        <view class="line-field">
          <text class="field-label required">所在区域：</text>
          <text class="picker-value" :class="{ muted: !form.district }">{{ regionText || '请选择' }}</text>
        </view>
      </picker>
      <view class="line-field">
        <text class="field-label required">详细地址：</text>
        <input v-model.trim="form.address" class="line-input" placeholder="如岳麓区学士街道三月小区" placeholder-class="placeholder" />
      </view>
      <view class="date-block">
        <text class="field-label required">工作日期：</text>
        <view class="date-row">
          <picker class="date-picker" mode="date" @change="onStartDateChange">
            <view class="date-box">
              <text class="date-icon">□</text>
              <text :class="{ muted: !form.startDate }">{{ form.startDate || '开始日期' }}</text>
            </view>
          </picker>
          <text class="date-separator">-</text>
          <picker class="date-picker" mode="date" @change="onEndDateChange">
            <view class="date-box">
              <text class="date-icon">□</text>
              <text :class="{ muted: !form.endDate }">{{ form.endDate || '结束日期' }}</text>
            </view>
          </picker>
        </view>
      </view>
    </view>

    <view class="card remark-card">
      <view class="remark-title">
        <text>备注</text>
        <text class="edit-mark">↗</text>
      </view>
      <textarea class="remark-textarea" v-model.trim="form.remark" placeholder="请输入" placeholder-class="placeholder" />
    </view>

    <button class="submit" :loading="submitting" @tap="submit">发布</button>
  </view>
</template>

<script>
import { createDemand, getCategories } from '@/services/api'
import { ensureLogin } from '@/services/request'

const genderOptions = ['男', '女']
const liveInOptions = ['住家', '不住家']
const salaryOptions = ['6000-8000/月', '8000-10000/月', '10000-15000/月', '15000以上/月', '面议']
const regionOptions = [
  { label: '杭州 西湖区', city: '杭州', district: '西湖区' },
  { label: '杭州 拱墅区', city: '杭州', district: '拱墅区' },
  { label: '杭州 上城区', city: '杭州', district: '上城区' },
  { label: '杭州 滨江区', city: '杭州', district: '滨江区' },
  { label: '长沙 岳麓区', city: '长沙', district: '岳麓区' },
]
const careOptions = ['做饭做家务', '照顾小孩（3岁以下）', '照顾小孩（3岁以上）', '照顾能自理老人', '照顾半自理老人', '照顾不能自理老人']
const serviceOptionsMap = {
  保姆: careOptions,
  钟点工: careOptions,
}

export default {
  data() {
    return {
      categories: [],
      maternityPeriods: [
        { label: '26天（单月子周期）', value: '26天' },
        { label: '42天（完整产褥期）', value: '42天' },
        { label: '52天（双月子周期）', value: '52天' },
        { label: '78天（大圆满）', value: '78天' },
      ],
      genderOptions,
      liveInOptions,
      salaryOptions,
      regionOptions,
      selectedCategoryName: '',
      submitting: false,
      loaded: false,
      initialCategoryId: undefined,
      form: {
        title: '',
        categoryId: undefined,
        maternityPeriod: '',
        serviceOptions: [],
        contactName: '',
        contactPhone: '',
        gender: '',
        liveIn: null,
        expectedSalary: '',
        city: '',
        district: '',
        address: '',
        startDate: '',
        endDate: '',
        remark: '',
      },
    }
  },
  computed: {
    liveInText() {
      if (this.form.liveIn === true) return '住家'
      if (this.form.liveIn === false) return '不住家'
      return '请选择'
    },
    regionText() {
      return [this.form.city, this.form.district].filter(Boolean).join(' ')
    },
    serviceOptions() {
      return serviceOptionsMap[this.selectedCategoryName] || []
    },
  },
  onLoad(options = {}) {
    this.initialCategoryId = options.categoryId ? Number(options.categoryId) : undefined
    this.init()
  },
  mounted() {
    this.init()
  },
  methods: {
    async init() {
      if (this.loaded) return
      this.loaded = true
      try {
        const res = await getCategories()
        this.categories = res.data || []
        const initialCategory = this.categories.find((item) => item.id === this.initialCategoryId)
        if (initialCategory || this.categories.length) {
          this.selectCategory(initialCategory || this.categories[0])
        }
      } catch (error) {
        this.loaded = false
        uni.showToast({ title: '服务分类加载失败', icon: 'none' })
      }
    },
    selectCategory(item) {
      this.form.categoryId = item.id
      this.selectedCategoryName = item.name
      if (item.name !== '月嫂') {
        this.form.maternityPeriod = ''
      }
      this.form.serviceOptions = []
    },
    onMaternityChange(event) {
      const checked = event.detail.value || []
      this.form.maternityPeriod = checked[checked.length - 1] || ''
    },
    onServiceOptionChange(event) {
      this.form.serviceOptions = event.detail.value || []
    },
    onGenderChange(event) {
      this.form.gender = this.genderOptions[event.detail.value]
    },
    onLiveInChange(event) {
      this.form.liveIn = this.liveInOptions[event.detail.value] === '住家'
    },
    onSalaryChange(event) {
      this.form.expectedSalary = this.salaryOptions[event.detail.value]
    },
    onRegionChange(event) {
      const item = this.regionOptions[event.detail.value]
      this.form.city = item.city
      this.form.district = item.district
    },
    onStartDateChange(event) {
      this.form.startDate = event.detail.value
    },
    onEndDateChange(event) {
      this.form.endDate = event.detail.value
    },
    validate() {
      const required = [
        this.form.title,
        this.form.categoryId,
        this.form.contactName,
        this.form.contactPhone,
        this.form.gender,
        this.form.liveIn !== null ? 'liveIn' : '',
        this.form.expectedSalary,
        this.form.district,
        this.form.address,
        this.form.startDate,
        this.form.endDate,
      ]
      if (required.some((item) => item === undefined || item === null || item === '')) {
        uni.showToast({ title: '请填写完整需求信息', icon: 'none' })
        return false
      }
      if (this.selectedCategoryName === '月嫂' && !this.form.maternityPeriod) {
        uni.showToast({ title: '请选择月嫂周期', icon: 'none' })
        return false
      }
      return true
    },
    buildRemark() {
      const dates = `工作日期：${this.form.startDate} 至 ${this.form.endDate}`
      const options = this.form.serviceOptions.length ? `服务选择：${this.form.serviceOptions.join('、')}` : ''
      return [dates, options, this.form.remark].filter(Boolean).join('\n')
    },
    async submit() {
      if (!this.validate() || this.submitting) return
      this.submitting = true
      try {
        await ensureLogin()
        const res = await createDemand({
          title: this.form.title,
          categoryId: this.form.categoryId,
          maternityPeriod: this.form.maternityPeriod,
          contactName: this.form.contactName,
          contactPhone: this.form.contactPhone,
          gender: this.form.gender,
          liveIn: this.form.liveIn,
          expectedSalary: this.form.expectedSalary,
          city: this.form.city,
          district: this.form.district,
          address: this.form.address,
          remark: this.buildRemark(),
        })
        uni.showToast({ title: '发布成功', icon: 'success' })
        setTimeout(() => {
          uni.navigateTo({ url: `/pages/demand/detail?id=${res.data}` })
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
  padding: 0 24rpx 156rpx;
  background: linear-gradient(180deg, #fff1ed 0, #fff8f4 330rpx, #f7f4ef 760rpx);
}

.hero {
  position: relative;
  height: 320rpx;
  margin: 0 -24rpx 24rpx;
  overflow: hidden;
  background: linear-gradient(105deg, #fff1ee 0%, #fff8f4 58%, #fff7dc 100%);
}

.hero::before {
  position: absolute;
  top: -100rpx;
  left: 160rpx;
  width: 260rpx;
  height: 420rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  content: '';
  transform: rotate(-18deg);
}

.hero-copy {
  position: relative;
  z-index: 2;
  padding: 72rpx 0 0 54rpx;
}

.hero-question {
  display: block;
  color: #ef4f5f;
  font-size: 38rpx;
  font-weight: 900;
}

.hero-title {
  display: block;
  margin-top: 12rpx;
  color: #222832;
  font-size: 62rpx;
  font-weight: 900;
  letter-spacing: 0;
}

.hero-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 220rpx;
  height: 72rpx;
  margin-top: 18rpx;
  padding: 0 28rpx;
  border-radius: 16rpx;
  background: #fff;
  color: #ef4f5f;
  font-size: 30rpx;
  font-weight: 800;
  box-shadow: 0 12rpx 24rpx rgba(239, 79, 95, 0.12);
}

.hero-art {
  position: absolute;
  right: 24rpx;
  bottom: 0;
  width: 280rpx;
  height: 280rpx;
}

.nanny-head,
.baby-head,
.nanny-body,
.baby-body {
  position: absolute;
}

.nanny-head {
  top: 18rpx;
  right: 58rpx;
  width: 78rpx;
  height: 78rpx;
  border-radius: 50%;
  background: #ffd1bc;
  box-shadow: inset 0 12rpx 0 #9e4a30;
}

.nanny-body {
  right: 24rpx;
  bottom: 22rpx;
  width: 138rpx;
  height: 178rpx;
  border-radius: 70rpx 70rpx 26rpx 26rpx;
  background: linear-gradient(180deg, #ef8f78, #ef4f5f);
}

.baby-head {
  left: 36rpx;
  bottom: 94rpx;
  width: 62rpx;
  height: 62rpx;
  border-radius: 50%;
  background: #ffd5bd;
}

.baby-body {
  left: 22rpx;
  bottom: 34rpx;
  width: 132rpx;
  height: 72rpx;
  border-radius: 36rpx;
  background: #ffe6c2;
  box-shadow: inset 0 -10rpx 0 #f2c987;
}

.card {
  margin-top: 22rpx;
  padding: 0 30rpx;
  border: 1rpx solid #f3e5dc;
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 12rpx 28rpx rgba(80, 45, 40, 0.05);
}

.title-card {
  margin-top: -10rpx;
}

.line-field {
  display: flex;
  min-height: 96rpx;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  border-bottom: 1rpx solid #ecefed;
}

.line-field:last-child {
  border-bottom: 0;
}

.field-label,
.block-label {
  color: #222832;
  font-size: 29rpx;
  font-weight: 600;
}

.required::before {
  color: #ef4f5f;
  content: '*';
}

.line-input {
  flex: 1;
  height: 92rpx;
  color: #1f252b;
  font-size: 29rpx;
  text-align: right;
}

.placeholder,
.muted {
  color: #9aa1aa;
}

.picker-value {
  flex: 1;
  color: #1f252b;
  font-size: 29rpx;
  text-align: right;
}

.service-card {
  padding-top: 28rpx;
  padding-bottom: 24rpx;
}

.block-label {
  display: block;
  margin-bottom: 20rpx;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18rpx;
}

.service-option {
  height: 70rpx;
  line-height: 70rpx;
  border: 1rpx solid #eadbd2;
  border-radius: 12rpx;
  color: #6c5f5b;
  font-size: 28rpx;
  text-align: center;
  background: #fff;
}

.service-option.active {
  border-color: #ef4f5f;
  color: #ef4f5f;
  background: #fff1ee;
}

.period-panel {
  margin-top: 28rpx;
  padding-top: 24rpx;
  border-top: 1rpx dashed #d9dde2;
}

.period-group {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18rpx 28rpx;
}

.period-option {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #4a5360;
  font-size: 25rpx;
}

.date-block {
  padding: 24rpx 0 28rpx;
}

.date-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 20rpx;
}

.date-picker {
  flex: 1;
}

.date-box {
  display: flex;
  height: 68rpx;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  border: 1rpx solid #eadbd2;
  border-radius: 12rpx;
  color: #222832;
  font-size: 27rpx;
  background: #fff;
}

.date-icon {
  color: #c8cdd2;
  font-size: 28rpx;
}

.date-separator {
  color: #8a8f99;
  font-size: 28rpx;
}

.remark-card {
  padding-top: 26rpx;
  padding-bottom: 26rpx;
}

.remark-title {
  display: flex;
  align-items: center;
  gap: 10rpx;
  color: #1f252b;
  font-size: 30rpx;
  font-weight: 600;
}

.edit-mark {
  color: #68717a;
  font-size: 26rpx;
}

.remark-textarea {
  width: 100%;
  height: 128rpx;
  margin-top: 20rpx;
  color: #1f252b;
  font-size: 28rpx;
  line-height: 1.5;
}

.submit {
  position: fixed;
  left: 62rpx;
  right: 62rpx;
  bottom: 104rpx;
  z-index: 10;
  height: 78rpx;
  line-height: 78rpx;
  border-radius: 999rpx;
  background: #ef4f5f;
  color: #fff;
  font-size: 31rpx;
  font-weight: 800;
  box-shadow: 0 14rpx 30rpx rgba(239, 79, 95, 0.24);
}
</style>
