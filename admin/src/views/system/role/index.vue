<template>
  <n-space vertical>
    <n-space style="margin-bottom:12px" align="center">
      <n-input v-model:value="query.name" placeholder="角色名搜索" style="width:180px" @keyup.enter="load" />
      <n-button type="primary" @click="openModal()">新增角色</n-button>
      <n-button @click="load">查询</n-button>
    </n-space>

    <n-data-table :columns="columns" :data="rows" :loading="loading" :scroll-x="1100" :pagination="pagination" @update:page="onPageChange" />

    <!-- 新增 / 编辑角色 -->
    <n-modal v-model:show="showModal" preset="card" :title="form.id ? '编辑角色' : '新增角色'" style="width:480px">
      <n-form label-placement="top">
        <n-form-item label="角色名称"><n-input v-model:value="form.name" /></n-form-item>
        <n-form-item label="角色编码"><n-input v-model:value="form.code" placeholder="如 WAITER / OPERATOR" /></n-form-item>
        <n-form-item label="备注"><n-input v-model:value="form.remark" /></n-form-item>
        <n-form-item label="状态">
          <n-switch :value="form.status === 1" @update:value="(v) => (form.status = v ? 1 : 0)" />
        </n-form-item>
        <n-form-item label="模块权限">
          <n-select v-model:value="form.modules" multiple :options="moduleOptions" placeholder="该角色可访问的模块" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="save">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-space>
</template>

<script setup>
import { h, ref, onMounted } from 'vue'
import { NSpace, NInput, NButton, NDataTable, NModal, NForm, NFormItem,
  NSelect, NSwitch, NTag, useMessage } from 'naive-ui'
import { listRoles, createRole, updateRole, deleteRole, updateRoleStatus } from '@/api/system'

const message = useMessage()

// 模块编码与侧边栏分组一致：core 点餐核心 / kitchen 后厨任务 / ops 运营管理 / sys 系统管理
const moduleOptions = [
  { label: '点餐核心（菜品/桌台/订单）', value: 'core' },
  { label: '后厨任务', value: 'kitchen' },
  { label: '运营管理（会员/券/支付/评价/反馈/轮播）', value: 'ops' },
  { label: '系统管理（用户/角色/日志/MQ）', value: 'sys' }
]
const moduleTagMap = { core: '点餐核心', kitchen: '后厨任务', ops: '运营管理', sys: '系统管理' }

const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const query = ref({ name: '' })
const showModal = ref(false)
const form = ref({ id: null, name: '', code: '', remark: '', status: 1, modules: [] })
const pagination = ref({ page: 1, pageSize: 10, itemCount: 0 })

const columns = [
  { title: '角色名', key: 'name', width: 120 },
  { title: '编码', key: 'code', width: 110 },
  {
    title: '模块权限', key: 'modules', width: 260,
    render: (r) =>
      (r.modules || []).length
        ? h(NSpace, { size: 4 }, () => (r.modules || []).map((m) =>
            h(NTag, { size: 'small', type: m === 'sys' ? 'warning' : 'info' }, () => moduleTagMap[m] || m)))
        : h('span', { style: 'color:#bbb' }, '无')
  },
  { title: '备注', key: 'remark', width: 200, ellipsis: { tooltip: true }, render: (r) => r.remark || '-' },
  {
    title: '状态', key: 'status', width: 70,
    render: (r) => h(NTag, { type: r.status === 1 ? 'success' : 'default', size: 'small' }, () => (r.status === 1 ? '启用' : '禁用'))
  },
  {
    title: '操作', key: 'op', width: 170,
    render: (r) =>
      h(NSpace, { size: 4 }, () => [
        h(NButton, { size: 'small', text: true, onClick: () => openModal(r) }, { default: () => '编辑' }),
        r.status === 1
          ? h(NButton, { size: 'small', text: true, type: 'error', onClick: () => toggle(r, 0) }, { default: () => '禁用' })
          : h(NButton, { size: 'small', text: true, onClick: () => toggle(r, 1) }, { default: () => '启用' }),
        r.code !== 'admin'
          ? h(NButton, { size: 'small', text: true, type: 'error', onClick: () => remove(r) }, { default: () => '删除' })
          : null
      ])
  }
]

const load = async () => {
  loading.value = true
  try {
    const d = await listRoles({ pageNum: pagination.value.page, pageSize: pagination.value.pageSize, name: query.value.name || undefined })
    rows.value = d.list
    pagination.value.itemCount = d.total
  } finally {
    loading.value = false
  }
}

const onPageChange = (p) => {
  pagination.value.page = p
  load()
}

const openModal = (row) => {
  form.value = row
    ? { ...row, modules: row.modules || [] }
    : { id: null, name: '', code: '', remark: '', status: 1, modules: [] }
  showModal.value = true
}

const save = async () => {
  saving.value = true
  try {
    const payload = { ...form.value }
    if (payload.id) await updateRole(payload)
    else await createRole(payload)
    message.success('保存成功')
    showModal.value = false
    load()
  } finally {
    saving.value = false
  }
}

const toggle = async (r, s) => {
  await updateRoleStatus(r.id, s)
  message.success('已更新')
  load()
}

const remove = async (r) => {
  await deleteRole(r.id)
  message.success(`已删除角色「${r.name}」`)
  load()
}

onMounted(load)
</script>