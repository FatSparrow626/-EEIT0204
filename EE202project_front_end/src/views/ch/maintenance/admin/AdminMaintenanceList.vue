<template>
  <el-card class="maintenance-list-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold">🔧 保養記錄列表</span>
    </template>

    <el-table
      :data="maintenanceList"
      stripe
      border
      v-loading="loading"
      element-loading-text="資料載入中..."
      style="width: 100%"
      empty-text="目前沒有保養記錄"
    >
      <el-table-column prop="scheduleId" label="保養單編號" width="120">
        <template #default="{ row }">
          <strong>#{{ row.scheduleId }}</strong>
        </template>
      </el-table-column>
      <el-table-column prop="machineId" label="機台編號" />
      <el-table-column prop="employeeId" label="保養人員編號" />
      <el-table-column prop="maintenanceDescription" label="保養描述" min-width="200">
        <template #default="{ row }">
          <el-tooltip :content="row.maintenanceDescription" placement="top">
            <span class="description-text">{{ row.maintenanceDescription }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="maintenanceStatus" label="保養狀態" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.maintenanceStatus)" size="small">
            {{ getStatusLabel(row.maintenanceStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="scheduleDate" label="預計保養日期" width="180">
        <template #default="{ row }">
          {{ formatDate(row.scheduleDate) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" icon="Edit" circle @click="openEditModal(row)" />
          <el-button type="danger" icon="Delete" circle @click="openDeleteModal(row)" />
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && maintenanceList.length === 0" class="empty-data">
      <el-empty description="目前沒有保養記錄" />
    </div>

    <MaintenanceEditModal
      v-if="showEditModal"
      :maintenance="selectedMaintenance"
      :status-options="statusOptions"
      @close="showEditModal = false"
      @updated="handleUpdated"
    />

    <MaintenanceDeleteModal
      v-if="showDeleteModal"
      :maintenance="selectedMaintenance"
      @close="showDeleteModal = false"
      @deleted="handleDeleted"
    />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'
import MaintenanceEditModal from './MaintenanceEditModal.vue'
import MaintenanceDeleteModal from './MaintenanceDeleteModal.vue'

const maintenanceList = ref([])
const loading = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const selectedMaintenance = ref(null)
const statusOptions = ref([])

const getStatusLabel = (code) => {
  const found = statusOptions.value.find(opt =>
    (opt.status_code || opt.statusCode) === code
  )
  return found ? (found.status_label || found.statusLabel || code) : code
}

const getStatusType = (code) => {
  const found = statusOptions.value.find(opt =>
    (opt.status_code || opt.statusCode) === code
  )
  return found ? (found.status_type || found.type || 'info') : 'info'
}

const formatDate = (dateString) => {
  if (!dateString) return '無資料'
  try {
    return new Date(dateString).toLocaleString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (error) {
    console.error('日期格式化錯誤:', error)
    return dateString
  }
}

const fetchStatusOptions = async () => {
  try {
    const token = localStorage.getItem('token')
    const res = await api.get('/api/status-codes/maintenance', {
      headers: { Authorization: `Bearer ${token}` }
    })
    statusOptions.value = res.data
  } catch (error) {
    ElMessage.error('載入保養狀態失敗')
    statusOptions.value = []
  }
}

const fetchMaintenance = async () => {
  try {
    loading.value = true
    const token = localStorage.getItem('token')
    const res = await api.get('/api/maintenance', {
      headers: { Authorization: `Bearer ${token}` }
    })
    maintenanceList.value = res.data || []
  } catch (error) {
    console.error('載入失敗：', error)
    ElMessage.error('載入保養記錄失敗')
    maintenanceList.value = []
  } finally {
    loading.value = false
  }
}

const openEditModal = (item) => {
  selectedMaintenance.value = { ...item }
  showEditModal.value = true
}

const openDeleteModal = (item) => {
  selectedMaintenance.value = item
  showDeleteModal.value = true
}

const handleUpdated = async () => {
  showEditModal.value = false
  await fetchMaintenance()
  ElMessage.success('更新成功')
}

const handleDeleted = async () => {
  showDeleteModal.value = false
  await fetchMaintenance()
  ElMessage.success('刪除成功')
}

onMounted(async () => {
  await fetchStatusOptions()
  await fetchMaintenance()
})
</script>

<style scoped>
.maintenance-list-card,
.maintenance-list-card * {
  font-size: 15px !important;
}
</style>
