<template>
  <n-space vertical>
    <n-card size="small">
      <n-space align="center">
        <span>自动接单：</span>
        <n-switch :value="autoAccept" @update:value="toggleAutoAccept" />
        <n-button secondary size="small" @click="loadAll">刷新</n-button>
      </n-space>
    </n-card>

    <n-tabs type="line" v-model:value="tab">
      <!-- 待接单 -->
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

      <!-- 制作中 -->
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

      <!-- 未上菜（已出餐待端上桌） -->
      <n-tab-pane name="notServed" :tab="`未上菜 (${notServed.length})`">
        <n-empty v-if="!notServed.length" description="暂无待上菜菜品" />
        <div class="task-grid">
          <n-card v-for="t in notServed" :key="t.id" size="small" class="task-card">
            <n-space justify="space-between">
              <b>{{ t.dishName }} × {{ t.quantity }}</b>
              <n-tag type="warning" size="small" :bordered="false">已出餐</n-tag>
            </n-space>
            <div class="meta">{{ t.orderNo }} · {{ t.tableCode || '-' }} · {{ t.areaName || '-' }}</div>
            <n-button type="primary" size="small" block style="margin-top: 8px" @click="doServe(t.id)">上菜</n-button>
          </n-card>
        </div>
      </n-tab-pane>

      <!-- 已上菜 -->
      <n-tab-pane name="served" :tab="`已上菜 (${served.length})`">
        <n-empty v-if="!served.length" description="暂无已上菜记录" />
        <div class="task-grid">
          <n-card v-for="t in served" :key="t.id" size="small" class="task-card served-card">
            <n-space justify="space-between">
              <b>{{ t.dishName }} × {{ t.quantity }}</b>
              <n-tag type="success" size="small" :bordered="false">已上菜</n-tag>
            </n-space>
            <div class="meta">{{ t.orderNo }} · {{ t.tableCode || '-' }}</div>
          </n-card>
        </div>
      </n-tab-pane>

      <!-- 桌台清理 -->
      <n-tab-pane name="clean" :tab="`桌台清理 (${dirtyTables.length})`">
        <n-space style="margin-bottom: 12px">
          <n-button secondary size="small" @click="loadTables">刷新</n-button>
        </n-space>
        <n-empty v-if="!dirtyTables.length" description="暂无需清理的桌台" />
        <div class="task-grid">
          <n-card v-for="t in dirtyTables" :key="t.id" size="small" class="task-card">
            <n-space justify="space-between">
              <b>{{ t.name || t.code }}</b>
              <n-tag type="warning" size="small" :bordered="false">待清理</n-tag>
            </n-space>
            <div class="meta">{{ t.code }} · {{ t.areaName || '-' }} · 容量 {{ t.capacity }}</div>
            <div v-if="cleanAssignees[t.id]" class="meta">清理人：{{ cleanAssignees[t.id] }}</div>
            <n-button type="success" size="small" block style="margin-top: 8px" @click="doCleanTable(t)">清理完成</n-button>
          </n-card>
        </div>
      </n-tab-pane>
    </n-tabs>
  </n-space>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, computed, watch } from 'vue'
import {
  NSpace, NCard, NButton, NTag, NEmpty, NTabs, NTabPane, NSwitch, useMessage
} from 'naive-ui'
import * as kitchenApi from '@/api/kitchen'
import * as tableApi from '@/api/table'
import { createStompClient } from '@/utils/ws'

const message = useMessage()
const tasks = ref([])
const tables = ref([])
const cleanAssignees = ref({}) // tableId -> 清理人姓名
const autoAccept = ref(false)
const tab = ref('pending')
let stompClient = null

// 任务分组：0待接单 1制作中 2已完成（未上菜/已上菜按 serveStatus 区分）
const pending = computed(() => tasks.value.filter((t) => t.status === 0))
const accepted = computed(() => tasks.value.filter((t) => t.status === 1))
const notServed = computed(() => tasks.value.filter((t) => t.status === 2 && t.serveStatus !== 1))
const served = computed(() => tasks.value.filter((t) => t.status === 2 && t.serveStatus === 1))
// 桌台清理：状态 3 = 待清理
const dirtyTables = computed(() => tables.value.filter((t) => t.status === 3))

async function loadTasks() {
  tasks.value = (await kitchenApi.listKitchenTasks()) || []
  autoAccept.value = await kitchenApi.getAutoAccept()
}

async function loadTables() {
  tables.value = (await tableApi.listTables()) || []
  // 未完成的清理派发 → 桌台到清理人映射（后厨显示谁负责清理）
  try {
    const d = await tableApi.listCleanTasks({ pageNum: 1, pageSize: 100, status: 0 })
    const map = {}
    ;(d.list || []).forEach((t) => { map[t.tableId] = t.assigneeName })
    cleanAssignees.value = map
  } catch {
    cleanAssignees.value = {}
  }
}

async function loadAll() {
  loadTasks()
  loadTables()
}

// 切到桌台清理 tab 时刷新桌台列表
watch(tab, (v) => { if (v === 'clean') loadTables() })

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

async function doServe(id) {
  await kitchenApi.serveTask(id)
  message.success('已上菜')
  loadTasks()
}

async function doCleanTable(t) {
  await tableApi.completeCleanByTable(t.id)
  message.success(`桌台 ${t.name || t.code} 清理完成`)
  loadTables()
}

async function toggleAutoAccept(v) {
  await kitchenApi.setAutoAccept(v)
  autoAccept.value = v
  message.success('已更新')
}

onMounted(() => {
  loadAll()
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
  border-left: 3px solid #f0a020;
}
.task-card :deep(.n-card__content) {
  padding: 10px 12px;
}
.served-card {
  border-left-color: #18a058;
  opacity: 0.8;
}
.meta {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
}
</style>