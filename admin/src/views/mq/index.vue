<template>
  <n-space vertical>
    <!-- 搜索栏 -->
    <n-space style="margin-bottom: 12px" align="center">
      <n-input v-model:value="query.messageKey" clearable placeholder="消息键" style="width: 220px" @keyup.enter="handleSearch" />
      <n-select v-model:value="query.deliverStatus" :options="statusOptions" clearable placeholder="投递状态" style="width: 140px" @update:value="handleSearch" />
      <n-button @click="handleSearch">查询</n-button>
      <n-button quaternary @click="handleReset">重置</n-button>
    </n-space>

    <n-data-table
      :columns="columns"
      :data="rows"
      :loading="loading"
      remote
      :pagination="pagination"
      :scroll-x="1400"
      @update:page="changePage"
    />
  </n-space>
</template>

<script setup>
import { h, onMounted, ref } from 'vue'
import {
  NSpace, NInput, NSelect, NButton, NDataTable, NTag, NPopconfirm, useMessage
} from 'naive-ui'
import { listMqMessages, retryMqMessage } from '@/api/mq'

const message = useMessage()
const rows = ref([])
const loading = ref(false)
const total = ref(0)

const query = ref({ messageKey: '', deliverStatus: null, pageNum: 1, pageSize: 10 })

const statusMap = {
  0: ['待投递', 'warning'],
  1: ['已投递', 'success'],
  2: ['投递失败', 'error']
}
const statusOptions = Object.entries(statusMap).map(([value, [label]]) => ({ label, value: Number(value) }))

const pagination = ref({ pageSize: 10, itemCount: 0 })

const columns = [
  { title: '消息键', key: 'messageKey', width: 220, ellipsis: { tooltip: true } },
  { title: '主题', key: 'topic', width: 120 },
  { title: '标签', key: 'tag', width: 100 },
  { title: '业务类型', key: 'bizType', width: 90 },
  { title: '业务键', key: 'bizKey', width: 110 },
  {
    title: '投递状态', key: 'deliverStatus', width: 90,
    render: (r) => {
      const [label, type] = statusMap[r.deliverStatus] || ['未知', 'warning']
      return h(NTag, { type, size: 'small' }, { default: () => label })
    }
  },
  { title: '重试次数', key: 'retryCount', width: 80 },
  { title: '下次重试', key: 'nextRetryTime', width: 160 },
  { title: '错误信息', key: 'lastError', width: 160, ellipsis: { tooltip: true }, render: (r) => r.lastError || '-' },
  { title: '投递时间', key: 'sentTime', width: 160 },
  { title: '创建时间', key: 'createTime', width: 160 },
  {
    title: '操作', key: 'action', width: 80, fixed: 'right',
    render: (r) => r.deliverStatus === 1
      ? '-'
      : h(NPopconfirm, {
          onPositiveClick: () => handleRetry(r)
        }, {
          trigger: () => h(NButton, { size: 'small', text: true }, { default: () => '重试' }),
          default: () => '确认重新投递该消息？'
        })
  }
]

async function load() {
  loading.value = true
  try {
    const data = await listMqMessages(query.value)
    rows.value = data?.list || []
    total.value = data?.total || 0
    pagination.value.itemCount = total.value
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.value.pageNum = 1
  load()
}

function handleReset() {
  query.value = { messageKey: '', deliverStatus: null, pageNum: 1, pageSize: 10 }
  load()
}

function changePage(page) {
  query.value.pageNum = page
  load()
}

async function handleRetry(row) {
  await retryMqMessage(row.id)
  message.success(`已触发重试：${row.messageKey}`)
  load()
}

onMounted(load)
</script>