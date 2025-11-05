<template>
  <div class="outbound-view">
    <el-row>
      <el-col :span="24">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>工單列表</span>
            </div>
          </template>

          <div class="toolbar">
            <div class="search-area">
              <el-input v-model="searchFilters.woNumber" placeholder="搜尋工單號碼" clearable @keyup.enter="handleSearch"></el-input>
              <el-input v-model="searchFilters.materialName" placeholder="搜尋產品名稱" clearable @keyup.enter="handleSearch"></el-input>
              <el-select v-model="searchFilters.status" placeholder="請選擇狀態" clearable @change="handleSearch">
                <el-option v-for="(label, value) in statusMap" :key="value" :label="label" :value="value"></el-option>
              </el-select>
              <el-button :icon="Search" type="primary" @click="handleSearch">搜尋</el-button>
              <el-button :icon="Refresh" @click="handleReset">重置</el-button>
            </div>
            <div class="action-area">
              <el-button :icon="Plus" type="success" @click="showAddWorkOrderDialog = true">新增工單</el-button>
              <el-dropdown>
                <el-button :icon="Download" type="info">
                  匯出<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="exportData('csv')">匯出為 CSV</el-dropdown-item>
                    
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <el-table :data="workOrders" style="width: 100%" v-loading="loadingWorkOrders">
            <el-table-column prop="woId" label="ID" width="80"></el-table-column>
            <el-table-column prop="woNumber" label="工單號碼" min-width="180"></el-table-column>
            <el-table-column prop="materialName" label="生產產品" min-width="150"></el-table-column>
            <el-table-column prop="requiredQuantity" label="需求數量" width="120"></el-table-column>
            <el-table-column prop="status" label="狀態" width="120">
              <template #default="scope">
                <el-tag :type="statusTagType(scope.row.status)">{{ translateStatus(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button link type="primary" size="small" @click="viewWorkOrderDetails(scope.row)">詳情</el-button>
                <el-button link type="danger" size="small" @click="deleteWorkOrder(scope.row.woId)">刪除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-block">
            <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              background
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showAddWorkOrderDialog" title="新增工單" width="600px" @close="addWorkOrderFormRef?.resetFields()">
      <el-form :model="newWorkOrder" :rules="addWorkOrderRules" ref="addWorkOrderFormRef" label-width="120px">
        <el-form-item label="生產產品" prop="materialId">
          <el-select v-model.number="newWorkOrder.materialId" placeholder="請選擇產品" style="width: 100%;" filterable>
            <el-option
              v-for="mat in availableProducts"
              :key="mat.materialId"
              :label="mat.materialName"
              :value="mat.materialId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="需求數量" prop="requiredQuantity">
          <el-input-number v-model="newWorkOrder.requiredQuantity" :min="1"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddWorkOrderDialog = false">取消</el-button>
          <el-button type="primary" @click="addWorkOrder">派出工單</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="showWorkOrderDetailsDialog" title="工單詳情" width="800px" top="5vh" custom-class="details-dialog">
      <div v-if="selectedWorkOrder" class="details-content">
        <el-steps :active="activeStep" :process-status="stepStatus" finish-status="success" align-center style="margin-bottom: 24px;">
          <el-step title="派出工單" :icon="Edit"></el-step>
          <el-step title="檢查庫存" :icon="Upload"></el-step>
          <el-step title="生產中" :icon="VideoPlay"></el-step>
          <el-step title="已完成" :icon="CircleCheck"></el-step>
        </el-steps>

        <el-descriptions title="工單資訊" :column="2" border>
          <el-descriptions-item label="工單號碼">{{ selectedWorkOrder.woNumber }}</el-descriptions-item>
          <el-descriptions-item label="生產產品">{{ selectedWorkOrder.materialName }}</el-descriptions-item>
          <el-descriptions-item label="需求數量">
            <el-tag size="small">{{ selectedWorkOrder.requiredQuantity }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="狀態">
            <el-tag :type="statusTagType(selectedWorkOrder.status)" size="small">{{ translateStatus(selectedWorkOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="建立時間" :span="2">{{ selectedWorkOrder.createdAt }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="bom-title">物料清單 (BOM)</h4>
        <el-table :data="selectedWorkOrderMaterials" style="width: 100%" empty-text="此工單沒有對應的物料清單">
          <el-table-column prop="materialName" label="物料名稱"></el-table-column>
          <el-table-column prop="requestedQuantity" label="BOM 需求數量"></el-table-column>
          <el-table-column prop="status" label="類型">
            <template #default="scope">
              <el-tag :type="statusTagType(scope.row.status)" size="small">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showWorkOrderDetailsDialog = false">關閉</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive } from 'vue'
import http from '../http-common' // 請確保您的 http client 路徑正確
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Edit, Upload, VideoPlay, CircleCheck, Search, Refresh, Plus, Download, ArrowDown } from '@element-plus/icons-vue'


// TypeScript Interfaces
interface Material {
  materialId: number;
  materialName: string;
}

interface WorkOrderDto {
  woId?: number;
  woNumber: string;
  materialId: number;
  materialName: string;
  requiredQuantity: number;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

interface WorkOrderMaterialDto {
  woMaterialId?: number;
  materialId: number;
  materialName: string;
  requestedQuantity: number;
  issuedQuantity?: number;
  status: string;
}

interface WorkOrderCreateDto {
  woNumber?: string; // 改為可選，讓後端生成
  materialId: number | null;
  requiredQuantity: number;
  status: string;
}

// API Endpoints
const API_BASE_URL = '/workorder'
const MATERIAL_API_BASE_URL = '/depot/materials'

// Component State (Refs and Reactives)
const workOrders = ref<WorkOrderDto[]>([])
const loadingWorkOrders = ref(false)

// 搜尋過濾器
const searchFilters = reactive({
  woNumber: '',
  materialName: '',
  status: '',
})

// 分頁狀態
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
})

// 新增工單 Dialog
const showAddWorkOrderDialog = ref(false)
const addWorkOrderFormRef = ref<FormInstance>()
const newWorkOrder = ref<WorkOrderCreateDto>({
  materialId: null, 
  requiredQuantity: 1,
  status: 'PENDING', 
})
const addWorkOrderRules = reactive<FormRules>({
  materialId: [{ required: true, message: '請選擇生產產品', trigger: 'change' }],
  requiredQuantity: [
    { required: true, message: '請輸入需求數量', trigger: 'blur' },
    { type: 'number', min: 1, message: '需求數量必須大於0', trigger: 'blur' },
  ],
})
const availableProducts = ref<Material[]>([])

// 工單詳情 Dialog
const showWorkOrderDetailsDialog = ref(false)
const selectedWorkOrder = ref<WorkOrderDto | null>(null)
const selectedWorkOrderMaterials = ref<WorkOrderMaterialDto[]>([])
const activeStep = ref(0);
const stepStatus = ref<'process' | 'error' | 'success' | 'wait'>('process');

// Helper Functions
const statusMap: Record<string, string> = {
  PENDING: '待處理',
  IN_PROGRESS: '進行中',
  OUT_OF_STOCK: '庫存不足',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
};
const translateStatus = (status: string) => statusMap[status] || status;
const statusTagType = (status: string) => {
  switch (status) {
    case 'COMPLETED': return 'success';
    case 'IN_PROGRESS': return 'primary';
    case 'OUT_OF_STOCK': return 'danger';
    case 'PENDING': return 'warning';
    default: return 'info';
  }
};

// API Functions
const fetchWorkOrders = async () => {
  loadingWorkOrders.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      woNumber: searchFilters.woNumber || null,
      materialName: searchFilters.materialName || null,
      status: searchFilters.status || null,
    }
    const response = await http.get<{ content: WorkOrderDto[]; totalElements: number }>(API_BASE_URL, { params })
    workOrders.value = response.data.content;
    pagination.total = response.data.totalElements;
  } catch (error: any) {
    ElMessage.error(`獲取工單失敗: ${error.response?.data?.message || error.message}`)
  } finally {
    loadingWorkOrders.value = false
  }
}

const fetchProducts = async () => {
  try {
    const response = await http.get<Material[]>(`${MATERIAL_API_BASE_URL}?materialType=PRODUCT`)
    availableProducts.value = response.data
  } catch (error: any) {
    ElMessage.error(`獲取可用產品列表失敗: ${error.response?.data?.message || error.message}`)
  }
}

const addWorkOrder = async () => {
  if (!addWorkOrderFormRef.value) return
  await addWorkOrderFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await http.post<WorkOrderDto>(API_BASE_URL, newWorkOrder.value)
        ElMessage.success('工單新增成功')
        showAddWorkOrderDialog.value = false
        fetchWorkOrders()
      } catch (error: any) {
        ElMessage.error(`新增工單失敗: ${error.response?.data?.message || error.message}`)
      }
    } else {
      ElMessage.warning('請檢查表單填寫是否正確')
    }
  })
}

