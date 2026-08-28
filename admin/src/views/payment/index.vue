<template>
  <n-space vertical>
    <n-tabs type="line" v-model:value="tab">
      <!-- 待支付订单 -->
      <n-tab-pane name="pending" tab="待支付订单">
        <n-space style="margin-bottom: 12px" align="center">
          <n-button type="primary" @click="openCash()">现金收银</n-button>
          <n-button @click="loadPending">刷新</n-button>
          <n-text depth="3">点击订单行可选中进行现金收银</n-text>
        </n-space>
        <n-data-table
          :columns="pendingColumns"
          :data="pendingOrders"
          :loading="pendingLoading"
          :scroll-x="900"
          :row-class-name="rowClassName"
          :row-props="rowProps"
        />
        <n-empty v-if="!pendingLoading && !pendingOrders.length" description="暂无待支付订单" style="margin-top: 24px" />
      </n-tab-pane>

      <!-- 支付记录 -->
      <n-tab-pane name="records" tab="支付记录">
        <n-data-table :columns="columns" :data="rows" :loading="loading" :scroll-x="750" :pagination="pagination" />
      </n-tab-pane>
    </n-tabs>

    <!-- 现金收银弹窗 -->
    <n-modal v-model:show="showCash" preset="card" title="现金收银（按桌台号搜索）" style="width: 480px">
      <n-space vertical>
        <n-space align="center">
          <n-input v-model:value="cash.tableCode" clearable placeholder="输入桌台号，如 A01" style="width: 220px" @keyup.enter="searchByTable" />
          <n-button type="primary" :loading="searching" @click="searchByTable">搜索</n-button>
        </n-space>

        <!-- 搜索结果 -->
        <div v-if="searched.length" class="search-result">
          <div
            v-for="o in searched"
            :key="o.id"
            class="search-item"
            :class="{ active: selectedOrder?.id === o.id }"
            @click="pickSearched(o)"
          >
            <span class="si-left">
              <span class="si-no">{{ o.orderNo }}</span>
              <span class="si-table">桌台 {{ o.tableCode || '-' }}</span>
            </span>
            <span class="si-amount">￥{{ o.actualAmount }}</span>
          </div>
        </div>
        <n-empty v-else-if="searchedShown" description="该桌台暂无待支付订单" size="small" style="padding: 8px 0" />

        <n-alert v-if="selectedOrder" type="success" :bordered="false">
          选中订单：<b>{{ selectedOrder.orderNo }}</b>（桌台 {{ selectedOrder.tableCode }}）应收 <b>￥{{ selectedOrder.actualAmount }}</b>
        </n-alert>

        <n-form label-placement="left">
          <n-form-item label="实收金额">
            <n-input-number v-model:value="cash.receivedAmount" style="width: 100%" :min="0" :precision="2" placeholder="顾客实付" />
          </n-form-item>
          <n-form-item label="找零">
            <n-text :type="changeAmount < 0 ? 'error' : 'success'" style="font-size: 16px; font-weight: 600">
              ￥{{ changeAmount.toFixed(2) }}
            </n-text>
          </n-form-item>
        </n-form>
      </n-space>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showCash = false">取消</n-button>
          <n-button type="primary" :loading="saving" :disabled="!selectedOrder || changeAmount < 0" @click="submitCash">确认收银</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-space>
</template>

