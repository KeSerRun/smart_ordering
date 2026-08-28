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
        :scroll-x="950"
        :pagination="pagination"
      />
    </n-tab-pane>
    <!-- 发券任务 -->
    <n-tab-pane name="task" tab="发券任务">
      <n-data-table :columns="taskColumns" :data="tasks" :loading="taskLoading" :scroll-x="1100" :pagination="taskPagination" />
    </n-tab-pane>
  </n-tabs>

  <!-- 新增/编辑模板 -->
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

  <!-- 发放弹窗 -->
  <n-modal v-model:show="showGrant" preset="card" :title="`发放优惠券 - ${grantForm.templateName}`" style="width: 520px">
    <n-form label-placement="top">
      <n-form-item label="发放方式">
        <n-radio-group v-model:value="grantForm.grantMode">
          <n-radio :value="3">按会员等级</n-radio>
          <n-radio :value="2">全部会员</n-radio>
          <n-radio :value="1" disabled>指定会员(敬请期待)</n-radio>
        </n-radio-group>
      </n-form-item>
      <n-form-item v-if="grantForm.grantMode === 3" label="目标会员等级（可多选，只发给所选等级的会员）">
        <n-select
          v-model:value="grantForm.levelIds"
          multiple
          :options="levelOptions"
          placeholder="请选择会员等级"
          :loading="levelLoading"
        />
      </n-form-item>
      <n-form-item v-if="grantForm.grantMode === 2" label="目标会员">
        <n-alert type="info" :bordered="false">
          将发放给全部启用状态的小程序会员
        </n-alert>
      </n-form-item>
      <n-form-item label="备注">
        <n-input v-model:value="grantForm.remark" type="textarea" placeholder="选填" />
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="showGrant=false">取消</n-button>
        <n-button type="primary" :loading="granting" @click="submitGrant">确认发放</n-button>
      </n-space>
    </template>
  </n-modal>

  <!-- 任务明细弹窗 -->
  <n-modal v-model:show="showDetail" preset="card" :title="`发券明细 - ${detailTemplateName}`" style="width: 640px">
    <n-data-table :columns="detailColumns" :data="detailRows" :loading="detailLoading" :scroll-x="620" :pagination="detailPagination" />
  </n-modal>
</template>

<script setup>
import { h, ref, onMounted, watch } from 'vue'
import { useMessage } from 'naive-ui'
import {
  listCouponTemplates, createCouponTemplate, updateCouponTemplate, updateCouponTemplateStatus,
  grantCoupons, getGrantTask, listGrantTasks, listGrantTaskDetails
} from '@/api/coupon'
import { memberLevels } from '@/api/member'

const message = useMessage()
const tab = ref('template')
const loading = ref(false)
const saving = ref(false)
const templates = ref([])
const showModal = ref(false)

// ===== 模板 =====
const empty = () => ({ id: null, name: '', type: 0, thresholdAmount: 0, discountAmount: 0, discountRate: 0, totalQuantity: 0, perUserLimit: 0, validityType: 0, validDays: 0, status: 1, description: '' })
const form = ref(empty())

const pagination = ref({ page: 1, pageSize: 20, itemCount: 0 })

const typeLabel = (t) => (t === 1 ? '满减' : t === 2 ? '折扣' : t === 3 ? '优惠' : '其他')

const columns = [
  { title: '名称', key: 'name', width: 180, ellipsis: { tooltip: true } },
  { title: '类型', key: 'type', width: 80, render: (r) => typeLabel(r.type) },
  { title: '门槛', key: 'thresholdAmount', width: 90 },
  { title: '面值/折扣', key: 'discountAmount', render: (r) => (r.discountRate ? `${r.discountRate}折` : r.discountAmount), width: 100 },
  { title: '已发/总量', key: 'totalQuantity', render: (r) => `${r.issuedQuantity ?? 0}/${r.totalQuantity}`, width: 90 },
  { title: '限领', key: 'perUserLimit', width: 70 },
  { title: '状态', key: 'status', render: (r) => (r.status === 1 ? '启用' : '停用'), width: 70 },
  { title: '操作', key: 'op', render: (r) =>
      h('span', {},
        [
          h('a', { href: 'javascript:;', style: 'margin-right:10px', onClick: () => openGrant(r) }, '发放'),
          h('a', { href: 'javascript:;', style: 'margin-right:10px', onClick: () => openEdit(r) }, '编辑'),
          r.status === 1
            ? h('a', { href: 'javascript:;', style: 'color:#c00', onClick: () => toggleStatus(r, 0) }, '停用')
            : h('a', { href: 'javascript:;', onClick: () => toggleStatus(r, 1) }, '启用')
        ]) }
]

// ===== 发券任务 =====
const grantForm = ref({ templateId: null, templateName: '', grantMode: 3, levelIds: [], remark: '' })
const showGrant = ref(false)
const granting = ref(false)
const levelOptions = ref([])
const levelLoading = ref(false)

const tasks = ref([])
const taskLoading = ref(false)
const taskPagination = ref({ page: 1, pageSize: 10, itemCount: 0 })

const showDetail = ref(false)
const detailRows = ref([])
const detailLoading = ref(false)
const detailTemplateName = ref('')
const detailPagination = ref({ page: 1, pageSize: 10, itemCount: 0 })

const taskStatusMap = {
  0: ['待处理', 'warning'],
  1: ['处理中', 'info'],
  2: ['成功', 'success'],
  3: ['失败', 'error']
}
const grantModeLabel = (m) => (m === 1 ? '指定用户' : m === 2 ? '全部会员' : m === 3 ? '按会员等级' : '未知')

