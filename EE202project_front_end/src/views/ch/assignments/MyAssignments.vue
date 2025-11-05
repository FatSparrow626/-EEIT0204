<template>
  <el-card class="assignment-card" style="width: 100%">
    <template #header>
      <span>我的派工任務</span>
    </template>
    <el-table :data="assignments" stripe border style="width: 100%">
      <el-table-column prop="taskType" label="任務類型"/>
      <el-table-column prop="taskId" label="任務編號"/>
      <el-table-column prop="assignedTime" label="派工時間">
        <template #default="{ row }">
          {{ formatDate(row.assignedTime) }}
        </template>
      </el-table-column>
      <el-table-column label="詳情">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="showDetail(row)">詳情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="assignments.length === 0" style="text-align:center;margin:20px;">
      <el-empty description="目前沒有派工任務"/>
    </div>

    <el-dialog v-model="detailDialogVisible" title="任務詳情" width="600px">
      <div v-if="detailData">
        <template v-if="selectedAssignment.taskType === '維修'">
          <p><b>維修編號：</b>{{ detailData.repairId }}</p>
          <p><b>機台編號：</b>{{ detailData.machineId }}</p>
          <p><b>維修時間：</b>{{ formatDate(detailData.repairTime) }}</p>
          <p><b>維修狀態：</b>{{ repairStatusLabel(detailData.repairStatus) }}</p>
          <p><b>維修描述：</b>{{ detailData.repairDescription }}</p>
        </template>
        <template v-else-if="selectedAssignment.taskType === '保養'">
          <p><b>保養編號：</b>{{ detailData.scheduleId }}</p>
          <p><b>機台編號：</b>{{ detailData.machineId }}</p>
          <p><b>保養時間：</b>{{ formatDate(detailData.scheduleDate) }}</p>
          <p><b>保養狀態：</b>{{ maintenanceStatusLabel(detailData.maintenanceStatus) }}</p>
          <p><b>保養描述：</b>{{ detailData.maintenanceDescription }}</p>
        </template>
      </div>
      <div v-else>
        <el-empty description="無法取得詳情資料"/>
      </div>
      <template #footer>
        <el-button @click="startWork" :disabled="!canStartWork">開始工作</el-button>
        <el-button type="success" @click="completeWork" :disabled="!canCompleteWork">完成工作</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '@/services/api'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/AuthStore'

const assignments = ref([])
const authStore = useAuthStore()
const employeeId = ref(authStore.user?.employeeId || null)

const detailDialogVisible = ref(false)
const selectedAssignment = ref(null)
const detailData = ref(null)

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

const showDetail = async (row) => {
  selectedAssignment.value = row
  detailData.value = null
  let apiUrl = ''
  if (row.taskType === '維修') {
    apiUrl = `/api/repair/${row.taskId}`
  } else if (row.taskType === '保養') {
    apiUrl = `/api/maintenance/${row.taskId}`
  }
  if (apiUrl) {
    try {
      const res = await api.get(apiUrl)
      detailData.value = res.data
    } catch (error) {
      ElMessage.error('載入詳情失敗')
    }
  }
  detailDialogVisible.value = true
}

const canStartWork = computed(() => {
  if (!detailData.value || !selectedAssignment.value) return false
  const type = selectedAssignment.value.taskType
  if (type === '維修') return !['REPAIRING','REPAIRED'].includes(detailData.value.repairStatus)
  if (type === '保養') return !['MAINTAINING','MAINTAINED'].includes(detailData.value.maintenanceStatus)
  return false
})

const canCompleteWork = computed(() => {
  if (!detailData.value || !selectedAssignment.value) return false
  const type = selectedAssignment.value.taskType
  if (type === '維修') return !['REPAIRED'].includes(detailData.value.repairStatus)
  if (type === '保養') return !['MAINTAINED'].includes(detailData.value.maintenanceStatus)
  return false
})

const fetchAssignments = async () => {
  if (!employeeId.value) {
    ElMessage.error('無法取得員工編號，請重新登入')
    return
  }
    console.log("Employee ID:", employeeId.value);
  try {
    const res = await api.get(`/api/task-assignments/employee/${employeeId.value}`)
    assignments.value = res.data || []
  } catch (error) {
    ElMessage.error('載入派工任務失敗')
    assignments.value = []
  }
}

const repairStatusLabel = (status) => {
  switch (status) {
    case 'NORMAL_REPAIR': return '無待修 / 正常'
    case 'WAIT_REPAIR': return '待處理'
    case 'REPAIRING': return '修理中'
    case 'REPAIRED': return '修理完成'
    case 'CANCEL_REPAIR': return '已取消'
    default: return status
  }
}

const maintenanceStatusLabel = (status) => {
  switch (status) {
    case 'NORMAL_MAINTENANCE': return '無保養 / 正常'
    case 'SCHEDULED': return '已排程'
    case 'MAINTAINING': return '保養中'
    case 'MAINTAINED': return '保養完成'
    case 'WAIT_MAINTENANCE': return '待處理'
    default: return status
  }
}

const startWork = async () => {
  try {
    if (!detailData.value || !selectedAssignment.value) {
      ElMessage.error('無法取得任務詳情');
      return;
    }

    const taskType = selectedAssignment.value.taskType
    const { machineId } = detailData.value

    await api.put(`/api/machines/${machineId}/status?newStatus=STOP`)
    ElMessage.success('機台狀態已更新為停機')

    if (taskType === '維修') {
      const repairId = detailData.value.repairId
      await api.put(`/api/repair/${repairId}/status?newStatus=REPAIRING`)
      ElMessage.success('維修狀態已更新為維修中')
    } else if (taskType === '保養') {
      const scheduleId = detailData.value.scheduleId
      await api.put(`/api/maintenance/${scheduleId}/status?newStatus=MAINTAINING`)
      ElMessage.success('保養狀態已更新為保養中')
    }

    detailDialogVisible.value = false
    await fetchAssignments()
  } catch (error) {
    console.error('開始工作失敗:', error)
    ElMessage.error('開始工作失敗，請稍後再試')
  }
}

const completeWork = async () => {
  try {
    if (!detailData.value || !selectedAssignment.value) {
      ElMessage.error('無法取得任務詳情');
      return;
    }

    const taskType = selectedAssignment.value.taskType
    const { machineId } = detailData.value

    await api.put(`/api/machines/${machineId}/status?newStatus=WAIT`)
    ElMessage.success('機台狀態已更新為待機')

    if (taskType === '維修') {
      const repairId = detailData.value.repairId
      await api.put(`/api/repair/${repairId}/status?newStatus=REPAIRED`)
      ElMessage.success('維修狀態已更新為修理完成')
    } else if (taskType === '保養') {
      const scheduleId = detailData.value.scheduleId
      await api.put(`/api/maintenance/${scheduleId}/status?newStatus=MAINTAINED`)
      ElMessage.success('保養狀態已更新為保養完成')
    }

    detailDialogVisible.value = false
    await fetchAssignments()
  } catch (error) {
    console.error('完成工作失敗:', error)
    ElMessage.error('完成工作失敗，請稍後再試')
  }
}

onMounted(fetchAssignments)
</script>

<style scoped>
/* 將整個 el-card 內文字基於原本字體再放大 30px */
.assignment-card, 
.assignment-card * {
  font-size: 15px !important;
}
</style>
