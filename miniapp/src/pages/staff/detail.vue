<template>
  <view class="page" v-if="staff">
    <image v-if="staff.avatarUrl" class="cover" :src="staff.avatarUrl" mode="aspectFill" />
    <view v-else class="cover placeholder">{{ staff.name.slice(0, 1) }}</view>

    <view class="panel">
      <view class="name-row">
        <text class="name">{{ staff.name }}</text>
        <text class="salary">¥{{ staff.salaryMin || '-' }}-{{ staff.salaryMax || '-' }}/{{ staff.salaryUnit || '月' }}</text>
      </view>
      <text class="meta">{{ staff.categoryName }} | {{ staff.city }} {{ staff.district }} | {{ staff.age }}岁 | {{ staff.experienceYears || 0 }}年经验</text>
      <view class="tags">
        <text v-for="tag in staff.tags" :key="tag.id" class="tag">{{ tag.tagName }}</text>
      </view>
    </view>

    <view class="panel">
      <text class="section-title">服务说明</text>
      <text class="paragraph">{{ staff.serviceDesc || '暂无服务说明' }}</text>
    </view>

    <view class="panel">
      <text class="section-title">证书</text>
      <text v-if="!staff.certificates || !staff.certificates.length" class="muted">暂无证书</text>
      <view v-for="item in staff.certificates" :key="item.id" class="line">{{ item.certificateName }}</view>
    </view>

    <view class="panel">
      <text class="section-title">照片</text>
      <scroll-view v-if="staff.photos && staff.photos.length" scroll-x>
        <view class="photo-row">
          <image v-for="item in staff.photos" :key="item.id" class="photo" :src="item.photoUrl" mode="aspectFill" />
        </view>
      </scroll-view>
      <text v-else class="muted">暂无照片</text>
    </view>

    <view class="panel">
      <text class="section-title">工作经历</text>
      <view v-for="item in staff.experiences" :key="item.id" class="experience">
        <text class="period">{{ item.startDate || '' }} - {{ item.endDate || '至今' }}</text>
        <text class="paragraph">{{ item.description }}</text>
      </view>
      <text v-if="!staff.experiences || !staff.experiences.length" class="muted">暂无经历</text>
    </view>

    <view class="actions">
      <button @tap="toggleFavorite">{{ favorited ? '取消收藏' : '收藏' }}</button>
      <button open-type="share">分享</button>
      <button open-type="contact">客服</button>
      <button class="primary" @tap="openInterview">预约面试</button>
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
    }
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
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  padding-bottom: 128rpx;
  background: #f6f7f9;
}

.cover {
  width: 100%;
  height: 420rpx;
  background: #f5c7d1;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ef3f5f;
  font-size: 80rpx;
  font-weight: 700;
}

.panel {
  margin: 24rpx;
  padding: 28rpx;
  border-radius: 16rpx;
  background: #fff;
}

.name-row {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.name {
  color: #20242c;
  font-size: 40rpx;
  font-weight: 700;
}

.salary {
  color: #ef3f5f;
  font-size: 28rpx;
  font-weight: 700;
}

.meta,
.paragraph,
.muted,
.period {
  display: block;
  margin-top: 14rpx;
  color: #6d7480;
  font-size: 26rpx;
  line-height: 1.7;
}

.section-title {
  color: #20242c;
  font-size: 30rpx;
  font-weight: 700;
}

.tags,
.photo-row {
  display: flex;
  gap: 12rpx;
  margin-top: 18rpx;
}

.tag {
  padding: 8rpx 14rpx;
  border-radius: 8rpx;
  background: #fff0f2;
  color: #ef3f5f;
  font-size: 22rpx;
}

.line,
.experience {
  padding: 18rpx 0;
  border-top: 1px solid #edf0f3;
  color: #3b414c;
  font-size: 26rpx;
}

.photo {
  width: 180rpx;
  height: 180rpx;
  border-radius: 12rpx;
  background: #edf0f3;
}

.actions {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
  padding: 18rpx 20rpx 28rpx;
  background: #fff;
}

.actions button {
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 999rpx;
  background: #f1f3f6;
  color: #3b414c;
  font-size: 24rpx;
}

.actions .primary {
  background: #ef3f5f;
  color: #fff;
}
</style>
