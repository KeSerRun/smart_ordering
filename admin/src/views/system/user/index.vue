<template>
  <n-space vertical>
    <n-space style="margin-bottom:12px" align="center" justify="space-between">
      <n-space align="center">
        <n-input v-model:value="query.username" placeholder="用户名搜索" style="width:180px" @keyup.enter="load" />
        <n-button type="primary" @click="openModal()">新增用户</n-button>
        <n-button @click="load">查询</n-button>
      </n-space>
    </n-space>

    <n-data-table :columns="columns" :data="rows" :loading="loading" :pagination="pagination" @update:page="onPageChange" />

    <!-- 新增 / 编辑用户 -->
    <n-modal v-model:show="showModal" preset="card" :title="form.id ? '编辑用户' : '新增用户'" style="width:520px">
      <n-form label-placement="top">
        <n-form-item label="用户名">
          <n-input v-model:value="form.username" :disabled="!!form.id" placeholder="登录账号" />
        </n-form-item>
        <n-form-item v-if="!form.id" label="初始密码">
          <n-input v-model:value="form.password" placeholder="默认 123456" />
        </n-form-item>
        <n-form-item label="昵称"><n-input v-model:value="form.nickname" /></n-form-item>
        <n-form-item label="邮箱"><n-input v-model:value="form.email" /></n-form-item>
        <n-form-item label="手机"><n-input v-model:value="form.phone" /></n-form-item>
        <n-form-item label="状态">
          <n-switch :value="form.status === 1" @update:value="(v) => (form.status = v ? 1 : 0)" />
        </n-form-item>
        <n-form-item label="模块权限">
          <n-select v-model:value="form.modules" multiple :options="moduleOptions" placeholder="勾选可访问的模块" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="save">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 重置密码 -->
    <n-modal v-model:show="showPwdModal" preset="card" title="重置密码" style="width:360px">
      <n-input v-model:value="pwdForm.newPassword" type="password" placeholder="输入新密码（默认 123456）" @keyup.enter="doResetPwd" />
      <template #footer>
        <n-space justify="end">
          <n-button @click="showPwdModal = false">取消</n-button>
          <n-button type="primary" :loading="pwdSaving" @click="doResetPwd">确认重置</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-space>
</template>

<script setup>
import { h, ref, onMounted } from 'vue'
import { NSpace, NInput, NButton, NDataTable, NModal, NForm, NFormItem,
  NSelect, NSwitch, NTag, useMessage } from 'naive-ui'
import { listUsers, createUser, updateUser, deleteUser, updateUserStatus, resetUserPassword } from '@/api/system'

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
const pwdSaving = ref(false)
const query = ref({ username: '' })
const showModal = ref(false)
const showPwdModal = ref(false)
const pwdTarget = ref(null)
const pwdForm = ref({ newPassword: '123456' })
const form = ref({ id: null, username: '', password: '123456', nickname: '', email: '', phone: '', status: 1, modules: [] })
const pagination = ref({ page: 1, pageSize: 10, itemCount: 0 })

const columns = [
  { title: 'ID', key: 'id', width: 90 },
  { title: '用户名', key: 'username', width: 100 },
  { title: '昵称', key: 'nickname' },
  { title: '邮箱', key: 'email' },
  { title: '手机', key: 'phone', width: 120 },
  {
    title: '状态', key: 'status', width: 70,
    render: (r) => h(NTag, { type: r.status === 1 ? 'success' : 'default', size: 'small' }, () => (r.status === 1 ? '启用' : '禁用'))
  },
  {
    title: '模块权限', key: 'modules', width: 220,
    render: (r) =>
      (r.modules || []).length
        ? h(NSpace, { size: 4 }, () => (r.modules || []).map((m) =>
            h(NTag, { size: 'small', type: m === 'sys' ? 'warning' : 'info' }, () => moduleTagMap[m] || m)))
        : h('span', { style: 'color:#bbb' }, '无')
  },
  {
    title: '操作', key: 'op', width: 220,
    render: (r) =>
      h(NSpace, { size: 4 }, () => [
        h('a', { href: 'javascript:;', style: 'margin-right:4px', onClick: () => openModal(r) }, '编辑'),
        h('a', { href: 'javascript:;', style: 'margin-right:4px', onClick: () => openPwdModal(r) }, '重置密码'),
        r.status === 1 && r.username !== 'admin'
          ? h('a', { href: 'javascript:;', style: 'color:#c00;margin-right:4px', onClick: () => toggle(r, 0) }, '禁用')
          : r.username !== 'admin' && h('a', { href: 'javascript:;', style: 'margin-right:4px', onClick: () => toggle(r, 1) }, '启用'),
        r.username !== 'admin'
          ? h('a', { href: 'javascript:;', style: 'color:#c00', onClick: () => remove(r) }, '删除')
          : null
      ])
  }
]

const load = async () => {
  loading.value = true
  try {
    const d = await listUsers({ pageNum: pagination.value.page, pageSize: pagination.value.pageSize, username: query.value.username || undefined })
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
    ? { ...row, password: '123456' }
    : { id: null, username: '', password: '123456', nickname: '', email: '', phone: '', status: 1, modules: [] }
  showModal.value = true
}

const save = async () => {
  saving.value = true
  try {
    const payload = { ...form.value }
    if (payload.id) {
      delete payload.password // 编辑：不允许改密码（走重置密码）
      delete payload.username // 用户名不可改
      await updateUser(payload.id, payload)
    } else {
      await createUser(payload)
    }
    message.success('保存成功')
    showModal.value = false
    load()
  } finally {
    saving.value = false
  }
}

const toggle = async (r, s) => {
  await updateUserStatus(r.id, s)
  message.success('已更新')
  load()
}

const remove = async (r) => {
  await deleteUser(r.id)
  message.success(`已删除用户「${r.username}」`)
  load()
}

const openPwdModal = (r) => {
  pwdTarget.value = r
  pwdForm.value = { newPassword: '123456' }
  showPwdModal.value = true
}

const doResetPwd = async () => {
  pwdSaving.value = true
  try {
    await resetUserPassword(pwdTarget.value.id, pwdForm.value.newPassword)
    message.success(`已重置「${pwdTarget.value.username}」的密码`)
    showPwdModal.value = false
  } finally {
    pwdSaving.value = false
  }
}

onMounted(load)
</script>