const deleteWorkOrder = async (id?: number) => {
  if (!id) return
  await ElMessageBox.confirm('確定要刪除此工單嗎？', '警告', {
    confirmButtonText: '確定',
    cancelButtonText: '取消',
    type: 'warning',
  })
  .then(async () => {
    try {
      await http.delete(`${API_BASE_URL}/${id}`)
      ElMessage.success('工單刪除成功')
      fetchWorkOrders()
      } catch (error: any) {
        ElMessage.error(`刪除工單失敗: ${error.response?.data?.message || error.message}`)
      }
  })
  .catch(() => ElMessage.info('已取消刪除'))
}

const viewWorkOrderDetails = async (workOrder: WorkOrderDto) => {
  selectedWorkOrder.value = workOrder;
  showWorkOrderDetailsDialog.value = true;

  switch (workOrder.status) {
    case 'PENDING': activeStep.value = 0; stepStatus.value = 'process'; break;
    case 'OUT_OF_STOCK': activeStep.value = 1; stepStatus.value = 'error'; break;
    case 'IN_PROGRESS': activeStep.value = 2; stepStatus.value = 'process'; break;
    case 'COMPLETED': activeStep.value = 4; stepStatus.value = 'success'; break;
    default: activeStep.value = 0; stepStatus.value = 'wait';
  }

  try {
    const response = await http.get<WorkOrderMaterialDto[]>(`${API_BASE_URL}/${workOrder.woId}/bom`);
    selectedWorkOrderMaterials.value = response.data;
  } catch (error) {
    ElMessage.error('獲取物料清單 (BOM) 失敗');
    selectedWorkOrderMaterials.value = [];
  }
};


