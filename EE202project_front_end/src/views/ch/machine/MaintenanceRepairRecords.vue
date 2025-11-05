<template>
  <el-card>
    <template #header>
      <span style="font-size: 20px; font-weight: bold">🛠️ 維修/保養紀錄</span>
    </template>
    <el-table :data="records" style="width: 100%">
      <el-table-column prop="type" label="類型" width="100" />
      <el-table-column prop="date" label="派工日期" width="180" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="狀態" width="120">
        <template #default="{ row }">
          <span>
            {{ statusLabelMap[row.type]?.[row.status] || row.status }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="employeeName" label="負責人" width="120" />
    </el-table>
    <el-empty v-if="records.length === 0" description="沒有紀錄" />
    <div style="margin-top: 20px; text-align: right;">
      <el-button @click="$emit('back')" type="primary">返回</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/services/api'

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.slice(0, 10) // 只顯示 yyyy-MM-dd
}

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  // 處理 LocalDateTime 格式，取日期部分
  return dateTimeStr.split('T')[0] || dateTimeStr.slice(0, 10)
}

const props = defineProps({
  machineId: {
    type: Number,
    required: true
  }
})

const records = ref([])
const statusLabelMap = ref({
  維修: {},
  保養: {}
})

const fetchStatusLabels = async () => {
  // 維修狀態
  const repairStatusRes = await api.get('/api/status-codes/repair')
  statusLabelMap.value['維修'] = Object.fromEntries(
    (repairStatusRes.data || []).map(item => [item.statusCode, item.statusLabel])
  )
  // 保養狀態
  const maintenanceStatusRes = await api.get('/api/status-codes/maintenance')
  statusLabelMap.value['保養'] = Object.fromEntries(
    (maintenanceStatusRes.data || []).map(item => [item.statusCode, item.statusLabel])
  )
}

const fetchAssignmentInfo = async (taskType, taskId) => {
  try {
    const res = await api.get('/api/task-assignments/search', {
      params: { taskType, taskId }
    })
    const assignment = res.data?.[0]
    return {
      assignedTime: assignment?.assignedTime || null,
      employeeId: assignment?.employeeId || null
    }
  } catch (error) {
    return { assignedTime: null, employeeId: null }
  }
}

const fetchRecords = async () => {
  await fetchStatusLabels()

  // 取得維修資料
  const repairRes = await api.get(`/api/repair/search/machine/${props.machineId}`)
  const repairList = repairRes.data || []

  // 取得保養資料
  const maintenanceRes = await api.get(`/api/maintenance`)
  const maintenanceList = (maintenanceRes.data || []).filter(m => m.machineId == props.machineId)

  // 處理維修資料，獲取派工資料
  const repairRecords = await Promise.all(
    repairList.map(async (r) => {
      const info = await fetchAssignmentInfo('維修', r.repairId)
      return {
        type: '維修',
        date: info.assignedTime ? formatDateTime(info.assignedTime) : '無',
        description: r.repairDescription,
        status: r.repairStatus,
        employeeName: info.employeeId ? info.employeeId : '無'
      }
    })
  )

  // 處理保養資料，獲取派工資料
  const maintenanceRecords = await Promise.all(
    maintenanceList.map(async (m) => {
      const info = await fetchAssignmentInfo('保養', m.scheduleId)
      return {
        type: '保養',
        date: info.assignedTime ? formatDateTime(info.assignedTime) : '無',
        description: m.maintenanceDescription,
        status: m.maintenanceStatus,
        employeeName: info.employeeId ? info.employeeId : '無'
      }
    })
  )

  const merged = [...repairRecords, ...maintenanceRecords]
  records.value = merged.sort((a, b) => new Date(b.date) - new Date(a.date))
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.el-card,
.el-card * {
  font-size: 25px !important;
}
</style>