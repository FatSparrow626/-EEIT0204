<template>
  <el-dialog
    :model-value="modelValue"
    title="生產管理"
    width="1200px"
    :close-on-click-modal="false"
    @close="handleClose"
    top="5vh"
  >
    <div v-if="workOrder && workOrder.woId" class="production-container">
      <!-- 工單信息摘要 -->
      <el-alert
        :title="`工單：${workOrder.woNumber}`"
        :description="`產品：${workOrder.materialName || '未知產品'}，要求數量：${workOrder.requiredQuantity}，已生產：${currentProducedQuantity}，剩餘：${remainingQuantity}`"
        type="info"
        show-icon
        :closable="false"
        class="work-order-summary"
      />

      <!-- 材料消耗明細 -->
      <el-card class="material-consumption-card" v-if="materialConsumption.length > 0">
        <template #header>
          <div class="card-header">
            <span>📦 材料消耗記錄</span>
          </div>
        </template>
        <el-table :data="materialConsumption" style="width: 100%;" size="small">
          <el-table-column prop="materialName" label="材料名稱" min-width="120" />
          <el-table-column prop="totalConsumed" label="總消耗數量" width="100">
            <template #default="{ row }">
              <el-tag type="warning">{{ row.totalConsumed }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="unitConsumption" label="單位消耗" width="100">
            <template #default="{ row }">
              <span>{{ row.unitConsumption }} / 件</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-row :gutter="20" class="production-sections">
        <!-- 可用機台 -->
        <el-col :span="12">
          <el-card class="section-card">
            <template #header>
              <div class="card-header">
                <span>🏭 可用的運行中機台</span>
                <el-button type="info" size="small" @click="fetchRunningMachines" :icon="Refresh">刷新</el-button>
              </div>
            </template>
            <div class="table-container">
              <el-table :data="runningMachines" style="width: 100%;" max-height="250" size="small">
                <el-table-column prop="machineId" label="機台ID" width="80" />
                <el-table-column prop="machineName" label="機台名稱" min-width="100" />
                <el-table-column prop="mstatus" label="狀態" width="80">
                  <template #default="scope">
                    <el-tag type="success" size="small">{{ scope.row.statusCode?.statusCode || '未知狀態' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="70">
                  <template #default="scope">
                    <el-button 
                      link 
                      type="primary" 
                      size="small" 
                      @click="addMachineToProduction(scope.row)" 
                      :disabled="isMachineInQueue(scope.row.machineId)"
                    >
                      加入
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="runningMachines.length === 0" description="沒有運行中機台" />
            </div>
          </el-card>
        </el-col>

        <!-- 生產佇列 -->
        <el-col :span="12">
          <el-card class="section-card">
            <template #header>
              <div class="card-header">
                <span>⚙️ 生產佇列</span>
                <el-text type="info" size="small">剩餘：{{ remainingQuantity }}</el-text>
              </div>
            </template>
            <div class="table-container">
              <el-table :data="selectedMachinesForProduction" style="width: 100%;" max-height="250" size="small">
                <el-table-column prop="machineName" label="機台名稱" min-width="100" />
                <el-table-column label="生產數量" width="100">
                  <template #default="scope">
                    <el-input-number
                      v-model="scope.row.quantityToProduce"
                      :min="1"
                      :max="remainingQuantity"
                      :disabled="scope.row.productionInProgress || remainingQuantity === 0"
                      size="small"
                      controls-position="right"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <div class="button-group">
                      <el-button
                        type="success"
                        size="small"
                        @click="startMachineProduction(scope.row)"
                        :disabled="scope.row.productionInProgress || remainingQuantity === 0 || scope.row.quantityToProduce === 0"
                      >
                        生產
                      </el-button>
                      <el-button
                        type="danger"
                        size="small"
                        @click="removeMachineFromProduction(scope.row.machineId)"
                        :disabled="scope.row.productionInProgress"
                      >
                        移除
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="selectedMachinesForProduction.length === 0" description="請從左側加入機台" />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 生產結果與進度 -->
      <el-card class="production-results-card">
        <template #header>
          <div class="card-header">
            <span>📊 生產結果與進度</span>
            <el-text v-if="totalSuccessful + totalFailed > 0" type="success">
              已完成：{{ totalSuccessful + totalFailed }} / {{ workOrder.requiredQuantity }}
            </el-text>
          </div>
        </template>
        
        <div v-if="productionMessage" class="production-message">
          <el-alert :title="productionMessage" type="info" show-icon :closable="false" />
        </div>

        <!-- 機台生產狀態 -->
        <div class="machine-statuses">
          <div v-for="machine in selectedMachinesForProduction" :key="machine.machineId" class="machine-status-item">
            <div v-if="machine.productionInProgress" class="production-progress">
              <div class="progress-info">
                <span>🔧 {{ machine.machineName }}</span>
                <span class="progress-text">生產中... {{ machine.quantityToProduce }} 件</span>
              </div>
              <el-progress 
                :percentage="machine.progress" 
                :text-inside="true" 
                :stroke-width="16" 
                status="success"
              />
            </div>
            <div v-else-if="machine.productionResult" class="production-result">
              <div class="result-header">
                <span>✅ {{ machine.machineName }} 生產完成</span>
              </div>
              <div class="result-tags">
                <el-tag type="success">成功 {{ machine.productionResult.success }}</el-tag>
                <el-tag type="danger" v-if="machine.productionResult.failed > 0">
                  失敗 {{ machine.productionResult.failed }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 總體統計 -->
        <div v-if="productionHistory.length > 0" class="production-summary">
          <el-divider>生產統計</el-divider>
          <el-row :gutter="16">
            <el-col :span="6">
              <el-statistic title="總成功數量" :value="totalSuccessful" suffix="件" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="總失敗數量" :value="totalFailed" suffix="件" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="成功率" :value="successRate" suffix="%" :precision="1" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="剩餘數量" :value="remainingQuantity" suffix="件" />
            </el-col>
          </el-row>
        </div>

        <!-- 完成工單 -->
        <div v-if="remainingQuantity === 0 && workOrder.status !== 'COMPLETED'" class="completion-section">
          <el-alert title="🎉 恭喜！工單已完成所有生產要求。" type="success" show-icon :closable="false" />
          <el-button 
            type="success" 
            size="large" 
            @click="completeWorkOrder" 
            :loading="completingWorkOrder"
            class="complete-button"
          >
            完成工單
          </el-button>
        </div>
      </el-card>
    </div>

    <div v-else class="empty-state">
      <el-empty description="沒有選擇工單" />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">關閉</el-button>
        <el-button 
          v-if="workOrder && workOrder.status === 'COMPLETED'" 
          type="success" 
          disabled
        >
          已完成
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  workOrder: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue', 'production-complete', 'production-updated'])

const runningMachines = ref([])
const selectedMachinesForProduction = ref([])
const productionMessage = ref('')
const productionHistory = ref([])
const materialConsumption = ref([])
const currentProducedQuantity = ref(0)
const completingWorkOrder = ref(false)

// 計算屬性
const remainingQuantity = computed(() => {
  const required = props.workOrder.requiredQuantity || 0
  const produced = currentProducedQuantity.value || 0
  return Math.max(0, required - produced)
})

const totalSuccessful = computed(() => {
  return productionHistory.value.reduce((sum, record) => sum + (record.quantityDone || 0), 0)
})

const totalFailed = computed(() => {
  return productionHistory.value.reduce((sum, record) => sum + (record.quantityFailed || 0), 0)
})

const successRate = computed(() => {
  const total = totalSuccessful.value + totalFailed.value
  return total > 0 ? (totalSuccessful.value / total) * 100 : 0
})

// 初始化當前已生產數量
watch(() => props.workOrder, (newWorkOrder) => {
  if (newWorkOrder && newWorkOrder.woId) {
    currentProducedQuantity.value = newWorkOrder.producedQuantity || 0
    fetchProductionHistory()
    fetchMaterialConsumption()
  }
}, { immediate: true, deep: true })

// 獲取運行中機台
const fetchRunningMachines = async () => {
  try {
    const response = await api.get('/api/machines')
    runningMachines.value = response.data.filter(m => m.statusCode.statusCode === 'WAIT')
  } catch (error) {
    console.error('Error fetching running machines:', error)
    ElMessage.error(`獲取運行中機台失敗: ${error.response?.data?.message || error.message}`)
    runningMachines.value = []
  }
}

// 獲取生產歷程
const fetchProductionHistory = async () => {
  if (!props.workOrder.woId) return
  try {
    const response = await api.get(`/api/workorderfinish/workorder/${props.workOrder.woId}`)
    productionHistory.value = Array.isArray(response.data) ? response.data : []
  } catch (error) {
    console.error('Error fetching production history:', error)
    productionHistory.value = []
  }
}

// 獲取材料消耗記錄
const fetchMaterialConsumption = async () => {
  if (!props.workOrder.woId) return
  try {
    const response = await api.get(`/api/workorder/${props.workOrder.woId}/materials`)
    if (Array.isArray(response.data)) {
      materialConsumption.value = response.data.map(material => ({
        materialName: material.materialName,
        totalConsumed: material.issuedQuantity || 0,
        unitConsumption: material.requestedQuantity / (props.workOrder.requiredQuantity || 1)
      }))
    }
  } catch (error) {
    console.error('Error fetching material consumption:', error)
    materialConsumption.value = []
  }
}

// 機台管理方法
const isMachineInQueue = (machineId) => {
  return selectedMachinesForProduction.value.some(m => m.machineId === machineId)
}

// 更健壯的 addMachineToProduction 函式
const addMachineToProduction = async (machine) => {
  if (isMachineInQueue(machine.machineId)) {
    ElMessage.warning('該機台已在生產佇列中。');
    return;
  }

  try {
    // 步驟 1: 分配機台到工單
    console.log(`正在分配機台 ${machine.machineId} 到工單 ${props.workOrder.woId}...`);
    await api.post(`/api/workorder/${props.workOrder.woId}/machines`, [machine.machineId]);
    ElMessage.success(`機台 ${machine.machineName} 分配請求已送出`);

    // 步驟 2: 重新獲取工單的完整資訊
    console.log(`重新獲取工單 ${props.workOrder.woId} 的資料...`);
    const res = await api.get(`/api/workorder/${props.workOrder.woId}`);

    // [關鍵除錯點] 在 Console 中印出後端回傳的資料，檢查其結構
    console.log('後端回傳的工單資料:', res.data);

    // 步驟 3: 更新前端畫面
    if (res.data && Array.isArray(res.data.machines)) {
      selectedMachinesForProduction.value = res.data.machines.map(m => ({
        ...m,
        quantityToProduce: 1,
        productionInProgress: false,
        productionResult: null,
        progress: 0,
        timer: null,
      }));

      // 左邊移除該機台
      runningMachines.value = runningMachines.value.filter(m => m.machineId !== machine.machineId);
    } else {
      // 如果回應格式不對，給出提示
      console.error('API 回應格式錯誤，預期應有 machines 陣列，但實際收到:', res.data);
      ElMessage.error('更新機台列表失敗：後端回應資料格式不符。');
    }

  } catch (error) {
    // 如果任何 API 請求失敗，都會在這裡捕捉到錯誤
    console.error('分配機台或更新時發生錯誤:', error);
    ElMessage.error(`操作失敗: ${error.response?.data?.message || error.message}`);
  }
};


const removeMachineFromProduction = async (machineId) => {
  const machine = selectedMachinesForProduction.value.find(m => m.machineId === machineId)
  if (!machine) return

  if (machine.productionInProgress) {
    ElMessage.warning('機台正在生產中，無法移除。')
    return
  }

  try {
    // 呼叫後端 API 移除關聯
    await api.delete(`/api/workorder/${props.workOrder.woId}/machine/${machineId}`)
    ElMessage.success(`機台 ${machine.machineName} 已從工單中移除。`)

    // 從生產佇列中移除
    selectedMachinesForProduction.value = selectedMachinesForProduction.value.filter(m => m.machineId !== machineId)

    // 將機台加回可用機台列表
    runningMachines.value.push(machine)
    // 重新排序可用機台列表 (可選)
    runningMachines.value.sort((a, b) => a.machineId - b.machineId)

  } catch (error) {
    console.error('Error removing machine from production:', error)
    ElMessage.error(`移除機台失敗: ${error.response?.data?.message || error.message}`)
  }
}

// --- Refactored Production Logic ---

// This function contains the core asynchronous production process for a single machine.
const runProductionCycle = async (machine, workOrderId) => {
  // 1. Animate progress bar
  machine.progress = 0;
  const progressInterval = setInterval(() => {
    if (machine.progress < 95) {
      machine.progress += 5;
    }
  }, 50);

  try {
    // 2. Simulate production time
    await new Promise(resolve => setTimeout(resolve, 10000));
    machine.progress = 100;

    // 3. Simulate production result
    const successRate = Math.random() * 0.25 + 0.7; // 70-95% success rate
    const successful = Math.round(machine.quantityToProduce * successRate);
    const failed = machine.quantityToProduce - successful;
    machine.productionResult = { success: successful, failed };

    // 4. Send production report to the backend
    if (successful > 0 || failed > 0) {
      await api.post('/api/workorderfinish', {
        woId: workOrderId,
        quantityDone: successful,
        quantityFailed: failed,
      });
    }
    
    ElMessage.success(`${machine.machineName} 生產完成: 成功 ${successful}, 失敗 ${failed}`);

    // 5. Update related data after successful reporting
    if (successful > 0) {
      const response = await api.put(`/api/workorder/${workOrderId}/produce-quantity`, { successfulQuantity: successful });
      if (response.data) {
        currentProducedQuantity.value = response.data.producedQuantity;
        emit('production-updated', response.data);
      }
    }
    await fetchProductionHistory(); // Refresh production history

  } catch (error) {
    console.error('Error during production cycle:', error);

    let detail = '請檢查網路或聯繫管理員'; // Default message
    if (error.response) {
      // We have a response from the server
      const status = error.response.status;
      const data = error.response.data;
      const message = typeof data === 'string' ? data : JSON.stringify(data);
      detail = `伺服器錯誤 ${status} - ${message}`;
    } else if (error.request) {
      // The request was made but no response was received
      detail = '無法連接到伺服器，請檢查您的網路連線。';
    } else {
      // Something happened in setting up the request that triggered an Error
      detail = `請求設定錯誤: ${error.message}`;
    }

    ElMessage.error(`[${machine.machineName}] 生產失敗: ${detail}`);
    machine.productionResult = { success: 0, failed: machine.quantityToProduce };
  } finally {
    // 6. Clean up and reset machine state
    clearInterval(progressInterval);
    machine.productionInProgress = false;
    
    // 7. Check if the entire work order is complete
    if (remainingQuantity.value <= 0) {
      ElMessage.success('🎉 工單已完成所有生產要求！');
      if (props.workOrder.status === 'PENDING') {
        try {
          await api.put(`/api/workorder/${workOrderId}/status`, { status: 'IN_PROGRESS' });
        } catch (error) {
          console.error('Error updating work order status:', error);
        }
      }
    }
  }
};

// This function validates the inputs and initiates the production cycle.
const startMachineProduction = async (machine) => {
  const { woId } = props.workOrder;

  // --- Input Validations ---
  if (!woId) {
    ElMessage.error('工單資訊不可為空，請重新選擇工單。');
    return;
  }
  if (machine.productionInProgress) {
    ElMessage.warning('機台已在生產中。');
    return;
  }
  if (machine.quantityToProduce <= 0) {
    ElMessage.warning('生產數量必須大於 0。');
    return;
  }
  if (remainingQuantity.value === 0) {
    ElMessage.warning('工單已完成所有生產要求，無需再生產。');
    return;
  }
  if (machine.quantityToProduce > remainingQuantity.value) {
    ElMessage.warning(`生產數量 (${machine.quantityToProduce}) 不能超過剩餘數量 (${remainingQuantity.value})。`);
    machine.quantityToProduce = remainingQuantity.value;
    return;
  }

  // --- Initiate Production ---
  machine.productionInProgress = true;
  machine.productionResult = null;
  productionMessage.value = `機台 ${machine.machineName} 開始生產 ${machine.quantityToProduce} 件...`;
  
  // Delegate to the core logic function
  runProductionCycle(machine, woId);
};


// 完成工單
const completeWorkOrder = async () => {
  if (!props.workOrder.woId) {
    ElMessage.error('無效的工單ID，無法完成工單。')
    return
  }
  if (remainingQuantity.value > 0) {
    ElMessage.warning('工單尚未完成所有生產要求。')
    return
  }

  completingWorkOrder.value = true
  try {
    // 更新工單狀態為已完成
    await api.put(`/api/workorder/${props.workOrder.woId}/status`, { status: 'COMPLETED' })
    
    // 更新所有使用中的機台狀態為停機
    const machineUpdatePromises = selectedMachinesForProduction.value.map(async (machine) => {
      try {
        const machineResponse = await api.get(`/api/machines/${machine.machineId}`)
        const machineData = machineResponse.data
        machineData.mstatus = '停機'
        await api.put(`/api/machines/${machine.machineId}`, machineData)
      } catch (error) {
        console.error(`Error updating machine ${machine.machineId}:`, error)
      }
    })
    
    await Promise.all(machineUpdatePromises)
    
    ElMessage.success(`🎉 工單 ${props.workOrder.woNumber} 已成功完成！`)
    
    // 發出完成事件
    const completedWorkOrder = {
      ...props.workOrder,
      status: 'COMPLETED',
      producedQuantity: currentProducedQuantity.value
    }
    emit('production-complete', completedWorkOrder)
    
  } catch (error) {
    console.error('Error completing work order:', error)
    ElMessage.error(`完成工單失敗: ${error.response?.data?.message || error.message}`)
  } finally {
    completingWorkOrder.value = false
  }
}

// 對話框關閉處理
const handleClose = () => {
  // 清理所有定時器
  selectedMachinesForProduction.value.forEach(machine => {
    if (machine.timer) {
      clearTimeout(machine.timer)
    }
  })
  
  // 重置狀態
  selectedMachinesForProduction.value = []
  productionMessage.value = ''
  
  emit('update:modelValue', false)
}

// 組件掛載時獲取數據
onMounted(async () => {
  if (props.workOrder && props.workOrder.woId) {
    const res = await api.get(`/api/workorder/${props.workOrder.woId}`)
    selectedMachinesForProduction.value = Array.isArray(res.data.machines) ? res.data.machines.map(m => ({
      ...m,
      quantityToProduce: 1,
      productionInProgress: false,
      productionResult: null,
      progress: 0,
      timer: null,
    })) : []
  }
  fetchRunningMachines()
})
</script>

<style scoped>
.el-dialog,
.el-dialog * {
  font-size: 25px !important;
}

.production-container {
  max-height: 70vh;
  overflow-y: auto;
}

.work-order-summary {
  margin-bottom: 20px;
}

.material-consumption-card {
  margin-bottom: 20px;
}

.production-sections {
  margin-bottom: 20px;
}

.section-card {
  height: 350px;
}

.table-container {
  height: 280px;
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.production-results-card {
  margin-top: 20px;
}

.production-message {
  margin-bottom: 16px;
}

.machine-statuses {
  margin-bottom: 20px;
}

.machine-status-item {
  margin-bottom: 16px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.production-progress {
  gap: 12px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-weight: 500;
}

.progress-text {
  color: #606266;
  font-size: 14px;
}

.production-result {
  text-align: left;
}

.result-header {
  margin-bottom: 8px;
  font-weight: 600;
  color: #67c23a;
}

.result-tags {
  display: flex;
  gap: 8px;
}

.production-summary {
  margin-top: 24px;
  padding: 20px;
  background-color: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #e6f7ff;
}

.completion-section {
  margin-top: 24px;
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  border-radius: 12px;
  border: 2px solid #91d5ff;
}

.complete-button {
  margin-top: 16px;
  padding: 12px 32px;
  font-size: 16px;
  border-radius: 8px;
}

.button-group {
  display: flex;
  gap: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.empty-state {
  text-align: center;
  padding: 40px;
}

/* 進度條動畫 */
:deep(.el-progress-bar__inner) {
  border-radius: 100px;
  background-image: linear-gradient(45deg, rgba(255, 255, 255, .15) 25%, transparent 25%, transparent 50%, rgba(255, 255, 255, .15) 50%, rgba(255, 255, 255, .15) 75%, transparent 75%, transparent);
  background-size: 40px 40px;
  animation: progress-bar-stripes 2s linear infinite;
}

@keyframes progress-bar-stripes {
  0% {
    background-position: 40px 0;
  }
  100% {
    background-position: 0 0;
  }
}

/* 響應式設計 */
@media (max-width: 1024px) {
  .production-sections {
    flex-direction: column;
  }
  
  .section-card {
    height: auto;
    margin-bottom: 16px;
  }
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    gap: 8px;
  }
  
  .progress-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .result-tags {
    flex-wrap: wrap;
  }
}

/* 自定義滾動條 */
.production-container::-webkit-scrollbar,
.table-container::-webkit-scrollbar {
  width: 6px;
}

.production-container::-webkit-scrollbar-track,
.table-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.production-container::-webkit-scrollbar-thumb,
.table-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.production-container::-webkit-scrollbar-thumb:hover,
.table-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>