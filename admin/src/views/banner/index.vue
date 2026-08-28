<template>
  <n-space vertical>
    <n-space style="margin-bottom:12px" align="center">
      <n-input v-model:value="title" placeholder="标题搜索" style="width:200px" @keyup.enter="load" />
      <n-button type="primary" @click="openModal()">新增轮播</n-button>
      <n-button @click="load">查询</n-button>
    </n-space>
    <n-data-table :columns="columns" :data="rows" :loading="loading" :pagination="pagination" @update:page="onPageChange" />

    <!-- 新增 / 编辑弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="form.id ? '编辑轮播' : '新增轮播'" style="width:520px">
      <n-form label-placement="top">
        <n-form-item label="标题"><n-input v-model:value="form.title" /></n-form-item>
        <n-form-item label="副标题"><n-input v-model:value="form.subtitle" /></n-form-item>
        <n-form-item label="图片">
          <div style="display:flex;align-items:center;gap:10px">
            <n-upload accept="image/*" :show-file-list="false" :custom-request="(opt) => handleUpload(opt)">
              <n-button size="small">{{ form.imageUrl ? '更换图片' : '上传图片' }}</n-button>
            </n-upload>
            <img v-if="form.imageUrl" :src="form.imageUrl" class="img-preview" />
          </div>
        </n-form-item>
        <n-form-item label="动作类型">
          <n-select v-model:value="form.actionType" :options="actionTypeOptions" style="width:100%" />
        </n-form-item>
        <n-form-item label="跳转路径"><n-input v-model:value="form.targetPath" placeholder="如 /pages/menu/index" /></n-form-item>
        <n-form-item label="场景">
          <n-select v-model:value="form.scene" :options="sceneOptions" style="width:100%" />
        </n-form-item>
        <n-form-item label="排序"><n-input-number v-model:value="form.sort" style="width:100%" /></n-form-item>
        <n-form-item label="状态">
          <n-switch :value="form.status === 1" @update:value="(v) => (form.status = v ? 1 : 0)" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="save">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-space>
</template>

<script setup>
import { h, ref, onMounted } from 'vue'
import { NSpace, NInput, NButton, NDataTable, NModal, NForm, NFormItem,
  NSelect, NInputNumber, NSwitch, NImage, NTag, useMessage } from 'naive-ui'
import { listBanners, createBanner, updateBanner, updateBannerStatus, deleteBanner, uploadBannerImage } from '@/api/banner'

const message = useMessage()
const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const title = ref('')
const showModal = ref(false)
const form = ref({ id: null, title: '', subtitle: '', imageUrl: '', actionType: 0, targetPath: '', scene: 'home', sort: 0, status: 1 })
const pagination = ref({ page: 1, pageSize: 10, itemCount: 0 })

const actionTypeOptions = [
  { label: '无动作', value: 0 },
  { label: '跳转页面', value: 1 },
  { label: '跳转链接', value: 2 }
]
const sceneOptions = [
  { label: '首页', value: 'home' },
  { label: '点餐页', value: 'menu' }
]

const columns = [
  {
    title: '图片', key: 'image', width: 70,
    render: (r) => h(NImage, { src: r.imageUrl, width: 48, height: 48, objectFit: 'cover', style: 'border-radius:4px' })
  },
  { title: '标题', key: 'title' },
  { title: '副标题', key: 'subtitle' },
  { title: '场景', key: 'scene', width: 80 },
  { title: '排序', key: 'sort', width: 60 },
  {
    title: '状态', key: 'status', width: 80,
    render: (r) => h(NTag, { type: r.status === 1 ? 'success' : 'default', size: 'small' }, () => (r.status === 1 ? '启用' : '停用'))
  },
  {
    title: '操作', key: 'op', width: 190,
    render: (r) =>
      h(NSpace, { size: 4 }, () => [
        h('a', { href: 'javascript:;', style: 'margin-right:4px', onClick: () => openModal(r) }, '编辑'),
        r.status === 1
          ? h('a', { href: 'javascript:;', style: 'color:#c00;margin-right:4px', onClick: () => toggle(r, 0) }, '停用')
          : h('a', { href: 'javascript:;', style: 'margin-right:4px', onClick: () => toggle(r, 1) }, '启用'),
        h('a', { href: 'javascript:;', style: 'color:#c00', onClick: () => remove(r) }, '删除')
      ])
  }
]

const load = async () => {
  loading.value = true
  try {
    const d = await listBanners({ pageNum: pagination.value.page, pageSize: pagination.value.pageSize, title: title.value || undefined })
    rows.value = d.list
    pagination.value.itemCount = d.total
  } finally {
    loading.value = false
  }
}

const onPageChange = (p) => {
  pagination.value.page = p
  load()
}

// 新增 / 编辑
const openModal = (row) => {
  form.value = row
    ? { ...row }
    : { id: null, title: '', subtitle: '', imageUrl: '', actionType: 0, targetPath: '', scene: 'home', sort: 0, status: 1 }
  showModal.value = true
}

const save = async () => {
  saving.value = true
  try {
    const payload = { ...form.value }
    if (payload.id) await updateBanner(payload.id, payload)
    else await createBanner(payload)
    message.success('保存成功')
    showModal.value = false
    load()
  } finally {
    saving.value = false
  }
}

const toggle = async (r, s) => {
  await updateBannerStatus(r.id, s)
  message.success('已更新')
  load()
}

const remove = async (r) => {
  await deleteBanner(r.id)
  message.success(`已删除「${r.title || r.id}」`)
  load()
}

// 图片上传（n-upload custom-request）：传 MinIO 后回填 imageUrl
// 注意：form 在 script 里是 ref，直接 form.value 操作，不要经模板参数传 ref
async function handleUpload(options) {
  const rawFile = options?.file?.file || options?.file
  try {
    const data = await uploadBannerImage(rawFile)
    form.value.imageUrl = data.url
    message.success('图片上传成功')
    options.onFinish()
  } catch (e) {
    message.error('图片上传失败')
    options.onError()
  }
}

onMounted(load)
</script>

<style scoped>
.img-preview {
  width: 64px;
  height: 64px;
  border-radius: 4px;
  object-fit: cover;
  border: 1px solid #eef0f3;
}
</style>