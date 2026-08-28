<template>
  <n-space vertical>
    <!-- 搜索栏 -->
    <n-space align="center" style="margin-bottom: 12px" wrap>
      <n-input v-model:value="query.username" clearable placeholder="用户名" style="width: 180px" @keyup.enter="handleSearch" />
      <n-select v-model:value="query.status" :options="statusOptions" clearable placeholder="登录状态" style="width: 130px" />
      <n-date-picker v-model:value="timeRange" type="daterange" clearable placeholder="登录时间范围" style="width: 260px" />
      <n-button type="primary" @click="handleSearch">查询</n-button>
      <n-button quaternary @click="handleReset">重置</n-button>
      <n-button @click="load">刷新</n-button>
    </n-space>

    <n-data-table
      remote
      :columns="columns"
      :data="rows"
      :loading="loading"
      :pagination="pagination"
      :scroll-x="1100"
      @update:page="changePage"
      @update:page-size="changePageSize"
    />
  </n-space>
</template>

<script setup>
import { h, onMounted, ref } from 'vue'
import { NDataTable, NSpace, NInput, NSelect, NButton, NDatePicker, NTag } from 'naive-ui'
import { listLoginLogs } from '@/api/system'
import { formatDateTime, toTimeRange } from '@/utils/format'

const rows = ref([])
const loading = ref(false)
const timeRange = ref(null)

const query = ref({ username: '', status: null, startTime: undefined, endTime: undefined, pageNum: 1, pageSize: 10 })

const statusMap = {
  1: ['成功', 'success'],
  0: ['失败', 'error']
}
const statusOptions = Object.entries(statusMap).map(([value, [label]]) => ({ label, value: Number(value) }))

const pagination = ref({
  page: 1, pageSize: 10, itemCount: 0,
  showSizePicker: true, pageSizes: [10, 20, 50],
  prefix: ({ itemCount }) => `共 ${itemCount} 条`
})

const columns = [
  { title: '用户名', key: 'username', width: 120 },
  { title: 'IP', key: 'ip', width: 140 },
  { title: '登录地点', key: 'location', width: 160, ellipsis: { tooltip: true }, render: (r) => r.location || '-' },
  { title: '浏览器', key: 'browser', width: 150, ellipsis: { tooltip: true }, render: (r) => r.browser || '-' },
  { title: '操作系统', key: 'os', width: 110, ellipsis: { tooltip: true }, render: (r) => r.os || '-' },
  {
    title: '状态', key: 'status', width: 80,
    render: (r) => {
      const [label, type] = statusMap[r.status] || ['未知', 'default']
      return h(NTag, { type, size: 'small' }, { default: () => label })
    }
  },
  {
    title: '失败原因', key: 'message', width: 190,
    ellipsis: { tooltip: true },
    render: (r) => (r.status === 0 && r.message ? r.message : '-')
  },
  {
    title: '登录时间', key: 'loginTime', width: 175,
    render: (r) => formatDateTime(r.loginTime)
  }
]

async function load() {
  loading.value = true
  try {
    const data = await listLoginLogs(query.value)
    rows.value = data?.list || []
    pagination.value.itemCount = data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.value.pageNum = 1
  query.value.startTime = toTimeRange(timeRange.value).startTime
  query.value.endTime = toTimeRange(timeRange.value).endTime
  load()
}

function handleReset() {
  timeRange.value = null
  query.value = { username: '', status: null, startTime: undefined, endTime: undefined, pageNum: 1, pageSize: pagination.value.pageSize }
  load()
}

function changePage(page) {
  query.value.pageNum = page
  pagination.value.page = page
  load()
}

function changePageSize(size) {
  query.value.pageSize = size
  pagination.value.pageSize = size
  query.value.pageNum = 1
  pagination.value.page = 1
  load()
}

onMounted(load)
</script>