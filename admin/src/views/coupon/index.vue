<template>
  <n-tabs type="line" v-model:value="tab">
    <!-- 优惠券模板 -->
    <n-tab-pane name="template" tab="优惠券模板">
      <n-space style="margin-bottom: 12px">
        <n-button type="primary" @click="openAdd">新增优惠券</n-button>
      </n-space>
      <n-data-table
        :columns="columns"
        :data="templates"
        :loading="loading"
        :pagination="pagination"
      />
    </n-tab-pane>
    <!-- 用户券 -->
    <n-tab-pane name="user" tab="用户优惠券">
      <n-data-table :columns="userColumns" :data="userCoupons" :loading="loading" :pagination="userPagination" />
    </n-tab-pane>
  </n-tabs>

  <n-modal v-model:show="showModal" preset="card" :title="form.id ? '编辑优惠券' : '新增优惠券'" style="width: 520px">
    <n-form label-placement="top">
      <n-form-item label="名称"><n-input v-model:value="form.name" /></n-form-item>
      <n-form-item label="类型">
        <n-input-number v-model:value="form.type" style="width:100%" :min="0" />
      </n-form-item>
      <n-form-item label="满减门槛金额"><n-input-number v-model:value="form.thresholdAmount" style="width:100%" :min="0" /></n-form-item>
      <n-form-item label="折扣金额(满减)">
        <n-input-number v-model:value="form.discountAmount" style="width:100%" :min="0" /></n-form-item>
      <n-form-item label="折扣率(折扣券)">
        <n-input-number v-model:value="form.discountRate" style="width:100%" :min="0" :max="10" :step="0.1" /></n-form-item>
      <n-form-item label="总量"><n-input-number v-model:value="form.totalQuantity" style="width:100%" :min="0" /></n-form-item>
      <n-form-item label="每人限领"><n-input-number v-model:value="form.perUserLimit" style="width:100%" :min="0" /></n-form-item>
      <n-form-item label="有效期类型">
        <n-input-number v-model:value="form.validityType" style="width:100%" :min="0" /></n-form-item>
      <n-form-item label="有效天数"><n-input-number v-model:value="form.validDays" style="width:100%" :min="0" /></n-form-item>
      <n-form-item label="状态">
        <n-input-number v-model:value="form.status" style="width:100%" :min="0" :max="1" /></n-form-item>
      <n-form-item label="备注"><n-input v-model:value="form.description" type="textarea" /></n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="showModal=false">取消</n-button>
        <n-button type="primary" :loading="saving" @click="save">保存</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { h, ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { listCouponTemplates, createCouponTemplate, updateCouponTemplate, updateCouponTemplateStatus, listUserCoupons } from '@/api/coupon'

const message = useMessage()
const tab = ref('template')
const loading = ref(false)
const saving = ref(false)
const templates = ref([])
const userCoupons = ref([])
const showModal = ref(false)

const empty = () => ({ id: null, name: '', type: 0, thresholdAmount: 0, discountAmount: 0, discountRate: 0, totalQuantity: 0, perUserLimit: 0, validityType: 0, validDays: 0, status: 1, description: '' })
const form = ref(empty())

const pagination = ref({ page: 1, pageSize: 20, itemCount: 0 })
const userPagination = ref({ page: 1, pageSize: 20, itemCount: 0 })

const typeLabel = (t) => (t === 1 ? '满减' : t === 2 ? '折扣' : t === 3 ? '优惠' : '其他')

const columns = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '名称', key: 'name' },
  { title: '类型', key: 'type', render: (r) => typeLabel(r.type) },
  { title: '门槛', key: 'thresholdAmount', width: 90 },
  { title: '面值/折扣', key: 'discountAmount', render: (r) => (r.discountRate ? `${r.discountRate}折` : r.discountAmount), width: 100 },
  { title: '已发/总量', key: 'totalQuantity', render: (r) => `${r.issuedQuantity ?? 0}/${r.totalQuantity}`, width: 90 },
  { title: '限领', key: 'perUserLimit', width: 70 },
  { title: '状态', key: 'status', render: (r) => (r.status === 1 ? '启用' : '停用'), width: 70 },
  { title: '操作', key: 'op', render: (r) =>
      h('span', {},
        [
          h('a', { href: 'javascript:;', style: 'margin-right:10px', onClick: () => openEdit(r) }, '编辑'),
          r.status === 1
            ? h('a', { href: 'javascript:;', style: 'color:#c00', onClick: () => toggleStatus(r, 0) }, '停用')
            : h('a', { href: 'javascript:;', onClick: () => toggleStatus(r, 1) }, '启用')
        ]) }
]

const userColumns = [
  { title: '券名', key: 'couponName' },
  { title: '类型', key: 'couponType', render: (r) => typeLabel(r.couponType) },
  { title: '门槛', key: 'thresholdAmount' },
  { title: '状态', key: 'status', render: (r) => r.status === 1 ? '未用' : '已用' },
  { title: '领取时间', key: 'receivedTime', width: 160 }
]

const loadTemplates = async () => {
  loading.value = true
  try {
    const d = await listCouponTemplates({ pageNum: pagination.value.page, pageSize: pagination.value.pageSize })
    templates.value = d.list
    pagination.value.itemCount = d.total
  } finally { loading.value = false }
}

const loadUserCoupons = async () => {
  loading.value = true
  try {
    const d = await listUserCoupons({ pageNum: userPagination.value.page, pageSize: userPagination.value.pageSize })
    userCoupons.value = d.list
    userPagination.value.itemCount = d.total
  } finally { loading.value = false }
}

pagination.value.onChange = (p) => { pagination.value.page = p; loadTemplates() }
userPagination.value.onChange = (p) => { userPagination.value.page = p; loadUserCoupons() }

const openAdd = () => { form.value = empty(); showModal.value = true }
const openEdit = (r) => { form.value = { ...r }; showModal.value = true }

const save = async () => {
  saving.value = true
  try {
    if (form.value.id) await updateCouponTemplate(form.value.id, form.value)
    else await createCouponTemplate(form.value)
    message.success('保存成功')
    showModal.value = false
    loadTemplates()
  } finally { saving.value = false }
}

const toggleStatus = async (r, s) => {
  await updateCouponTemplateStatus(r.id, s)
  message.success('状态已更新')
  loadTemplates()
}

onMounted(() => { loadTemplates() })
</script>