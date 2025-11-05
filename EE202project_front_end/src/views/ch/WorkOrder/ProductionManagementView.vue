<template>
  <div class="production-management-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span style="font-size: 20px; font-weight: bold">🏭 生產管理中心</span>
          <el-button type="primary" @click="refreshAllData" :icon="Refresh">刷新數據</el-button>
        </div>
      </template>

      <!-- 統計面板 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <el-statistic
              title="進行中工單"
              :value="inProgressCount"
              suffix="個"
              :value-style="{ color: '#E6A23C' }"
            />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <el-statistic
              title="待執行工單"
              :value="pendingCount"
              suffix="個"
              :value-style="{ color: '#409EFF' }"
            />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <el-statistic
              title="運行中機台"
              :value="runningMachineCount"
              suffix="台"
              :value-style="{ color: '#67C23A' }"
            />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <el-statistic
              title="今日完成"
              :value="todayCompletedCount"
              suffix="單"
              :value-style="{ color: '#67C23A' }"
            />
          </el-card>
        </el-col>
      </el-row>

      <!-- 快速操作面板 -->
      <el-card class="quick-actions-card">
        <template #header>
          <span style="font-weight: bold">⚡ 快速操作</span>
        </template>
        <div class="quick-actions">
          <el-button type="primary" @click="showAllPendingOrders">查看待執行工單</el-button>
          <el-button type="warning" @click="showInProgressOrders">查看進行中工單</el-button>
         
          <el-button type="info" @click="showProductionAnalysis">生產分析報告</el-button>
        </div>
      </el-card>

      <!-- 主要內容區域 -->
      <div class="main-content">
        <!-- 待執行工單 -->
        <el-card v-if="currentView === 'pending'" class="content-card">
          <template #header>
            <div class="card-header">
              <span style="font-weight: bold">📋 待執行工單</span>
              <el-button @click="currentView = 'overview'">返回總覽</el-button>
            </div>
          </template>
          <el-table :data="pendingOrders" style="width: 100%">
            <el-table-column prop="woNumber" label="工單編號" min-width="150" />
            <el-table-column prop="materialName" label="產品名稱" min-width="120" />
            <el-table-column prop="requiredQuantity" label="要求數量" width="100" />
            <el-table-column prop="createdAt" label="建立時間" width="150">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <!-- 操作按鈕整欄移除 -->
          </el-table>
          <el-empty v-if="pendingOrders.length === 0" description="沒有待執行工單" />
        </el-card>

        <!-- 進行中工單 -->
        <el-card v-if="currentView === 'inProgress'" class="content-card">
          <template #header>
            <div class="card-header">
              <span style="font-weight: bold">⚙️ 進行中工單</span>
              <el-button @click="currentView = 'overview'">返回總覽</el-button>
            </div>
          </template>
          <el-table :data="inProgressOrders" style="width: 100%">
            <el-table-column prop="woNumber" label="工單編號" min-width="150" />
            <el-table-column prop="materialName" label="產品名稱" min-width="120" />
            <el-table-column prop="requiredQuantity" label="要求數量" width="100" />
            <el-table-column prop="producedQuantity" label="已生產" width="100">
              <template #default="{ row }">
                <el-tag type="warning">{{ row.producedQuantity || 0 }}</el-tag>
              </template>
            </el-table-column>
            <!-- 操作按鈕整欄移除 -->
          </el-table>
          <el-empty v-if="inProgressOrders.length === 0" description="沒有進行中工單" />
        </el-card>

        <!-- 生產分析報告 -->
        <el-card v-if="currentView === 'analysis'" class="content-card">
          <template #header>
            <div class="card-header">
              <span style="font-weight: bold">📊 生產分析報告</span>
              <el-button @click="currentView = 'overview'">返回總覽</el-button>
            </div>
          </template>
          <el-row :gutter="20">
        
            <el-col :span="12">
              <el-card class="recent-orders-card">
                <template #header>
                  <span>⏱️ 工單完成統計</span>
                </template>
                <div class="analysis-stats">
                  <el-statistic title="已完成工單" :value="completedOrdersCount" suffix="個" />
                  <el-statistic title="平均完成時間" :value="averageCompletionTime" suffix="小時" :precision="1" />
                  <el-statistic title="準時完成率" :value="onTimeCompletionRate" suffix="%" :precision="1" />
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 最近完成的工單 -->
          <el-card class="recent-orders-card">
            <template #header>
              <span style="font-weight: bold">🎯 最近完成的工單</span>
            </template>
            <el-table :data="recentCompletedOrders" style="width: 100%" size="small">
              <el-table-column prop="woNumber" label="工單編號" min-width="120" />
              <el-table-column prop="materialName" label="產品名稱" min-width="100" />
              <el-table-column prop="requiredQuantity" label="要求數量" width="90" />
              <el-table-column prop="producedQuantity" label="實際產量" width="90" />
              <el-table-column label="完成率" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.producedQuantity >= row.requiredQuantity ? 'success' : 'warning'" size="small">
                    {{ Math.round((row.producedQuantity / row.requiredQuantity) * 100) }}%
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="updatedAt" label="完成時間" width="150">
                <template #default="{ row }">
                  {{ formatDate(row.updatedAt) }}
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="recentCompletedOrders.length === 0" description="暫無完成的工單" />
          </el-card>
        </el-card>

        <!-- 總覽 -->
        <div v-if="currentView === 'overview'" class="overview-grid">
          <!-- 待處理工單概覽 -->
          <el-card class="overview-card">
            <template #header>
              <div class="overview-header">
                <span>📋 待處理工單</span>
                <el-button link type="primary" @click="showAllPendingOrders">查看全部</el-button>
              </div>
            </template>
            <div class="overview-content">
              <div v-for="order in pendingOrders.slice(0, 3)" :key="order.woId" class="overview-item">
                <div class="item-info">
                  <strong>{{ order.woNumber }}</strong>
                  <span>{{ order.materialName }}</span>
                </div>
                <!-- 開始按鈕移除 -->
              </div>
              <div v-if="pendingOrders.length > 3" class="more-items">
                還有 {{ pendingOrders.length - 3 }} 個工單...
              </div>
              <el-empty v-if="pendingOrders.length === 0" description="沒有待處理工單" :image-size="60" />
            </div>
          </el-card>

          <!-- 進行中工單概覽 -->
          <el-card class="overview-card">
            <template #header>
              <div class="overview-header">
                <span>⚙️ 進行中工單</span>
                <el-button link type="primary" @click="showInProgressOrders">查看全部</el-button>
              </div>
            </template>
            <div class="overview-content">
              <div v-for="order in inProgressOrders.slice(0, 3)" :key="order.woId" class="overview-item">
                <div class="item-info">
                  <strong>{{ order.woNumber }}</strong>
                  <span>{{ order.materialName }}</span>
                  <el-progress 
                    :percentage="getProgressPercentage(order)" 
                    :stroke-width="6"
                    size="small"
                  />
                </div>
                <!-- 繼續按鈕移除 -->
              </div>
              <div v-if="inProgressOrders.length > 3" class="more-items">
                還有 {{ inProgressOrders.length - 3 }} 個工單...
              </div>
              <el-empty v-if="inProgressOrders.length === 0" description="沒有進行中工單" :image-size="60" />
            </div>
          </el-card>
        </div>
      </div>
    </el-card>

    <!-- 工單詳情與生產流程彈窗 -->
    <WorkOrderDetailModalFinish
      v-if="showWorkOrderDetailModal"
      :work-order="selectedWorkOrder"
      @back="closeWorkOrderDetailModal"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import api from '@/services/api'
