<template>
  <n-data-table :columns="columns" :data="rows" :loading="loading" :pagination="pagination" />
</template>

<script setup>
import { h, ref, onMounted } from 'vue'
import { listReviews } from '@/api/review'

const rows = ref([])
const loading = ref(false)
const pagination = ref({ page: 1, pageSize: 20, itemCount: 0 })

const star = (n) => '★'.repeat(n || 0) + '☆'.repeat(Math.max(5 - (n || 0), 0))

const columns = [
  { title: '订单号', key: 'orderNo' },
  { title: '桌号', key: 'tableCode' },
  { title: '评分', key: 'overallRating', render: (r) => star(r.overallRating) },
  { title: '内容', key: 'content' },
  { title: '时间', key: 'createTime', width: 160 }
]

const load = async () => {
  loading.value = true
  try {
    const d = await listReviews({ pageNum: pagination.value.page, pageSize: pagination.value.pageSize })
    rows.value = d.list
    pagination.value.itemCount = d.total
  } finally { loading.value = false }
}
pagination.value.onChange = (p) => { pagination.value.page = p; load() }
onMounted(load)
</script>