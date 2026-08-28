<template>
  <n-space vertical>
    <n-tabs type="line" v-model:value="tab">
      <!-- 桌台 -->
      <n-tab-pane name="tables" tab="桌台">
        <n-space style="margin-bottom: 12px">
          <n-button type="primary" @click="openTableModal()">新增桌台</n-button>
          <n-button @click="handleGenAllQr">生成全部桌台二维码</n-button>
          <n-button @click="handleDownloadAllQr">打包下载全部二维码</n-button>
          <n-button @click="loadTables">刷新</n-button>
        </n-space>
        <n-data-table :columns="tableColumns" :data="tables" :loading="tableLoading" :scroll-x="1300" />
      </n-tab-pane>

      <!-- 桌区 -->
      <n-tab-pane name="areas" tab="桌区">
        <n-space style="margin-bottom: 12px">
          <n-button type="primary" @click="openAreaModal()">新增桌区</n-button>
        </n-space>
        <n-data-table :columns="areaColumns" :data="areas" :loading="areaLoading" :scroll-x="700" />
      </n-tab-pane>
    </n-tabs>

    <!-- 桌台弹窗 -->
    <n-modal v-model:show="tableModal" preset="card" :title="tableForm.id ? '编辑桌台' : '新增桌台'" style="width: 480px">
      <n-form :model="tableForm" label-placement="left">
        <n-form-item label="桌号代码"><n-input v-model:value="tableForm.code" placeholder="如 A01" /></n-form-item>
        <n-form-item label="桌台名称"><n-input v-model:value="tableForm.name" placeholder="如 大厅A-01" /></n-form-item>
        <n-form-item label="容量"><n-input-number v-model:value="tableForm.capacity" min="1" /></n-form-item>
        <n-form-item label="所属桌区">
          <n-select v-model:value="tableForm.areaId" :options="areaOptions" clearable />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="tableModal = false">取消</n-button>
          <n-button type="primary" :loading="tableSaving" @click="saveTable">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 桌区弹窗 -->
    <n-modal v-model:show="areaModal" preset="card" :title="areaForm.id ? '编辑桌区' : '新增桌区'" style="width: 420px">
      <n-form :model="areaForm" label-placement="left">
        <n-form-item label="桌区名称"><n-input v-model:value="areaForm.name" placeholder="如 包间" /></n-form-item>
        <n-form-item label="排序"><n-input-number v-model:value="areaForm.sort" /></n-form-item>
        <n-form-item label="状态">
          <n-switch :value="areaForm.status === 1" @update:value="(v) => (areaForm.status = v ? 1 : 0)" />
        </n-form-item>
        <n-form-item label="备注"><n-input v-model:value="areaForm.remark" /></n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="areaModal = false">取消</n-button>
          <n-button type="primary" :loading="areaSaving" @click="saveArea">保存</n-button>
        </n-space>
      </template>
    </n-modal>

        <!-- 点餐弹窗 -->
    <n-modal v-model:show="orderModal" preset="card" :title="`点餐 - ${orderTable?.name || ''} (${orderTable?.code || ''})`" style="width: 880px">
      <n-space vertical>
        <n-space align="center">
          <n-input v-model:value="dishKeyword" clearable placeholder="搜索菜品名称" style="width: 220px" @keyup.enter="loadDishes" />
          <n-text depth="3">右侧点击菜品加入清单，仅显示在售菜品</n-text>
        </n-space>
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <div class="dish-panel">
              <div v-for="d in filteredDishes" :key="d.id" class="dish-item" @click="addToCart(d)">
                <span class="dish-name" :title="d.name">{{ d.name }}</span>
                <span class="dish-price">￥{{ d.price }}</span>
                <n-button size="tiny" type="primary" dashed>+</n-button>
              </div>
              <n-empty v-if="!filteredDishes.length" description="暂无可售菜品" style="margin-top: 40px" />
            </div>
          </n-gi>
          <n-gi>
            <div class="cart-panel">
              <div class="cart-title">已选 {{ cartItems.length }} 种菜品</div>
              <div v-for="it in cartItems" :key="it.dishId" class="cart-item">
                <span class="cart-name" :title="it.name">{{ it.name }}</span>
                <span class="cart-qty">
                  <n-button size="tiny" text @click="changeQty(it, -1)">−</n-button>
                  <span class="qty-num">{{ it.quantity }}</span>
                  <n-button size="tiny" text @click="changeQty(it, 1)">+</n-button>
                </span>
                <span class="cart-amount">￥{{ (it.price * it.quantity).toFixed(2) }}</span>
              </div>
              <n-empty v-if="!cartItems.length" description="还未选择菜品" size="small" style="margin-top: 30px" />
              <div class="cart-total">合计：￥{{ cartTotal.toFixed(2) }}</div>
            </div>
          </n-gi>
        </n-grid>
        <n-form-item label="备注">
          <n-input v-model:value="orderRemark" placeholder="选填，如口味要求" />
        </n-form-item>
      </n-space>
      <template #footer>
        <n-space justify="end">
          <n-button @click="orderModal = false">取消</n-button>
          <n-button type="primary" :loading="orderSaving" @click="submitOrder">确认下单</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-space>
