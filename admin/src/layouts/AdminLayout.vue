<template>
  <n-layout has-sider style="height: 100vh; overflow: hidden">
    <n-layout-sider bordered :width="220" :native-scrollbar="false" style="min-width: 220px">
      <div style="height: 100%; display: flex; flex-direction: column">
        <div style="padding: 18px 20px; font-weight: 700; font-size: 16px; color: #18a058; border-bottom: 1px solid #eee">
          智慧点餐·管理端
        </div>
        <div style="flex: 1; overflow-y: auto">
          <n-menu
            :value="active"
            :options="menuOptions"
            :indented="true"
            :on-update:value="handleMenu"
          />
        </div>
      </div>
    </n-layout-sider>
    <n-layout style="min-width: 0">
      <n-layout-header bordered style="height: 52px; display: flex; align-items: center; justify-content: space-between; padding: 0 16px">
        <div style="font-weight: 600">{{ title }}</div>
        <n-space>
          <span style="color: #666">{{ userInfo?.nickname || '管理员' }}</span>
          <n-button size="small" quaternary @click="handleLogout">退出</n-button>
        </n-space>
      </n-layout-header>
      <n-layout-content style="padding: 16px; background: #f5f7fa; overflow: auto">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NLayout, NLayoutSider, NLayoutHeader, NLayoutContent, NMenu, NButton, NSpace } from 'naive-ui'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

const userInfo = computed(() => user.userInfo)
const active = computed(() => route.name)
const title = computed(() => route.meta.title || '')

const menuOptions = [
  {
    type: 'group', label: '点餐核心', key: 'core', children: [
      { label: '菜品管理', key: 'dish' },
      { label: '桌台管理', key: 'table' },
      { label: '订单管理', key: 'order' },
      { label: '后厨', key: 'kitchen' }
    ]
  },
  {
    type: 'group', label: '运营管理', key: 'ops', children: [
      { label: '会员管理', key: 'member' },
      { label: '优惠券', key: 'coupon' },
      { label: '支付管理', key: 'payment' },
      { label: '评价管理', key: 'review' },
      { label: '反馈管理', key: 'feedback' },
      { label: '首页轮播', key: 'banner' }
    ]
  },
  {
    type: 'group', label: '系统管理', key: 'sys', children: [
      { label: '用户管理', key: 'system-user' },
      { label: '角色管理', key: 'system-role' },
      { label: '菜单管理', key: 'system-menu' },
      { label: '字典管理', key: 'system-dict' },
      { label: '系统配置', key: 'system-config' },
      { label: '登录日志', key: 'system-login-log' },
      { label: '操作日志', key: 'system-operation-log' }
    ]
  }
]

function handleMenu(key) {
  router.push({ name: key })
}

function handleLogout() {
  user.logout()
  router.push('/login')
}
</script>