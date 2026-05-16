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
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
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
        path: 'shop',
        name: 'ShopManage',
        component: () => import('../views/ShopManage.vue'),
        meta: { title: '店铺管理' }
      },
      {
        path: 'foods',
        name: 'FoodManage',
        component: () => import('../views/FoodManage.vue'),
        meta: { title: '菜品管理' }
      },
      {
        path: 'foods/add',
        name: 'FoodAdd',
        component: () => import('../views/FoodEdit.vue'),
        meta: { title: '添加菜品' }
      },
      {
        path: 'foods/edit/:id',
        name: 'FoodEdit',
        component: () => import('../views/FoodEdit.vue'),
        meta: { title: '编辑菜品' }
      },
      {
        path: 'orders',
        name: 'OrderManage',
        component: () => import('../views/OrderManage.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'orders/:id',
        name: 'OrderDetail',
        component: () => import('../views/OrderDetail.vue'),
        meta: { title: '订单详情' }
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('../views/Messages.vue'),
        meta: { title: '消息中心' }
      },
      {
        path: 'reviews',
        name: 'ReviewManage',
        component: () => import('../views/ReviewManage.vue'),
        meta: { title: '评价管理' }
      },
      {
        path: 'account',
        name: 'AccountManage',
        component: () => import('../views/AccountManage.vue'),
        meta: { title: '账户管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token || localStorage.getItem('merchant_token')

  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
