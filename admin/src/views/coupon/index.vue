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
        :scroll-x="1050"
        :pagination="pagination"
      />
    </n-tab-pane>
    <!-- 持券用户 -->
    <n-tab-pane name="holders" tab="持券用户">
      <n-space style="margin-bottom: 12px" align="center">
        <n-select
          v-model:value="holderQuery.templateId"
          :options="templateOptions"
          clearable
          placeholder="选择优惠券模板"
          style="width: 240px"
          @update:value="loadHolders"
        />
        <n-select
          v-model:value="holderQuery.status"
          :options="holderStatusOptions"
          clearable
          placeholder="券状态"
          style="width: 130px"
          @update:value="loadHolders"
        />
        <n-input v-model:value="holderQuery.keyword" clearable placeholder="用户名/昵称/手机号" style="width: 180px" @keyup.enter="loadHolders" />
        <n-button @click="loadHolders">查询</n-button>
        <n-button @click="refreshHolders">刷新</n-button>
      </n-space>
      <n-data-table
        :columns="holderColumns"
        :data="holderRows"
        :loading="holderLoading"
        :scroll-x="1050"
        :pagination="holderPagination"
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
  <n-modal v-model:show="showGrant" preset="card" :title="`发放优惠券 - ${grantForm.templateName}`" style="width: 560px">
    <n-form label-placement="top">
      <n-form-item label="发放方式">
        <n-radio-group v-model:value="grantForm.grantMode">
          <n-radio :value="1">指定会员</n-radio>
          <n-radio :value="3">按会员等级</n-radio>
          <n-radio :value="2">全部会员</n-radio>
        </n-radio-group>
      </n-form-item>
      <n-form-item v-if="grantForm.grantMode === 1" label="目标会员（可多选，只发给选中的小程序会员）">
        <n-select
          v-model:value="grantForm.userIds"
          multiple
          filterable
          :options="userOptions"
          placeholder="请选择会员"
          :loading="userLoading"
          style="width: 100%"
        />
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
import { useMessage, useDialog, NSpace, NButton } from 'naive-ui'
import {
  listCouponTemplates, createCouponTemplate, updateCouponTemplate, updateCouponTemplateStatus,
  grantCoupons, getGrantTask, listGrantTasks, listGrantTaskDetails,
  listUserCoupons, revokeUserCoupon
} from '@/api/coupon'
import { memberLevels } from '@/api/member'
import { listUsers } from '@/api/system'

const message = useMessage()
const dialog = useDialog()
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

const templateOptions = ref([])

const columns = [
  { title: '名称', key: 'name', width: 180, ellipsis: { tooltip: true } },
  { title: '类型', key: 'type', width: 80, render: (r) => typeLabel(r.type) },
  { title: '门槛', key: 'thresholdAmount', width: 90 },
  { title: '面值/折扣', key: 'discountAmount', render: (r) => (r.discountRate ? `${r.discountRate}折` : r.discountAmount), width: 100 },
  { title: '已发/总量', key: 'totalQuantity', render: (r) => `${r.issuedQuantity ?? 0}/${r.totalQuantity}`, width: 90 },
  { title: '限领', key: 'perUserLimit', width: 70 },
  { title: '状态', key: 'status', render: (r) => (r.status === 1 ? '启用' : '停用'), width: 70 },
  { title: '操作', key: 'op', render: (r) =>
      h(NSpace, { size: 4, justify: 'center' }, () => [
          h(NButton, { size: 'small', text: true, onClick: () => openGrant(r) }, { default: () => '发放' }),
          h(NButton, { size: 'small', text: true, onClick: () => openHoldersFor(r) }, { default: () => '持券用户' }),
          h(NButton, { size: 'small', text: true, onClick: () => openEdit(r) }, { default: () => '编辑' }),
          r.status === 1
            ? h(NButton, { size: 'small', text: true, type: 'error', onClick: () => toggleStatus(r, 0) }, { default: () => '停用' })
            : h(NButton, { size: 'small', text: true, onClick: () => toggleStatus(r, 1) }, { default: () => '启用' })
        ]) }
]

// ===== 持券用户 =====
const holderQuery = ref({ templateId: null, status: null, keyword: '', pageNum: 1, pageSize: 10 })
const holderRows = ref([])
const holderLoading = ref(false)
const holderPagination = ref({ page: 1, pageSize: 10, itemCount: 0 })

const holderStatusOptions = [
  { label: '未使用', value: 0 },
  { label: '已使用', value: 1 },
  { label: '已过期', value: 2 },
  { label: '锁定', value: 3 }
]

const holderStatusLabel = (s) => {
  const m = { 0: ['未使用', '#18a058'], 1: ['已使用', '#999'], 2: ['已过期', '#d03050'], 3: ['锁定', '#f0a020'] }
  return (m[s] || ['未知', '#999'])
}

