<template>
  <el-card class="search-card">
    <template #header>
      <span style="font-size: 18px; font-weight: bold">🔍 保養記錄查詢</span>
    </template>

    <!-- 搜尋表單 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="保養單編號：">
        <el-input
          v-model="searchForm.scheduleId"
          placeholder="請輸入保養單編號"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="Search" :loading="searching" @click="handleSearch">
          查詢
        </el-button>
        <el-button icon="Refresh" @click="handleClear"> 清除 </el-button>
      </el-form-item>
    </el-form>

    <!-- 查詢結果 -->
    <div v-if="showResults" class="search-results">
      <el-alert
        :title="
          searchResult ? `查詢結果：找到保養單 #${searchForm.scheduleId}` : '查詢結果：查無資料'
        "
        :type="searchResult ? 'success' : 'warning'"
        :closable="false"
        style="margin-bottom: 20px"
      />

      <el-table v-if="searchResult" :data="[searchResult]" stripe border style="width: 100%">
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

      <el-empty v-else :description="`沒有找到編號為 #${searchForm.scheduleId} 的保養記錄`" />
    </div>

    <!-- 編輯 Modal -->
    <MaintenanceEditModal
      v-if="showEditModal"
      :maintenance="selectedMaintenance"
      :status-options="statusOptions"
      @close="showEditModal = false"
      @updated="handleUpdated"
    />

    <!-- 刪除 Modal -->
    <MaintenanceDeleteModal
      v-if="showDeleteModal"
      :maintenance="selectedMaintenance"
      @close="showDeleteModal = false"
      @deleted="handleDeleted"
    />
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'
import MaintenanceEditModal from './MaintenanceEditModal.vue'
import MaintenanceDeleteModal from './MaintenanceDeleteModal.vue'

const emit = defineEmits(['search-complete', 'search-clear'])

// 表單數據
const searchForm = ref({
  scheduleId: '',
})

const searching = ref(false)
const searchResult = ref(null)
const showResults = ref(false)
const selectedMaintenance = ref(null)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const statusOptions = ref([])

// 獲取狀態標籤
const getStatusLabel = (code) => {
  const found = statusOptions.value.find(opt =>
    (opt.status_code || opt.statusCode) === code
  )
  return found ? (found.status_label || found.statusLabel || code) : code
}

// 獲取狀態類型
const getStatusType = (code) => {
  const found = statusOptions.value.find(opt =>
    (opt.status_code || opt.statusCode) === code
  )
  return found ? (found.status_type || found.type || 'info') : 'info'
}

// 格式化日期
const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString()
}

// 獲取狀態選項
const fetchStatusOptions = async () => {
  try {
    const res = await api.get('/api/status-codes/maintenance')
    statusOptions.value = res.data
  } catch {
    statusOptions.value = []
  }
}

onMounted(fetchStatusOptions)

// 處理搜尋
const handleSearch = async () => {
  if (!searchForm.value.scheduleId) {
    ElMessage.warning('請輸入保養單編號！')
    return
  }

  const id = parseInt(searchForm.value.scheduleId)
  if (isNaN(id) || id <= 0) {
    ElMessage.warning('請輸入有效的保養單編號！')
    return
  }

  try {
    searching.value = true
    const res = await api.get(`/api/maintenance/${id}`)
    searchResult.value = res.data
    showResults.value = true
    emit('search-complete')
    ElMessage.success('查詢成功')
  } catch (error) {
    if (error.response?.status === 404) {
      searchResult.value = null
      showResults.value = true
      ElMessage.warning('查無該筆保養資料')
    } else {
      console.error(error)
      ElMessage.error('查詢失敗，請稍後再試')
    }
  } finally {
    searching.value = false
  }
}

// 處理清除
const handleClear = () => {
  searchForm.value.scheduleId = ''
  searchResult.value = null
  showResults.value = false
  emit('search-clear')
}

// 打開編輯 Modal
const openEditModal = (maintenance) => {
  selectedMaintenance.value = { ...maintenance }
  showEditModal.value = true
}

// 打開刪除 Modal
const openDeleteModal = (maintenance) => {
  selectedMaintenance.value = { ...maintenance }
  showDeleteModal.value = true
}

// 處理更新完成
const handleUpdated = async () => {
  showEditModal.value = false
  if (showResults.value) {
    await handleSearch()
  }
  ElMessage.success('更新成功')
}

// 處理刪除完成
const handleDeleted = async () => {
  showDeleteModal.value = false
  if (showResults.value) {
    handleClear()
  }
  ElMessage.success('刪除成功')
}

// 提交保養單
const handleSubmit = async () => {
  // 1. 送出保養單
  const maintenanceRes = await api.post('/api/maintenance', {
    // ...你的保養單表單資料...
  })
  const maintenanceId = maintenanceRes.data.id || maintenanceRes.data.scheduleId

  // 2. 派工
  await api.post('/api/task-assignments', {
    taskType: '保養',
    taskId: maintenanceId,
    employeeId: selectedEmployeeId // 你表單選的員工 id
  })

  ElMessage.success('保養單與派工已完成')
}
</script>

<style scoped>
.search-card,
.search-card * {
  font-size: 15px !important;
}
</style>
