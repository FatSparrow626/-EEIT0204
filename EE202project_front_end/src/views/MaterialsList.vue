<template>
  <div class="depot-view">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>物料列表（庫存）</span>
          <el-button type="primary" @click="openAddMaterialDialog">新增物料</el-button>
        </div>
      </template>

      <el-table :data="materials" style="width: 100%" v-loading="loadingMaterials">
        <el-table-column prop="materialId" label="ID" width="80" />
        <el-table-column prop="materialName" label="物料名稱" />

        <el-table-column label="庫存數量" width="120">
          <template #default="{ row }">
            <el-tag :type="getStockStatusType(row)">
              {{ row.stockCurrent }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="unit" label="單位" width="80" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="editMaterial(row)">編輯</el-button>
            <el-button link type="danger" size="small" @click="deleteMaterial(row.materialId)">刪除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/編輯物料 -->
    <el-dialog v-model="showAddMaterialDialog" :title="isEditMode ? '編輯物料' : '新增物料'" width="700">
      <el-form :model="currentMaterial" label-width="100px">
        <el-form-item label="物料ID" v-if="isEditMode">
          <el-input v-model="currentMaterial.materialId" disabled />
        </el-form-item>

        <el-form-item label="物料名稱">
          <el-input v-model="currentMaterial.materialName" />
        </el-form-item>

        <el-form-item label="物料類型">
          <el-input v-model="currentMaterial.materialType" />
        </el-form-item>

        <el-form-item label="單位">
          <el-input v-model="currentMaterial.unit" />
        </el-form-item>

        <el-form-item label="描述">
          <el-input v-model="currentMaterial.materialDescription" type="textarea" />
        </el-form-item>

        <el-form-item label="儲位">
          <el-input v-model="currentMaterial.location" />
        </el-form-item>

        <el-form-item label="安全庫存">
          <el-input-number v-model="currentMaterial.safetyStock" :min="0" />
        </el-form-item>

        <el-form-item label="目前庫存">
          <el-input-number v-model="currentMaterial.stockCurrent" :min="0" />
        </el-form-item>

        <el-form-item label="啟用">
          <el-switch v-model="currentMaterial.active" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddMaterialDialog = false">取消</el-button>
          <el-button @click="fillWithSampleData" v-if="!isEditMode">一鍵輸入</el-button>
          <el-button type="primary" @click="saveMaterial">{{ isEditMode ? '更新' : '新增' }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/http-common'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Material {
  materialId?: number
  materialName: string
  stockCurrent: number
  unit: string
  materialDescription?: string
  location?: string
  safetyStock?: number | null
  reorderLevel?: number | null
  active?: boolean
  materialType?: string
}

const API_BASE_URL = 'http://localhost:8082/api/depot'

const materials = ref<Material[]>([])
const loadingMaterials = ref(false)

const showAddMaterialDialog = ref(false)
const isEditMode = ref(false)
const currentMaterial = ref<Material>({
  materialName: '',
  stockCurrent: 0,
  unit: '',
  materialDescription: '',
  location: '',
  safetyStock: 0,
  reorderLevel: 0,
  active: true,
  materialType: ''
})

function fillWithSampleData() {
  currentMaterial.value = {
    materialName: '螢幕主機線',
    materialType: '電子元件',
    unit: '個',
    materialDescription: '這是一個範例物料的描述',
    location: 'A-01-01',
    safetyStock: 10,
    stockCurrent: 100,
    active: true,
    reorderLevel: 0
  }
}

function resetMaterialForm() {
  currentMaterial.value = {
    materialName: '',
    stockCurrent: 0,
    unit: '',
    materialDescription: '',
    location: '',
    safetyStock: 0,
    reorderLevel: 0,
    active: true,
    materialType: ''
  }
}

async function fetchMaterials() {
  loadingMaterials.value = true
  try {
    const res = await http.get<Material[]>(`${API_BASE_URL}/materials`)
    materials.value = res.data
  } catch (err: any) {
    ElMessage.error(`獲取物料列表失敗：${err.response?.data || err.message}`)
  } finally {
    loadingMaterials.value = false
  }
}

function openAddMaterialDialog() {
  isEditMode.value = false
  resetMaterialForm()
  showAddMaterialDialog.value = true
}

function editMaterial(m: Material) {
  isEditMode.value = true
  currentMaterial.value = { ...m }
  showAddMaterialDialog.value = true
}

async function saveMaterial() {
  try {
    if (isEditMode.value) {
      await http.put(`${API_BASE_URL}/materials/${currentMaterial.value.materialId}`, currentMaterial.value)
      ElMessage.success('物料更新成功')
    } else {
      await http.post(`${API_BASE_URL}/materials`, currentMaterial.value)
      ElMessage.success('物料新增成功')
    }
    showAddMaterialDialog.value = false
    fetchMaterials()
  } catch (err: any) {
    ElMessage.error(`儲存物料失敗：${err.response?.data || err.message}`)
  }
}

async function deleteMaterial(id?: number) {
  if (!id) return
  try {
    await ElMessageBox.confirm('確定要刪除此物料嗎？', '警告', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await http.delete(`${API_BASE_URL}/materials/${id}`)
    ElMessage.success('物料刪除成功')
    fetchMaterials()
  } catch (err: any) {
    ElMessage.error(`刪除物料失敗：${err.response?.data || err.message}`)
  }
}

/** 依安全庫存標示顏色 */
function getStockStatusType(material: Material): string {
  if (material.safetyStock == null || material.safetyStock <= 0) return 'info'
  const stock = Number(material.stockCurrent ?? 0)
  const safety = Number(material.safetyStock ?? 0)
  if (stock < safety) return 'danger'      // 低於安全庫存：紅
  if (stock <= safety * 1.3) return 'warning' // 接近安全庫存：黃
  return 'success'                         // 充足：綠
}

onMounted(fetchMaterials)
</script>

<style scoped>
.depot-view {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}

.dialog-footer button:first-child {
  margin-right: 10px;
}
</style>
