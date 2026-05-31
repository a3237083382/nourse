<template>
  <view class="page" v-if="staff">
    <image v-if="validImage(staff.avatarUrl)" class="cover" :src="staff.avatarUrl" mode="aspectFill" />
    <view v-else class="cover placeholder">{{ staff.name.slice(0, 1) }}</view>

    <view class="panel">
      <view class="name-row">
        <text class="name">{{ staff.name }}</text>
        <text class="salary">¥{{ money(staff.salaryMin) }}-{{ money(staff.salaryMax) }}/{{ unitText(staff.salaryUnit) }}</text>
      </view>
      <view class="meta-pills">
        <text>{{ staff.categoryName }}</text>
        <text>{{ staff.city }} {{ staff.district }}</text>
        <text>{{ staff.age }}岁</text>
        <text>{{ staff.experienceYears || 0 }}年经验</text>
      </view>
      <view class="tags">
        <text v-for="tag in staff.tags" :key="tag.id" class="tag">{{ tag.tagName }}</text>
      </view>
    </view>

    <view class="panel">
      <text class="section-title">服务说明</text>
      <text class="paragraph">{{ staff.serviceDesc || '暂无服务说明' }}</text>
    </view>

    <view class="panel">
      <text class="section-title">平台核验</text>
      <view class="trust-grid">
        <view v-for="item in trustItems" :key="item.title" class="trust">
          <text class="trust-mark">✓</text>
          <view>
            <text class="trust-title">{{ item.title }}</text>
            <text class="trust-desc">{{ item.desc }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="panel">
      <text class="section-title">证书</text>
      <text v-if="!staff.certificates || !staff.certificates.length" class="muted">暂无证书</text>
      <view v-for="item in staff.certificates" :key="item.id" class="line">{{ item.certificateName }}</view>
    </view>

    <view class="panel">
      <text class="section-title">照片</text>
      <scroll-view v-if="displayPhotos.length" scroll-x>
        <view class="photo-row">
          <image v-for="item in displayPhotos" :key="item.id" class="photo" :src="item.photoUrl" mode="aspectFill" />
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
      trustItems: [
        { title: '资料登记', desc: '身份与基础资料已在后台留档' },
        { title: '证书记录', desc: '资质证书可由平台人员核对' },
        { title: '面试协助', desc: '预约后由工作人员线下跟进' },
        { title: '服务跟踪', desc: '合同与订单状态可持续查看' },
      ],
    }
  },
  computed: {
    displayPhotos() {
      return ((this.staff && this.staff.photos) || []).filter((item) => this.validImage(item.photoUrl))
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
    money(value) {
      if (value === null || value === undefined || value === '') return '-'
      return Number(value).toString().replace(/\.0+$/, '')
    },
    unitText(value) {
      return { month: '月', MONTH: '月', day: '天', DAY: '天', time: '次', TIME: '次', hour: '小时', HOUR: '小时' }[value] || value || '月'
    },
    validImage(url) {
      return !!url && !String(url).includes('example.com')
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  padding-bottom: 150rpx;
  background: #f4f5f2;
}

.cover {
  width: 100%;
  height: 430rpx;
  background: #f4c8d0;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d93f58;
  font-size: 80rpx;
  font-weight: 700;
}

.panel {
  margin: 22rpx 24rpx;
  padding: 28rpx;
  border: 1px solid rgba(31, 37, 43, 0.05);
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 12rpx 30rpx rgba(32, 38, 44, 0.05);
}

.name-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18rpx;
}

.name {
  flex: 1;
  min-width: 0;
  color: #20242c;
  font-size: 42rpx;
  font-weight: 700;
  line-height: 1.25;
}

.salary {
  flex: 0 0 auto;
  max-width: 280rpx;
  color: #e84d64;
  font-size: 26rpx;
  font-weight: 700;
  line-height: 1.35;
  text-align: right;
  word-break: break-all;
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

.meta-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}

.meta-pills text {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #f3f4f1;
  color: #68717a;
  font-size: 23rpx;
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

.trust-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14rpx;
  margin-top: 18rpx;
}

.trust {
  display: flex;
  min-height: 96rpx;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx;
  border-radius: 18rpx;
  background: #f7f8f5;
}

.trust-mark {
  display: flex;
  width: 34rpx;
  height: 34rpx;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  border-radius: 50%;
  background: #20252b;
  color: #fff;
  font-size: 22rpx;
  font-weight: 800;
}

.trust-title,
.trust-desc {
  display: block;
}

.trust-title {
  color: #20242c;
  font-size: 24rpx;
  font-weight: 700;
}

.trust-desc {
  margin-top: 4rpx;
  color: #8a8f99;
  font-size: 20rpx;
  line-height: 1.4;
}

.tag {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #fff0f2;
  color: #d93f58;
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
  border-radius: 18rpx;
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
  padding: 18rpx 20rpx calc(24rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -10rpx 28rpx rgba(32, 38, 44, 0.08);
}

.actions button {
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 18rpx;
  background: #f3f4f1;
  color: #3b414c;
  font-size: 24rpx;
}

.actions .primary {
  background: #e84d64;
  color: #fff;
}
</style>
