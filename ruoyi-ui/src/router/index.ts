import { createWebHistory, createRouter, RouteRecordRaw } from 'vue-router';
import Layout from '@/layout/index.vue';

export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/social-callback',
    hidden: true,
    component: () => import('@/layout/components/SocialCallback/index.vue')
  },
  {
    path: '/login',
    component: () => import('@/views/login.vue'),
    hidden: true
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/error/404.vue'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401.vue'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: '/index',
        component: () => import('@/views/index.vue'),
        name: 'Index',
        meta: { title: '首页', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index.vue'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  },
  {
    path: '/staff',
    component: Layout,
    redirect: '/staff/category',
    name: 'StaffManage',
    meta: { title: '家政管理', icon: 'peoples' },
    children: [
      {
        path: 'category',
        component: () => import('@/views/staff/category/index.vue'),
        name: 'ServiceCategory',
        meta: { title: '服务分类', icon: 'tree' }
      },
      {
        path: 'list',
        component: () => import('@/views/staff/index.vue'),
        name: 'ServiceStaff',
        meta: { title: '服务人员', icon: 'user' }
      }
    ]
  },
  {
    path: '/demand',
    component: Layout,
    redirect: '/demand/list',
    name: 'DemandManageRoot',
    meta: { title: '需求管理', icon: 'list' },
    children: [
      {
        path: 'list',
        component: () => import('@/views/demand/index.vue'),
        name: 'DemandManage',
        meta: { title: '用户需求', icon: 'clipboard' }
      }
    ]
  },
  {
    path: '/interview',
    component: Layout,
    redirect: '/interview/list',
    name: 'InterviewManageRoot',
    meta: { title: '预约面试', icon: 'message' },
    children: [
      {
        path: 'list',
        component: () => import('@/views/interview/index.vue'),
        name: 'InterviewManage',
        meta: { title: '预约面试', icon: 'phone' }
      }
    ]
  },
  {
    path: '/content',
    component: Layout,
    redirect: '/content/list',
    name: 'ContentManageRoot',
    meta: { title: '内容管理', icon: 'edit' },
    children: [
      {
        path: 'list',
        component: () => import('@/views/content/index.vue'),
        name: 'ContentManage',
        meta: { title: '内容配置', icon: 'documentation' }
      }
    ]
  },
  {
    path: '/contract',
    component: Layout,
    redirect: '/contract/list',
    name: 'ContractManageRoot',
    meta: { title: '合同管理', icon: 'documentation' },
    children: [
      {
        path: 'list',
        component: () => import('@/views/contract/index.vue'),
        name: 'ContractManage',
        meta: { title: '合同列表', icon: 'form' }
      }
    ]
  },
  {
    path: '/order',
    component: Layout,
    redirect: '/order/service',
    name: 'OrderManageRoot',
    meta: { title: '订单管理', icon: 'shopping' },
    children: [
      {
        path: 'service',
        component: () => import('@/views/order/service.vue'),
        name: 'ServiceOrderManage',
        meta: { title: '服务订单', icon: 'date' }
      }
    ]
  },
  {
    path: '/group',
    component: Layout,
    redirect: '/group/products',
    name: 'GroupManageRoot',
    meta: { title: '团购管理', icon: 'shopping' },
    children: [
      {
        path: 'products',
        component: () => import('@/views/group/product.vue'),
        name: 'GroupProductManage',
        meta: { title: '团购商品', icon: 'goods' }
      },
      {
        path: 'teams',
        component: () => import('@/views/group/team.vue'),
        name: 'GroupTeamManage',
        meta: { title: '拼团记录', icon: 'peoples' }
      },
      {
        path: 'orders',
        component: () => import('@/views/group/order.vue'),
        name: 'GroupOrderManage',
        meta: { title: '团购订单', icon: 'money' }
      }
    ]
  }
];

export const dynamicRoutes: RouteRecordRaw[] = [];

const router = createRouter({
  history: createWebHistory(import.meta.env.VITE_APP_CONTEXT_PATH),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }
    return { top: 0 };
  }
});

export default router;
