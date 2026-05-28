<template>
  <view class="page">
    <view class="search">
      <input v-model="query.keyword" placeholder="请输入姓名或服务说明" confirm-type="search" @confirm="reload" />
      <button @tap="reload">搜索</button>
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
          <text class="salary">¥{{ item.salaryMin || '-' }}-{{ item.salaryMax || '-' }}/{{ item.salaryUnit || '月' }}</text>
        </view>
        <text class="meta">{{ item.city || '-' }} {{ item.district || '' }} | {{ item.age || '-' }}岁 | {{ item.experienceYears || 0 }}年经验</text>
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
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  background: #f6f7f9;
  padding: 24rpx;
}

.search {
  display: flex;
  gap: 16rpx;
}

.search input {
  flex: 1;
  height: 72rpx;
  padding: 0 24rpx;
  border-radius: 999rpx;
  background: #ffffff;
  font-size: 26rpx;
}

.search button {
  width: 132rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 999rpx;
  background: #ef3f5f;
  color: #fff;
  font-size: 26rpx;
}

.category-scroll {
  margin: 24rpx 0 12rpx;
  white-space: nowrap;
}

.category-row {
  display: flex;
  gap: 16rpx;
}

.category {
  padding: 14rpx 24rpx;
  border-radius: 999rpx;
  background: #fff;
  color: #525a66;
  font-size: 24rpx;
}

.category.active {
  background: #20242c;
  color: #fff;
}

.filters {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
  margin-bottom: 22rpx;
}

.filters text,
.region {
  display: block;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 12rpx;
  background: #fff;
  color: #3b414c;
  text-align: center;
  font-size: 24rpx;
}

.empty {
  padding: 80rpx 0;
  color: #8a8f99;
  text-align: center;
  font-size: 26rpx;
}

.card {
  display: flex;
  gap: 24rpx;
  margin-bottom: 20rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  background: #ffffff;
}

.avatar {
  width: 132rpx;
  height: 132rpx;
  border-radius: 12rpx;
  background: #f5c7d1;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ef3f5f;
  font-size: 40rpx;
  font-weight: 700;
}

.info {
  flex: 1;
  min-width: 0;
}

.row {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.name {
  color: #20242c;
  font-size: 32rpx;
  font-weight: 700;
}

.salary {
  color: #ef3f5f;
  font-size: 24rpx;
  font-weight: 700;
}

.meta,
.desc {
  display: block;
  margin-top: 12rpx;
  color: #6d7480;
  font-size: 24rpx;
}
</style>
