<template>
  <el-card class="maintenance-list-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold">🔧 保養記錄查詢</span>
    </template>

    <el-table
      :data="maintenanceList"
      stripe
      border
      v-loading="loading"
      element-loading-text="資料載入中..."
      style="width: 100%"
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
    </el-table>

    <!-- 空數據提示 -->
    <div v-if="!loading && maintenanceList.length === 0" class="empty-data">
      <el-empty description="目前沒有保養記錄" />
    </div>

    <!-- 錯誤提示 -->
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      center
      :closable="false"
      style="margin-top: 20px"
    />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'

const maintenanceList = ref([])
const loading = ref(false)
const error = ref(null)
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
  return new Date(dateString).toLocaleString()
}

const fetchStatusOptions = async () => {
  try {
    const res = await api.get('/api/status-codes/maintenance')
    statusOptions.value = res.data
  } catch (error) {
    statusOptions.value = []
  }
}

const fetchMaintenanceList = async () => {
  try {
    loading.value = true
    error.value = null
    const res = await api.get('/api/maintenance')
    maintenanceList.value = res.data
  } catch (err) {
    error.value = '資料載入失敗，請稍後再試'
    ElMessage.error('資料載入失敗')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchStatusOptions()
  await fetchMaintenanceList()
})
</script>

<style scoped>
.maintenance-list-card,
.maintenance-list-card * {
  font-size: 15px !important;
}
</style>
