<template>
  <div class="work-order-container">
    <!-- 導航按鈕 -->
    <div class="nav-buttons" v-if="currentView !== 'detail'">
      <el-button 
        :type="currentView === 'list' ? 'primary' : 'default'"
        @click="showList"
        :icon="List"
      >
        工單列表
      </el-button>
      <el-button 
        :type="currentView === 'production' ? 'primary' : 'default'"
        @click="showProductionView"
        :icon="Setting"
      >
        生產管理
      </el-button>
    </div>

    <!-- 返回按鈕 (僅在詳情頁顯示) -->
    <div class="back-button" v-if="currentView === 'detail'">
      <el-button @click="backToList" :icon="ArrowLeft">返回列表</el-button>
    </div>

    <!-- 動態顯示不同頁面 -->
    <div class="view-content">
      <!-- 工單列表 -->
      <WorkOrderFinishList 
        v-if="currentView === 'list'"
        @view-detail="viewDetail"
        @start-production="startProduction"
        @continue-production="continueProduction"
        ref="listComponentRef"
      />
      
      <!-- 工單詳情 -->
      <WorkOrderDetailModalFinish 
        v-if="currentView === 'detail' && selectedWorkOrder"
        :work-order="selectedWorkOrder"
        @back="backToList"
        @production-started="onProductionStarted"
      />

      <!-- 生產管理視圖 -->
      <ProductionManagementView
        v-if="currentView === 'production'"
        @back="showList"
        ref="productionViewRef"
      />
    </div>

    <!-- 生產對話框 (從列表直接開始生產) -->
    <ProductionDialog
      v-model="showProductionDialog"
      :work-order="workOrderForProduction"
      @production-complete="onProductionComplete"
      @production-updated="onProductionUpdated"
    />
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, List, Setting } from '@element-plus/icons-vue'
import WorkOrderFinishList from './WorkOrderFinishList.vue'
import WorkOrderDetailModalFinish from './WorkOrderDetailModalFinish.vue'
import ProductionManagementView from './ProductionManagementView.vue'
import ProductionDialog from './ProductionDialog.vue'

const currentView = ref('list') // 'list', 'detail', 'production'
const selectedWorkOrder = ref(null)
const workOrderForProduction = ref(null)
const showProductionDialog = ref(false)
const listComponentRef = ref(null)
const productionViewRef = ref(null)

const showList = () => {
  currentView.value = 'list'
  selectedWorkOrder.value = null
  workOrderForProduction.value = null
}

const showProductionView = () => {
  currentView.value = 'production'
  selectedWorkOrder.value = null
}

const viewDetail = (workOrder) => {
  console.log('查看工單詳情:', workOrder)
  selectedWorkOrder.value = { ...workOrder }
  currentView.value = 'detail'
}

const startProduction = (workOrder) => {
  console.log('開始生產工單:', workOrder)
  if (!workOrder || !workOrder.woId) {
    ElMessage.error('無法開始生產：工單資訊不完整。')
    return
  }
  workOrderForProduction.value = { ...workOrder }
  showProductionDialog.value = true
}

const continueProduction = (workOrder) => {
  console.log('繼續生產工單:', workOrder)
  if (!workOrder || !workOrder.woId) {
    ElMessage.error('無法繼續生產：工單資訊不完整。')
    return
  }
  workOrderForProduction.value = { ...workOrder }
  showProductionDialog.value = true
}

const backToList = async () => {
  currentView.value = 'list'
  selectedWorkOrder.value = null
  
  // 等待 DOM 更新後重新載入列表資料
  await nextTick()
  if (listComponentRef.value && listComponentRef.value.fetchWorkOrders) {
    listComponentRef.value.fetchWorkOrders()
  }
}

const onProductionStarted = (workOrder) => {
  ElMessage.success(`工單 ${workOrder.woNumber} 已開始生產`)
  // 更新工單狀態
  if (selectedWorkOrder.value && selectedWorkOrder.value.woId === workOrder.woId) {
    selectedWorkOrder.value.status = 'IN_PROGRESS'
  }
}

const onProductionComplete = async (workOrder) => {
  ElMessage.success(`工單 ${workOrder.woNumber} 生產完成`)
  showProductionDialog.value = false
  workOrderForProduction.value = null
  
  // 刷新列表數據
  await nextTick()
  if (listComponentRef.value && listComponentRef.value.fetchWorkOrders) {
    listComponentRef.value.fetchWorkOrders()
  }
  
  // 如果在詳情頁面，也更新詳情
  if (selectedWorkOrder.value && selectedWorkOrder.value.woId === workOrder.woId) {
    selectedWorkOrder.value = { ...workOrder }
  }
}

const onProductionUpdated = (workOrder) => {
  console.log('工單生產數據更新:', workOrder)
  
  // 更新當前選中的工單
  if (selectedWorkOrder.value && selectedWorkOrder.value.woId === workOrder.woId) {
    selectedWorkOrder.value = { ...workOrder }
  }
  
  // 更新生產對話框中的工單
  if (workOrderForProduction.value && workOrderForProduction.value.woId === workOrder.woId) {
    workOrderForProduction.value = { ...workOrder }
  }
}
</script>

<style scoped>
.work-order-container {
  padding: 20px;
  height: 100vh;
  overflow-y: auto;
  background-color: #f5f7fa;
}

.nav-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
  background: white;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.back-button {
  margin-bottom: 20px;
}

.view-content {
  width: 100%;
}

:deep(.el-button) {
  border-radius: 6px;
  font-weight: 500;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #6cb4ff 100%);
  border: none;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.3);
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #337ecc 0%, #5a9cee 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.4);
}

@media (max-width: 768px) {
  .work-order-container {
    padding: 12px;
  }
  
  .nav-buttons {
    flex-direction: column;
    gap: 8px;
  }
}
</style>