const holderColumns = [
  { title: '优惠券', key: 'couponName', width: 180, ellipsis: { tooltip: true } },
  { title: '用户', key: 'username', width: 130, render: (r) => r.nickname || r.username || '-' },
  { title: '手机号', key: 'phone', width: 130, render: (r) => r.phone || '-' },
  { title: '状态', key: 'status', width: 90, render: (r) => { const [label, color] = holderStatusLabel(r.status); return h('span', { style: `color:${color}` }, label) } },
  { title: '领取时间', key: 'receivedTime', width: 170, render: (r) => (r.receivedTime ? String(r.receivedTime).replace('T', ' ') : '-') },
  { title: '有效期至', key: 'validTo', width: 170, render: (r) => (r.validTo ? String(r.validTo).replace('T', ' ') : '-') },
  {
    title: '操作', key: 'op', width: 90,
    render: (r) =>
      r.status === 0
        ? h(NButton, { size: 'small', text: true, type: 'error', onClick: () => doRevoke(r) }, { default: () => '收回' })
        : h('span', { style: 'color:#ccc' }, '-')
  }
]

const loadHolders = async () => {
  holderLoading.value = true
  try {
    const d = await listUserCoupons({
      templateId: holderQuery.value.templateId || undefined,
      status: holderQuery.value.status === null ? undefined : holderQuery.value.status,
      keyword: holderQuery.value.keyword || undefined,
      pageNum: holderQuery.value.pageNum,
      pageSize: holderQuery.value.pageSize
    })
    holderRows.value = d.list || []
    holderPagination.value.itemCount = d.total || 0
  } finally { holderLoading.value = false }
}

const refreshHolders = () => {
  holderQuery.value.pageNum = 1
  loadHolders()
}

holderPagination.value.onChange = (p) => { holderQuery.value.pageNum = p; loadHolders() }

// 从模板行「持券用户」进入：切 tab 并选中该模板
const openHoldersFor = (r) => {
  holderQuery.value = { templateId: r.id, status: null, keyword: '', pageNum: 1, pageSize: 10 }
  tab.value = 'holders'
  loadHolders()
}

const doRevoke = (r) => {
  dialog.warning({
    title: '收回优惠券',
    content: `确认收回 ${r.nickname || r.username || r.userId} 持有的「${r.couponName}」？收回后该券不可使用。`,
    positiveText: '确认收回',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await revokeUserCoupon(r.id)
        message.success('已收回')
        loadHolders()
        loadTemplates() // 已发数量回退，刷新模板统计
      } catch (e) {
        message.error(e?.msg || '收回失败')
      }
    }
  })
}

// ===== 发券任务 =====
const grantForm = ref({ templateId: null, templateName: '', grantMode: 3, levelIds: [], userIds: [], remark: '' })
const showGrant = ref(false)
const granting = ref(false)
const levelOptions = ref([])
const levelLoading = ref(false)
const userOptions = ref([])
const userLoading = ref(false)

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
      h(NButton, { size: 'small', text: true, onClick: () => openDetail(r) }, { default: () => '明细' }) }
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
    templateOptions.value = (d.list || []).map((t) => ({ label: t.name, value: t.id }))
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

// 加载小程序会员（指定用户发券用）
const loadAppUsers = async () => {
  if (userOptions.value.length) return
  userLoading.value = true
  try {
    const d = await listUsers({ pageNum: 1, pageSize: 200, userType: 'APP' })
    userOptions.value = (d.list || []).map((u) => ({ label: `${u.nickname || u.username}${u.phone ? ` (${u.phone})` : ''}`, value: u.id }))
  } finally { userLoading.value = false }
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
  grantForm.value = { templateId: r.id, templateName: r.name, grantMode: 3, levelIds: [], userIds: [], remark: '' }
  showGrant.value = true
  loadLevels()
  loadAppUsers()
}

const submitGrant = async () => {
  if (grantForm.value.grantMode === 3 && (!grantForm.value.levelIds || !grantForm.value.levelIds.length)) {
    message.warning('请选择要发放的会员等级')
    return
  }
  if (grantForm.value.grantMode === 1 && (!grantForm.value.userIds || !grantForm.value.userIds.length)) {
    message.warning('请选择要发放的会员')
    return
  }
  granting.value = true
  try {
    const task = await grantCoupons({
      templateId: grantForm.value.templateId,
      grantMode: grantForm.value.grantMode,
      levelIds: grantForm.value.grantMode === 3 ? grantForm.value.levelIds : undefined,
      userIds: grantForm.value.grantMode === 1 ? grantForm.value.userIds : undefined,
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