<template>
  <el-card class="search-card">
    <template #header>
      <span>🔍 搜尋維修記錄</span>
    </template>

    <el-form :inline="true" :model="searchForm" @submit.prevent="handleSearch">
      <el-form-item label="報修編號">
        <el-input
          v-model="searchForm.searchId"
          placeholder="請輸入報修編號"
          :disabled="searching"
          @keyup.enter="handleSearch"
          clearable
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="searching" @click="handleSearch"> 🔍 查詢 </el-button>
        <el-button @click="handleClear" :disabled="searching"> 🧹 清除 </el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <el-card v-if="showResult" class="result-card" style="margin-top: 20px">
    <template #header>
      <span>🔎 查詢結果</span>
    </template>

    <el-table :data="searchResultList" stripe border style="width: 100%">
      <el-table-column prop="repairId" label="報修編號" width="120">
        <template #default="{ row }">
          <strong>#{{ row.repairId }}</strong>
        </template>
      </el-table-column>
      <el-table-column prop="machineId" label="機台編號" />
      <el-table-column prop="employeeId" label="報修人員編號" />
      <el-table-column prop="repairDescription" label="維修描述" min-width="200">
        <template #default="{ row }">
          <el-tooltip :content="row.repairDescription" placement="top">
            <span class="description-text">{{ row.repairDescription }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="repairStatus" label="維修狀態" width="120">
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
    </el-table>

    <div v-if="searchResultList.length === 0" class="empty-data">
      <el-empty description="找不到該筆報修資料" />
    </div>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'

const emit = defineEmits(['search-complete', 'search-clear'])

const searchForm = reactive({
  searchId: '',
})

const searching = ref(false)
const searchResultList = ref([])
const showResult = ref(false)
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
    const res = await api.get('/api/status-codes/repair')
    statusOptions.value = res.data
  } catch (error) {
    ElMessage.error('載入維修狀態失敗')
    statusOptions.value = []
  }
}

async function handleSearch() {
  const id = String(searchForm.searchId).trim()
  if (!id) {
    ElMessage.warning('請輸入報修編號')
    return
  }

  try {
    searching.value = true
    const res = await api.get(`/api/repair/${id}`)

    searchResultList.value = res.data ? [res.data] : []
    showResult.value = true

    if (searchResultList.value.length > 0) {
      emit('search-complete')
      ElMessage.success('查詢成功')
    } else {
      ElMessage.info('找不到該筆報修資料')
    }
  } catch (error) {
    console.error('查詢錯誤:', error)

    if (error.response?.status === 404) {
      ElMessage.info('找不到該筆報修資料')
      searchResultList.value = []
      showResult.value = true
    } else if (error.response?.status === 401) {
      ElMessage.error('驗證已過期，請重新登入。')
    } else if (error.response?.status === 403) {
      ElMessage.error('您的權限不足。')
    } else {
      ElMessage.error('查詢失敗，請稍後再試')
    }
  } finally {
    searching.value = false
  }
}

function handleClear() {
  searchForm.searchId = ''
  searchResultList.value = []
  showResult.value = false
  emit('search-clear')
}

onMounted(fetchStatusOptions)
</script>

<style scoped>
.search-card,
.search-card * {
  font-size: 25px !important;
}

.result-card,
.result-card * {
  font-size: 15px !important;
}

.description-text {
  display: block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
