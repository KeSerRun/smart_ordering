<template>
  <n-data-table :columns="columns" :data="rows" :loading="loading" :pagination="pagination" />
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { listLoginLogs } from '@/api/system'
const rows = ref([])
const loading = ref(false)
const pagination = ref({ page: 1, pageSize: 20, itemCount: 0 })
const columns = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '用户名', key: 'username' },
  { title: 'IP', key: 'ip' },
  { title: '地点', key: 'location' },
  { title: '浏览器', key: 'browser' },
  { title: '系统', key: 'os' },
  { title: '状态', key: 'status', render: (r) => (r.status === 1 ? '成功' : '失败'), width: 70 },
  { title: '登录时间', key: 'loginTime', width: 160 }
]
const load = async () => {
  loading.value = true
  try { const d = await listLoginLogs({ pageNum: pagination.value.page, pageSize: pagination.value.pageSize }); rows.value = d.list; pagination.value.itemCount = d.total }
  finally { loading.value = false }
}
pagination.value.onChange = (p) => { pagination.value.page = p; load() }
onMounted(load)
</script>