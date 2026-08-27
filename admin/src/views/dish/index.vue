<template>
  <n-tabs type="line" v-model:value="tab">
    <!-- 菜品 -->
    <n-tab-pane name="dishes" tab="菜品">
      <n-space style="margin-bottom: 12px" align="center">
        <n-button type="primary" @click="openDishModal()">新增菜品</n-button>
        <n-select
          v-model:value="dishQuery.categoryId"
          :options="categoryOptions"
          clearable
          placeholder="全部分类"
          style="width: 180px"
          @update:value="loadDishes"
        />
        <n-input v-model:value="dishQuery.name" clearable placeholder="菜品名称" style="width: 180px" @keyup.enter="loadDishes" />
        <n-button @click="loadDishes">查询</n-button>
      </n-space>
      <n-data-table :columns="dishColumns" :data="dishes" :loading="dishLoading" remote :pagination="pagination" @update:page="changePage" />
    </n-tab-pane>

    <!-- 分类 -->
    <n-tab-pane name="categories" tab="分类">
      <n-space style="margin-bottom: 12px">
        <n-button type="primary" @click="openCategoryModal()">新增分类</n-button>
      </n-space>
      <n-data-table :columns="categoryColumns" :data="categories" :loading="categoryLoading" />
    </n-tab-pane>

    <!-- 规格 -->
    <n-tab-pane name="specs" tab="规格">
      <n-space style="margin-bottom: 12px">
        <n-button type="primary" @click="openSpecModal()">新增规格组</n-button>
      </n-space>
      <n-data-table :columns="specColumns" :data="specGroups" :loading="specLoading" />
    </n-tab-pane>
  </n-tabs>

  <!-- 菜品弹窗 -->
  <n-modal v-model:show="dishModal" preset="card" :title="dishForm.id ? '编辑菜品' : '新增菜品'" style="width: 560px">
    <n-form :model="dishForm" label-placement="left">
      <n-form-item label="名称"><n-input v-model:value="dishForm.name" /></n-form-item>
      <n-form-item label="分类">
        <n-select v-model:value="dishForm.categoryId" :options="categoryOptions" clearable />
      </n-form-item>
      <n-form-item label="价格"><n-input-number v-model:value="dishForm.price" :precision="2" min="0" /></n-form-item>
      <n-form-item label="库存"><n-input-number v-model:value="dishForm.stock" min="0" /></n-form-item>
      <n-form-item label="图片URL"><n-input v-model:value="dishForm.image" placeholder="http://..." /></n-form-item>
      <n-form-item label="介绍"><n-input v-model:value="dishForm.description" type="textarea" /></n-form-item>
      <n-form-item label="配料"><n-input v-model:value="dishForm.ingredients" /></n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="dishModal = false">取消</n-button>
        <n-button type="primary" :loading="dishSaving" @click="saveDish">保存</n-button>
      </n-space>
    </template>
  </n-modal>

  <!-- 分类弹窗 -->
  <n-modal v-model:show="categoryModal" preset="card" :title="categoryForm.id ? '编辑分类' : '新增分类'" style="width: 420px">
    <n-form :model="categoryForm" label-placement="left">
      <n-form-item label="名称"><n-input v-model:value="categoryForm.name" /></n-form-item>
      <n-form-item label="排序"><n-input-number v-model:value="categoryForm.sort" /></n-form-item>
      <n-form-item label="状态">
        <n-switch :value="categoryForm.status === 1" @update:value="(v) => (categoryForm.status = v ? 1 : 0)" />
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="categoryModal = false">取消</n-button>
        <n-button type="primary" :loading="categorySaving" @click="saveCategory">保存</n-button>
      </n-space>
    </template>
  </n-modal>

  <!-- 规格弹窗 -->
  <n-modal v-model:show="specModal" preset="card" :title="specForm.id ? '编辑规格组' : '新增规格组'" style="width: 420px">
    <n-form :model="specForm" label-placement="left">
      <n-form-item label="名称"><n-input v-model:value="specForm.name" /></n-form-item>
      <n-form-item label="排序"><n-input-number v-model:value="specForm.sort" /></n-form-item>
      <n-form-item label="选项">
        <n-space vertical v-for="(opt, i) in specOptions" :key="i">
          <n-input v-model:value="opt.name" placeholder="选项名称（如 中/大/辣）" />
        </n-space>
        <n-button size="small" @click="specOptions.push({ name: '' })">+ 添加选项</n-button>
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="specModal = false">取消</n-button>
        <n-button type="primary" :loading="specSaving" @click="saveSpec">保存</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { h, onMounted, ref, computed } from 'vue'
import {
  NTabs, NTabPane, NSpace, NButton, NSelect, NInput, NInputNumber, NDataTable,
  NModal, NForm, NFormItem, NSwitch, NTag, useMessage
} from 'naive-ui'
import * as dishApi from '@/api/dish'

const message = useMessage()
const tab = ref('dishes')

const categories = ref([])
const specGroups = ref([])
const dishes = ref([])
const dishLoading = ref(false)
const categoryLoading = ref(false)
const specLoading = ref(false)
const dishModal = ref(false)
const categoryModal = ref(false)
const specModal = ref(false)
const dishSaving = ref(false)
const categorySaving = ref(false)
const specSaving = ref(false)
const dishForm = ref({})
const categoryForm = ref({})
const specForm = ref({})
const specOptions = ref([])

