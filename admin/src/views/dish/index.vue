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
        <n-text depth="3" style="font-size: 13px">选项可设加价/减价金额，绑定到分类后顾客点餐时可选</n-text>
      </n-space>
      <n-data-table :columns="specColumns" :data="specGroups" :loading="specLoading" />
    </n-tab-pane>
  </n-tabs>

  <!-- 菜品弹窗 -->
  <n-modal v-model:show="dishModal" preset="card" :title="dishForm.id ? '编辑菜品' : '新增菜品'" style="width: 600px">
    <n-form :model="dishForm" label-placement="left">
      <n-form-item label="名称"><n-input v-model:value="dishForm.name" /></n-form-item>
      <n-form-item label="分类">
        <n-select v-model:value="dishForm.categoryId" :options="categoryOptions" clearable @update:value="onDishCategoryChange" />
      </n-form-item>
      <n-form-item label="价格"><n-input-number v-model:value="dishForm.price" :precision="2" min="0" /></n-form-item>
      <n-form-item label="库存"><n-input-number v-model:value="dishForm.stock" min="0" /></n-form-item>
      <n-form-item label="图片">
        <div style="display: flex; align-items: center; gap: 10px">
          <n-upload accept="image/*" :show-file-list="false" :custom-request="(opt) => handleImageUpload(opt, dishForm)">
            <n-button size="small">{{ dishForm.image ? '更换图片' : '上传图片' }}</n-button>
          </n-upload>
          <template v-if="dishForm.image">
            <img :src="dishForm.image" class="img-preview" @error="onPreviewError($event, dishForm)" />
            <n-button size="tiny" quaternary type="error" @click="dishForm.image = ''">移除</n-button>
          </template>
        </div>
      </n-form-item>
      <n-form-item label="介绍"><n-input v-model:value="dishForm.description" type="textarea" /></n-form-item>
      <n-form-item label="配料"><n-input v-model:value="dishForm.ingredients" /></n-form-item>
      <n-form-item label="规格">
        <div v-if="availableSpecGroups.length" style="width: 100%">
          <div v-for="g in availableSpecGroups" :key="g.id" style="margin-bottom: 10px">
            <div style="font-weight: 600; font-size: 13px; margin-bottom: 4px">{{ g.name }}</div>
            <n-checkbox-group :value="dishSpecSel[g.id] || []" @update:value="(v) => onSpecGroupChange(g, v)">
              <n-space>
                <n-checkbox v-for="opt in g.options || []" :key="opt.id" :value="opt.id" :label="optionLabel(opt)" />
              </n-space>
            </n-checkbox-group>
          </div>
        </div>
        <div v-else style="color: #999; font-size: 13px">当前分类未绑定规格组，请在「分类」弹窗中绑定后生效</div>
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="dishModal = false">取消</n-button>
        <n-button type="primary" :loading="dishSaving" @click="saveDish">保存</n-button>
      </n-space>
    </template>
  </n-modal>

  <!-- 分类弹窗 -->
  <n-modal v-model:show="categoryModal" preset="card" :title="categoryForm.id ? '编辑分类' : '新增分类'" style="width: 460px">
    <n-form :model="categoryForm" label-placement="left">
      <n-form-item label="名称"><n-input v-model:value="categoryForm.name" /></n-form-item>
      <n-form-item label="图片">
        <div style="display: flex; align-items: center; gap: 10px">
          <n-upload accept="image/*" :show-file-list="false" :custom-request="(opt) => handleImageUpload(opt, categoryForm)">
            <n-button size="small">{{ categoryForm.image ? '更换图片' : '上传图片' }}</n-button>
          </n-upload>
          <template v-if="categoryForm.image">
            <img :src="categoryForm.image" class="img-preview" @error="onPreviewError($event, categoryForm)" />
            <n-button size="tiny" quaternary type="error" @click="categoryForm.image = ''">移除</n-button>
          </template>
        </div>
      </n-form-item>
      <n-form-item label="排序"><n-input-number v-model:value="categoryForm.sort" /></n-form-item>
      <n-form-item label="状态">
        <n-switch :value="categoryForm.status === 1" @update:value="(v) => (categoryForm.status = v ? 1 : 0)" />
      </n-form-item>
      <n-form-item label="规格组">
        <n-select v-model:value="categoryForm.specGroupIds" :options="specGroupOptions" multiple clearable placeholder="绑定规格组（该分类下菜品可选）" />
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
  <n-modal v-model:show="specModal" preset="card" :title="specForm.id ? '编辑规格组' : '新增规格组'" style="width: 520px">
    <n-form :model="specForm" label-placement="left">
      <n-form-item label="名称"><n-input v-model:value="specForm.name" /></n-form-item>
      <n-form-item label="排序"><n-input-number v-model:value="specForm.sort" /></n-form-item>
      <n-form-item label="选项">
        <n-space vertical style="width: 100%">
          <div v-for="(opt, i) in specOptions" :key="i" style="display: flex; align-items: center; gap: 8px; width: 100%">
            <n-input v-model:value="opt.name" placeholder="选项名称（如 中/大/辣）" style="flex: 1; min-width: 0" />
            <n-input-number v-model:value="opt.price" :precision="2" placeholder="加价" style="width: 110px; flex-shrink: 0" />
            <n-button size="small" quaternary circle style="flex-shrink: 0" @click="specOptions.splice(i, 1)">✕</n-button>
          </div>
        </n-space>
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="space-between" style="width: 100%">
        <n-button size="small" @click="specOptions.push({ name: '', price: 0 })">+ 添加选项</n-button>
        <n-space>
          <n-button @click="specModal = false">取消</n-button>
          <n-button type="primary" :loading="specSaving" @click="saveSpec">保存</n-button>
        </n-space>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { h, onMounted, ref, computed } from 'vue'
