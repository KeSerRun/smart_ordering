<template>
  <n-space vertical>
    <!-- 搜索栏 -->
    <n-space align="center" style="margin-bottom: 12px" wrap>
      <n-input v-model:value="query.module" clearable placeholder="模块" style="width: 150px" @keyup.enter="handleSearch" />
      <n-input v-model:value="query.username" clearable placeholder="用户名" style="width: 150px" @keyup.enter="handleSearch" />
      <n-select v-model:value="query.status" :options="statusOptions" clearable placeholder="操作状态" style="width: 130px" />
      <n-date-picker v-model:value="timeRange" type="daterange" clearable placeholder="操作时间范围" style="width: 260px" />
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
      :scroll-x="1300"
      @update:page="changePage"
      @update:page-size="changePageSize"
    />

    <!-- 详情抽屉 -->
    <n-drawer v-model:show="showDrawer" :width="720" placement="right">
      <n-drawer-content title="操作日志详情" closable>
        <n-descriptions :column="2" label-placement="left" bordered size="small">
          <n-descriptions-item label="模块">{{ detail?.module || '-' }}</n-descriptions-item>
          <n-descriptions-item label="操作">{{ detail?.operation || '-' }}</n-descriptions-item>
          <n-descriptions-item label="用户">{{ detail?.username || '-' }}</n-descriptions-item>
          <n-descriptions-item label="IP">{{ detail?.ip || '-' }}</n-descriptions-item>
          <n-descriptions-item label="请求方式">{{ detail?.requestMethod || '-' }}</n-descriptions-item>
          <n-descriptions-item label="耗时">{{ detail?.duration != null ? `${detail.duration} ms` : '-' }}</n-descriptions-item>
          <n-descriptions-item label="状态">
            <n-tag :type="detail?.status === 1 ? 'success' : 'error'" size="small">
              {{ detail?.status === 1 ? '成功' : '失败' }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="创建时间">{{ formatDateTime(detail?.createTime) }}</n-descriptions-item>
          <n-descriptions-item label="请求路径" :span="2">{{ detail?.requestUrl || '-' }}</n-descriptions-item>
        </n-descriptions>

        <n-space vertical style="margin-top: 16px">
          <n-divider title-placement="left" style="margin: 8px 0">请求参数</n-divider>
          <pre class="log-code">{{ detail?.requestParams || '(无)' }}</pre>

          <n-divider v-if="detail?.status === 0" title-placement="left" style="margin: 8px 0">错误信息</n-divider>
          <pre v-if="detail?.status === 0" class="log-code log-error">{{ detail?.errorMsg || '(无)' }}</pre>

          <n-divider title-placement="left" style="margin: 8px 0">响应结果</n-divider>
          <pre class="log-code">{{ detail?.responseResult || '(无)' }}</pre>
        </n-space>
      </n-drawer-content>
    </n-drawer>
  </n-space>
</template>

<script setup>
import { h, onMounted, ref } from 'vue'
import {
  NDataTable, NSpace, NInput, NSelect, NButton, NDatePicker, NTag,
  NDrawer, NDrawerContent, NDescriptions, NDescriptionsItem, NDivider
} from 'naive-ui'
import { listOperationLogs } from '@/api/system'
import { formatDateTime, toTimeRange } from '@/utils/format'

const rows = ref([])
const loading = ref(false)
const timeRange = ref(null)

const query = ref({ module: '', username: '', status: null, startTime: undefined, endTime: undefined, pageNum: 1, pageSize: 10 })

const statusMap = {
  1: ['成功', 'success'],
  0: ['失败', 'error']
}
const statusOptions = Object.entries(statusMap).map(([value, [label]]) => ({ label, value: Number(value) }))

// HTTP 方法 → tag 颜色
const methodType = { GET: 'info', POST: 'success', PUT: 'warning', DELETE: 'error' }

const pagination = ref({
  page: 1, pageSize: 10, itemCount: 0,
  showSizePicker: true, pageSizes: [10, 20, 50],
  prefix: ({ itemCount }) => `共 ${itemCount} 条`
})

const showDrawer = ref(false)
const detail = ref(null)

const columns = [
  { title: '模块', key: 'module', width: 110, render: (r) => r.module || '-' },
  { title: '操作', key: 'operation', width: 150, ellipsis: { tooltip: true }, render: (r) => r.operation || '-' },
  {
    title: '请求方式', key: 'requestMethod', width: 95,
    render: (r) => h(NTag, { type: methodType[r.requestMethod] || 'default', size: 'small' }, { default: () => r.requestMethod || '-' })
  },
  { title: '请求路径', key: 'requestUrl', width: 220, ellipsis: { tooltip: true }, render: (r) => r.requestUrl || '-' },
  {
    title: '耗时', key: 'duration', width: 90,
    render: (r) => (r.duration != null ? `${r.duration} ms` : '-')
  },
  {
    title: '状态', key: 'status', width: 80,
    render: (r) => {
      const [label, type] = statusMap[r.status] || ['未知', 'default']
      return h(NTag, { type, size: 'small' }, { default: () => label })
    }
  },
  { title: '用户', key: 'username', width: 100, render: (r) => r.username || '-' },
  { title: 'IP', key: 'ip', width: 130, render: (r) => r.ip || '-' },
  { title: '创建时间', key: 'createTime', width: 175, render: (r) => formatDateTime(r.createTime) },
  {
    title: '操作', key: 'action', width: 70, fixed: 'right',
    render: (r) => h(NButton, { size: 'small', text: true, type: 'primary', onClick: () => openDetail(r) }, { default: () => '详情' })
  }
]

function openDetail(row) {
  detail.value = row
  showDrawer.value = true
}

async function load() {
  loading.value = true
  try {
    const data = await listOperationLogs(query.value)
    rows.value = data?.list || []
    pagination.value.itemCount = data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.value.pageNum = 1
  const range = toTimeRange(timeRange.value)
  query.value.startTime = range.startTime
  query.value.endTime = range.endTime
  load()
}

function handleReset() {
  timeRange.value = null
  query.value = { module: '', username: '', status: null, startTime: undefined, endTime: undefined, pageNum: 1, pageSize: pagination.value.pageSize }
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

<style scoped>
.log-code {
  margin: 0;
  padding: 10px 12px;
  background: var(--n-color, #f5f7fa);
  border: 1px solid var(--n-border-color, #e5e7eb);
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 220px;
  overflow: auto;
}
.log-error {
  color: #d03050;
  background: #fff1f0;
  border-color: #ffccc7;
}
</style>