<script setup>
import { h, ref, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { listPayments, cashPay } from '@/api/payment'
import { listOrders } from '@/api/order'

const message = useMessage()
const tab = ref('pending')

// ===== 支付记录 =====
const rows = ref([])
const loading = ref(false)
const pagination = ref({ page: 1, pageSize: 20, itemCount: 0 })

// ===== 待支付订单 =====
const pendingOrders = ref([])
const pendingLoading = ref(false)
const selectedOrder = ref(null)

const columns = [
  { title: '订单号', key: 'orderNo', width: 200, ellipsis: { tooltip: true }, render: (r) => r.orderNo || '-' },
  { title: '支付单号', key: 'paymentNo', width: 200, ellipsis: { tooltip: true } },
  { title: '方式', key: 'paymentMethod', width: 90, render: (r) => r.paymentMethod === 1 ? '现金' : r.paymentMethod === 2 ? '支付宝' : r.paymentMethod === 3 ? '微信' : '其他' },
  { title: '金额', key: 'amount', width: 90 },
  { title: '状态', key: 'status', width: 70, render: (r) => r.status === 1 ? '成功' : '失败' },
  { title: '时间', key: 'createTime', width: 160 }
]

const pendingColumns = [
  {
    title: '订单号', key: 'orderNo', width: 200, ellipsis: { tooltip: true },
    render: (r) => h('span', { style: 'font-weight: 600' }, r.orderNo)
  },
  { title: '桌台', key: 'tableCode', width: 80, render: (r) => r.tableCode || '-' },
  { title: '应收金额', key: 'actualAmount', width: 110, render: (r) => `￥${r.actualAmount}` },
  { title: '支付方式', key: 'paymentMode', width: 90, render: (r) => (r.paymentMode === 0 ? '先付' : '后付') },
  { title: '下单时间', key: 'createTime', width: 165, render: (r) => (r.createTime ? String(r.createTime).replace('T', ' ') : '-') },
  {
    title: '操作', key: 'op', width: 80, fixed: 'right',
    render: (r) => h('a', { href: 'javascript:;', style: 'color:#18a058;font-weight:600', onClick: () => selectAndPay(r) }, '收银')
  }
]

// 选中行高亮
const rowClassName = (row) => (selectedOrder.value?.id === row.id ? 'selected-row' : '')
const rowProps = (row) => ({
  style: 'cursor: pointer',
  onClick: () => selectAndPay(row)
})

// ===== 现金收银 =====
const showCash = ref(false)
const saving = ref(false)
const searching = ref(false)
const searched = ref([])
const searchedShown = ref(false)
const cash = ref({ tableCode: '', receivedAmount: 0 })

const changeAmount = computed(() => (cash.value.receivedAmount || 0) - (selectedOrder.value?.actualAmount || 0))

function openCash() {
  selectedOrder.value = null
  searched.value = []
  searchedShown.value = false
  cash.value = { tableCode: '', receivedAmount: 0 }
  showCash.value = true
}

// 按桌台号搜索待支付订单
async function searchByTable() {
  const code = cash.value.tableCode.trim()
  if (!code) {
    message.warning('请先输入桌台号')
    return
  }
  searching.value = true
  try {
    const d = await listOrders({ pageNum: 1, pageSize: 20, status: 0, tableCode: code })
    searched.value = d.list || []
    searchedShown.value = true
    selectedOrder.value = null
    cash.value.receivedAmount = 0
    if (!searched.value.length) {
      message.info(`桌台「${code}」暂无待支付订单`)
    }
  } finally {
    searching.value = false
  }
}

function pickSearched(order) {
  selectedOrder.value = order
  cash.value.receivedAmount = Number(order.actualAmount)
}

function selectAndPay(order) {
  selectedOrder.value = order
  cash.value = { tableCode: order.tableCode || '', receivedAmount: Number(order.actualAmount) }
  showCash.value = true
}

async function submitCash() {
  const order = selectedOrder.value
  if (!order) {
    message.warning('请先搜索并选中待支付订单')
    return
  }
  if (changeAmount.value < 0) {
    message.warning('实收金额不足')
    return
  }
  saving.value = true
  try {
    const p = await cashPay({ orderNo: order.orderNo, receivedAmount: cash.value.receivedAmount })
    message.success(`收银成功：${p?.orderNo || order.orderNo}，找零 ￥${changeAmount.value.toFixed(2)}`)
    showCash.value = false
    selectedOrder.value = null
    loadPending()
    load()
  } finally {
    saving.value = false
  }
}

// ===== 加载 =====
async function loadPending() {
  pendingLoading.value = true
  try {
    // status=0：待支付订单（开台下单未结账）
    const d = await listOrders({ pageNum: 1, pageSize: 200, status: 0 })
    pendingOrders.value = d.list || []
    // 已收银的订单若仍被选中，清掉选中态
    if (selectedOrder.value && !pendingOrders.value.some((o) => o.id === selectedOrder.value.id)) {
      selectedOrder.value = null
    }
  } finally {
    pendingLoading.value = false
  }
}

async function load() {
  loading.value = true
  try {
    const d = await listPayments({ pageNum: pagination.value.page, pageSize: pagination.value.pageSize })
    rows.value = d.list
    pagination.value.itemCount = d.total
  } finally {
    loading.value = false
  }
}

pagination.value.onChange = (p) => { pagination.value.page = p; load() }

onMounted(() => {
  loadPending()
  load()
})
</script>

<style scoped>
:deep(.selected-row > td) {
  background: rgba(24, 160, 88, 0.1) !important;
}
.search-result {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  max-height: 220px;
  overflow-y: auto;
}
.search-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.15s;
}
.search-item:last-child {
  border-bottom: none;
}
.search-item:hover {
  background: rgba(24, 160, 88, 0.06);
}
.search-item.active {
  background: rgba(24, 160, 88, 0.14);
}
.si-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.si-no {
  font-size: 13px;
  font-weight: 600;
}
.si-table {
  font-size: 12px;
  color: #888;
}
.si-amount {
  font-size: 15px;
  font-weight: 700;
  color: #e65c4a;
}
</style>