import WorkOrderDetailModalFinish from './WorkOrderDetailModalFinish.vue'

const emit = defineEmits(['back'])

// 響應式狀態
const currentView = ref('overview') // 'overview', 'pending', 'inProgress', 'machines', 'analysis'
const workOrders = ref([])
const machines = ref([])
const productionReports = ref([])
const showWorkOrderDetailModal = ref(false)
const selectedWorkOrder = ref(null)

// 計算屬性
const pendingOrders = computed(() => 
  workOrders.value.filter(order => order.status === 'PENDING')
)

const inProgressOrders = computed(() => 
  workOrders.value.filter(order => order.status === 'IN_PROGRESS')
)

const completedOrders = computed(() => 
  workOrders.value.filter(order => order.status === 'COMPLETED')
)

const pendingCount = computed(() => pendingOrders.value.length)
const inProgressCount = computed(() => inProgressOrders.value.length)
const completedOrdersCount = computed(() => completedOrders.value.length)

const runningMachines = computed(() => 
  machines.value.filter(machine => machine.statusCode.statusCode === '運轉中')
)

const maintenanceMachines = computed(() => 
  machines.value.filter(machine => machine.statusCode.statusCode === '維修中')
)

const stoppedMachines = computed(() => 
  machines.value.filter(machine => machine.statusCode.statusCode === '停機')
)

