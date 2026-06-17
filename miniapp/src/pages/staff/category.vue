<template>
  <view class="page">
    <view class="search-bar">
      <view class="search-box">
        <text class="search-icon">⌕</text>
        <input
          v-model.trim="keyword"
          class="search-input"
          placeholder="请输入阿姨姓名"
          placeholder-class="placeholder"
          confirm-type="search"
          @confirm="applySearch"
        />
      </view>
      <button class="search-btn" @tap="applySearch">搜索</button>
    </view>

    <view class="filter-row">
      <picker :range="ageOptions" range-key="label" @change="onAgeChange">
        <view class="filter-item">
          <text>{{ ageOptions[ageIndex].label }}</text>
          <text class="chevron">⌄</text>
        </view>
      </picker>
      <picker :range="educationOptions" range-key="label" @change="onEducationChange">
        <view class="filter-item">
          <text>{{ educationOptions[educationIndex].label }}</text>
          <text class="chevron">⌄</text>
        </view>
      </picker>
      <picker :range="salaryOptions" range-key="label" @change="onSalaryChange">
        <view class="filter-item">
          <text>{{ salaryOptions[salaryIndex].label }}</text>
          <text class="chevron">⌄</text>
        </view>
      </picker>
    </view>

    <view class="list">
      <view v-if="loading && !staffList.length" class="empty">加载中...</view>
      <view v-else-if="!staffList.length" class="empty">当前分类暂无服务人员</view>

      <view v-for="item in staffList" :key="item.id" class="staff-card" @tap="openDetail(item.id)">
        <view class="avatar">
          <image v-if="validImage(item.avatarUrl)" :src="item.avatarUrl" mode="aspectFill" />
          <text v-else>{{ firstChar(item.name) }}</text>
        </view>
        <view class="info">
          <view class="head">
            <text class="name">{{ item.name }}</text>
            <text class="price">{{ salaryText(item) }}</text>
          </view>
          <text class="region">{{ regionText(item) }}</text>
          <view class="meta">
            <text v-if="item.age">{{ item.age }}岁</text>
            <text v-if="item.experienceYears">{{ item.experienceYears }}年经验</text>
            <text v-if="item.categoryName">{{ item.categoryName }}</text>
            <text v-if="item.education">{{ item.education }}</text>
          </view>
          <view v-if="staffTags(item).length" class="tag-row">
            <text v-for="tag in staffTags(item)" :key="tag" class="tag">{{ tag }}</text>
          </view>
        </view>
      </view>

      <view v-if="loading && staffList.length" class="load-tip">加载中...</view>
      <view v-else-if="noMore && staffList.length" class="load-tip">没有更多了</view>
    </view>
  </view>
</template>

<script>
import { getStaffList } from '@/services/api'

const ageOptions = [
  { label: '年龄' },
  { label: '30岁以下', ageMax: 29 },
  { label: '30-39岁', ageMin: 30, ageMax: 39 },
  { label: '40-49岁', ageMin: 40, ageMax: 49 },
  { label: '50岁以上', ageMin: 50 },
]

const educationOptions = [
  { label: '学历' },
  { label: '初中', education: '初中' },
  { label: '高中', education: '高中' },
  { label: '中专', education: '中专' },
  { label: '大专', education: '大专' },
  { label: '本科', education: '本科' },
]

const salaryOptions = [
  { label: '工资' },
  { label: '6000以下', salaryMax: 5999 },
  { label: '6000-9000', salaryMin: 6000, salaryMax: 9000 },
  { label: '9000-12000', salaryMin: 9000, salaryMax: 12000 },
  { label: '12000以上', salaryMin: 12000 },
]

