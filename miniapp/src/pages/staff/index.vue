<template>
  <view class="page">
    <view class="search-card">
      <view class="search">
        <input v-model="query.keyword" placeholder="请输入姓名或服务说明" confirm-type="search" @confirm="reload" />
        <button @tap="reload">搜索</button>
      </view>
    </view>

    <scroll-view class="category-scroll" scroll-x>
      <view class="category-row">
        <view :class="['category', !query.categoryId ? 'active' : '']" @tap="selectCategory(undefined)">全部</view>
        <view
          v-for="item in categories"
          :key="item.id"
          :class="['category', query.categoryId === item.id ? 'active' : '']"
          @tap="selectCategory(item.id)"
        >
          {{ item.name }}
        </view>
      </view>
    </scroll-view>

    <view class="filters">
      <picker :range="ageOptions" range-key="label" @change="changeAge">
        <text>{{ currentAgeLabel }}</text>
      </picker>
      <picker :range="educationOptions" range-key="label" @change="changeEducation">
        <text>{{ currentEducationLabel }}</text>
      </picker>
      <picker :range="salaryOptions" range-key="label" @change="changeSalary">
        <text>{{ currentSalaryLabel }}</text>
      </picker>
      <input v-model="query.district" class="region" placeholder="地区" confirm-type="search" @confirm="reload" />
    </view>

    <view v-if="staff.length === 0" class="empty">暂无符合条件的服务人员</view>
    <view v-for="item in staff" :key="item.id" class="card" @tap="openDetail(item.id)">
      <image v-if="item.avatarUrl" class="avatar" :src="item.avatarUrl" mode="aspectFill" />
      <view v-else class="avatar placeholder">{{ item.name.slice(0, 1) }}</view>
      <view class="info">
        <view class="row">
          <text class="name">{{ item.name }}</text>
          <text class="salary">¥{{ money(item.salaryMin) }}-{{ money(item.salaryMax) }}/{{ unitText(item.salaryUnit) }}</text>
        </view>
        <view class="meta-row">
          <text>{{ item.city || '-' }} {{ item.district || '' }}</text>
          <text>{{ item.age || '-' }}岁</text>
          <text>{{ item.experienceYears || 0 }}年经验</text>
        </view>
        <text class="desc">{{ item.serviceDesc || '暂无服务说明' }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getCategories, getStaffList } from '@/services/api'

const ageOptions = [
  { label: '年龄', min: undefined, max: undefined },
  { label: '30岁以下', min: undefined, max: 29 },
  { label: '30-39岁', min: 30, max: 39 },
  { label: '40-49岁', min: 40, max: 49 },
  { label: '50岁以上', min: 50, max: undefined },
]
const educationOptions = [
  { label: '学历', value: '' },
  { label: '高中', value: '高中' },
  { label: '大专', value: '大专' },
  { label: '本科', value: '本科' },
]
const salaryOptions = [
  { label: '工资', min: undefined, max: undefined },
  { label: '6000以下', min: undefined, max: 5999 },
  { label: '6000-9000', min: 6000, max: 9000 },
  { label: '9000以上', min: 9000, max: undefined },
]

