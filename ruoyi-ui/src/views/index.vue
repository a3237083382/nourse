<template>
  <div class="dashboard">
    <section class="hero">
      <div>
        <p class="eyebrow">运营工作台</p>
        <h1>家政到家服务管理后台</h1>
        <p class="summary">集中处理客户需求、阿姨资料、推荐匹配、预约面试和小程序内容消息。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="go('/demand/list')">处理用户需求</el-button>
        <el-button @click="go('/staff/list')">维护服务人员</el-button>
      </div>
    </section>

    <section class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <span class="metric-label">{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <span class="metric-desc">{{ item.desc }}</span>
      </div>
    </section>

    <section class="content-grid">
      <div class="panel">
        <div class="panel-head">
          <h2>今日重点</h2>
          <span>按流程优先处理</span>
        </div>
        <div v-for="item in tasks" :key="item.title" class="task" @click="go(item.path)">
          <div>
            <strong>{{ item.title }}</strong>
            <p>{{ item.desc }}</p>
          </div>
          <el-tag :type="item.type" effect="light">{{ item.status }}</el-tag>
        </div>
      </div>

      <div class="panel">
        <div class="panel-head">
          <h2>常用入口</h2>
          <span>客户后台只保留业务功能</span>
        </div>
        <div class="shortcut-grid">
          <button v-for="item in shortcuts" :key="item.title" type="button" class="shortcut" @click="go(item.path)">
            <span>{{ item.icon }}</span>
            <strong>{{ item.title }}</strong>
            <small>{{ item.desc }}</small>
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup name="Index" lang="ts">
import router from '@/router';

const metrics = [
  { label: '服务人员', value: '已上线', desc: '维护阿姨资料、证书、照片与上下架' },
  { label: '用户需求', value: '待审核', desc: '审核通过后可推荐合适阿姨' },
  { label: '预约面试', value: '待跟进', desc: '线下联系客户并更新处理状态' },
  { label: '内容消息', value: '已接入', desc: '首页内容、协议、FAQ 和系统消息' }
];

const tasks = [
  { title: '审核新发布需求', desc: '确认服务类型、联系人和地址信息，审核通过后进入推荐流程。', status: '需求管理', type: 'danger', path: '/demand/list' },
  { title: '推荐合适阿姨', desc: '在已通过需求中选择服务人员，客户会在小程序查看推荐列表。', status: '推荐匹配', type: 'success', path: '/demand/list' },
  { title: '处理预约面试', desc: '查看客户提交的邀约，线下联系后更新为已联系、已安排或已完成。', status: '预约面试', type: 'warning', path: '/interview/list' },
  { title: '维护小程序内容', desc: '更新首页轮播、签约动态、FAQ、协议、隐私政策和关于我们。', status: '内容配置', type: 'info', path: '/content/list' }
];

const shortcuts = [
  { icon: '类', title: '服务分类', desc: '维护月嫂、保姆等分类', path: '/staff/category' },
  { icon: '姨', title: '服务人员', desc: '录入和上下架阿姨', path: '/staff/list' },
  { icon: '需', title: '用户需求', desc: '审核与推荐阿姨', path: '/demand/list' },
  { icon: '约', title: '预约面试', desc: '跟进客户邀约', path: '/interview/list' },
  { icon: '文', title: '内容配置', desc: '维护小程序展示内容', path: '/content/list' }
];

const go = (path: string) => {
  router.push(path);
};
</script>

<style lang="scss" scoped>
.dashboard {
  min-height: calc(100vh - 84px);
  padding: 24px;
  background: #f5f6f4;
  color: #20252b;
}

.hero {
  display: flex;
  min-height: 188px;
  align-items: flex-end;
  justify-content: space-between;
  gap: 32px;
  padding: 34px;
  border-radius: 22px;
  background:
    radial-gradient(circle at 88% 16%, rgba(232, 77, 100, 0.28), transparent 26%),
    linear-gradient(135deg, #20252b 0%, #2d3831 100%);
  color: #fff;
  box-shadow: 0 18px 44px rgba(32, 37, 43, 0.16);
}

.eyebrow {
  margin: 0 0 10px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 14px;
}

h1 {
  margin: 0;
  font-size: 32px;
  line-height: 1.25;
  font-weight: 800;
}

.summary {
  max-width: 560px;
  margin: 12px 0 0;
  color: rgba(255, 255, 255, 0.74);
  font-size: 15px;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  flex: 0 0 auto;
  gap: 12px;
}

.hero-actions :deep(.el-button--primary) {
  border-color: #e84d64;
  background: #e84d64;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-top: 18px;
}

.metric-card,
.panel {
  border: 1px solid rgba(32, 37, 43, 0.06);
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(32, 37, 43, 0.05);
}

.metric-card {
  padding: 22px;
}

.metric-label,
.metric-desc {
  display: block;
  color: #7b8490;
}

.metric-label {
  font-size: 13px;
}

.metric-card strong {
  display: block;
  margin: 10px 0 8px;
  font-size: 25px;
}

.metric-desc {
  font-size: 13px;
  line-height: 1.6;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(0, 0.95fr);
  gap: 18px;
  margin-top: 18px;
}

.panel {
  padding: 24px;
}

.panel-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;

  h2 {
    margin: 0;
    font-size: 20px;
  }

  span {
    color: #8a929b;
    font-size: 13px;
  }
}

.task {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 0;
  border-top: 1px solid #eef0ec;
  cursor: pointer;

  &:first-of-type {
    border-top: 0;
  }

  strong {
    font-size: 15px;
  }

  p {
    margin: 7px 0 0;
    color: #747d86;
    font-size: 13px;
    line-height: 1.6;
  }
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.shortcut {
  min-height: 116px;
  padding: 18px;
  border: 1px solid #eef0ec;
  border-radius: 16px;
  background: #f8f9f6;
  color: #20252b;
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;

  &:hover {
    box-shadow: 0 12px 26px rgba(32, 37, 43, 0.08);
    transform: translateY(-2px);
  }

  span {
    display: inline-flex;
    width: 32px;
    height: 32px;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    background: #e84d64;
    color: #fff;
    font-weight: 800;
  }

  strong,
  small {
    display: block;
  }

  strong {
    margin-top: 12px;
    font-size: 15px;
  }

  small {
    margin-top: 6px;
    color: #7b8490;
  }
}

@media (max-width: 1180px) {
  .metric-grid,
  .content-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .hero,
  .content-grid,
  .metric-grid {
    grid-template-columns: 1fr;
  }

  .hero {
    display: block;
  }

  .hero-actions {
    margin-top: 24px;
  }
}
</style>