import {
  NTabs, NTabPane, NSpace, NButton, NSelect, NInput, NInputNumber, NDataTable,
  NModal, NForm, NFormItem, NSwitch, NTag, NText, NCheckbox, NCheckboxGroup,
  NUpload, NImage, useMessage
} from 'naive-ui'
import * as dishApi from '@/api/dish'
import { DEFAULT_IMG } from '@/utils/img'

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
// 菜品规格勾选状态：specGroupId -> optionIds[]
const dishSpecSel = ref({})

const dishQuery = ref({ categoryId: null, name: '', pageNum: 1, pageSize: 10 })
const total = ref(0)

const pagination = computed(() => ({
  pageSize: dishQuery.value.pageSize,
  itemCount: total.value
}))

const categoryOptions = computed(() => categories.value.map((c) => ({ label: c.name, value: c.id })))
const specGroupOptions = computed(() => specGroups.value.map((g) => ({ label: g.name, value: g.id })))

// 菜品弹窗中可选的规格组 = 当前分类绑定的规格组
const availableSpecGroups = computed(() => {
  const cat = categories.value.find((c) => c.id === dishForm.value.categoryId)
  const ids = cat?.specGroupIds || []
  return specGroups.value.filter((g) => ids.includes(g.id))
})

function optionLabel(opt) {
  const p = Number(opt.price || 0)
  if (p > 0) return `${opt.name}（+¥${p.toFixed(2)}）`
  if (p < 0) return `${opt.name}（-¥${Math.abs(p).toFixed(2)}）`
  return opt.name
}

function optionText(o) {
  const p = Number(o.price || 0)
  if (p > 0) return `${o.name}(+${p.toFixed(2)})`
  if (p < 0) return `${o.name}(${p.toFixed(2)})`
  return o.name
}

function onSpecGroupChange(group, value) {
  dishSpecSel.value[group.id] = value
}