export default {
  data() {
    return {
      categoryId: undefined,
      categoryName: '',
      keyword: '',
      ageOptions,
      educationOptions,
      salaryOptions,
      ageIndex: 0,
      educationIndex: 0,
      salaryIndex: 0,
      staffList: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      noMore: false,
    }
  },
  onLoad(options = {}) {
    this.categoryId = this.parseId(options.categoryId)
    this.categoryName = decodeURIComponent(options.categoryName || '') || '服务人员'
    uni.setNavigationBarTitle({ title: this.categoryName })
    this.reload()
  },
  onReachBottom() {
    this.loadMore()
  },
  methods: {
    applySearch() {
      this.reload()
    },
    onAgeChange(event) {
      this.ageIndex = Number(event.detail.value)
      this.reload()
    },
    onEducationChange(event) {
      this.educationIndex = Number(event.detail.value)
      this.reload()
    },
    onSalaryChange(event) {
      this.salaryIndex = Number(event.detail.value)
      this.reload()
    },
    reload() {
      this.pageNum = 1
      this.staffList = []
      this.total = 0
      this.noMore = false
      this.loadStaff()
    },
    loadMore() {
      if (this.loading || this.noMore) return
      this.pageNum += 1
      this.loadStaff()
    },
    async loadStaff() {
      if (this.loading || this.noMore) return
      this.loading = true
      try {
        const age = this.ageOptions[this.ageIndex]
        const education = this.educationOptions[this.educationIndex]
        const salary = this.salaryOptions[this.salaryIndex]
        const res = await getStaffList({
          categoryId: this.categoryId,
          keyword: this.keyword || undefined,
          ageMin: age.ageMin,
          ageMax: age.ageMax,
          education: education.education,
          salaryMin: salary.salaryMin,
          salaryMax: salary.salaryMax,
          pageNum: this.pageNum,
          pageSize: this.pageSize,
        })
        const rows = res.rows || []
        this.total = res.total || rows.length
        this.staffList = this.pageNum === 1 ? rows : this.staffList.concat(rows)
        this.noMore = rows.length < this.pageSize || this.staffList.length >= this.total
      } catch (error) {
        uni.showToast({ title: error.message || '服务人员加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    openDetail(id) {
      uni.navigateTo({ url: `/pages/staff/detail?id=${id}` })
    },
    parseId(value) {
      const id = Number(value)
      return Number.isFinite(id) && id > 0 ? id : undefined
    },
    validImage(value) {
      const url = String(value || '')
      return !!url && !url.includes('/static/logo.png')
    },
    firstChar(value) {
      return (value || '?').slice(0, 1)
    },
    money(value) {
      return Number(value || 0).toFixed(2).replace(/\.00$/, '')
    },
    salaryText(item) {
      const min = item.salaryMin || item.priceMin
      const max = item.salaryMax || item.priceMax
      const unit = item.salaryUnit || item.priceUnit || '月'
      if (min && max) return `¥${this.money(min)}-${this.money(max)}/${unit}`
      if (min) return `¥${this.money(min)}起/${unit}`
      return '价格面议'
    },
    regionText(item) {
      const region = [item.city, item.district].filter(Boolean).join(' ')
      return region || '地区待完善'
    },
    staffTags(item) {
      const tags = item.tags || item.tagList || item.tagNames || item.skills || ''
      if (Array.isArray(tags)) {
        return tags.map((tag) => tag.tagName || tag.name || tag).filter(Boolean).slice(0, 3)
      }
      return String(tags)
        .split(/[,，、\s]+/)
        .filter(Boolean)
        .slice(0, 3)
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  background: #f7f7f8;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 24rpx 28rpx 20rpx;
  background: #fff;
}

.search-box {
  display: flex;
  flex: 1;
  height: 64rpx;
  align-items: center;
  gap: 12rpx;
  padding: 0 22rpx;
  border-radius: 32rpx;
  background: #f0f0f1;
  box-sizing: border-box;
}

.search-icon {
  color: #657180;
  font-size: 34rpx;
  line-height: 1;
}

.search-input {
  flex: 1;
  height: 64rpx;
  color: #242a33;
  font-size: 28rpx;
}

.placeholder {
  color: #9aa1aa;
}

.search-btn {
  height: 64rpx;
  margin: 0;
  padding: 0 8rpx;
  border: 0;
  background: transparent;
  color: #202733;
  font-size: 28rpx;
  line-height: 64rpx;
}

.search-btn::after {
  border: 0;
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  height: 96rpx;
  align-items: center;
  border-bottom: 1rpx solid #eceff2;
  background: #fff;
}

.filter-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  color: #525b66;
  font-size: 29rpx;
}

.chevron {
  color: #9aa1aa;
  font-size: 28rpx;
}

.list {
  padding: 22rpx 24rpx 40rpx;
}

.staff-card {
  display: flex;
  gap: 22rpx;
  margin-bottom: 22rpx;
  padding: 24rpx;
  border-radius: 8rpx;
  background: #fff;
  box-shadow: 0 8rpx 22rpx rgba(31, 39, 49, 0.05);
}

.avatar {
  display: flex;
  flex: 0 0 144rpx;
  width: 144rpx;
  height: 156rpx;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 8rpx;
  background: #fff0f0;
  color: #ef4f5f;
  font-size: 50rpx;
  font-weight: 900;
}

.avatar image {
  width: 100%;
  height: 100%;
}

.info {
  min-width: 0;
  flex: 1;
}

.head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.name {
  color: #2b3038;
  font-size: 33rpx;
  font-weight: 900;
  line-height: 42rpx;
}

.price {
  flex-shrink: 0;
  color: #ef2f3d;
  font-size: 27rpx;
  font-weight: 900;
  line-height: 42rpx;
}

.region {
  display: block;
  margin-top: 10rpx;
  color: #59616d;
  font-size: 26rpx;
  line-height: 36rpx;
}

.meta,
.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 12rpx;
}

.meta text {
  color: #727985;
  font-size: 25rpx;
  line-height: 34rpx;
}

.meta text + text::before {
  margin-right: 10rpx;
  color: #c5c9cf;
  content: '|';
}

.tag {
  max-width: 132rpx;
  height: 40rpx;
  padding: 0 16rpx;
  overflow: hidden;
  border-radius: 6rpx;
  background: #fff0ec;
  color: #ef4f5f;
  font-size: 24rpx;
  line-height: 40rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty,
.load-tip {
  padding: 80rpx 24rpx;
  color: #8a8f99;
  font-size: 27rpx;
  text-align: center;
}

.load-tip {
  padding: 18rpx 24rpx 32rpx;
}
</style>
