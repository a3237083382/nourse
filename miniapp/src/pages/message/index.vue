<template>
  <view class="page">
    <view v-if="messages.length === 0" class="empty">暂无系统消息</view>
    <view
      v-for="item in messages"
      :key="item.id"
      class="message"
      :class="{ unread: !item.readFlag }"
      @tap="read(item)"
    >
      <view class="row">
        <text class="title">{{ item.title }}</text>
        <text v-if="!item.readFlag" class="badge">未读</text>
      </view>
      <text class="content">{{ item.content }}</text>
      <text class="time">{{ item.createdAt }}</text>
    </view>
  </view>
</template>

<script>
import { getMessageList, markMessageRead } from '@/services/api'
import { ensureLogin } from '@/services/request'

export default {
  data() {
    return {
      messages: [],
    }
  },
  async onShow() {
    await ensureLogin({ refresh: true })
    this.loadMessages()
  },
  methods: {
    async loadMessages() {
      const res = await getMessageList({ pageNum: 1, pageSize: 50 })
      this.messages = res.rows || []
    },
    async read(item) {
      if (item.readFlag) {
        return
      }
      await markMessageRead(item.id)
      item.readFlag = 1
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

.message,
.empty {
  margin-bottom: 20rpx;
  padding: 28rpx;
  border: 1px solid rgba(31, 37, 43, 0.05);
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 12rpx 30rpx rgba(32, 38, 44, 0.05);
}

.message.unread {
  border-color: rgba(232, 77, 100, 0.28);
  box-shadow: 0 14rpx 34rpx rgba(232, 77, 100, 0.08);
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.title {
  color: #20242c;
  font-size: 30rpx;
  font-weight: 700;
}

.badge {
  flex: 0 0 auto;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #ffe9ee;
  color: #d93f58;
  font-size: 22rpx;
}

.content {
  display: block;
  margin-top: 14rpx;
  color: #4f5662;
  font-size: 26rpx;
  line-height: 1.6;
}

.time {
  display: block;
  margin-top: 14rpx;
  color: #9aa1ad;
  font-size: 24rpx;
}

.empty {
  color: #8a8f99;
  text-align: center;
  font-size: 28rpx;
}
</style>
