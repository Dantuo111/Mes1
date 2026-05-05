import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  {
    path: '/',
    component: () => import('../layout/FrontLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/front/Home.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/front/user/Profile.vue') },
      { path: 'my-orders', name: 'MyWorkOrders', component: () => import('../views/front/user/WorkOrders.vue') },
      { path: 'plans', name: 'FrontPlans', component: () => import('../views/front/Plans.vue') },
      { path: 'materials', name: 'FrontMaterials', component: () => import('../views/front/Materials.vue') },
      { path: 'quality', name: 'FrontQuality', component: () => import('../views/front/Quality.vue') },
      { path: 'my-stats', name: 'MyStats', component: () => import('../views/front/user/MyStats.vue') },
      { path: 'my-requests', name: 'MyRequests', component: () => import('../views/front/user/MyRequests.vue') },
      { path: 'my-feedback', name: 'MyFeedback', component: () => import('../views/front/user/MyFeedback.vue') }
    ]
  },
  {
    path: '/admin',
    component: () => import('../layout/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'users', name: 'Users', component: () => import('../views/admin/Users.vue') },
      { path: 'plans', name: 'ProductionPlans', component: () => import('../views/admin/ProductionPlans.vue') },
      { path: 'orders', name: 'WorkOrders', component: () => import('../views/admin/WorkOrders.vue') },
      { path: 'materials', name: 'Materials', component: () => import('../views/admin/Materials.vue') },
      { path: 'quality', name: 'Quality', component: () => import('../views/admin/Quality.vue') },
      { path: 'material-requests', name: 'MaterialRequests', component: () => import('../views/admin/MaterialRequests.vue') },
      { path: 'quality-feedback', name: 'QualityFeedback', component: () => import('../views/admin/QualityFeedback.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || '{}')

  // 登录和注册页面不需要认证
  if (to.path === '/login' || to.path === '/register') {
    next()
    return
  }

  // 未登录跳转登录页
  if (!token) {
    next('/login')
    return
  }

  // 管理员路由权限检查
  if (to.matched.some(record => record.meta.requiresAdmin)) {
    if (user.role !== 1) {
      next('/')
      return
    }
  }

  next()
})

export default router