const dishQuery = ref({ categoryId: null, name: '', pageNum: 1, pageSize: 10 })
const total = ref(0)

const pagination = computed(() => ({
  pageSize: dishQuery.value.pageSize,
  itemCount: total.value
}))

const categoryOptions = computed(() => categories.value.map((c) => ({ label: c.name, value: c.id })))

const dishColumns = [
  { title: '名称', key: 'name' },
  { title: '分类', key: 'categoryName' },
  { title: '价格', key: 'price' },
  {
    title: '状态', key: 'status', width: 100,
    render: (r) =>
      h(NTag, { type: r.status === 1 ? 'success' : 'default', size: 'small' }, () => (r.status === 1 ? '在售' : '下架'))
  },
  {
    title: '操作', key: 'action', width: 170,
    render: (r) =>
      h(NSpace, { size: 4 }, () => [
        h(NButton, { size: 'small', text: true, onClick: () => openDishModal(r) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', text: true, type: r.status === 1 ? 'warning' : 'success', onClick: () => toggleDish(r) }, { default: () => (r.status === 1 ? '下架' : '上架') })
      ])
  }
]

const categoryColumns = [
  { title: '名称', key: 'name' },
  { title: '排序', key: 'sort', width: 80 },
  {
    title: '状态', key: 'status', width: 90,
    render: (r) => h(NTag, { type: r.status === 1 ? 'success' : 'default', size: 'small' }, () => (r.status === 1 ? '启用' : '停用'))
  },
  { title: '规格', key: 'specGroupNames' },
  {
    title: '操作', key: 'action', width: 140,
    render: (r) =>
      h(NSpace, { size: 4 }, () => [
        h(NButton, { size: 'small', text: true, onClick: () => openCategoryModal(r) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', text: true, type: 'error', onClick: () => delCategory(r.id) }, { default: () => '删除' })
      ])
  }
]

const specColumns = [
  { title: '名称', key: 'name' },
  { title: '排序', key: 'sort', width: 80 },
  { title: '选项', key: 'options', render: (r) => (r.options || []).map((o) => o.name).join('、') },
  {
    title: '操作', key: 'action', width: 140,
    render: (r) =>
      h(NSpace, { size: 4 }, () => [
        h(NButton, { size: 'small', text: true, onClick: () => openSpecModal(r) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', text: true, type: 'error', onClick: () => delSpec(r.id) }, { default: () => '删除' })
      ])
  }
]

async function loadDishes() {
  dishLoading.value = true
  try {
    const data = await dishApi.listDishes(dishQuery.value)
    dishes.value = data?.list || []
    total.value = data?.total || 0
  } finally {
    dishLoading.value = false
  }
}

async function loadCategories() {
  categoryLoading.value = true
  try {
    categories.value = (await dishApi.listCategories()) || []
  } finally {
    categoryLoading.value = false
  }
}

async function loadSpecGroups() {
  specLoading.value = true
  try {
    specGroups.value = (await dishApi.listSpecGroups()) || []
  } finally {
    specLoading.value = false
  }
}

function changePage(page) {
  dishQuery.value.pageNum = page
  loadDishes()
}

function openDishModal(row) {
  dishForm.value = row ? { ...row } : { name: '', categoryId: null, price: 0, stock: 0, image: '', description: '', ingredients: '' }
  dishModal.value = true
}

function openCategoryModal(row) {
  categoryForm.value = row ? { ...row } : { name: '', sort: 0, status: 1 }
  categoryModal.value = true
}

function openSpecModal(row) {
  specForm.value = row ? { ...row } : { name: '', sort: 0 }
  specOptions.value = (row?.options || []).map((o) => ({ id: o.id, name: o.name }))
  specModal.value = true
}

async function saveDish() {
  dishSaving.value = true
  try {
    if (dishForm.value.id) await dishApi.updateDish(dishForm.value.id, dishForm.value)
    else await dishApi.createDish(dishForm.value)
    message.success('保存成功')
    dishModal.value = false
    loadDishes()
  } finally {
    dishSaving.value = false
  }
}

async function saveCategory() {
  categorySaving.value = true
  try {
    if (categoryForm.value.id) await dishApi.updateCategory(categoryForm.value.id, categoryForm.value)
    else await dishApi.createCategory(categoryForm.value)
    message.success('保存成功')
    categoryModal.value = false
    loadCategories()
  } finally {
    categorySaving.value = false
  }
}

async function saveSpec() {
  specSaving.value = true
  try {
    const payload = { ...specForm.value, options: specOptions.value.filter((o) => o.name) }
    if (payload.id) await dishApi.updateSpecGroup(payload.id, payload)
    else await dishApi.createSpecGroup(payload)
    message.success('保存成功')
    specModal.value = false
    loadSpecGroups()
    loadCategories()
  } finally {
    specSaving.value = false
  }
}

async function toggleDish(row) {
  await dishApi.updateDishStatus(row.id, row.status === 1 ? 0 : 1)
  message.success('已更新')
  loadDishes()
}

async function delCategory(id) {
  await dishApi.deleteCategory(id)
  message.success('已删除')
  loadCategories()
  loadDishes()
}

async function delSpec(id) {
  await dishApi.deleteSpecGroup(id)
  message.success('已删除')
  loadSpecGroups()
}

onMounted(() => {
  loadDishes()
  loadCategories()
  loadSpecGroups()
})
</script>