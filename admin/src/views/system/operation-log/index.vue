<template>
  <n-data-table :columns="columns" :data="rows" :loading="loading" :pagination="pagination" />
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { listOperationLogs } from '@/api/system'
const rows = ref([])
const loading = ref(false)
const pagination = ref({ page: 1, pageSize: 20, itemCount: 0 })
const columns = [
  { title: '模块', key: 'module' },
  { title: '操作', key: 'operation' },
  { title: '路径', key: 'requestUrl' },
  { title: '方法', key: 'requestMethod', width: 80 },
  { title: '耗时(ms)', key: 'duration', width: 90 },
  { title: '状态', key: 'status', render: (r) => (r.status === 1 ? '成功' : '失败'), width: 70 },
  { title: '用户', key: 'username', width: 100 },
  { title: '时间', key: 'createTime', width: 160 }
]
const load = async () => {
  loading.value = true
  try { const d = await listOperationLogs({ pageNum: pagination.value.page, pageSize: pagination.value.pageSize }); rows.value = d.list; pagination.value.itemCount = d.total }
  finally { loading.value = false }
}
pagination.value.onChange = (p) => { pagination.value.page = p; load() }
onMounted(load)
</script>