export default {
  data() {
    return {
      categories: [],
      staff: [],
      query: {
        pageNum: 1,
        pageSize: 20,
        categoryId: undefined,
        keyword: '',
        district: '',
        education: '',
        ageMin: undefined,
        ageMax: undefined,
        salaryMin: undefined,
        salaryMax: undefined,
      },
      ageOptions,
      educationOptions,
      salaryOptions,
      currentAgeLabel: '年龄',
      currentEducationLabel: '学历',
      currentSalaryLabel: '工资',
    }
  },
  onLoad() {
    this.loadCategories()
  },
  onShow() {
    const categoryId = uni.getStorageSync('staffCategoryId')
    if (categoryId) {
      this.query.categoryId = Number(categoryId)
      uni.removeStorageSync('staffCategoryId')
    }
    this.reload()
  },
  methods: {
    async loadCategories() {
      const res = await getCategories()
      this.categories = res.data || []
    },
    async reload() {
      const res = await getStaffList(this.query)
      this.staff = res.rows || []
    },
    selectCategory(id) {
      this.query.categoryId = id
      this.reload()
    },
    changeAge(event) {
      const option = this.ageOptions[event.detail.value]
      this.currentAgeLabel = option.label
      this.query.ageMin = option.min
      this.query.ageMax = option.max
      this.reload()
    },
    changeEducation(event) {
      const option = this.educationOptions[event.detail.value]
      this.currentEducationLabel = option.label
      this.query.education = option.value
      this.reload()
    },
    changeSalary(event) {
      const option = this.salaryOptions[event.detail.value]
      this.currentSalaryLabel = option.label
      this.query.salaryMin = option.min
      this.query.salaryMax = option.max
      this.reload()
    },
    openDetail(id) {
      uni.navigateTo({ url: `/pages/staff/detail?id=${id}` })
    },
    money(value) {
      if (value === null || value === undefined || value === '') return '-'
      return Number(value).toString().replace(/\.0+$/, '')
    },
    unitText(value) {
      return { month: '月', MONTH: '月', day: '天', DAY: '天', time: '次', TIME: '次', hour: '小时', HOUR: '小时' }[value] || value || '月'
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  background: #f4f5f2;
  padding: 22rpx 24rpx 34rpx;
}

.search-card {
  padding: 16rpx;
  border: 1px solid rgba(31, 37, 43, 0.05);
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 16rpx 38rpx rgba(32, 38, 44, 0.06);
}

.search {
  display: flex;
  gap: 12rpx;
  align-items: center;
}

.search input {
  flex: 1;
  height: 76rpx;
  padding: 0 26rpx;
  border-radius: 20rpx;
  background: #f6f7f5;
  color: #1f252b;
  font-size: 26rpx;
}

.search button {
  width: 124rpx;
  height: 76rpx;
  line-height: 76rpx;
  border-radius: 20rpx;
  background: #e84d64;
  color: #fff;
  font-size: 26rpx;
  box-shadow: 0 12rpx 24rpx rgba(232, 77, 100, 0.22);
}

.category-scroll {
  margin: 24rpx -24rpx 14rpx;
  padding-left: 24rpx;
  white-space: nowrap;
}

.category-row {
  display: flex;
  gap: 14rpx;
  padding-right: 24rpx;
}

.category {
  flex: 0 0 auto;
  min-width: 96rpx;
  padding: 16rpx 24rpx;
  border: 1px solid #edf0ec;
  border-radius: 18rpx;
  background: #fff;
  color: #68717a;
  font-size: 24rpx;
  text-align: center;
}

.category.active {
  border-color: #1f252b;
  background: #1f252b;
  color: #fff;
}

.filters {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
  margin-bottom: 24rpx;
}

.filters text,
.region {
  display: block;
  height: 68rpx;
  line-height: 68rpx;
  border: 1px solid #ecefed;
  border-radius: 18rpx;
  background: #fff;
  color: #59636d;
  text-align: center;
  font-size: 24rpx;
}

.empty {
  margin-top: 28rpx;
  padding: 96rpx 0;
  border-radius: 24rpx;
  background: #fff;
  color: #929aa3;
  text-align: center;
  font-size: 26rpx;
}

.card {
  display: flex;
  gap: 22rpx;
  margin-bottom: 18rpx;
  padding: 22rpx;
  border: 1px solid rgba(31, 37, 43, 0.05);
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 30rpx rgba(32, 38, 44, 0.05);
}

.avatar {
  flex: 0 0 auto;
  width: 128rpx;
  height: 128rpx;
  border-radius: 20rpx;
  background: #f4c8d0;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d93f58;
  font-size: 42rpx;
  font-weight: 700;
}

.info {
  flex: 1;
  min-width: 0;
}

.row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14rpx;
}

.name {
  flex: 1;
  min-width: 0;
  color: #20242c;
  font-size: 31rpx;
  font-weight: 700;
  line-height: 1.25;
}

.salary {
  flex: 0 0 auto;
  max-width: 236rpx;
  color: #e84d64;
  font-size: 23rpx;
  font-weight: 700;
  line-height: 1.3;
  text-align: right;
  word-break: break-all;
}

.meta-row,
.desc {
  display: block;
  margin-top: 12rpx;
  color: #68717a;
  font-size: 24rpx;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx 12rpx;
}

.meta-row text {
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  background: #f3f4f1;
  color: #68717a;
  font-size: 22rpx;
}

.desc {
  display: -webkit-box;
  overflow: hidden;
  line-height: 1.55;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
</style>