const taskColumns = [
  { title: '模板', key: 'templateName', width: 160, ellipsis: { tooltip: true } },
  { title: '发放方式', key: 'grantMode', width: 100, render: (r) => grantModeLabel(r.grantMode) },
  { title: '目标/成功/失败', key: 'counts', width: 120, render: (r) => `${r.targetCount ?? 0}/${r.successCount ?? 0}/${r.failCount ?? 0}` },
  {
    title: '状态', key: 'taskStatus', width: 90,
    render: (r) => {
      const [label, type] = taskStatusMap[r.taskStatus] || ['未知', 'default']
      return h('span', { style: `color:${type === 'success' ? '#18a058' : type === 'error' ? '#d03050' : type === 'warning' ? '#f0a020' : '#2080f0'}` }, label)
    }
  },
  { title: '备注', key: 'remark', width: 160, ellipsis: { tooltip: true }, render: (r) => r.remark || '-' },
  { title: '开始时间', key: 'startedTime', width: 165, render: (r) => r.startedTime ? String(r.startedTime).replace('T', ' ') : '-' },
  { title: '完成时间', key: 'finishedTime', width: 165, render: (r) => r.finishedTime ? String(r.finishedTime).replace('T', ' ') : '-' },
  { title: '操作', key: 'op', width: 70, render: (r) =>
      h('a', { href: 'javascript:;', onClick: () => openDetail(r) }, '明细') }
]

const detailColumns = [
  { title: '用户名', key: 'username', width: 150, render: (r) => r.username || '-' },
  { title: '手机号', key: 'phone', width: 130, render: (r) => r.phone || '-' },
  { title: '发放状态', key: 'grantStatus', width: 90, render: (r) => (r.grantStatus === 1 ? '已发放' : '失败') },
  { title: '发券时间', key: 'finishedTime', width: 170, render: (r) => (r.finishedTime ? String(r.finishedTime).replace('T', ' ') : '-') }
]

// ===== 加载 =====
const loadTemplates = async () => {
  loading.value = true
  try {
    const d = await listCouponTemplates({ pageNum: pagination.value.page, pageSize: pagination.value.pageSize })
    templates.value = d.list
    pagination.value.itemCount = d.total
  } finally { loading.value = false }
}

const loadTasks = async () => {
  taskLoading.value = true
  try {
    const d = await listGrantTasks({ pageNum: taskPagination.value.page, pageSize: taskPagination.value.pageSize })
    tasks.value = d.list || []
    taskPagination.value.itemCount = d.total || 0
  } finally { taskLoading.value = false }
}

const loadLevels = async () => {
  if (levelOptions.value.length) return
  levelLoading.value = true
  try {
    const list = await memberLevels()
    levelOptions.value = (list || []).map((l) => ({ label: l.levelName, value: l.id }))
  } finally { levelLoading.value = false }
}

pagination.value.onChange = (p) => { pagination.value.page = p; loadTemplates() }
taskPagination.value.onChange = (p) => { taskPagination.value.page = p; loadTasks() }
detailPagination.value.onChange = (p) => { detailPagination.value.page = p; loadDetail(detailPagination.value.taskId) }

// ===== 模板操作 =====
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

// ===== 发放 =====
const openGrant = async (r) => {
  if (r.status !== 1) {
    message.warning('模板已停用，请先启用再发放')
    return
  }
  grantForm.value = { templateId: r.id, templateName: r.name, grantMode: 3, levelIds: [], remark: '' }
  showGrant.value = true
  loadLevels()
}

const submitGrant = async () => {
  if (grantForm.value.grantMode === 3 && (!grantForm.value.levelIds || !grantForm.value.levelIds.length)) {
    message.warning('请选择要发放的会员等级')
    return
  }
  granting.value = true
  try {
    const task = await grantCoupons({
      templateId: grantForm.value.templateId,
      grantMode: grantForm.value.grantMode,
      levelIds: grantForm.value.grantMode === 3 ? grantForm.value.levelIds : undefined,
      remark: grantForm.value.remark || undefined
    })
    if (task && task.id) {
      message.success('发券任务已提交，正在异步发放…')
      showGrant.value = false
      pollTask(task.id)
    }
  } finally { granting.value = false }
}

const pollTask = (taskId) => {
  const poll = async () => {
    const t = await getGrantTask(taskId)
    if (t && t.taskStatus === 2) {
      message.success(`发券完成：成功 ${t.successCount ?? 0} 张，失败 ${t.failCount ?? 0}`)
      loadTasks()
      loadTemplates()
    } else if (t && t.taskStatus === 3) {
      message.error(t.lastError || '发券任务失败')
      loadTasks()
      loadTemplates()
    } else {
      setTimeout(poll, 2000) // 待处理/处理中：继续轮询
    }
  }
  poll()
}

// ===== 任务明细 =====
const openDetail = (r) => {
  detailTemplateName.value = r.templateName || ''
  detailPagination.value = { page: 1, pageSize: 10, itemCount: 0, taskId: r.id }
  showDetail.value = true
  loadDetail(r.id)
}

const loadDetail = async (taskId) => {
  detailLoading.value = true
  try {
    const d = await listGrantTaskDetails({ taskId, pageNum: detailPagination.value.page, pageSize: detailPagination.value.pageSize })
    detailRows.value = d.list || []
    detailPagination.value.itemCount = d.total || 0
  } finally { detailLoading.value = false }
}

onMounted(() => {
  loadTemplates()
  tab.value = 'template'
})

// 切到发券任务 tab 时加载
watch(tab, (v) => { if (v === 'task') loadTasks() })
</script>