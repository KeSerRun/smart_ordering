<template>
  <n-tabs type="line" v-model:value="tab">
    <n-tab-pane name="types" tab="字典类型">
      <n-data-table :columns="tColumns" :data="types" :loading="loading" :pagination="tPagination" />
    </n-tab-pane>
    <n-tab-pane name="datas" tab="字典数据">
      <n-space style="margin-bottom:12px" align="center">
        <n-select v-model:value="typeId" :options="typeOptions" clearable placeholder="选择字典类型" style="width:220px" @update:value="loadDatas" />
      </n-space>
      <n-data-table :columns="dColumns" :data="datas" :loading="loading" />
    </n-tab-pane>
  </n-tabs>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { listDictTypes, listDictDatas } from '@/api/system'
const tab = ref('types')
const types = ref([])
const datas = ref([])
const loading = ref(false)
const typeId = ref(null)
const tPagination = ref({ page: 1, pageSize: 20, itemCount: 0 })
const typeOptions = computed(() => types.value.map((t) => ({ label: `${t.name}(${t.code})`, value: t.id })))
const tColumns = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '名称', key: 'name' },
  { title: '编码', key: 'code' },
  { title: '状态', key: 'status', render: (r) => (r.status === 1 ? '启用' : '停用'), width: 70 },
  { title: '备注', key: 'remark' }
]
const dColumns = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '标签', key: 'label' },
  { title: '值', key: 'value' },
  { title: '排序', key: 'orderNum', width: 70 },
  { title: '状态', key: 'status', render: (r) => (r.status === 1 ? '启用' : '停用'), width: 70 },
  { title: '备注', key: 'remark' }
]
const loadTypes = async () => {
  loading.value = true
  try { const d = await listDictTypes({ pageNum: tPagination.value.page, pageSize: tPagination.value.pageSize }); types.value = d.list; tPagination.value.itemCount = d.total }
  finally { loading.value = false }
}
const loadDatas = async () => {
  if (!typeId.value) return
  loading.value = true
  try { datas.value = await listDictDatas(typeId.value) } finally { loading.value = false }
}
tPagination.value.onChange = (p) => { tPagination.value.page = p; loadTypes() }
onMounted(loadTypes)
</script>