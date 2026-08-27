<template>
  <n-data-table :columns="columns" :data="rows" :loading="loading" :pagination="pagination" />
</template>
<script setup>
import { h, ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { listRoles, updateRoleStatus } from '@/api/system'
const message = useMessage()
const rows = ref([])
const loading = ref(false)
const pagination = ref({ page: 1, pageSize: 20, itemCount: 0 })
const columns = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '角色名', key: 'name' },
  { title: '编码', key: 'code' },
  { title: '备注', key: 'remark' },
  { title: '状态', key: 'status', render: (r) => (r.status === 1 ? '启用' : '禁用'), width: 70 },
  { title: '操作', key: 'op', render: (r) => h('a', {
    href: 'javascript:;',
    style: r.status === 1 ? 'color:#c00' : '',
    onClick: async () => { await updateRoleStatus(r.id, r.status === 1 ? 0 : 1); message.success('已更新'); load() }
  }, r.status === 1 ? '禁用' : '启用') }
]
const load = async () => {
  loading.value = true
  try { const d = await listRoles({ pageNum: pagination.value.page, pageSize: pagination.value.pageSize }); rows.value = d.list; pagination.value.itemCount = d.total }
  finally { loading.value = false }
}
pagination.value.onChange = (p) => { pagination.value.page = p; load() }
onMounted(load)
</script>