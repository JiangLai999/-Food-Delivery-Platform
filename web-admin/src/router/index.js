import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard',
    component: () => import('../components/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('../views/UserManagement.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'merchants',
        name: 'MerchantManagement',
        component: () => import('../views/MerchantManagement.vue'),
        meta: { title: '商家管理' }
      },
      {
        path: 'categories',
        name: 'CategoryManagement',
        component: () => import('../views/CategoryManagement.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'notices',
        name: 'NoticeManagement',
        component: () => import('../views/NoticeManagement.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'reviews',
        name: 'ReviewAudit',
        component: () => import('../views/ReviewAudit.vue'),
        meta: { title: '内容审核' }
      },
      {
        path: 'messages',
        name: 'MessageCenter',
        component: () => import('../views/MessageCenter.vue'),
        meta: { title: '客服中心' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue'),
        meta: { title: '系统设置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token || localStorage.getItem('token')

  if (to.meta.requiresAuth !== false && !token) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else if (to.path === '/login' && token) {
    // 已登录访问登录页，跳转到首页
    next('/dashboard')
  } else {
    next()
  }
})

export default router
