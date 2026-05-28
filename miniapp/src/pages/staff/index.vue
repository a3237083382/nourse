<template>
  <view class="page">
    <view class="search">请输入阿姨姓名</view>
    <view class="filters">
      <text>年龄</text>
      <text>学历</text>
      <text>工资</text>
      <text>地区</text>
    </view>
    <view class="card" v-for="item in staff" :key="item.name">
      <view class="avatar"></view>
      <view class="info">
        <view class="row">
          <text class="name">{{ item.name }}</text>
          <text class="salary">{{ item.salary }}</text>
        </view>
        <text class="meta">{{ item.city }} | {{ item.age }}岁 | {{ item.exp }}年经验 | {{ item.type }}</text>
        <view class="tags">
          <text v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      staff: [
        { name: '示例阿姨', salary: '¥8000-12000/月', city: '长沙市', age: 42, exp: 8, type: '月嫂', tags: ['经验丰富', '亲和力强'] },
        { name: '服务人员', salary: '¥6000-9000/月', city: '岳阳市', age: 39, exp: 6, type: '保姆', tags: ['责任心强', '擅长家务'] },
      ],
    }
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
  padding: 24rpx;
  border-radius: 999rpx;
  background: #ffffff;
  color: #a2a8b1;
  font-size: 28rpx;
}

.filters {
  display: flex;
  justify-content: space-around;
  padding: 28rpx 0;
  color: #3b414c;
  font-size: 28rpx;
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

.info {
  flex: 1;
}

.row {
  display: flex;
  justify-content: space-between;
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

.meta {
  display: block;
  margin: 14rpx 0;
  color: #6d7480;
  font-size: 24rpx;
}

.tags {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.tag {
  padding: 8rpx 14rpx;
  border-radius: 8rpx;
  background: #fff0f2;
  color: #ef3f5f;
  font-size: 22rpx;
}
</style>
