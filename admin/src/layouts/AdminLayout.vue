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

// 模块分组定义：key 与后端 sys_user_module.module_code 一致
// kitchen（后厨）已从「点餐核心」拆出，独立成「后厨任务」模块
const MODULE_GROUPS = {
  core: {
    label: '点餐核心', key: 'core', children: [
      { label: '菜品管理', key: 'dish' },
      { label: '桌台管理', key: 'table' },
      { label: '订单管理', key: 'order' }
    ]
  },
  kitchen: {
    label: '后厨任务', key: 'kitchen', children: [
      { label: '后厨', key: 'kitchen' }
    ]
  },
  ops: {
    label: '运营管理', key: 'ops', children: [
      { label: '会员管理', key: 'member' },
      { label: '优惠券', key: 'coupon' },
      { label: '支付管理', key: 'payment' },
      { label: '评价管理', key: 'review' },
      { label: '反馈管理', key: 'feedback' },
      { label: '首页轮播', key: 'banner' }
    ]
  },
  sys: {
    label: '系统管理', key: 'sys', children: [
      { label: '用户管理', key: 'system-user' },
      { label: '角色管理', key: 'system-role' },
      { label: '登录日志', key: 'system-login-log' },
      { label: '操作日志', key: 'system-operation-log' },
      { label: 'MQ 消息', key: 'mq' }
    ]
  }
}

// 按用户模块权限过滤菜单；旧登录态（无 modules 字段）默认显示全部，兼容
const menuOptions = computed(() => {
  const granted = userInfo.value?.modules
  const hasGrant = Array.isArray(granted) && granted.length > 0
  return Object.values(MODULE_GROUPS)
    .filter((g) => !hasGrant || granted.includes(g.key))
    .map((g) => ({ type: 'group', label: g.label, key: g.key, children: g.children }))
})

function handleMenu(key) {
  router.push({ name: key })
}

function handleLogout() {
  user.logout()
  router.push('/login')
}
</script>