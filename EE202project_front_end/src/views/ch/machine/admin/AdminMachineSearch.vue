<template>
  <div class="machine-search">
    <!-- 搜尋表單 -->
    <el-card class="search-card">
      <template #header>
        <span style="font-weight: 600; font-size: 18px">🔎 機台搜尋</span>
      </template>

      <el-row :gutter="20" class="form-row" align="middle">
        <el-col :span="6">
          <el-form-item label="狀態">
            <el-select
              v-model="selectedStatus"
              placeholder="請選擇狀態"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="s in statusOptions"
                :key="s.value"
                :label="s.label"
                :value="s.value"
              />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="10">
          <el-form-item label="關鍵字">
            <el-input
              v-model="searchText"
              placeholder="機台名稱、ID、位置..."
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
        </el-col>

        <el-col :span="8" style="text-align: right">
          <el-button type="primary" @click="handleSearch" :loading="searching">
            <el-icon><Search /></el-icon>
            查詢
          </el-button>
          <el-button @click="handleClear" :disabled="searching">
            <el-icon><Refresh /></el-icon>
            清除
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 搜尋結果 -->
    <el-card v-if="showResults && showSearchResultsCard" class="results-card">
      <template #header>
        <div>
          <span style="font-size: 18px; font-weight: bold">
            🔍 查詢結果：共 {{ searchResults.length }} 筆
          </span>
          <div class="search-conditions" style="margin-top: 4px; color: #666; font-size: 14px">
            <span v-if="selectedStatus">狀態：{{ selectedStatus }}</span>
            <span v-if="selectedStatus && searchText"> + </span>
            <span v-if="searchText">關鍵字：{{ searchText }}</span>
          </div>
        </div>
      </template>

      <!-- 有資料時 -->
      <template v-if="searchResults.length > 0">
        <el-table :data="searchResults" stripe border style="width: 100%">
          <el-table-column prop="machineId" label="機台ID" width="100">
            <template #default="{ row }">
              <strong>#{{ row.machineId }}</strong>
            </template>
          </el-table-column>

          <el-table-column prop="machineName" label="機台名稱" />

          <el-table-column prop="serialNumber" label="出廠編號">
            <template #default="{ row }">
              <el-tag type="info">{{ row.serialNumber }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="mstatus" label="運行狀態">
            <template #default="{ row }">
              <span>
                {{ statusLabelMap[row.statusCode?.statusCode] || row.statusCode?.statusCode || '未知狀態' }}
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="machineLocation" label="機台位置">
            <template #default="{ row }">📍 {{ row.machineLocation }}</template>
          </el-table-column>

          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" @click="goToDetail(row.machineId)">詳情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <!-- 無資料時 -->
      <el-empty
        v-else
        description="📂 沒有符合條件的機台資料"
        style="padding: 40px 0"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import api from '@/services/api'

// 定義 emit
const emit = defineEmits(['search-complete', 'search-clear', 'show-detail'])

// 狀態與圖示對應
const statusIconMap = {
  運行中: '🟢',
  維護中: '🟡',
  停機: '🔴',
}

const statusClassMap = {
  運行中: 'running',
  維護中: 'maintenance',
  停機: 'stopped',
}

const statusOptions = ref([])
const selectedStatus = ref('')
const searchText = ref('')
const searching = ref(false)
const searchResults = ref([])
const showResults = ref(false)
const showSearchResultsCard = ref(true) // 新增：控制搜尋結果卡片顯示

// 狀態標籤對應
const statusLabelMap = ref({})

// 取得狀態選項
const fetchStatusOptions = async () => {
  const res = await api.get('/api/status-codes/machine')
  statusOptions.value = res.data.map(item => ({
    label: item.statusLabel,
    value: item.statusCode
  }))
  // 新增這行：建立狀態碼對應中文名稱
  statusLabelMap.value = Object.fromEntries(res.data.map(item => [item.statusCode, item.statusLabel]))
}

onMounted(() => {
  fetchStatusOptions()
})

// 搜尋
const handleSearch = async () => {
  if (!selectedStatus.value && !searchText.value.trim()) {
    ElMessage.warning('請至少選擇狀態或輸入關鍵字！')
    return
  }

  searching.value = true
  try {
    const params = {}
    if (selectedStatus.value) params.statusFilter = selectedStatus.value
    if (searchText.value.trim()) params.search = searchText.value.trim()

    const res = await api.get('/api/machines', { params })

    searchResults.value = res.data || []
    showResults.value = true
    showSearchResultsCard.value = true
    emit('search-complete')
  } catch (err) {
    console.error('搜尋失敗:', err)
    ElMessage.error('查詢失敗，請稍後再試')
    searchResults.value = []
  } finally {
    searching.value = false
  }
}

// 清除
const handleClear = () => {
  selectedStatus.value = ''
  searchText.value = ''
  searchResults.value = []
  showResults.value = false
  showSearchResultsCard.value = true
  emit('search-clear')
}

const goToDetail = (id) => {
  emit('show-detail', id)
}

// 新增：外部調用的方法
const hideSearchResults = () => {
  showSearchResultsCard.value = false
}

const showSearchResults = () => {
  showSearchResultsCard.value = true
}

const hasSearchConditions = () => {
  return !!(selectedStatus.value || searchText.value.trim())
}

const clearSearch = () => {
  handleClear()
}

// 暴露方法給父組件使用
defineExpose({
  hideSearchResults,
  showSearchResults,
  hasSearchConditions,
  clearSearch
})
</script>

<style scoped>
/* 將整個 el-card 內文字基於原本字體再放大 25px */
.machine-search,
.machine-search * {
  font-size: 15px !important;
}

.status.running {
  color: #67c23a;
  font-weight: 600;
}
.status.maintenance {
  color: #e6a23c;
  font-weight: 600;
}
.status.stopped {
  color: #f56c6c;
  font-weight: 600;
}
</style>