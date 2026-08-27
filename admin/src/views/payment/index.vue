<template>
  <n-space vertical>
    <n-space style="margin-bottom:12px">
      <n-button type="primary" @click="openCash">现金收银</n-button>
    </n-space>
    <n-data-table :columns="columns" :data="rows" :loading="loading" :pagination="pagination" />
  </n-space>
  <n-modal v-model:show="showCash" preset="card" title="现金收银" style="width:420px">
    <n-form label-placement="top">
      <n-form-item label="订单号"><n-input v-model:value="cash.orderNo" placeholder="订单号或订单ID" /></n-form-item>
      <n-form-item label="实收金额"><n-input-number v-model:value="cash.receivedAmount" style="width:100%" :min="0" /></n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end"><n-button @click="showCash=false">取消</n-button><n-button type="primary" :loading="saving" @click="submitCash">确定</n-button></n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { listPayments, cashPay } from '@/api/payment'
const message = useMessage()
const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const showCash = ref(false)
const cash = ref({ orderNo: '', receivedAmount: 0 })
const pagination = ref({ page: 1, pageSize: 20, itemCount: 0 })
const columns = [
  { title: '支付单号', key: 'paymentNo' },
  { title: '订单', key: 'orderId', width: 90 },
  { title: '方式', key: 'paymentMethod', render: (r) => r.paymentMethod === 1 ? '现金' : r.paymentMethod === 2 ? '支付宝' : r.paymentMethod === 3 ? '微信' : '其他' },
  { title: '金额', key: 'amount' },
  { title: '状态', key: 'status', render: (r) => r.status === 1 ? '成功' : '失败' },
  { title: '时间', key: 'createTime', width: 160 }
]
const load = async () => {
  loading.value = true
  try {
    const d = await listPayments({ pageNum: pagination.value.page, pageSize: pagination.value.pageSize })
    rows.value = d.list
    pagination.value.itemCount = d.total
  } finally { loading.value = false }
}
pagination.value.onChange = (p) => { pagination.value.page = p; load() }
const openCash = () => { cash.value = { orderNo: '', receivedAmount: 0 }; showCash.value = true }
const submitCash = async () => {
  saving.value = true
  try { await cashPay(cash.value); message.success('收银成功'); showCash.value = false; load() }
  finally { saving.value = false }
}
onMounted(load)
</script>