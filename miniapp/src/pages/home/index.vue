<template>
  <view class="page">
    <swiper v-if="banners.length" class="banner" circular autoplay indicator-dots :interval="3500" :duration="500">
      <swiper-item v-for="item in banners" :key="item.id">
        <view class="banner-item">
          <image v-if="item.imageUrl" class="banner-image" :src="item.imageUrl" mode="aspectFill" />
          <view v-else class="banner-fallback">
            <text class="banner-fallback-title">{{ item.title || '家政服务' }}</text>
          </view>
          <view v-if="!item.imageUrl" class="banner-copy">
            <text class="banner-title">{{ item.title || '家政交给我' }}</text>
            <text v-if="item.content" class="banner-desc">{{ item.content }}</text>
          </view>
        </view>
      </swiper-item>
    </swiper>

    <view class="ticker" v-if="tips.length">
      <view class="ticker-icon">!</view>
      <swiper class="ticker-swiper" vertical autoplay circular :interval="2800" :duration="420">
        <swiper-item v-for="tip in tips" :key="tip">
          <text class="ticker-text">{{ tip }}</text>
        </swiper-item>
      </swiper>
    </view>

    <view class="section" v-if="categories.length">
      <view class="category-grid">
        <view
          v-for="item in categories"
          :key="item.id"
          class="category"
          :class="{ active: activeCategoryId === item.id }"
          @tap="openCategory(item)"
        >
          <view class="category-icon">
            <image v-if="item.iconUrl" :src="item.iconUrl" mode="aspectFill" />
            <text v-else>{{ firstChar(item.name) }}</text>
          </view>
          <text class="category-name">{{ item.name }}</text>
        </view>
      </view>
    </view>

    <view class="group-section">
      <view class="section-head group-head">
        <text class="section-title">团购活动</text>
        <text class="more" @tap="openGroupList">更多 ›</text>
      </view>
      <view v-if="visibleGroupProducts.length === 0" class="empty white-empty">暂无团购活动</view>
      <view v-for="item in visibleGroupProducts" :key="item.id" class="group-card" @tap="openGroupDetail(item.id)">
        <view class="group-cover">
          <image v-if="validCover(item.coverUrl)" :src="item.coverUrl" mode="aspectFill" />
          <text v-else>{{ shortTitle(item.title) }}</text>
        </view>
        <view class="group-info">
          <text class="group-title">{{ item.title }}</text>
          <view class="group-bottom">
            <text class="price">¥{{ money(item.groupPrice) }}</text>
            <text class="group-meta">{{ groupMeta(item) }}</text>
          </view>
        </view>
      </view>
    </view>

    <scroll-view v-if="staffCategories.length" class="staff-tabs" scroll-x :show-scrollbar="false">
      <view class="staff-tabs-inner">
        <view
          v-for="item in staffCategories"
          :key="item.id"
          class="staff-tab"
          :class="{ active: activeCategoryId === item.id }"
          @tap="selectCategory(item)"
        >
          <text>{{ item.name }}</text>
        </view>
      </view>
    </scroll-view>

    <view class="section staff-section">
      <view v-if="staffList.length === 0 && !staffLoading" class="empty">当前分类暂无服务人员</view>
      <view v-for="item in staffList" :key="item.id" class="staff-card" @tap="openStaffDetail(item.id)">
        <view class="avatar">
          <image v-if="item.avatarUrl" :src="item.avatarUrl" mode="aspectFill" />
          <text v-else>{{ firstChar(item.name) }}</text>
        </view>
        <view class="staff-info">
          <view class="staff-head">
            <text class="staff-name">{{ item.name }}</text>
            <text class="staff-price">{{ salaryText(item) }}</text>
          </view>
          <text v-if="item.city || item.district" class="staff-location">{{ regionText(item) }}</text>
          <view class="meta-line">
            <text v-if="item.age">{{ item.age }}岁</text>
            <text v-if="item.experienceYears">{{ item.experienceYears }}年经验</text>
            <text v-if="item.categoryName">{{ item.categoryName }}</text>
            <text v-if="item.education">{{ item.education }}</text>
          </view>
          <view v-if="staffTags(item).length" class="staff-tag-row">
            <text v-for="tag in staffTags(item)" :key="tag" class="tag">{{ tag }}</text>
          </view>
          <text class="staff-desc">{{ item.serviceDesc || item.introduction || item.summary || '经验稳定，服务认真，可预约面试。' }}</text>
        </view>
      </view>
      <view class="load-tip" v-if="staffLoading">加载中...</view>
      <view class="load-tip" v-else-if="staffNoMore && staffList.length">没有更多了</view>
    </view>

    <button class="contact" open-type="contact">联系客服</button>
  </view>
