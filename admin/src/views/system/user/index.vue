<template>
  <n-data-table :columns="columns" :data="rows" :loading="loading" :pagination="pagination" />
</template>
<script setup>
import { h, ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { listUsers, updateUserStatus } from '@/api/system'
const message = useMessage()
const rows = ref([])
const loading = ref(false)
const pagination = ref({ page: 1, pageSize: 20, itemCount: 0 })
const columns = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '用户名', key: 'username' },
  { title: '昵称', key: 'nickname' },
  { title: '邮箱', key: 'email' },
  { title: '手机', key: 'phone' },
  { title: '状态', key: 'status', render: (r) => (r.status === 1 ? '启用' : '禁用'), width: 70 },
  { title: '操作', key: 'op', render: (r) => h('a', {
    href: 'javascript:;',
    style: r.status === 1 ? 'color:#c00' : '',
    onClick: async () => { await updateUserStatus(r.id, r.status === 1 ? 0 : 1); message.success('已更新'); load() }
  }, r.status === 1 ? '禁用' : '启用') }
]
const load = async () => {
  loading.value = true
  try { const d = await listUsers({ pageNum: pagination.value.page, pageSize: pagination.value.pageSize }); rows.value = d.list; pagination.value.itemCount = d.total }
  finally { loading.value = false }
}
pagination.value.onChange = (p) => { pagination.value.page = p; load() }
onMounted(load)
</script>