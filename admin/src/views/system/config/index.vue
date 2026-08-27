<template>
  <n-data-table :columns="columns" :data="rows" :loading="loading" :pagination="pagination" />
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { listConfigs } from '@/api/system'
const rows = ref([])
const loading = ref(false)
const pagination = ref({ page: 1, pageSize: 20, itemCount: 0 })
const columns = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '名称', key: 'name' },
  { title: '键', key: 'configKey' },
  { title: '值', key: 'configValue' },
  { title: '备注', key: 'remark' },
  { title: '时间', key: 'createTime', width: 160 }
]
const load = async () => {
  loading.value = true
  try { const d = await listConfigs({ pageNum: pagination.value.page, pageSize: pagination.value.pageSize }); rows.value = d.list; pagination.value.itemCount = d.total }
  finally { loading.value = false }
}
pagination.value.onChange = (p) => { pagination.value.page = p; load() }
onMounted(load)
</script>