function onDishCategoryChange() {
  // 切换分类后旧分类的规格勾选不再适用
  dishSpecSel.value = {}
}

const dishColumns = [
  {
    title: '图片', key: 'image', width: 60,
    render: (r) => h(NImage, { src: r.image || DEFAULT_IMG, fallbackSrc: DEFAULT_IMG, width: 44, height: 44, objectFit: 'cover', style: 'border-radius: 4px' })
  },
  { title: '名称', key: 'name' },
  { title: '分类', key: 'categoryName' },
  { title: '价格', key: 'price' },
  {
    title: '规格', key: 'specItems', width: 200, ellipsis: { tooltip: true },
    render: (r) => (r.specItems || []).map((g) => `${g.specGroupName}：${(g.optionNames || []).join('/')}`).join('；') || '-'
  },
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
  {
    title: '图片', key: 'image', width: 60,
    render: (r) => h(NImage, { src: r.image || DEFAULT_IMG, fallbackSrc: DEFAULT_IMG, width: 44, height: 44, objectFit: 'cover', style: 'border-radius: 4px' })
  },
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
  { title: '选项', key: 'options', render: (r) => (r.options || []).map((o) => optionText(o)).join('、') },
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
  dishForm.value = row
    ? { ...row }
    : { name: '', categoryId: null, price: 0, stock: 0, image: '', description: '', ingredients: '', specItems: [] }
  // 回显已绑定的规格（specItems 快照 -> 勾选状态）
  dishSpecSel.value = {}
  for (const item of row?.specItems || []) {
    dishSpecSel.value[item.specGroupId] = item.optionIds || []
  }
  dishModal.value = true
}

function openCategoryModal(row) {
  categoryForm.value = row ? { ...row } : { name: '', sort: 0, status: 1, specGroupIds: [], image: '' }
  categoryModal.value = true
}

// 图片上传（n-upload custom-request）：传 MinIO 后回填 url
// 注意：naive-ui customRequest 的 file 是 UploadFileInfo 包装对象，原始 File 在 file.file
// 坑：模板里传 dishForm 给回调时，Vue3 已自动解包成 dishForm.value（普通对象），
//     所以这里 form 直接是对象，不能再用 form.value.xxx（form.value 为 undefined 会报错）。
async function handleImageUpload(options, form) {
  const rawFile = options?.file?.file || options?.file
  try {
    const data = await dishApi.uploadImage(rawFile, 'dish')
    form.image = data.url
    message.success('图片上传成功')
    options.onFinish()
  } catch (e) {
    message.error('图片上传失败')
    options.onError()
  }
}

// 预览图加载失败时换成默认图（form 已被模板解包，直接操作属性）
function onPreviewError(e, form) {
  if (form.image !== DEFAULT_IMG) {
    form.image = DEFAULT_IMG
  }
}

function openSpecModal(row) {
  specForm.value = row ? { ...row } : { name: '', sort: 0 }
  specOptions.value = (row?.options || []).map((o) => ({ id: o.id, name: o.name, price: o.price }))
  specModal.value = true
}

// 从勾选状态构建保存用的 specItems
function buildSpecItems() {
  const items = []
  for (const g of availableSpecGroups.value) {
    const ids = dishSpecSel.value[g.id] || []
    const opts = (g.options || []).filter((o) => ids.includes(o.id))
    if (!opts.length) continue
    items.push({
      specGroupId: g.id,
      specGroupName: g.name,
      optionIds: opts.map((o) => o.id),
      optionNames: opts.map((o) => o.name)
    })
  }
  return items
}

async function saveDish() {
  dishSaving.value = true
  try {
    const payload = { ...dishForm.value, specItems: buildSpecItems() }
    if (payload.id) await dishApi.updateDish(payload.id, payload)
    else await dishApi.createDish(payload)
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
  loadCategories()
}

onMounted(() => {
  loadDishes()
  loadCategories()
  loadSpecGroups()
})
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