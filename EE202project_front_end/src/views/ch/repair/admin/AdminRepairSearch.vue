<template>
  <el-card class="search-card">
    <template #header>
      <span style="font-size: 18px; font-weight: bold">🔍 搜尋維修記錄</span>
    </template>

    <!-- 搜尋表單 -->
    <el-form :inline="true" :model="searchForm" @submit.prevent="handleSearch">
      <el-form-item label="報修編號">
        <el-input
          v-model="searchForm.searchId"
          placeholder="請輸入報修編號"
          :disabled="searching"
          @keyup.enter="handleSearch"
          clearable
          style="width: 200px"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="searching" @click="handleSearch" icon="Search"> 
          查詢 
        </el-button>
        <el-button @click="handleClear" :disabled="searching" icon="RefreshLeft"> 
          清除 
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <!-- 搜尋結果 -->
  <el-card v-if="showResults" class="result-card" style="margin-top: 20px">
    <template #header>
      <span style="font-size: 18px; font-weight: bold">🔎 查詢結果</span>
    </template>

    <el-table 
      v-if="searchResult" 
      :data="[searchResult]" 
      stripe 
      border 
      style="width: 100%"
      empty-text="查無資料"
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
            :content="row.repairDescription || '無描述'" 
            placement="top"
            :show-after="500"
          >
            <span class="description-text">
              {{ row.repairDescription || '無描述' }}
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
          {{ formatDate(row.repairTime) }}
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

    <!-- 無搜尋結果 -->
    <div v-else class="empty-data">
      <el-empty description="找不到該筆報修資料">
        <el-button type="primary" @click="handleClear">重新搜尋</el-button>
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

    <!-- 派工彈窗 -->
    <RepairAssignModal
      :visible="showAssignModal"
      :repair="selectedRepair"
      @close="handleAssignModalClose"
      @assigned="handleAssigned"
    />
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'
import RepairEditModal from './RepairEditModal.vue'
import RepairAssignModal from './RepairAssignModal.vue'

const emit = defineEmits(['search-complete', 'search-clear'])

const searchForm = reactive({
  searchId: '',
})

const searching = ref(false)
const searchResult = ref(null)
const showResults = ref(false)
const showEditModal = ref(false)
const showAssignModal = ref(false)
const selectedRepair = ref(null)
const statusOptions = ref([])

// 狀態相關函數 - 統一使用繁體中文
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

// 載入狀態選項
const fetchStatusOptions = async () => {
  try {
    console.log('載入維修狀態選項...')
    const res = await api.get('/api/status-codes/repair')
    statusOptions.value = res.data
    console.log('維修狀態選項載入成功:', statusOptions.value)
  } catch (error) {
    console.error('載入維修狀態失敗:', error)
    ElMessage.error('載入維修狀態失敗')
    statusOptions.value = []
  }
}

// 處理搜尋
async function handleSearch() {
  const id = String(searchForm.searchId).trim()
  
  if (!id) {
    ElMessage.warning('請輸入報修編號')
    return
  }
  
  if (isNaN(Number(id))) {
    ElMessage.warning('請輸入有效的報修編號（數字）')
    return
  }
  
  try {
    searching.value = true
    searchResult.value = null
    console.log('搜尋報修編號:', id)
    
    const res = await api.get(`/api/repair/${id}`)
    searchResult.value = res.data
    showResults.value = true
    emit('search-complete')
    ElMessage.success('查詢成功')
    console.log('搜尋結果:', searchResult.value)
  } catch (error) {
    console.error('查詢錯誤:', error)
    if (error.response?.status === 404) {
      ElMessage.info('查無此報修編號的記錄')
      searchResult.value = null
      showResults.value = true
    } else if (error.response?.status === 401) {
      ElMessage.error('驗證已過期，請重新登入')
    } else if (error.response?.status === 403) {
      ElMessage.error('權限不足，無法查詢維修記錄')
    } else {
      ElMessage.error('查詢失敗，請稍後再試')
    }
  } finally {
    searching.value = false
  }
}

// 清除搜尋
function handleClear() {
  searchForm.searchId = ''
  searchResult.value = null
  showResults.value = false
  emit('search-clear')
  ElMessage.info('已清除搜尋條件')
}

// 開啟編輯 Modal
function openEditModal(repair) {
  console.log('開啟編輯 Modal:', repair)
  selectedRepair.value = { ...repair }
  showEditModal.value = true
}

// 開啟派工 Modal
function openAssignModal(repair) {
  console.log('開啟派工 Modal:', repair)
  selectedRepair.value = { ...repair }
  showAssignModal.value = true
}

// 處理 Modal 關閉
const handleModalClose = () => {
  showEditModal.value = false
  selectedRepair.value = null
}

// 處理派工 Modal 關閉
const handleAssignModalClose = () => {
  showAssignModal.value = false
  selectedRepair.value = null
}

// 處理更新完成
const handleUpdated = async () => {
  console.log('維修記錄已更新，重新搜尋')
  showEditModal.value = false
  selectedRepair.value = null
  
  // 如果有搜尋條件，重新搜尋
  if (showResults.value && searchForm.searchId) {
    await handleSearch()
  }
  ElMessage.success('維修記錄更新成功')
}

// 處理派工完成
const handleAssigned = async () => {
  console.log('維修記錄已派工，重新搜尋')
  showAssignModal.value = false
  selectedRepair.value = null
  // 如果有搜尋條件，重新搜尋
  if (showResults.value && searchForm.searchId) {
    await handleSearch()
  }
  ElMessage.success('維修記錄派工成功')
}

// 初始化
onMounted(async () => {
  console.log('AdminRepairSearch 組件初始化')
  await fetchStatusOptions()
})
</script>

<style scoped>
.search-card,
.search-card * {
  font-size: 15px !important;
}

.result-card,
.result-card * {
  font-size: 15px !important;
}
</style>