<template>
  <view class="page" v-if="staff">
    <view class="hero">
      <view class="profile-card">
        <image v-if="validImage(staff.avatarUrl)" class="avatar" :src="staff.avatarUrl" mode="aspectFill" />
        <view v-else class="avatar placeholder">{{ firstName }}</view>
        <view class="profile-main">
          <view class="name-row">
            <text class="name">{{ staff.name }}</text>
            <text class="salary">¥{{ money(staff.salaryMin) }}-{{ money(staff.salaryMax) }}/{{ unitText(staff.salaryUnit) }}</text>
          </view>
          <text class="location">{{ staff.city || '-' }} {{ staff.district || '' }}</text>
          <view class="meta-line">
            <text>{{ staff.age || '-' }}岁</text>
            <text>{{ staff.experienceYears || 0 }}年经验</text>
            <text>{{ staff.categoryName || '-' }}</text>
            <text>{{ staff.education || '-' }}</text>
          </view>
          <view class="tags" v-if="tagList.length">
            <text v-for="tag in tagList" :key="tag.id || tag.tagName" class="tag">{{ tag.tagName }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="section" v-if="certificateList.length">
      <view class="section-head">
        <text class="ornament">◆</text>
        <text class="section-title">证书展示</text>
        <text class="ornament">◆</text>
      </view>
      <scroll-view scroll-x class="cert-scroll">
        <view class="cert-row">
          <view v-for="item in certificateList" :key="item.id || item.certificateName" class="cert-card">
            <image
              v-if="validImage(item.fileUrl)"
              class="cert-image"
              :src="item.fileUrl"
              mode="aspectFill"
              @tap="previewCertificate(item.fileUrl)"
            />
            <view v-else class="cert-fallback">
              <text>{{ item.certificateName }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="audit-card">
      <view class="audit-title">严核验 细筛选</view>
      <view class="audit-body">
        <view class="shield">
          <text>✓</text>
        </view>
        <view class="audit-list">
          <view v-for="item in verificationItems" :key="item.title" class="audit-line">
            <text class="check">✓</text>
            <text>{{ item.title }}</text>
          </view>
        </view>
      </view>
      <view class="tips">
        <view v-for="item in verificationItems" :key="item.desc" class="tip-line">
          <text class="tip-title">{{ item.title }}：</text>
          <text>{{ item.desc }}</text>
        </view>
        <text v-if="staff.verificationNote" class="tip-note">{{ staff.verificationNote }}</text>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="ornament">◆</text>
        <text class="section-title">基本信息</text>
        <text class="ornament">◆</text>
      </view>
      <view class="info-grid">
        <view v-for="item in profileRows" :key="item.label" class="info-cell">
          <text class="info-label">{{ item.label }}</text>
          <text class="info-value">{{ item.value }}</text>
        </view>
      </view>
      <view class="text-block" v-if="staff.selfIntro || staff.serviceDesc">
        <text class="inline-label">自我介绍：</text>
        <text>{{ staff.selfIntro || staff.serviceDesc }}</text>
      </view>
    </view>

    <view class="section" v-if="staff.skills">
      <view class="section-head">
        <text class="ornament">◆</text>
        <text class="section-title">个人技能</text>
        <text class="ornament">◆</text>
      </view>
      <text class="paragraph">{{ staff.skills }}</text>
    </view>

    <view class="section" v-if="displayPhotos.length">
      <view class="section-head">
        <text class="ornament">◆</text>
        <text class="section-title">照片展示</text>
        <text class="ornament">◆</text>
      </view>
      <scroll-view scroll-x>
        <view class="photo-row">
          <image
            v-for="item in displayPhotos"
            :key="item.id || item.photoUrl"
            class="photo"
            :src="item.photoUrl"
            mode="aspectFill"
            @tap="previewPhoto(item.photoUrl)"
          />
        </view>
      </scroll-view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="ornament">◆</text>
        <text class="section-title">工作经历</text>
        <text class="ornament">◆</text>
      </view>
      <view v-if="experienceList.length" class="timeline">
        <view v-for="item in experienceList" :key="item.id || item.description" class="timeline-item">
          <text class="period">{{ item.startDate || '-' }} / {{ item.endDate || '至今' }}</text>
          <text class="paragraph">{{ item.description }}</text>
        </view>
      </view>
      <text v-else class="muted">暂无工作经历</text>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="ornament">◆</text>
        <text class="section-title">服务流程</text>
        <text class="ornament">◆</text>
      </view>
      <view class="flow">
        <view v-for="item in flowItems" :key="item.index" class="flow-step">
          <view class="flow-icon">{{ item.index }}</view>
          <text>{{ item.label }}</text>
        </view>
      </view>
    </view>

    <view class="section review-section">
      <view class="review-head">
        <text class="review-title">用户评价</text>
        <text class="review-all">全部</text>
      </view>
      <view v-for="item in reviewList" :key="item.id" class="review-item">
        <view class="review-top">
          <text class="review-user">{{ item.userName || '匿名用户' }}</text>
          <text class="review-stars">{{ stars(item.rating) }}</text>
        </view>
        <text class="paragraph">{{ item.content || '用户未填写文字评价' }}</text>
      </view>
      <view v-if="!reviewList.length" class="empty-review">暂无评价</view>
    </view>

    <view class="actions">
      <button class="icon-action" @tap="toggleFavorite">
        <text class="icon-star">{{ favorited ? '★' : '☆' }}</text>
        <text>{{ favorited ? '已收藏' : '收藏' }}</text>
      </button>
      <button class="icon-action" open-type="share">
        <text class="share-icon">↗</text>
        <text>分享</text>
      </button>
      <button class="primary" @tap="openInterview">预约面试</button>
      <button class="outline" open-type="contact">联系客服</button>
    </view>
  </view>
</template>

<script>
import { cancelFavoriteStaff, favoriteStaff, getStaffDetail } from '@/services/api'
import { ensureLogin } from '@/services/request'

export default {
  data() {
    return {
      id: undefined,
      demandId: undefined,
      staff: null,
      favorited: false,
      flowItems: [
        { index: 1, label: '面试' },
        { index: 2, label: '签约' },
        { index: 3, label: '上户' },
        { index: 4, label: '服务完成' },
      ],
      verificationItems: [
        { title: '身份认证', desc: '身份资料已由平台留档' },
        { title: '技能认证', desc: '证书与技能资料由后台核验' },
        { title: '无犯罪认证', desc: '相关证明可由平台线下核对' },
        { title: '体检报告', desc: '健康资料由后台维护展示' },
      ],
    }
  },
  computed: {
    firstName() {
      return this.staff && this.staff.name ? this.staff.name.slice(0, 1) : '姨'
    },
    tagList() {
      return (this.staff && this.staff.tags) || []
    },
    certificateList() {
      return (this.staff && this.staff.certificates) || []
    },
    certificateImages() {
      return this.certificateList.map((item) => item.fileUrl).filter((url) => this.validImage(url))
    },
    displayPhotos() {
      return ((this.staff && this.staff.photos) || []).filter((item) => this.validImage(item.photoUrl))
    },
    photoImages() {
      return this.displayPhotos.map((item) => item.photoUrl)
    },
    experienceList() {
      return (this.staff && this.staff.experiences) || []
    },
    reviewList() {
      return (this.staff && this.staff.reviews) || []
    },
    profileRows() {
      if (!this.staff) return []
      return [
        { label: '学历', value: this.displayText(this.staff.education) },
        { label: '籍贯', value: this.displayText(this.staff.nativePlace || `${this.staff.city || ''}${this.staff.district || ''}`) },
        { label: '身高', value: this.staff.heightCm ? `${this.staff.heightCm}cm` : '-' },
        { label: '体重', value: this.staff.weightKg ? `${this.staff.weightKg}kg` : '-' },
        { label: '出生年月', value: this.displayText(this.staff.birthDate) },
        { label: '婚姻', value: this.displayText(this.staff.maritalStatus) },
      ]
    },
  },
  onLoad(options) {
    this.id = options.id
    this.demandId = options.demandId
    this.loadDetail()
  },
  onShareAppMessage() {
    return {
      title: this.staff ? `${this.staff.name} - 到家服务` : '到家服务',
      path: `/pages/staff/detail?id=${this.id}`,
    }
  },
  methods: {
    async loadDetail() {
      const res = await getStaffDetail(this.id)
      this.staff = res.data
    },
    async toggleFavorite() {
      await ensureLogin()
      if (this.favorited) {
        await cancelFavoriteStaff(this.id)
        this.favorited = false
        uni.showToast({ title: '已取消收藏', icon: 'success' })
      } else {
        await favoriteStaff(this.id)
        this.favorited = true
        uni.showToast({ title: '已收藏', icon: 'success' })
      }
    },
    openInterview() {
      const query = this.demandId ? `?staffId=${this.id}&demandId=${this.demandId}` : `?staffId=${this.id}`
      uni.navigateTo({ url: `/pages/interview/form${query}` })
    },
    previewCertificate(url) {
      this.previewImages(url, this.certificateImages)
    },
    previewPhoto(url) {
      this.previewImages(url, this.photoImages)
    },
    previewImages(current, urls) {
      const list = (urls || []).filter((url) => this.validImage(url))
      if (!list.length) return
      uni.previewImage({
        current,
        urls: list,
      })
    },
    money(value) {
      if (value === null || value === undefined || value === '') return '0'
      return Number(value).toString().replace(/\.0+$/, '')
    },
    unitText(value) {
      return { month: '月', MONTH: '月', day: '天', DAY: '天', time: '次', TIME: '次', hour: '小时', HOUR: '小时' }[value] || value || '月'
    },
    stars(value) {
      const score = Math.max(0, Math.min(5, Number(value || 0)))
      return '★★★★★'.slice(0, score) + '☆☆☆☆☆'.slice(0, 5 - score)
    },
    validImage(url) {
      const value = String(url || '')
      return !!value && !value.includes('example.com') && (value.startsWith('http') || value.startsWith('/') || value.startsWith('static/'))
    },
    displayText(value) {
      return value === null || value === undefined || value === '' ? '-' : String(value)
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  padding-bottom: 156rpx;
  background: linear-gradient(180deg, #fff1ed 0, #fff8f4 330rpx, #f7f4ef 760rpx);
}

.hero {
  padding: 28rpx 24rpx 10rpx;
}

.profile-card {
  display: flex;
  gap: 24rpx;
  padding: 22rpx;
  border: 1rpx solid #f3e5dc;
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 14rpx 30rpx rgba(176, 70, 78, 0.1);
}

.avatar {
  width: 150rpx;
  height: 196rpx;
  flex: 0 0 auto;
  border-radius: 8rpx;
  background: #edf0f3;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ef4f5f;
  font-size: 64rpx;
  font-weight: 700;
}

.profile-main {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12rpx;
}

.name {
  color: #222832;
  font-size: 36rpx;
  font-weight: 900;
  line-height: 1.25;
}

.salary {
  flex: 0 0 auto;
  color: #ef4f5f;
  font-size: 31rpx;
  font-weight: 900;
  line-height: 1.25;
  text-align: right;
  white-space: nowrap;
}

.location {
  display: block;
  margin-top: 12rpx;
  color: #2f343b;
  font-size: 30rpx;
}

.meta-line {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  margin-top: 16rpx;
  color: #6f7680;
  font-size: 28rpx;
}

.meta-line text {
  padding-right: 18rpx;
  margin-right: 18rpx;
  border-right: 1px solid #d5d9dd;
}

.meta-line text:last-child {
  border-right: 0;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}

.tag {
  max-width: 150rpx;
  padding: 8rpx 16rpx;
  overflow: hidden;
  border-radius: 10rpx;
  background: #fff1ee;
  color: #ef4f5f;
  font-size: 24rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.section {
  margin: 24rpx;
  padding: 24rpx 20rpx;
  border: 1rpx solid #f3e5dc;
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 12rpx 28rpx rgba(80, 45, 40, 0.05);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24rpx;
  margin-bottom: 24rpx;
}

.section-title {
  color: #643b34;
  font-size: 34rpx;
  font-weight: 800;
}

.ornament {
  color: #ef4f5f;
  font-size: 30rpx;
  transform: rotate(45deg);
}

.cert-scroll {
  white-space: nowrap;
}

.cert-row,
.photo-row {
  display: flex;
  gap: 22rpx;
}

.cert-card,
.cert-image,
.cert-fallback {
  width: 222rpx;
  height: 136rpx;
  border-radius: 16rpx;
}

.cert-card {
  flex: 0 0 auto;
  overflow: hidden;
  background: #fff7f2;
}

.cert-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16rpx;
  box-sizing: border-box;
  color: #7c5f5b;
  font-size: 24rpx;
  line-height: 1.4;
  text-align: center;
}

.audit-card {
  margin: 24rpx;
  overflow: hidden;
  border: 1rpx solid #f3e5dc;
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 12rpx 28rpx rgba(80, 45, 40, 0.05);
}

.audit-title {
  padding: 28rpx 20rpx 18rpx;
  background: linear-gradient(90deg, #fff1ee, #fff7dc);
  color: #643b34;
  font-size: 42rpx;
  font-weight: 900;
  text-align: center;
}

.audit-body {
  display: flex;
  align-items: center;
  gap: 34rpx;
  padding: 34rpx 48rpx 22rpx;
}

.shield {
  display: flex;
  width: 170rpx;
  height: 170rpx;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(180deg, #ffdca8, #ef4f5f);
  color: #fff;
  font-size: 76rpx;
  font-weight: 900;
  box-shadow: 0 16rpx 30rpx rgba(239, 79, 95, 0.18);
}

.audit-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  color: #643b34;
  font-size: 28rpx;
  font-weight: 700;
}

.audit-line {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.check {
  display: flex;
  width: 32rpx;
  height: 32rpx;
  align-items: center;
  justify-content: center;
  border: 3rpx solid #ffc9bd;
  border-radius: 50%;
  color: #ef4f5f;
  font-size: 22rpx;
  font-weight: 900;
}

.tips {
  margin: 0 32rpx 32rpx;
  padding: 20rpx;
  border: 1rpx solid #f3e5dc;
  border-radius: 12rpx;
  color: #6f7680;
  font-size: 25rpx;
  line-height: 1.7;
}

.tip-line,
.tip-note {
  display: block;
}

.tip-title {
  color: #643b34;
  font-weight: 800;
}

.tip-note {
  margin-top: 12rpx;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  border-top: 1px solid #f0e3dc;
  border-left: 1px solid #f0e3dc;
}

.info-cell {
  display: grid;
  grid-template-columns: 140rpx 1fr;
  min-height: 72rpx;
  border-right: 1px solid #f0e3dc;
  border-bottom: 1px solid #f0e3dc;
}

.info-label,
.info-value {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10rpx;
  font-size: 25rpx;
}

.info-label {
  background: #fff7f2;
  color: #643b34;
  font-weight: 700;
}

.info-value {
  color: #3b414c;
}

.text-block {
  margin-top: 22rpx;
  padding: 22rpx 8rpx 0;
  border-top: 1px solid #f0e3dc;
  color: #3b414c;
  font-size: 27rpx;
  line-height: 1.75;
}

.inline-label {
  color: #643b34;
  font-weight: 800;
}

.paragraph,
.muted {
  display: block;
  color: #555f6b;
  font-size: 27rpx;
  line-height: 1.8;
}

.muted {
  color: #9aa0a6;
  text-align: center;
}

.photo {
  width: 148rpx;
  height: 148rpx;
  flex: 0 0 auto;
  border-radius: 8rpx;
  background: #edf0f3;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.timeline-item {
  position: relative;
  padding-left: 30rpx;
}

.timeline-item::before {
  position: absolute;
  top: 8rpx;
  left: 0;
  width: 14rpx;
  height: 14rpx;
  border: 4rpx solid #ffb7a7;
  border-radius: 50%;
  background: #fff;
  content: '';
}

.period {
  display: block;
  margin-bottom: 12rpx;
  color: #ef4f5f;
  font-size: 26rpx;
  font-weight: 800;
}

.flow {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10rpx;
}

.flow-step {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 12rpx;
  color: #68717a;
  font-size: 25rpx;
}

.flow-icon {
  display: flex;
  width: 78rpx;
  height: 78rpx;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #fff1ee;
  color: #ef4f5f;
  font-size: 30rpx;
  font-weight: 900;
}

.review-section {
  padding-bottom: 64rpx;
}

.review-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18rpx;
}

.review-title {
  color: #222832;
  font-size: 30rpx;
  font-weight: 800;
}

.review-all {
  color: #ef4f5f;
  font-size: 26rpx;
}

.review-item {
  padding: 20rpx 0;
  border-top: 1px solid #edf0f3;
}

.review-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.review-user {
  color: #222832;
  font-size: 26rpx;
  font-weight: 700;
}

.review-stars {
  color: #ff9f1c;
  font-size: 26rpx;
}

.empty-review {
  padding: 34rpx 0 12rpx;
  color: #9aa0a6;
  font-size: 27rpx;
  text-align: center;
}

.actions {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: grid;
  grid-template-columns: 86rpx 86rpx 1fr 1fr;
  gap: 18rpx;
  align-items: center;
  padding: 18rpx 24rpx calc(22rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -10rpx 28rpx rgba(80, 45, 40, 0.08);
}

.actions button {
  margin: 0;
  padding: 0;
}

.actions button::after {
  border: 0;
}

.icon-action {
  display: flex;
  height: 86rpx;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 2rpx;
  background: transparent;
  color: #4c3b37;
  font-size: 22rpx;
  line-height: 1.2;
}

.icon-star {
  color: #ef4f5f;
  font-size: 34rpx;
  line-height: 1;
}

.share-icon {
  color: #4c3b37;
  font-size: 34rpx;
  line-height: 1;
}

.primary,
.outline {
  height: 76rpx;
  border-radius: 16rpx;
  font-size: 30rpx;
  line-height: 76rpx;
}

.primary {
  background: #ef4f5f;
  color: #fff;
}

.outline {
  border: 1px solid #ef4f5f;
  background: #fff;
  color: #ef4f5f;
}
</style>
