<template>
  <el-card class="repair-list-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold">🔧 維修記錄管理列表</span>
    </template>

    <!-- 維修記錄表格 -->
    <el-table
      :data="repairList"
      stripe
      border
      v-loading="loading"
      element-loading-text="資料載入中..."
      style="width: 100%"
      empty-text="目前沒有維修記錄"
    >
      <el-table-column prop="repairId" label="報修編號" width="120">
        <template #default="{ row }">
          <strong>#{{ row.repairId }}</strong>
        </template>
      </el-table-column>

      <el-table-column prop="machineId" label="機台編號" width="120"/>
      <el-table-column prop="employeeId" label="報修人員編號" width="150"/>

      <el-table-column prop="repairDescription" label="維修描述" min-width="200">
        <template #default="{ row }">
          <el-tooltip 
            :content="row.repairDescription || row.description || '無描述'" 
            placement="top"
            :show-after="500"
          >
            <span class="description-text">
              {{ row.repairDescription || row.description || '無描述' }}
            </span>
          </el-tooltip>
        </template>
      </el-table-column>

      <el-table-column prop="repairStatus" label="維修狀態" width="140">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.repairStatus)" size="small">
            {{ getStatusLabel(row.repairStatus) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="repairTime" label="報修時間" width="180">
        <template #default="{ row }">
          {{ formatDate(row.repairTime || row.reportedAt) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button 
            type="primary" 
            icon="Edit" 
            circle 
            @click="openEditModal(row)"
            title="編輯維修單"
          />
          <el-button
            type="success"
            icon="User"
            circle
            @click="openAssignModal(row)"
            title="派工"
            style="margin-left: 8px;"
          />
        </template>
      </el-table-column>
    </el-table>

    <!-- 空數據提示 -->
    <div v-if="!loading && repairList.length === 0" class="empty-data">
      <el-empty description="目前沒有維修記錄">
        <el-button type="primary" @click="fetchRepairs">重新載入</el-button>
      </el-empty>
    </div>

    <!-- 編輯 Modal -->
    <RepairEditModal
      :visible="showEditModal"
      :repair="selectedRepair"
      :status-options="statusOptions"
      @close="handleModalClose"
      @updated="handleUpdated"
    />

    <!-- 派工 Modal -->
    <RepairAssignModal
      :visible="showAssignModal"
      :repair="selectedRepair"
      @close="handleAssignModalClose"
      @assigned="handleAssigned"
    />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'
import RepairEditModal from './RepairEditModal.vue'
import RepairAssignModal from './RepairAssignModal.vue'

const repairList = ref([])
const loading = ref(false)
const showEditModal = ref(false)
const showAssignModal = ref(false)
const selectedRepair = ref(null)
const statusOptions = ref([])

// 狀態函數
const getStatusLabel = (code) => {
  const found = statusOptions.value.find(opt => (opt.status_code || opt.statusCode) === code)
  return found ? (found.status_label || found.statusLabel || code) : code
}

const getStatusType = (code) => {
  const found = statusOptions.value.find(opt => (opt.status_code || opt.statusCode) === code)
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
  } catch {
    return dateString
  }
}

// 取得狀態選項
const fetchStatusOptions = async () => {
  try {
    const res = await api.get('/api/status-codes/repair')
    statusOptions.value = res.data
  } catch (error) {
    console.error('載入維修狀態失敗:', error)
    ElMessage.error('載入維修狀態失敗')
    statusOptions.value = []
  }
}

// 取得維修列表
const fetchRepairs = async () => {
  try {
    loading.value = true
    const res = await api.get('/api/repair')
    repairList.value = res.data || []
  } catch (error) {
    console.error('載入維修記錄失敗：', error)
    ElMessage.error('載入維修記錄失敗，請稍後再試')
    repairList.value = []
  } finally {
    loading.value = false
  }
}

// 開啟編輯 Modal
const openEditModal = (item) => {
  selectedRepair.value = {
    repairId: item.repairId,
    machineId: item.machineId,
    employeeId: item.employeeId,
    repairStatus: item.repairStatus || item.status,
    repairDescription: item.repairDescription || item.description,
    repairTime: item.repairTime || item.reportedAt,
  }
  showEditModal.value = true
}

// 開啟派工 Modal
const openAssignModal = (item) => {
  selectedRepair.value = { ...item }
  showAssignModal.value = true
}

// 關閉編輯 Modal
const handleModalClose = () => {
  showEditModal.value = false
  selectedRepair.value = null
}

// 關閉派工 Modal
const handleAssignModalClose = () => {
  showAssignModal.value = false
  selectedRepair.value = null
}

// 編輯完成
const handleUpdated = async () => {
  showEditModal.value = false
  selectedRepair.value = null
  await fetchRepairs()
  ElMessage.success('維修記錄更新成功')
}

// 派工完成
const handleAssigned = async () => {
  showAssignModal.value = false
  selectedRepair.value = null
  await fetchRepairs()
}

// 初始化
onMounted(async () => {
  await fetchStatusOptions()
  await fetchRepairs()
})
</script>

<style scoped>
.repair-list-card,
.repair-list-card * {
  font-size: 15px !important;
}

.repair-list-card {
  margin: 20px 0;
}

.description-text {
  display: block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: help;
}

.empty-data {
  padding: 40px 0;
}
</style>
