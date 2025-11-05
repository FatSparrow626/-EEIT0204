<template>
  <el-card class="workorder-list-card">
    <!-- 標題與篩選 -->
    <template #header>
      <div class="card-header">
        <span style="font-size: 20px; font-weight: bold">📋 工單列表</span>
        <div class="filter-controls">
          <el-select v-model="statusFilter" placeholder="篩選狀態" clearable @change="fetchWorkOrders">
            <el-option label="全部" value="" />
            <el-option label="未執行" value="PENDING" />
            <el-option label="進行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
          <el-button type="primary" @click="fetchWorkOrders" :icon="Refresh">刷新</el-button>
        </div>
      </div>
    </template>

    <!-- 工單表格 -->
    <el-table 
      v-loading="loading" 
      :data="workOrderList" 
      stripe 
      border 
      style="width: 100%"
      @row-click="viewDetail"
    >
      <el-table-column prop="woId" label="工單ID" width="100" />
      <el-table-column prop="woNumber" label="工單編號" min-width="150" />
      <el-table-column prop="materialName" label="產品名稱" min-width="120" />
      <el-table-column prop="requiredQuantity" label="要求數量" width="100" />
      <el-table-column prop="successfulQuantity" label="已生產數量" width="120">
        <template #default="{ row }">
          <span>{{ row.successfulQuantity || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="狀態" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="建立時間" width="150">
        <template #default="{ row }">
          <span>{{ formatDate(row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click.stop="viewDetail(row)">查看詳情</el-button>
          <el-button
            v-if="row.status === 'PENDING'"
            type="success"
            size="small"
            @click.stop="startProduction(row)"
          >開始生產</el-button>
          <el-button
            v-if="row.status === 'IN_PROGRESS'"
            type="warning"
            size="small"
            @click.stop="continueProduction(row)"
          >繼續生產</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分頁 -->
    <div class="pagination-container">
      <el-pagination
        v-if="pagination.total > 0"
        :current-page="pagination.currentPage"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 無資料或錯誤提示 -->
    <el-empty v-if="!loading && workOrderList.length === 0" description="📂 沒有工單資料" />
    <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon style="margin-top: 16px;" />
  </el-card>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import api from '@/services/api'

const workOrderList = ref([])
const loading = ref(false)
const errorMsg = ref('')
const statusFilter = ref('')
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
})

const emit = defineEmits(['view-detail', 'start-production', 'continue-production'])

// 開始生產
const startProduction = (workOrder) => {
  if (!workOrder || workOrder.woId === undefined || workOrder.woId === null) {
    ElMessage.error('工單ID無效，無法開始生產。');
    return;
  }
  console.log('Emitting start-production with workOrder:', workOrder);
  emit('start-production', workOrder);
};

// 繼續生產
const continueProduction = (workOrder) => {
  if (!workOrder || workOrder.woId === undefined || workOrder.woId === null) {
    ElMessage.error('工單ID無效，無法繼續生產。');
    return;
  }
  console.log('Emitting continue-production with workOrder:', workOrder);
  emit('continue-production', workOrder);
};

// 取得工單列表
const fetchWorkOrders = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      status: statusFilter.value || null,
    }
    const res = await api.get('/api/workorder', { params })
    workOrderList.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } catch (err) {
    console.error('獲取工單失敗:', err)
    workOrderList.value = []
    errorMsg.value = err.response?.data?.message || '獲取工單失敗'
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  pagination.currentPage = page
  fetchWorkOrders()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchWorkOrders()
}

// 查看詳情
const viewDetail = (workOrder) => {
  if (!workOrder || workOrder.woId === undefined || workOrder.woId === null) {
    ElMessage.error('工單ID無效，無法查看詳情。');
    return;
  }
  emit('view-detail', workOrder);
};

// 標籤顏色
const statusTagType = (status) => ({
  COMPLETED: 'success',
  IN_PROGRESS: 'warning',
  CANCELLED: 'danger',
  PENDING: 'info'
}[status] || 'info')

// 標籤文字
const getStatusText = (status) => ({
  PENDING: '未執行',
  IN_PROGRESS: '進行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}[status] || status)

// 日期格式化
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  try {
    return new Date(dateStr).toLocaleString('zh-TW')
  } catch {
    return dateStr
  }
}

onMounted(fetchWorkOrders)

defineExpose({ fetchWorkOrders })
</script>

<style scoped>
.workorder-list-card {
  width: 100%;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.filter-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table .cell) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.el-table__row) {
  cursor: pointer;
  transition: background-color 0.2s;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

:deep(.el-button + .el-button) {
  margin-left: 8px;
}
</style>