<template>
  <n-space vertical>
    <n-data-table :columns="columns" :data="rows" :loading="loading" :scroll-x="1000" :pagination="pagination" />
  </n-space>
  <n-modal v-model:show="showModal" preset="card" title="回复反馈" style="width:460px">
    <n-form label-placement="top">
      <n-form-item label="反馈内容">
        <n-input :value="form.content" type="textarea" disabled />
      </n-form-item>
      <n-form-item label="回复内容">
        <n-input v-model:value="form.replyContent" type="textarea" />
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="showModal=false">取消</n-button>
        <n-button type="primary" :loading="saving" @click="save">提交</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { h, ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { listFeedback, replyFeedback } from '@/api/feedback'
const message = useMessage()
const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const form = ref({ id: null, content: '', replyContent: '' })
const pagination = ref({ page: 1, pageSize: 20, itemCount: 0 })

const columns = [
  { title: '内容', key: 'content', width: 260, ellipsis: { tooltip: true }, render: (r) => r.content || '-' },
  { title: '联系方式', key: 'contactPhone', width: 130, render: (r) => r.contactPhone || '-' },
  { title: '状态', key: 'status', render: (r) => (r.status === 1 ? '未处理' : '已回复'), width: 80 },
  { title: '回复内容', key: 'replyContent', width: 240, ellipsis: { tooltip: true }, render: (r) => r.replyContent || '-' },
  { title: '时间', key: 'createTime', width: 160 },
  { title: '操作', key: 'op', width: 60, render: (r) => h('a', { href: 'javascript:;', onClick: () => { form.value = { ...r }; showModal.value = true } }, '回复') }
]

const load = async () => {
  loading.value = true
  try {
    const d = await listFeedback({ pageNum: pagination.value.page, pageSize: pagination.value.pageSize })
    rows.value = d.list
    pagination.value.itemCount = d.total
  } finally { loading.value = false }
}
pagination.value.onChange = (p) => { pagination.value.page = p; load() }
const save = async () => {
  saving.value = true
  try { await replyFeedback(form.value.id, { replyContent: form.value.replyContent }); message.success('已回复'); showModal.value = false; load() }
  finally { saving.value = false }
}
onMounted(load)
</script>