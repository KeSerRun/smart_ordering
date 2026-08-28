<template>
  <n-space vertical>
    <n-card size="small">
      <n-space align="center">
        <span>自动接单：</span>
        <n-switch :value="autoAccept" @update:value="toggleAutoAccept" />
        <n-button secondary size="small" @click="loadTasks">刷新</n-button>
      </n-space>
    </n-card>

    <n-tabs type="line" v-model:value="tab">
      <n-tab-pane name="pending" :tab="`待接单 (${pending.length})`">
        <n-empty v-if="!pending.length" description="暂无待接单任务" />
        <div class="task-grid">
          <n-card v-for="t in pending" :key="t.id" size="small" class="task-card">
            <n-space justify="space-between">
              <b>{{ t.dishName }} × {{ t.quantity }}</b>
              <n-tag type="warning" size="small" :bordered="false">待接</n-tag>
            </n-space>
            <div class="meta">{{ t.orderNo }} · {{ t.tableCode || '-' }} · {{ t.areaName || '-' }}</div>
            <div class="meta">{{ t.remark || '无备注' }}</div>
            <n-button type="primary" size="small" block style="margin-top: 8px" @click="doAccept(t.id)">接单</n-button>
          </n-card>
        </div>
      </n-tab-pane>

      <n-tab-pane name="accepted" :tab="`制作中 (${accepted.length})`">
        <n-empty v-if="!accepted.length" description="暂无制作中任务" />
        <div class="task-grid">
          <n-card v-for="t in accepted" :key="t.id" size="small" class="task-card">
            <n-space justify="space-between">
              <b>{{ t.dishName }} × {{ t.quantity }}</b>
              <n-tag type="info" size="small" :bordered="false">制作中</n-tag>
            </n-space>
            <div class="meta">{{ t.orderNo }} · {{ t.tableCode || '-' }}</div>
            <n-button type="success" size="small" block style="margin-top: 8px" @click="doComplete(t.id)">出餐</n-button>
          </n-card>
        </div>
      </n-tab-pane>
    </n-tabs>
  </n-space>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, computed } from 'vue'
import {
  NSpace, NCard, NButton, NTag, NEmpty, NTabs, NTabPane, NSwitch, useMessage
} from 'naive-ui'
import * as kitchenApi from '@/api/kitchen'
import { createStompClient } from '@/utils/ws'

const message = useMessage()
const tasks = ref([])
const autoAccept = ref(false)
const tab = ref('pending')
let stompClient = null

const STATUS = { 0: 'pending', 1: 'accepted', 2: 'done' }

const pending = computed(() => tasks.value.filter((t) => t.status === 0))
const accepted = computed(() => tasks.value.filter((t) => t.status === 1))

async function loadTasks() {
  tasks.value = (await kitchenApi.listKitchenTasks()) || []
  autoAccept.value = await kitchenApi.getAutoAccept()
}

// WebSocket 推送：服务端把全量后厨任务列表广播到 /topic/kitchen，直接整体替换
function onWsMessage(body) {
  if (body && Array.isArray(body.data)) {
    tasks.value = body.data
  }
}

async function doAccept(id) {
  await kitchenApi.acceptTask(id)
  message.success('已接单')
  loadTasks()
}

async function doComplete(id) {
  await kitchenApi.completeTask(id)
  message.success('已出餐')
  loadTasks()
}

async function toggleAutoAccept(v) {
  await kitchenApi.setAutoAccept(v)
  autoAccept.value = v
  message.success('已更新')
}

onMounted(() => {
  loadTasks()
  stompClient = createStompClient({ onMessage: onWsMessage })
  stompClient.activate()
})

onBeforeUnmount(() => {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }
})
</script>

<style scoped>
.task-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}
.task-card {
  margin-bottom: 0;
}
.meta {
  color: #888;
  font-size: 12px;
  margin-top: 4px;
}
</style>