const runningMachineCount = computed(() => runningMachines.value.length)

const todayCompletedCount = computed(() => {
  const today = new Date().toDateString()
  return completedOrders.value.filter(order => {
    if (!order.updatedAt) return false
    return new Date(order.updatedAt).toDateString() === today
  }).length
})

const recentCompletedOrders = computed(() => 
  completedOrders.value.slice(0, 5)
)

// 生產分析統計
const totalProduced = computed(() => 
  productionReports.value.reduce((sum, report) => sum + (report.quantityProduced || 0), 0)
)

const totalFailed = computed(() => 
  productionReports.value.reduce((sum, report) => sum + (report.quantityFailed || 0), 0)
)

const averageSuccessRate = computed(() => {
  const total = totalProduced.value + totalFailed.value
  return total > 0 ? (totalProduced.value / total) * 100 : 0
})

const averageCompletionTime = computed(() => {
  // 模擬計算平均完成時間
  return completedOrders.value.length > 0 ? 24.5 : 0
})

const onTimeCompletionRate = computed(() => {
  // 模擬準時完成率
  return completedOrders.value.length > 0 ? 85.2 : 0
})

// 方法
const refreshAllData = async () => {
  await Promise.all([
    fetchWorkOrders(),
    fetchMachines(),
    fetchProductionReports()
  ])
  ElMessage.success('數據刷新完成')
}

const fetchWorkOrders = async () => {
  try {
    let allWorkOrders = [];
    let page = 0;
    let totalPages = 1;
    
    do {
      const response = await api.get('/api/workorder', {
        params: { page: page, size: 100 } // Fetch 100 items per page
      });
      
      if (response.data && Array.isArray(response.data.content)) {
        allWorkOrders = allWorkOrders.concat(response.data.content);
        totalPages = response.data.totalPages;
        page++;
      } else {
        break;
      }
    } while (page < totalPages);

    workOrders.value = allWorkOrders;

  } catch (error) {
    console.error('Error fetching work orders:', error);
    ElMessage.error('獲取工單列表失敗');
    workOrders.value = [];
  }
}

const fetchMachines = async () => {
  try {
    const response = await api.get('/api/machines')
    machines.value = Array.isArray(response.data) ? response.data : []
  } catch (error) {
    console.error('Error fetching machines:', error)
    ElMessage.error('獲取機台列表失敗')
    machines.value = []
  }
}

const fetchProductionReports = async () => {
  try {
    const response = await api.get('/api/workorderfinish')
    productionReports.value = Array.isArray(response.data) ? response.data : []
  } catch (error) {
    console.error('Error fetching production reports:', error)
    productionReports.value = []
  }
}

// 視圖切換方法
const showAllPendingOrders = () => {
  currentView.value = 'pending'
}

const showInProgressOrders = () => {
  currentView.value = 'inProgress'
}

const showMachineStatus = () => {
  currentView.value = 'machines'
}

const showProductionAnalysis = () => {
  currentView.value = 'analysis'
}

