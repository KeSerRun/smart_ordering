<template>
  <n-space vertical>
    <n-space style="margin-bottom: 12px" align="center">
      <n-select v-model:value="query.status" :options="statusOptions" clearable placeholder="订单状态" style="width: 140px" @update:value="loadOrders" />
      <n-input v-model:value="query.orderNo" clearable placeholder="订单号" style="width: 180px" @keyup.enter="loadOrders" />
      <n-button @click="loadOrders">查询</n-button>
    </n-space>

    <n-data-table
      :columns="columns"
      :data="orders"
      :loading="loading"
      remote
      :scroll-x="1000"
      :pagination="pagination"
      @update:page="changePage"
    />

    <!-- 详情弹窗 -->
    <n-drawer v-model:show="detailShow" :width="520">
      <n-drawer-content :title="`订单 ${current?.orderNo || ''}`" closable>
        <n-descriptions label-placement="left" bordered size="small" :column="2">
          <n-descriptions-item label="桌台">{{ current?.tableCode }}</n-descriptions-item>
          <n-descriptions-item label="状态">{{ statusLabel(current?.status) }}</n-descriptions-item>
          <n-descriptions-item label="下单时间">{{ current?.createTime }}</n-descriptions-item>
          <n-descriptions-item label="支付方式">{{ current?.paymentMode === 1 ? '现金' : '在线' }}</n-descriptions-item>
        </n-descriptions>
        <n-data-table :columns="itemColumns" :data="current?.items || []" size="small" style="margin-top: 12px" />
        <n-space justify="end" style="margin-top: 12px">
          <span>合计：¥<b>{{ current?.actualAmount }}</b></span>
        </n-space>
      </n-drawer-content>
    </n-drawer>
  </n-space>
</template>

<script setup>
import { h, onMounted, ref, computed } from 'vue'
import {
  NSpace, NSelect, NInput, NButton, NDataTable, NDrawer, NDrawerContent,
  NDescriptions, NDescriptionsItem, NTag, useMessage
} from 'naive-ui'
import * as orderApi from '@/api/order'

const message = useMessage()
const orders = ref([])
const loading = ref(false)
const total = ref(0)
const detailShow = ref(false)
const current = ref(null)

const query = ref({ status: null, orderNo: '', pageNum: 1, pageSize: 10 })

const statusMap = {
  0: ['待支付', 'warning'],
  1: ['已支付', 'success'],
  2: ['已取消', 'error'],
  3: ['已退款', 'info']
}
const statusOptions = [
  { label: '待支付', value: 0 },
  { label: '已支付', value: 1 },
  { label: '已取消', value: 2 },
  { label: '已退款', value: 3 }
]

function statusLabel(s) {
  return (statusMap[s] || ['未知', 'warning'])[0]
}

const pagination = computed(() => ({ pageSize: query.value.pageSize, itemCount: total.value }))

const columns = [
  { title: '订单号', key: 'orderNo', width: 200, ellipsis: { tooltip: true } },
  { title: '桌台', key: 'tableCode', width: 90 },
  { title: '原价', key: 'originalAmount', width: 90 },
  { title: '实付', key: 'actualAmount', width: 90 },
  {
    title: '状态', key: 'status', width: 100,
    render: (r) => h(NTag, { type: (statusMap[r.status] || ['未知', 'warning'])[1], size: 'small' }, () => statusLabel(r.status))
  },
  { title: '下单时间', key: 'createTime', width: 165 },
  {
    title: '操作', key: 'action', width: 140,
    render: (r) =>
      h(NSpace, { size: 4, justify: 'center' }, () => [
        h(NButton, { size: 'small', text: true, onClick: () => showDetail(r) }, { default: () => '详情' }),
        r.status === 0
          ? h(NButton, { size: 'small', text: true, type: 'error', onClick: () => doCancel(r) }, { default: () => '取消' })
          : null
      ])
  }
]

const itemColumns = [
  { title: '菜品', key: 'dishName', width: 180, ellipsis: { tooltip: true } },
  { title: '数量', key: 'quantity', width: 70 },
  { title: '单价', key: 'price', width: 80 },
  { title: '小计', key: 'amount', width: 80, render: (r) => (r.isGift ? '赠' : r.amount) }
]

async function loadOrders() {
  loading.value = true
  try {
    const data = await orderApi.listOrders(query.value)
    orders.value = data?.list || []
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

function changePage(page) {
  query.value.pageNum = page
  loadOrders()
}

async function showDetail(row) {
  current.value = await orderApi.orderDetail(row.id)
  current.value = { ...current.value, orderNo: row.orderNo }
  detailShow.value = true
}

// 跑单处理：取消待支付订单，释放桌台为空闲
async function doCancel(row) {
  try {
    await orderApi.cancelOrder(row.id)
    message.success(`订单 ${row.orderNo} 已取消，桌台已释放`)
    loadOrders()
  } catch (e) {
    message.error(e?.msg || '取消失败')
  }
}

onMounted(loadOrders)
</script>