</template>

<script setup>
import { h, onMounted, ref, computed } from 'vue'
import {
  NSpace, NTabs, NTabPane, NButton, NDataTable, NModal, NForm, NFormItem,
  NInput, NInputNumber, NSelect, NSwitch, NTag, NImage, useMessage
} from 'naive-ui'
import * as tableApi from '@/api/table'
import { listDishes } from '@/api/dish'

const message = useMessage()
const tab = ref('tables')

const tables = ref([])
const areas = ref([])
const tableLoading = ref(false)
const areaLoading = ref(false)
const tableModal = ref(false)
const areaModal = ref(false)
const tableSaving = ref(false)
const areaSaving = ref(false)

const tableForm = ref({})
const areaForm = ref({})

const areaOptions = computed(() => areas.value.map((a) => ({ label: a.name, value: a.id })))

const tableStatusMap = { 0: ['空闲', 'success'], 1: ['占用', 'error'], 3: ['待清理', 'warning'] }

const tableColumns = [
  { title: '名称', key: 'name', width: 140, ellipsis: { tooltip: true } },
  { title: '代码', key: 'code', width: 100 },
  { title: '容量', key: 'capacity', width: 80 },
  { title: '桌区', key: 'areaName', width: 100, render: (r) => r.areaName || '-' },
  {
    title: '状态', key: 'status', width: 90,
    render: (r) => h(NTag, { type: (tableStatusMap[r.status] || ['-', 'default'])[1], size: 'small' }, () => (tableStatusMap[r.status] || ['-'])[0])
  },
  {
    title: '二维码', key: 'qr', width: 170,
    render: (r) =>
      h(NSpace, { size: 4, align: 'center' }, () => [
        r.qrCodeUrl
          ? h(NImage, { src: r.qrCodeUrl, width: 44, height: 44, objectFit: 'cover', style: 'border-radius: 4px; border: 1px solid #eee' })
          : h('div', { style: 'width:44px;height:44px;border-radius:4px;border:1px dashed #ccc;display:flex;align-items:center;justify-content:center;color:#999;font-size:12px' }, () => '未生成'),
        h(NButton, { size: 'small', text: true, onClick: () => downloadQr(r.id) }, { default: () => '下载' }),
        r.qrCodeUrl
          ? h(NButton, { size: 'small', text: true, type: 'error', onClick: () => removeQr(r) }, { default: () => '删除' })
          : null
      ])
  },
  {
    title: '操作', key: 'action', width: 230,
    render: (r) =>
      h(NSpace, { size: 4, justify: 'center' }, () => [
        h(NButton, { size: 'small', text: true, type: 'primary', onClick: () => openOrderModal(r) }, { default: () => '点餐' }),
        r.status === 3
          ? h(NButton, { size: 'small', text: true, type: 'warning', onClick: () => doAssignClean(r) }, { default: () => '派发清理' })
          : null,
        h(NButton, { size: 'small', text: true, onClick: () => openTableModal(r) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', text: true, type: 'error', onClick: () => delTable(r.id) }, { default: () => '删除' })
      ])
  }
]

const areaColumns = [
  { title: '名称', key: 'name', width: 140, ellipsis: { tooltip: true } },
  { title: '排序', key: 'sort', width: 80 },
  {
    title: '状态', key: 'status', width: 90,
    render: (r) => h(NTag, { type: r.status === 1 ? 'success' : 'default', size: 'small' }, () => (r.status === 1 ? '启用' : '停用'))
  },
  { title: '备注', key: 'remark' },
  {
    title: '操作', key: 'action', width: 140,
    render: (r) =>
      h(NSpace, { size: 4 }, () => [
        h(NButton, { size: 'small', text: true, onClick: () => openAreaModal(r) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', text: true, type: 'error', onClick: () => delArea(r.id) }, { default: () => '删除' })
      ])
  }
]

async function loadTables() {
  tableLoading.value = true
  try {
    tables.value = (await tableApi.listTables()) || []
  } finally {
    tableLoading.value = false
  }
}

async function loadAreas() {
  areaLoading.value = true
  try {
    areas.value = (await tableApi.listAreas()) || []
  } finally {
    areaLoading.value = false
  }
}

function openTableModal(row) {
  tableForm.value = row ? { ...row } : { code: '', name: '', capacity: 2, areaId: null }
  tableModal.value = true
}

function openAreaModal(row) {
  areaForm.value = row ? { ...row } : { name: '', sort: 0, status: 1, remark: '' }
  areaModal.value = true
}

async function saveTable() {
  tableSaving.value = true
  try {
    if (tableForm.value.id) await tableApi.updateTable(tableForm.value.id, tableForm.value)
    else await tableApi.createTable(tableForm.value)
    message.success('保存成功')
    tableModal.value = false
    loadTables()
  } finally {
    tableSaving.value = false
  }
}

async function saveArea() {
  areaSaving.value = true
  try {
    if (areaForm.value.id) await tableApi.updateArea(areaForm.value.id, areaForm.value)
    else await tableApi.createArea(areaForm.value)
    message.success('保存成功')
    areaModal.value = false
    loadAreas()
    loadTables()
  } finally {
    areaSaving.value = false
  }
}

async function delTable(id) {
  await tableApi.deleteTable(id)
  message.success('已删除')
  loadTables()
}

async function delArea(id) {
  await tableApi.deleteArea(id)
  message.success('已删除')
  loadAreas()
  loadTables()
}

async function handleGenAllQr() {
  const task = await tableApi.genAllQrTask()
  if (task && task.taskId) {
    const poll = async () => {
      const t = await tableApi.getQrTask(task.taskId)
      if (t && t.status === 'SUCCESS') {
        message.success(`已生成 ${t.completed || 0} 张二维码`)
        loadTables()
      } else if (t && t.status === 'FAILED') {
        message.error(t.message || '二维码批量生成失败')
        loadTables()
      } else {
        setTimeout(poll, 1500) // PENDING：继续轮询
      }
    }
    poll()
  }
}

function saveBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

async function downloadQr(id) {
  const blob = await tableApi.downloadTableQr(id)
  saveBlob(blob, `qr-${id}.png`)
  // 下载接口对未生成的桌台会现场生成二维码，刷新列表同步状态
  loadTables()
}

async function removeQr(row) {
  await tableApi.deleteTableQr(row.id)
  message.success(`已删除 ${row.name || row.code} 的二维码`)
  loadTables()
}

// 打包下载全部桌台二维码（zip，文件名：名称-代码-桌区.png）
async function handleDownloadAllQr() {
  const task = await tableApi.genDownloadAllQrTask()
  if (!task || !task.taskId) return
  const poll = async () => {
    const t = await tableApi.getQrTask(task.taskId)
    if (t && t.status === 'SUCCESS' && t.downloadable) {
      const blob = await tableApi.downloadQrTaskFile(task.taskId)
      saveBlob(blob, t.fileName || 'tables-qrcodes.zip')
      message.success(`已打包 ${t.completed || 0} 张二维码`)
    } else if (t && t.status === 'FAILED') {
      message.error(t.message || '打包失败')
    } else {
      setTimeout(poll, 1000) // 仍在打包
    }
  }
  poll()
}

// ==================== 桌台点餐 ====================

const orderModal = ref(false)
const orderTable = ref(null)
const orderSaving = ref(false)
const orderRemark = ref('')
const dishKeyword = ref('')
const dishes = ref([])
const cartItems = ref([]) // { dishId, name, price, quantity }

const cartTotal = computed(() => cartItems.value.reduce((s, i) => s + i.price * i.quantity, 0))

const filteredDishes = computed(() => {
  const kw = dishKeyword.value.trim()
  if (!kw) return dishes.value
  return dishes.value.filter((d) => (d.name || '').includes(kw))
})

async function loadDishes() {
  if (!dishes.value.length && !orderModal.value) return
  try {
    const d = await listDishes({ pageNum: 1, pageSize: 200, status: 1 })
    dishes.value = d.list || []
  } catch {
    dishes.value = []
  }
}

function openOrderModal(row) {
  if (row.status !== 0) {
    message.warning(row.status === 1 ? '桌台已占用，如需加菜请到订单页处理' : '桌台待清理，请先操作')
    return
  }
  orderTable.value = row
  cartItems.value = []
  orderRemark.value = ''
  orderModal.value = true
  loadDishes()
}

function addToCart(dish) {
  const it = cartItems.value.find((i) => i.dishId === dish.id)
  if (it) it.quantity += 1
  else cartItems.value.push({ dishId: dish.id, name: dish.name, price: Number(dish.price || 0), quantity: 1 })
}

function changeQty(it, delta) {
  it.quantity += delta
  if (it.quantity <= 0) cartItems.value = cartItems.value.filter((x) => x.dishId !== it.dishId)
}

async function submitOrder() {
  if (!cartItems.value.length) {
    message.warning('请先选择菜品')
    return
  }
  orderSaving.value = true
  try {
    const order = await tableApi.createTableOrder({
      tableId: orderTable.value.id,
      items: cartItems.value.map((i) => ({ dishId: i.dishId, quantity: i.quantity })),
      remark: orderRemark.value || undefined
    })
    message.success(`下单成功：${order?.orderNo || ''}，已推送后厨`)
    orderModal.value = false
    loadTables()
  } finally {
    orderSaving.value = false
  }
}

// ==================== 清理派发（一键派发，无需指定清理人） ====================

async function doAssignClean(table) {
  try {
    await tableApi.assignCleanTask({ tableId: table.id })
    message.success(`已派发清理任务，后厨将清理 ${table.name || table.code}`)
    loadTables()
  } catch (e) {
    message.error(e?.msg || '派发失败')
  }
}

onMounted(() => {
  loadTables()
  loadAreas()
})
</script>

<style scoped>
.dish-panel,
.cart-panel {
  border: 1px solid var(--n-border-color, #e5e7eb);
  border-radius: 6px;
  min-height: 320px;
  max-height: 420px;
  overflow-y: auto;
  padding: 8px;
}
.dish-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.15s;
}
.dish-item:hover {
  background: rgba(24, 160, 88, 0.08);
}
.dish-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}
.dish-price {
  color: #e65c4a;
  font-weight: 600;
  font-size: 13px;
}
.cart-title {
  font-size: 13px;
  color: var(--n-text-color-3, #888);
  padding: 4px 8px;
}
.cart-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
}
.cart-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}
.cart-qty {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.qty-num {
  min-width: 20px;
  text-align: center;
  font-size: 13px;
}
.cart-amount {
  width: 80px;
  text-align: right;
  font-size: 13px;
}
.cart-total {
  border-top: 1px dashed var(--n-border-color, #e5e7eb);
  margin-top: 6px;
  padding: 10px 8px 4px;
  text-align: right;
  font-weight: 600;
  font-size: 14px;
  color: #e65c4a;
}
</style>