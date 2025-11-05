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
          style="width: 250px"
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
        :title="`查詢結果：共 ${searchResults.length} 筆`"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #default>
          <div class="search-conditions">
            <span v-if="searchForm.scheduleId">保養單編號：{{ searchForm.scheduleId }}</span>
          </div>
        </template>
      </el-alert>

      <el-table
        v-if="searchResults.length > 0"
        :data="searchResults"
        stripe
        border
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

      <el-empty v-else description="沒有符合的保養記錄" />
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'

const emit = defineEmits(['search-complete', 'search-clear'])

// 表單數據
const searchForm = ref({
  scheduleId: '',
})

const searching = ref(false)
const searchResults = ref([])
const showResults = ref(false)
const statusOptions = ref([])

// 取得狀態標籤
const getStatusLabel = (code) => {
  const found = statusOptions.value.find(opt =>
    (opt.status_code || opt.statusCode) === code
  )
  return found ? (found.status_label || found.statusLabel || code) : code
}

// 狀態類型映射
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

// 取得保養狀態選項
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
  const id = searchForm.value.scheduleId

  if (!id || isNaN(id)) {
    ElMessage.warning('請輸入有效的保養單編號（數字）')
    return
  }

  try {
    searching.value = true
    const res = await api.get(`/api/maintenance/${id}`)

    // 將單筆結果包成陣列
    searchResults.value = [res.data]
    showResults.value = true
    emit('search-complete')
    ElMessage.success('查詢成功')
  } catch (error) {
    if (error.response?.status === 404) {
      searchResults.value = []
      showResults.value = true
      ElMessage.warning('查無該筆保養資料')
    } else {
      console.error('查詢失敗：', error)
      ElMessage.error('查詢失敗，請稍後再試')
    }
  } finally {
    searching.value = false
  }
}

// 處理清除
const handleClear = () => {
  searchForm.value.scheduleId = ''
  searchResults.value = []
  showResults.value = false
  emit('search-clear')
}
</script>

<style scoped>
.search-card,
.search-card * {
  font-size: 15px !important;
}
</style>