// Event Handlers for Search and Pagination
const handleSearch = () => {
  pagination.currentPage = 1;
  fetchWorkOrders();
}

const handleReset = () => {
  searchFilters.woNumber = '';
  searchFilters.materialName = '';
  searchFilters.status = '';
  pagination.currentPage = 1;
  fetchWorkOrders();
}

const handlePageChange = (newPage: number) => {
  pagination.currentPage = newPage;
  fetchWorkOrders();
}

const handleSizeChange = (newSize: number) => {
  pagination.pageSize = newSize;
  pagination.currentPage = 1;
  fetchWorkOrders();
}

// Export Functions
const exportData = (format: 'csv' | 'md') => {
  if (workOrders.value.length === 0) {
    ElMessage.warning('沒有資料可匯出');
    return;
  }
  const headers = ['ID', '工單號碼', '生產產品', '需求數量', '狀態'];
  const data = workOrders.value.map(wo => [
    wo.woId,
    wo.woNumber,
    wo.materialName,
    wo.requiredQuantity,
    translateStatus(wo.status)
  ]);
  
  let content = '';
  const fileName = `work-orders-${new Date().toISOString().split('T')[0]}`;

  if (format === 'csv') {
    content = [headers.join(','), ...data.map(row => row.join(','))].join('\n');
    triggerDownload(content, `${fileName}.csv`, 'text/csv;charset=utf-8;');
  } else if (format === 'md') {
    const headerLine = `| ${headers.join(' | ')} |`;
    const separatorLine = `| ${headers.map(() => '---').join(' | ')} |`;
    const bodyLines = data.map(row => `| ${row.join(' | ')} |`).join('\n');
    content = [headerLine, separatorLine, bodyLines].join('\n');
    triggerDownload(content, `${fileName}.md`, 'text/markdown;charset=utf-8;');
  }
}

const triggerDownload = (content: string, fileName: string, mimeType: string) => {
  const blob = new Blob([`\uFEFF${content}`], { type: mimeType });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(link.href);
}

// Lifecycle Hooks
onMounted(() => {
  fetchWorkOrders();
  fetchProducts();
});
</script>

<style scoped>
.outbound-view {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}

.search-area {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.search-area .el-input {
  width: 200px;
}

.action-area {
  display: flex;
  gap: 10px;
  align-items: center;
}

.pagination-block {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer button:first-child {
  margin-right: 10px;
}

.details-dialog .el-dialog__body {
  padding-top: 10px;
  padding-bottom: 20px;
}

.details-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.bom-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: -12px; /* Adjust spacing with table */
  color: var(--el-text-color-primary);
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-area, 
  .action-area {
    width: 100%;
    justify-content: flex-start; /* Align items to the start */
  }

  .action-area {
    justify-content: flex-end; /* Keep action buttons to the right on mobile */
  }
}

@media (max-width: 480px) {
  .action-area {
    flex-direction: column;
    align-items: stretch;
  }
  .action-area .el-button, 
  .action-area .el-dropdown {
    width: 100%;
  }
}
</style>