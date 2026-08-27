<template>
  <n-space vertical>
    <n-space style="margin-bottom:12px" align="center">
      <n-input v-model:value="title" placeholder="标题搜索" style="width:200px" @keyup.enter="load" />
      <n-button type="primary" @click="load">查询</n-button>
    </n-space>
    <n-data-table :columns="columns" :data="rows" :loading="loading" :pagination="pagination" />
  </n-space>
  <n-modal v-model:show="showModal" preset="card" title="编辑轮播" style="width:480px">
    <n-form label-placement="top">
      <n-form-item label="标题"><n-input v-model:value="form.title" /></n-form-item>
      <n-form-item label="副标题"><n-input v-model:value="form.subtitle" /></n-form-item>
      <n-form-item label="图片URL"><n-input v-model:value="form.imageUrl" /></n-form-item>
      <n-form-item label="动作类型"><n-input-number v-model:value="form.actionType" style="width:100%" /></n-form-item>
      <n-form-item label="场景"><n-input-number v-model:value="form.scene" style="width:100%" /></n-form-item>
      <n-form-item label="排序"><n-input-number v-model:value="form.sort" style="width:100%" /></n-form-item>
    </n-form>
    <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" :loading="saving" @click="save">保存</n-button></n-space></template>
  </n-modal>
</template>

<script setup>
import { h, ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { listBanners, updateBanner, updateBannerStatus } from '@/api/banner'
const message = useMessage()
const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const title = ref('')
const showModal = ref(false)
const form = ref({ id: null, title: '', subtitle: '', imageUrl: '', actionType: 0, scene: 0, sort: 0 })
const pagination = ref({ page: 1, pageSize: 20, itemCount: 0 })
const columns = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '标题', key: 'title' },
  { title: '副标题', key: 'subtitle' },
  { title: '排序', key: 'sort', width: 60 },
  { title: '场景', key: 'scene', width: 60 },
  { title: '状态', key: 'status', render: (r) => (r.status === 1 ? '启用' : '停用'), width: 70 },
  { title: '操作', key: 'op', render: (r) => h('span', {},
    [
      h('a', { href: 'javascript:;', style: 'margin-right:10px', onClick: () => { form.value = { ...r }; showModal.value = true } }, '编辑'),
      r.status === 1
        ? h('a', { href: 'javascript:;', style: 'color:#c00', onClick: () => toggle(r, 0) }, '停用')
        : h('a', { href: 'javascript:;', onClick: () => toggle(r, 1) }, '启用')
    ]) }
]
const load = async () => {
  loading.value = true
  try {
    const d = await listBanners({ pageNum: pagination.value.page, pageSize: pagination.value.pageSize, title: title.value || undefined })
    rows.value = d.list
    pagination.value.itemCount = d.total
  } finally { loading.value = false }
}
pagination.value.onChange = (p) => { pagination.value.page = p; load() }
const save = async () => {
  saving.value = true
  try { await updateBanner(form.value.id, form.value); message.success('保存成功'); showModal.value = false; load() }
  finally { saving.value = false }
}
const toggle = async (r, s) => { await updateBannerStatus(r.id, s); message.success('已更新'); load() }
onMounted(load)
</script>