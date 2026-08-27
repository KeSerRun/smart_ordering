<template>
  <n-data-table :columns="columns" :data="rows" :loading="loading" />
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { listMenus } from '@/api/system'
const rows = ref([])
const loading = ref(false)
const columns = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '名称', key: 'name' },
  { title: '父ID', key: 'parentId', width: 70 },
  { title: '路径', key: 'path' },
  { title: '组件', key: 'component' },
  { title: '权限', key: 'permission' },
  { title: '类型', key: 'type', render: (r) => (r.type === 1 ? '菜单' : r.type === 2 ? '按钮' : '目录'), width: 70 },
  { title: '排序', key: 'orderNum', width: 60 },
  { title: '状态', key: 'status', render: (r) => (r.status === 1 ? '启用' : '停用'), width: 70 }
]
const load = async () => { loading.value = true; try { rows.value = await listMenus({}) } finally { loading.value = false } }
onMounted(load)
</script>