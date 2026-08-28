<template>
  <n-tabs type="line" v-model:value="tab">
    <n-tab-pane name="members" tab="会员列表">
      <n-data-table :columns="mColumns" :data="members" :loading="loading" :scroll-x="1000" :pagination="mPagination" />
    </n-tab-pane>
    <n-tab-pane name="levels" tab="等级管理">
      <n-space style="margin-bottom:12px"><n-button type="primary" @click="openAddLevel">新增等级</n-button></n-space>
      <n-data-table :columns="lColumns" :data="levels" :loading="loading" :scroll-x="900" />
    </n-tab-pane>
    <n-tab-pane name="points" tab="积分/成长记录">
      <n-data-table :columns="pColumns" :data="points" :loading="loading" :scroll-x="650" :pagination="pPagination" />
    </n-tab-pane>
  </n-tabs>

  <n-modal v-model:show="showLevel" preset="card" :title="levelForm.id ? '编辑等级' : '新增等级'" style="width:480px">
    <n-form label-placement="top">
      <n-form-item label="等级编码"><n-input v-model:value="levelForm.levelCode" /></n-form-item>
      <n-form-item label="等级名称"><n-input v-model:value="levelForm.levelName" /></n-form-item>
      <n-form-item label="成长值门槛"><n-input-number v-model:value="levelForm.growthThreshold" style="width:100%" /></n-form-item>
      <n-form-item label="积分倍率"><n-input-number v-model:value="levelForm.pointsRate" style="width:100%" :step="0.1" /></n-form-item>
      <n-form-item label="折扣率"><n-input-number v-model:value="levelForm.discountRate" style="width:100%" :step="0.01" /></n-form-item>
      <n-form-item label="备注"><n-input v-model:value="levelForm.remark" /></n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="showLevel=false">取消</n-button>
        <n-button type="primary" :loading="saving" @click="saveLevel">保存</n-button>
      </n-space>
    </template>
  </n-modal>

  <n-modal v-model:show="showAdjust" preset="card" title="积分调整" style="width:420px">
    <n-form label-placement="top">
      <n-form-item label="调整积分(正负)"><n-input-number v-model:value="adjust.points" style="width:100%" /></n-form-item>
      <n-form-item label="类型(1收入/2支出)"><n-input-number v-model:value="adjust.changeType" style="width:100%" /></n-form-item>
      <n-form-item label="备注"><n-input v-model:value="adjust.remark" /></n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="showAdjust=false">取消</n-button>
        <n-button type="primary" :loading="saving" @click="saveAdjust">确定</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { h, ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { listMembers, memberLevels, createMemberLevel, updateMemberLevel, updateMemberLevelStatus, listMemberPoints, adjustMemberPoints } from '@/api/member'

const message = useMessage()
const tab = ref('members')
const loading = ref(false)
const saving = ref(false)
const members = ref([])
const levels = ref([])
const points = ref([])
const showLevel = ref(false)
const showAdjust = ref(false)
const adjustId = ref(null)
const adjust = ref({ points: 0, changeType: 1, remark: '' })
const levelForm = ref({ id: null, levelCode: '', levelName: '', growthThreshold: 0, pointsRate: 1, discountRate: 1, remark: '' })

const mPagination = ref({ page: 1, pageSize: 20, itemCount: 0 })
const pPagination = ref({ page: 1, pageSize: 20, itemCount: 0 })

const mColumns = [
  { title: '会员号', key: 'memberNo', width: 130, ellipsis: { tooltip: true } },
  { title: '昵称', key: 'nickname', width: 140, ellipsis: { tooltip: true }, render: (r) => r.nickname || '-' },
  { title: '手机号', key: 'phone', width: 130, render: (r) => r.phone || '-' },
  { title: '等级', key: 'levelName', width: 110, render: (r) => r.levelName || '-' },
  { title: '成长值', key: 'growthValue', width: 90 },
  { title: '积分', key: 'pointsBalance', width: 90 },
  { title: '状态', key: 'status', width: 70, render: (r) => (r.status === 1 ? '正常' : '禁用') },
  { title: '操作', key: 'op', width: 90, render: (r) => h('a', { href: 'javascript:;', onClick: () => openAdjust(r) }, '调整积分') }
]

const lColumns = [
  { title: '编码', key: 'levelCode', width: 110 },
  { title: '名称', key: 'levelName', width: 140, ellipsis: { tooltip: true } },
  { title: '成长值门槛', key: 'growthThreshold', width: 110 },
  { title: '积分倍率', key: 'pointsRate', width: 100 },
  { title: '折扣率', key: 'discountRate', width: 100 },
  { title: '状态', key: 'status', width: 70, render: (r) => (r.status === 1 ? '启用' : '停用') },
  { title: '操作', key: 'op', render: (r) => h('span', {},
    [
      h('a', { href: 'javascript:;', style: 'margin-right:10px', onClick: () => openEditLevel(r) }, '编辑'),
      r.status === 1
        ? h('a', { href: 'javascript:;', style: 'color:#c00', onClick: () => toggleLevel(r, 0) }, '停用')
        : h('a', { href: 'javascript:;', onClick: () => toggleLevel(r, 1) }, '启用')
    ]) }
]

const pColumns = [
  { title: '变动积分', key: 'points', width: 100 },
  { title: '类型', key: 'changeType', width: 70, render: (r) => (r.changeType === 1 ? '收入' : '支出') },
  { title: '备注', key: 'remark', width: 220, ellipsis: { tooltip: true }, render: (r) => r.remark || '-' },
  { title: '时间', key: 'createTime', width: 160 }
]

const loadMembers = async () => {
  loading.value = true
  try {
    const d = await listMembers({ pageNum: mPagination.value.page, pageSize: mPagination.value.pageSize })
    members.value = d.list
    mPagination.value.itemCount = d.total
  } finally { loading.value = false }
}
const loadLevels = async () => { levels.value = await memberLevels() }
const loadPoints = async () => {
  loading.value = true
  try {
    const d = await listMemberPoints({ pageNum: pPagination.value.page, pageSize: pPagination.value.pageSize })
    points.value = d.list
    pPagination.value.itemCount = d.total
  } finally { loading.value = false }
}
mPagination.value.onChange = (p) => { mPagination.value.page = p; loadMembers() }
pPagination.value.onChange = (p) => { pPagination.value.page = p; loadPoints() }

const openAddLevel = () => { levelForm.value = { id: null, levelCode: '', levelName: '', growthThreshold: 0, pointsRate: 1, discountRate: 1, remark: '' }; showLevel.value = true }
const openEditLevel = (r) => { levelForm.value = { ...r }; showLevel.value = true }
const saveLevel = async () => {
  saving.value = true
  try {
    if (levelForm.value.id) await updateMemberLevel(levelForm.value.id, levelForm.value)
    else await createMemberLevel(levelForm.value)
    message.success('保存成功'); showLevel.value = false; loadLevels()
  } finally { saving.value = false }
}
const toggleLevel = async (r, s) => { await updateMemberLevelStatus(r.id, s); message.success('已更新'); loadLevels() }
const openAdjust = (r) => { adjustId.value = r.id; adjust.value = { points: 0, changeType: 1, remark: '' }; showAdjust.value = true }
const saveAdjust = async () => {
  saving.value = true
  try { await adjustMemberPoints(adjustId.value, adjust.value); message.success('调整成功'); showAdjust.value = false; loadMembers() }
  finally { saving.value = false }
}

onMounted(() => { loadMembers(); loadLevels() })
</script>