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
    path: '/register',
    component: () => import('@/views/register.vue'),
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
