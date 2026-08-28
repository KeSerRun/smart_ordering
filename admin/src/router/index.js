import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  { path: '/login', name: 'login', component: () => import('@/views/login/index.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dish',
    children: [
      // 点餐核心
      { path: 'dish', name: 'dish', component: () => import('@/views/dish/index.vue'), meta: { title: '菜品管理' } },
      { path: 'table', name: 'table', component: () => import('@/views/table/index.vue'), meta: { title: '桌台管理' } },
      { path: 'order', name: 'order', component: () => import('@/views/order/index.vue'), meta: { title: '订单管理' } },
      { path: 'kitchen', name: 'kitchen', component: () => import('@/views/kitchen/index.vue'), meta: { title: '后厨' } },
      // 运营管理
      { path: 'member', name: 'member', component: () => import('@/views/member/index.vue'), meta: { title: '会员管理' } },
      { path: 'coupon', name: 'coupon', component: () => import('@/views/coupon/index.vue'), meta: { title: '优惠券' } },
      { path: 'payment', name: 'payment', component: () => import('@/views/payment/index.vue'), meta: { title: '支付管理' } },
      { path: 'review', name: 'review', component: () => import('@/views/review/index.vue'), meta: { title: '评价管理' } },
      { path: 'feedback', name: 'feedback', component: () => import('@/views/feedback/index.vue'), meta: { title: '反馈管理' } },
      { path: 'banner', name: 'banner', component: () => import('@/views/banner/index.vue'), meta: { title: '首页轮播' } },
      // 系统管理
      { path: 'system/user', name: 'system-user', component: () => import('@/views/system/user/index.vue'), meta: { title: '用户管理' } },
      { path: 'system/role', name: 'system-role', component: () => import('@/views/system/role/index.vue'), meta: { title: '角色管理' } },
      { path: 'system/login-log', name: 'system-login-log', component: () => import('@/views/system/login-log/index.vue'), meta: { title: '登录日志' } },
      { path: 'system/operation-log', name: 'system-operation-log', component: () => import('@/views/system/operation-log/index.vue'), meta: { title: '操作日志' } },
      { path: 'mq', name: 'mq', component: () => import('@/views/mq/index.vue'), meta: { title: 'MQ 消息' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach((to) => {
  const user = useUserStore()
  if (!to.meta.public && !user.token) return '/login'
  if (to.path === '/login' && user.token) return '/'
})
export default router