// 生產相關方法
const startOrderProduction = (workOrder) => {
  console.log('開始生產工單:', workOrder)
  selectedWorkOrder.value = { ...workOrder }
  showProductionDialog.value = true
}

const continueOrderProduction = (workOrder) => {
  console.log('繼續生產工單:', workOrder)
  selectedWorkOrder.value = { ...workOrder }
  showProductionDialog.value = true
}

const openWorkOrderDetailModal = (workOrder) => {
  selectedWorkOrder.value = workOrder
  showWorkOrderDetailModal.value = true
}

const closeWorkOrderDetailModal = () => {
  showWorkOrderDetailModal.value = false
  selectedWorkOrder.value = null
  refreshAllData()
}

// 輔助方法
const getProgressPercentage = (row) => {
  if (!row.requiredQuantity || row.requiredQuantity === 0) return 0
  const produced = row.producedQuantity || 0
  return Math.round((produced / row.requiredQuantity) * 100)
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  try {
    return new Date(dateStr).toLocaleString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return dateStr
  }
}

// 組件掛載
onMounted(refreshAllData)
</script>

<style scoped>
.production-management-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.main-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.stat-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.quick-actions-card {
  margin-bottom: 24px;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.main-content {
  margin-top: 20px;
}

.content-card {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.overview-card {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.overview-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  font-weight: 600;
}

.overview-content {
  max-height: 300px;
  overflow-y: auto;
}

.overview-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.overview-item:last-child {
  border-bottom: none;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-info strong {
  color: #303133;
  font-size: 14px;
}

.item-info span {
  color: #606266;
  font-size: 12px;
}

.more-items {
  text-align: center;
  color: #909399;
  font-size: 12px;
  padding: 8px 0;
  border-top: 1px solid #f0f0f0;
}

.machine-status-card {
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
}

.machine-list {
  height: 320px;
  overflow-y: auto;
}

.machine-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.machine-item:last-child {
  border-bottom: none;
}

.machine-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.machine-location {
  color: #909399;
  font-size: 12px;
}

.analysis-card {
  border-radius: 8px;
  margin-bottom: 20px;
}

.analysis-stats {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 20px 0;
}

.recent-orders-card {
  margin-top: 20px;
  border-radius: 8px;
}

/* 狀態卡片顏色 */
.machine-status-card.running {
  border-left: 4px solid #67c23a;
}

.machine-status-card.maintenance {
  border-left: 4px solid #e6a23c;
}

.machine-status-card.stopped {
  border-left: 4px solid #909399;
}

/* 響應式設計 */
@media (max-width: 1200px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-actions {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .production-management-container {
    padding: 12px;
  }
  
  .stats-row :deep(.el-col) {
    margin-bottom: 12px;
  }
  
  .quick-actions {
    flex-direction: column;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .overview-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .analysis-stats {
    flex-direction: column;
    gap: 16px;
  }
}

/* 表格樣式優化 */
:deep(.el-table) {
  border-radius: 6px;
}

:deep(.el-table th) {
  background-color: #fafafa;
  font-weight: 600;
  color: #606266;
}

:deep(.el-table tr:hover > td) {
  background-color: #f0f9ff;
}

:deep(.el-table__row) {
  cursor: pointer;
  transition: background-color 0.2s;
}

/* 滾動條樣式 */
.overview-content::-webkit-scrollbar,
.machine-list::-webkit-scrollbar {
  width: 6px;
}

.overview-content::-webkit-scrollbar-track,
.machine-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.overview-content::-webkit-scrollbar-thumb,
.machine-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.overview-content::-webkit-scrollbar-thumb:hover,
.machine-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 動畫效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.content-card,
.overview-card {
  animation: fadeIn 0.3s ease-out;
}

/* 按鈕樣式增強 */
:deep(.el-button) {
  border-radius: 6px;
  transition: all 0.2s;
}

:deep(.el-button:hover) {
  transform: translateY(-1px);
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #6cb4ff 100%);
  border: none;
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
}

:deep(.el-button--warning) {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
  border: none;
}

.production-management-container,
.production-management-container * {
  font-size: 25px !important;
}
</style>