<template>
  <n-space vertical>
    <n-tabs type="line" v-model:value="tab">
      <!-- 桌台 -->
      <n-tab-pane name="tables" tab="桌台">
        <n-space style="margin-bottom: 12px">
          <n-button type="primary" @click="openTableModal()">新增桌台</n-button>
          <n-button @click="handleGenAllQr">生成全部桌台二维码</n-button>
          <n-button @click="loadTables">刷新</n-button>
        </n-space>
        <n-data-table :columns="tableColumns" :data="tables" :loading="tableLoading" />
      </n-tab-pane>

      <!-- 桌区 -->
      <n-tab-pane name="areas" tab="桌区">
        <n-space style="margin-bottom: 12px">
          <n-button type="primary" @click="openAreaModal()">新增桌区</n-button>
        </n-space>
        <n-data-table :columns="areaColumns" :data="areas" :loading="areaLoading" />
      </n-tab-pane>
    </n-tabs>

    <!-- 桌台弹窗 -->
    <n-modal v-model:show="tableModal" preset="card" :title="tableForm.id ? '编辑桌台' : '新增桌台'" style="width: 480px">
      <n-form :model="tableForm" label-placement="left">
        <n-form-item label="桌号代码"><n-input v-model:value="tableForm.code" placeholder="如 A01" /></n-form-item>
        <n-form-item label="桌台名称"><n-input v-model:value="tableForm.name" placeholder="如 大厅A-01" /></n-form-item>
        <n-form-item label="容量"><n-input-number v-model:value="tableForm.capacity" min="1" /></n-form-item>
        <n-form-item label="所属桌区">
          <n-select v-model:value="tableForm.areaId" :options="areaOptions" clearable />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="tableModal = false">取消</n-button>
          <n-button type="primary" :loading="tableSaving" @click="saveTable">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 桌区弹窗 -->
    <n-modal v-model:show="areaModal" preset="card" :title="areaForm.id ? '编辑桌区' : '新增桌区'" style="width: 420px">
      <n-form :model="areaForm" label-placement="left">
        <n-form-item label="桌区名称"><n-input v-model:value="areaForm.name" placeholder="如 包间" /></n-form-item>
        <n-form-item label="排序"><n-input-number v-model:value="areaForm.sort" /></n-form-item>
        <n-form-item label="状态">
          <n-switch :value="areaForm.status === 1" @update:value="(v) => (areaForm.status = v ? 1 : 0)" />
        </n-form-item>
        <n-form-item label="备注"><n-input v-model:value="areaForm.remark" /></n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="areaModal = false">取消</n-button>
          <n-button type="primary" :loading="areaSaving" @click="saveArea">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-space>
</template>

<script setup>
import { h, onMounted, ref, computed } from 'vue'
import {
  NSpace, NTabs, NTabPane, NButton, NDataTable, NModal, NForm, NFormItem,
  NInput, NInputNumber, NSelect, NSwitch, NTag, useMessage
} from 'naive-ui'
import * as tableApi from '@/api/table'

const message = useMessage()
const tab = ref('tables')

const tables = ref([])
const areas = ref([])
const tableLoading = ref(false)
const areaLoading = ref(false)
const tableModal = ref(false)
const areaModal = ref(false)
const tableSaving = ref(false)
const areaSaving = ref(false)

const tableForm = ref({})
const areaForm = ref({})

const areaOptions = computed(() => areas.value.map((a) => ({ label: a.name, value: a.id })))

const tableStatusMap = { 0: ['空闲', 'success'], 1: ['占用', 'error'], 3: ['待清理', 'warning'] }

const tableColumns = [
  { title: '名称', key: 'name' },
  { title: '代码', key: 'code' },
  { title: '容量', key: 'capacity', width: 80 },
  { title: '桌区', key: 'areaName' },
  {
    title: '状态', key: 'status', width: 90,
    render: (r) => h(NTag, { type: (tableStatusMap[r.status] || ['-', 'default'])[1], size: 'small' }, () => (tableStatusMap[r.status] || ['-'])[0])
  },
  {
    title: '二维码', key: 'qr', width: 100,
    render: (r) => h(NButton, { size: 'small', text: true, onClick: () => downloadQr(r.id) }, { default: () => '下载' })
  },
  {
    title: '操作', key: 'action', width: 140,
    render: (r) =>
      h(NSpace, { size: 4, justify: 'center' }, () => [
        h(NButton, { size: 'small', text: true, onClick: () => openTableModal(r) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', text: true, type: 'error', onClick: () => delTable(r.id) }, { default: () => '删除' })
      ])
  }
]

const areaColumns = [
  { title: '名称', key: 'name' },
  { title: '排序', key: 'sort', width: 80 },
  {
    title: '状态', key: 'status', width: 90,
    render: (r) => h(NTag, { type: r.status === 1 ? 'success' : 'default', size: 'small' }, () => (r.status === 1 ? '启用' : '停用'))
  },
  { title: '备注', key: 'remark' },
  {
    title: '操作', key: 'action', width: 140,
    render: (r) =>
      h(NSpace, { size: 4 }, () => [
        h(NButton, { size: 'small', text: true, onClick: () => openAreaModal(r) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', text: true, type: 'error', onClick: () => delArea(r.id) }, { default: () => '删除' })
      ])
  }
]

async function loadTables() {
  tableLoading.value = true
  try {
    tables.value = (await tableApi.listTables()) || []
  } finally {
    tableLoading.value = false
  }
}

async function loadAreas() {
  areaLoading.value = true
  try {
    areas.value = (await tableApi.listAreas()) || []
  } finally {
    areaLoading.value = false
  }
}

function openTableModal(row) {
  tableForm.value = row ? { ...row } : { code: '', name: '', capacity: 2, areaId: null }
  tableModal.value = true
}

function openAreaModal(row) {
  areaForm.value = row ? { ...row } : { name: '', sort: 0, status: 1, remark: '' }
  areaModal.value = true
}

async function saveTable() {
  tableSaving.value = true
  try {
    if (tableForm.value.id) await tableApi.updateTable(tableForm.value.id, tableForm.value)
    else await tableApi.createTable(tableForm.value)
    message.success('保存成功')
    tableModal.value = false
    loadTables()
  } finally {
    tableSaving.value = false
  }
}

async function saveArea() {
  areaSaving.value = true
  try {
    if (areaForm.value.id) await tableApi.updateArea(areaForm.value.id, areaForm.value)
    else await tableApi.createArea(areaForm.value)
    message.success('保存成功')
    areaModal.value = false
    loadAreas()
    loadTables()
  } finally {
    areaSaving.value = false
  }
}

async function delTable(id) {
  await tableApi.deleteTable(id)
  message.success('已删除')
  loadTables()
}

async function delArea(id) {
  await tableApi.deleteArea(id)
  message.success('已删除')
  loadAreas()
  loadTables()
}

async function handleGenAllQr() {
  const task = await tableApi.genAllQrTask()
  if (task && task.taskId) {
    const poll = async () => {
      const t = await tableApi.getQrTask(task.taskId)
      if (t && t.status === 'SUCCESS') {
        message.success(`已生成 ${t.completed || 0} 张二维码`)
        loadTables()
      } else {
        setTimeout(poll, 1500)
      }
    }
    poll()
  }
}

async function downloadQr(id) {
  const blob = await tableApi.downloadTableQr(id)
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `qr-${id}.png`
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(() => {
  loadTables()
  loadAreas()
})
</script>