</template>

<script>
import { getHome, getStaffList } from '@/services/api'

export default {
  data() {
    return {
      categories: [],
      staffCategories: [],
      banners: [],
      tips: [],
      groupProducts: [],
      activeCategoryId: undefined,
      activeCategoryName: '',
      staffList: [],
      staffPageNum: 1,
      staffPageSize: 6,
      staffTotal: 0,
      staffLoading: false,
      staffNoMore: false,
      nowTime: Date.now(),
      countdownTimer: null,
    }
  },
  computed: {
    visibleGroupProducts() {
      return this.groupProducts.slice(0, 3)
    },
  },
  onLoad() {
    this.loadHome()
    this.countdownTimer = setInterval(() => {
      this.nowTime = Date.now()
    }, 1000)
  },
  onUnload() {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
      this.countdownTimer = null
    }
  },
  onReachBottom() {
    this.loadMoreStaff()
  },
  methods: {
    async loadHome() {
      const res = await getHome()
      this.banners = (res.data.banners && res.data.banners.length)
        ? res.data.banners
        : [{ id: 'default', title: '家政交给我 生活还给你', content: '月嫂、保姆、育婴师、养老与清洁服务' }]
      this.categories = res.data.categories || []
      this.staffCategories = (res.data.staffCategories && res.data.staffCategories.length)
        ? res.data.staffCategories
        : this.categories.filter((item) => ['月嫂', '保姆', '育婴师'].includes(item.name))
      this.tips = res.data.signSuccessTips || []
      this.groupProducts = res.data.groupProducts || []
      if (this.staffCategories.length) {
        this.selectCategory(this.staffCategories[0])
      } else {
        this.activeCategoryName = '推荐阿姨'
        this.staffList = res.data.recommendedStaff || []
        this.staffTotal = this.staffList.length
        this.staffNoMore = true
      }
    },
    openCategory(item) {
      const categoryName = encodeURIComponent(item.name || '')
      uni.navigateTo({ url: `/pages/staff/category?categoryId=${item.id}&categoryName=${categoryName}` })
    },
    selectCategory(item) {
      this.activeCategoryId = item.id
      this.activeCategoryName = item.name
      this.staffPageNum = 1
      this.staffList = []
      this.staffTotal = 0
      this.staffNoMore = false
      this.loadStaff()
    },
    async loadStaff() {
      if (this.staffLoading || this.staffNoMore) return
      this.staffLoading = true
      try {
        const res = await getStaffList({
          categoryId: this.activeCategoryId,
          pageNum: this.staffPageNum,
          pageSize: this.staffPageSize,
        })
        const rows = res.rows || []
        this.staffTotal = res.total || rows.length
        this.staffList = this.staffPageNum === 1 ? rows : this.staffList.concat(rows)
        this.staffNoMore = rows.length < this.staffPageSize || this.staffList.length >= this.staffTotal
      } finally {
        this.staffLoading = false
      }
    },
    loadMoreStaff() {
      if (this.staffLoading || this.staffNoMore) return
      this.staffPageNum += 1
      this.loadStaff()
    },
    openGroupList() {
      uni.navigateTo({ url: '/pages/group/list' })
    },
    openGroupDetail(id) {
      uni.navigateTo({ url: `/pages/group/detail?id=${id}` })
    },
    openStaffDetail(id) {
      uni.navigateTo({ url: `/pages/staff/detail?id=${id}` })
    },
    firstChar(value) {
      return (value || '?').slice(0, 1)
    },
    shortTitle(value) {
      return (value || '团购').slice(0, 2)
    },
    validCover(value) {
      const url = String(value || '')
      return !!url && !url.includes('/static/logo.png')
    },
    money(value) {
      return Number(value || 0).toFixed(2).replace(/\.00$/, '')
    },
    groupMeta(item) {
      const seconds = this.remainingSeconds(item.activeTeamExpireAt)
      if (seconds > 0) {
        return `拼团时间剩余 ${this.formatDuration(seconds)}`
      }
      return `已售 ${item.soldCount || 0}`
    },
    remainingSeconds(value) {
      if (!value) return 0
      const time = new Date(String(value).replace(/-/g, '/')).getTime()
      if (!time) return 0
      return Math.max(0, Math.floor((time - this.nowTime) / 1000))
    },
    formatDuration(seconds) {
      const hours = String(Math.floor(seconds / 3600)).padStart(2, '0')
      const minutes = String(Math.floor((seconds % 3600) / 60)).padStart(2, '0')
      const secs = String(seconds % 60).padStart(2, '0')
      return `${hours}:${minutes}:${secs}`
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
      return [item.city, item.district].filter(Boolean).join(' ')
    },
    staffTags(item) {
      const tags = item.tags || item.tagList || item.tagNames || []
      if (Array.isArray(tags)) {
        return tags.map((tag) => tag.tagName || tag.name || tag).filter(Boolean).slice(0, 3)
      }
      return String(tags || '')
        .split(/[,，\s]+/)
        .filter(Boolean)
        .slice(0, 3)
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  padding: 24rpx 24rpx 42rpx;
  background: linear-gradient(180deg, #fff1ed 0, #fff8f4 330rpx, #f7f4ef 760rpx);
}

.banner {
  height: 320rpx;
  border-radius: 16rpx;
  overflow: hidden;
  background: #ef4f5f;
  box-shadow: 0 16rpx 34rpx rgba(176, 70, 78, 0.16);
}

.banner-item {
  position: relative;
  height: 320rpx;
  overflow: hidden;
  border-radius: 16rpx;
}

.banner-image,
.group-cover image,
.avatar image,
.category-icon image {
  width: 100%;
  height: 100%;
}

.banner-fallback {
  display: flex;
  height: 320rpx;
  align-items: center;
  padding-left: 46rpx;
  background: linear-gradient(135deg, #ef4f5f 0%, #ff8f78 58%, #ffe1c7 100%);
}

.banner-fallback-title {
  width: 320rpx;
  color: #fff;
  font-size: 58rpx;
  font-weight: 900;
  line-height: 1.18;
}

.banner-copy {
  position: absolute;
  left: 32rpx;
  right: 32rpx;
  bottom: 28rpx;
  color: #fff;
  text-shadow: 0 4rpx 12rpx rgba(40, 20, 10, 0.18);
}

.banner-title,
.banner-desc,
.ticker-text,
.category-name,
.group-title,
.group-meta,
.staff-name,
.staff-desc {
  display: block;
}

.banner-title {
  font-size: 42rpx;
  font-weight: 900;
}

.banner-desc {
  width: 72%;
  margin-top: 8rpx;
  font-size: 24rpx;
  line-height: 1.45;
}

.ticker {
  display: flex;
  height: 78rpx;
  align-items: center;
  gap: 18rpx;
  margin-top: 22rpx;
  padding: 0 24rpx;
  border: 1rpx solid #ffe3dd;
  border-radius: 16rpx;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 12rpx 26rpx rgba(80, 45, 40, 0.06);
}

.ticker-icon {
  display: flex;
  width: 42rpx;
  height: 42rpx;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  border-radius: 50%;
  background: #ef4f5f;
  color: #fff;
  font-size: 25rpx;
  font-weight: 900;
}

.ticker-swiper {
  flex: 1;
  height: 78rpx;
}

.ticker-text {
  height: 78rpx;
  line-height: 78rpx;
  color: #4a3432;
  font-size: 27rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.section {
  margin-top: 22rpx;
  padding: 24rpx;
  border: 1rpx solid #f3e5dc;
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 12rpx 28rpx rgba(80, 45, 40, 0.05);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.section-title {
  color: #222832;
  font-size: 34rpx;
  font-weight: 900;
}

.section-sub,
.more {
  color: #8a8f99;
  font-size: 25rpx;
}

.more {
  color: #606873;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  row-gap: 24rpx;
}

.category {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 10rpx;
  color: #222832;
}

.category-icon {
  display: flex;
  width: 86rpx;
  height: 86rpx;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  background: #fff1ee;
  color: #ef4f5f;
  font-size: 32rpx;
  font-weight: 900;
  overflow: hidden;
}

.category:nth-child(2n) .category-icon {
  background: #f0f7ee;
}

.category:nth-child(3n) .category-icon {
  background: #fff7dc;
}

.category:nth-child(4n) .category-icon {
  background: #eef7f5;
}

.category.active .category-icon {
  box-shadow: 0 0 0 4rpx rgba(239, 79, 95, 0.14);
}

.category-name {
  max-width: 128rpx;
  color: #222832;
  font-size: 25rpx;
  text-align: center;
  line-height: 1.25;
}

.group-section {
  margin-top: 28rpx;
}

.group-head {
  margin-bottom: 16rpx;
}

.group-card {
  margin-top: 18rpx;
  overflow: hidden;
  border: 1rpx solid #f3e5dc;
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 12rpx 26rpx rgba(80, 45, 40, 0.05);
}

.group-card:first-of-type {
  margin-top: 0;
}

.group-cover {
  display: flex;
  width: 100%;
  height: 250rpx;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(135deg, #ef4f5f, #f6a05f);
  color: #fff;
  font-size: 46rpx;
  font-weight: 900;
}

.group-info {
  padding: 18rpx 20rpx 20rpx;
}

.group-title {
  color: #222832;
  font-size: 30rpx;
  font-weight: 800;
  line-height: 1.35;
}

.group-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  margin-top: 12rpx;
}

.group-meta {
  flex: 1;
  color: #8a8f99;
  font-size: 24rpx;
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  color: #ef4f5f;
  font-size: 34rpx;
  font-weight: 900;
}

.white-empty {
  border-radius: 16rpx;
  background: #fff;
}

.staff-tabs {
  margin: 30rpx 0 0;
  border: 1rpx solid #f3e5dc;
  border-bottom: 0;
  border-radius: 16rpx 16rpx 0 0;
  background: #fff;
  white-space: nowrap;
}

.staff-tabs-inner {
  display: flex;
  gap: 34rpx;
  padding: 0 22rpx 14rpx;
}

.staff-tab {
  position: relative;
  display: inline-flex;
  align-items: center;
  height: 62rpx;
  min-width: 76rpx;
  color: #6c5f5b;
  font-size: 30rpx;
  font-weight: 500;
}

.staff-tab.active {
  color: #222832;
  font-size: 36rpx;
  font-weight: 900;
}

.staff-tab.active::after {
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 26rpx;
  height: 8rpx;
  border-radius: 999rpx;
  background: #ef4f5f;
  content: '';
  transform: translateX(-50%);
}

.staff-section {
  margin-top: 0;
  padding: 18rpx 18rpx 28rpx;
  border-top: 0;
  border-radius: 0 0 16rpx 16rpx;
}

.staff-card {
  display: flex;
  gap: 20rpx;
  margin-top: 18rpx;
  padding: 20rpx;
  border: 1rpx solid #f3e5dc;
  border-radius: 16rpx;
  background: #fff;
}

.staff-card:first-of-type {
  margin-top: 0;
}

.avatar {
  display: flex;
  width: 128rpx;
  height: 128rpx;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  overflow: hidden;
  border-radius: 8rpx;
  background: #fff1ee;
  color: #ef4f5f;
  font-size: 42rpx;
  font-weight: 900;
}

.staff-info {
  flex: 1;
  min-width: 0;
}

.staff-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12rpx;
}

.staff-name {
  flex: 1;
  min-width: 0;
  color: #222832;
  font-size: 31rpx;
  font-weight: 900;
  line-height: 1.25;
}

.staff-price {
  flex: 0 0 auto;
  color: #ef4f5f;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 1.25;
  white-space: nowrap;
}

.staff-location {
  display: block;
  margin-top: 9rpx;
  color: #2f343b;
  font-size: 26rpx;
  line-height: 1.35;
}

.meta-line {
  display: flex;
  flex-wrap: wrap;
  margin-top: 9rpx;
  color: #6f7680;
  font-size: 24rpx;
}

.meta-line text {
  padding-right: 16rpx;
  margin-right: 16rpx;
  border-right: 1rpx solid #d8dde2;
  line-height: 1.25;
}

.meta-line text:last-child {
  border-right: 0;
  margin-right: 0;
  padding-right: 0;
}

.staff-tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 10rpx;
}

.tag {
  max-width: 156rpx;
  padding: 6rpx 14rpx;
  overflow: hidden;
  border-radius: 8rpx;
  background: #fff1ee;
  color: #ef4f5f;
  font-size: 22rpx;
  line-height: 1.15;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.staff-desc {
  margin-top: 9rpx;
  color: #68717a;
  font-size: 24rpx;
  line-height: 1.42;
}

.empty,
.load-tip {
  padding: 28rpx 0;
  color: #8a8f99;
  text-align: center;
  font-size: 26rpx;
}

.contact {
  position: fixed;
  right: 24rpx;
  bottom: 140rpx;
  width: 172rpx;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 16rpx;
  background: #ef4f5f;
  color: #fff;
  font-size: 24rpx;
  box-shadow: 0 14rpx 30rpx rgba(239, 79, 95, 0.22);
}
</style>
