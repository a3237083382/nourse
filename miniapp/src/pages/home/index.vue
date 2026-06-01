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
          <image v-if="item.coverUrl" :src="item.coverUrl" mode="aspectFill" />
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
      <view class="section-head">
        <text class="section-title">{{ activeCategoryName || '推荐阿姨' }}</text>
        <text class="section-sub">向下滑动查看更多</text>
      </view>
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
          <view class="tag-row">
            <text v-if="item.city || item.district" class="tag">{{ regionText(item) }}</text>
            <text v-if="item.age" class="tag">{{ item.age }}岁</text>
            <text v-if="item.experienceYears" class="tag">{{ item.experienceYears }}年经验</text>
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
      }
    },
    openCategory(item) {
      const staffCategory = this.staffCategories.find((category) => category.id === item.id)
      if (staffCategory) {
        this.selectCategory(staffCategory)
        return
      }
      uni.navigateTo({ url: `/pages/demand/form?categoryId=${item.id}` })
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
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  padding: 24rpx 24rpx 42rpx;
  background: #f5f6f3;
}

.banner {
  height: 320rpx;
  border-radius: 18rpx;
  overflow: hidden;
  background: #ff584d;
  box-shadow: 0 12rpx 26rpx rgba(239, 63, 95, 0.16);
}

.banner-item {
  position: relative;
  height: 320rpx;
  overflow: hidden;
  border-radius: 18rpx;
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
  background: linear-gradient(135deg, #ff4b4b 0%, #ff8759 58%, #ffd3a6 100%);
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
  border-radius: 14rpx;
  background: #fff;
  box-shadow: 0 10rpx 24rpx rgba(32, 38, 44, 0.05);
}

.ticker-icon {
  display: flex;
  width: 42rpx;
  height: 42rpx;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  border-radius: 50%;
  background: #ef3f5f;
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
  color: #333942;
  font-size: 27rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.section {
  margin-top: 22rpx;
  padding: 24rpx;
  border-radius: 18rpx;
  background: #fff;
  box-shadow: 0 10rpx 24rpx rgba(32, 38, 44, 0.05);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.section-title {
  color: #20242c;
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
  color: #20242c;
}

.category-icon {
  display: flex;
  width: 86rpx;
  height: 86rpx;
  align-items: center;
  justify-content: center;
  border-radius: 26rpx;
  background: #fff1f3;
  color: #ef3f5f;
  font-size: 32rpx;
  font-weight: 900;
  overflow: hidden;
}

.category:nth-child(2n) .category-icon {
  background: #eef9ec;
}

.category:nth-child(3n) .category-icon {
  background: #eef5ff;
}

.category:nth-child(4n) .category-icon {
  background: #fff7e7;
}

.category.active .category-icon {
  box-shadow: 0 0 0 4rpx rgba(239, 63, 95, 0.14);
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
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 10rpx 24rpx rgba(32, 38, 44, 0.05);
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
  background: linear-gradient(135deg, #ff802f, #ffd175);
  color: #fff;
  font-size: 46rpx;
  font-weight: 900;
}

.group-info {
  padding: 18rpx 20rpx 20rpx;
}

.group-title {
  color: #20242c;
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
  color: #ef3f5f;
  font-size: 34rpx;
  font-weight: 900;
}

.white-empty {
  border-radius: 16rpx;
  background: #fff;
}

.staff-tabs {
  margin: 30rpx -24rpx 0;
  white-space: nowrap;
}

.staff-tabs-inner {
  display: flex;
  gap: 38rpx;
  padding: 0 24rpx 18rpx;
}

.staff-tab {
  position: relative;
  display: inline-flex;
  align-items: center;
  height: 58rpx;
  color: #5e6570;
  font-size: 31rpx;
  font-weight: 500;
}

.staff-tab.active {
  color: #20242c;
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
  background: #ef3f5f;
  content: '';
  transform: translateX(-50%);
}

.staff-section {
  margin-top: 0;
  padding-bottom: 28rpx;
}

.staff-card {
  display: flex;
  gap: 20rpx;
  margin-top: 18rpx;
  padding: 20rpx;
  border-radius: 18rpx;
  background: #fafbf9;
}

.avatar {
  display: flex;
  width: 128rpx;
  height: 128rpx;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  overflow: hidden;
  border-radius: 18rpx;
  background: #f9c6d2;
  color: #ef3f5f;
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
  color: #20242c;
  font-size: 31rpx;
  font-weight: 900;
  line-height: 1.25;
}

.staff-price {
  flex: 0 0 auto;
  color: #ef3f5f;
  font-size: 24rpx;
  font-weight: 800;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 10rpx;
}

.tag {
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  background: #eef0f1;
  color: #727b86;
  font-size: 21rpx;
}

.staff-desc {
  margin-top: 10rpx;
  color: #68717a;
  font-size: 24rpx;
  line-height: 1.45;
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
  border-radius: 18rpx;
  background: #20242c;
  color: #fff;
  font-size: 24rpx;
  box-shadow: 0 12rpx 26rpx rgba(32, 37, 43, 0.18);
}
</style>
