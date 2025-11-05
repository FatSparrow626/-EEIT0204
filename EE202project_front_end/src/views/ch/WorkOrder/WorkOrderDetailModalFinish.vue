<template>
  <el-dialog
    :model-value="true"
    title="完成工單回報"
    width="800px"
    custom-class="finish-dialog"
    @close="$emit('close')"
  >
    <div v-if="localWorkOrder" class="finish-dialog-content">
      <el-row :gutter="20" class="two-column-layout">
        <el-col :span="12">
          <div class="work-order-details">
            <h3>{{ localWorkOrder?.woNumber }}</h3>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="生產產品">{{ localWorkOrder?.materialName }}</el-descriptions-item>
              <el-descriptions-item label="預計產量">{{ localWorkOrder?.requiredQuantity }}</el-descriptions-item>
              <el-descriptions-item label="狀態">
                <el-tag :type="statusTagType(localWorkOrder?.status)">{{ getStatusText(localWorkOrder?.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="已生產數量">{{ localWorkOrder?.successfulQuantity || 0 }}</el-descriptions-item>
              <el-descriptions-item label="不良品數量">{{ localWorkOrder?.failedQuantity || 0 }}</el-descriptions-item>
              <el-descriptions-item label="建立時間">{{ formatDate(localWorkOrder?.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="備註">{{ localWorkOrder?.notes || '無' }}</el-descriptions-item>
            </el-descriptions>

            <h4>物料清單 (BOM)</h4>
            <el-table :data="materials" style="width: 100%" size="small" height="250">
              <el-table-column prop="materialName" label="物料名稱"></el-table-column>
              <el-table-column prop="requestedQuantity" label="需求數量"></el-table-column>
            </el-table>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="finish-form">
            <h4>生產回報</h4>
            <el-form :model="form" ref="formRef" label-position="top">
              <el-form-item label="良品數量" prop="successfulQuantity">
                <el-input-number v-model="form.successfulQuantity" :min="0" />
              </el-form-item>
              <el-form-item label="不良品數量" prop="failedQuantity">
                <el-input-number v-model="form.failedQuantity" :min="0" />
              </el-form-item>
              <el-form-item label="備註" prop="notes">
                <el-input v-model="form.notes" type="textarea" :rows="4" />
              </el-form-item>
            </el-form>
          </div>
        </el-col>
      </el-row>
    </div>
    <div v-else class="empty-state">
      <el-empty description="沒有載入工單資料"></el-empty>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="$emit('close')">取消</el-button>
        <el-button type="primary" @click="submitForm">提交回報</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { ElMessage, ElForm } from 'element-plus';
import http from '../../../http-common';

// DTOs
interface WorkOrderDto {
  woId: number;
  woNumber: string;
  materialName: string;
  requiredQuantity: number;
  status: string;
  createdAt: string;
  successfulQuantity: number;
  failedQuantity: number;
  notes: string;
}

interface WorkOrderMaterialDto {
  materialId: number;
  materialName: string;
  materialType: string;
  requestedQuantity: number;
}

// Props and Emits
const props = defineProps<{ workOrder: WorkOrderDto | null }>();
const emit = defineEmits(['close', 'finish', 'production-started']);

// Component State
const localWorkOrder = ref<WorkOrderDto | null>(null);
const materials = ref<WorkOrderMaterialDto[]>([]);
const form = reactive({
  successfulQuantity: 0,
  failedQuantity: 0,
  notes: ''
});
const formRef = ref<InstanceType<typeof ElForm> | null>(null);

// Utility functions (copied from WorkOrderFinishList.vue for consistency)
const statusTagType = (status: string) => ({
  COMPLETED: 'success',
  IN_PROGRESS: 'warning',
  CANCELLED: 'danger',
  PENDING: 'info'
}[status] || 'info');

const getStatusText = (status: string) => ({
  PENDING: '未執行',
  IN_PROGRESS: '進行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}[status] || status);

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-';
  try {
    return new Date(dateStr).toLocaleString('zh-TW');
  } catch {
    return dateStr;
  }
};

// Fetch BOM
const fetchWorkOrderMaterials = async () => {
  if (!localWorkOrder.value || !localWorkOrder.value.woId) return;
  try {
    const res = await http.get(`/workorder/${localWorkOrder.value.woId}/bom`);
    materials.value = res.data;
  } catch (error) {
    console.error('Failed to fetch work order materials:', error);
    ElMessage.error('加載物料清單失敗');
  }
};

// Form Submission
const submitForm = async () => {
  if (!localWorkOrder.value) return;
  try {
    await http.put(`/workorder/${localWorkOrder.value.woId}/quantities`, {
      successfulQuantity: form.successfulQuantity,
      failedQuantity: form.failedQuantity,
    });
    ElMessage.success('生產數量回報成功');
    emit('finish');
    emit('close');
  } catch (error) {
    console.error('Failed to update quantities:', error);
    ElMessage.error('回報失敗');
  }
};

// Lifecycle and Watchers
watch(() => props.workOrder, (newVal) => {
  if (newVal) {
    localWorkOrder.value = { ...newVal };
    fetchWorkOrderMaterials();
  } else {
    localWorkOrder.value = null;
  }
}, { immediate: true });

</script>

<style scoped>
.finish-dialog-content {
  padding: 0 10px;
}

.work-order-details h3 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 1.2rem;
}

.work-order-details h4 {
  margin-top: 20px;
  margin-bottom: 10px;
}

.finish-form h4 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 1.1rem;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

/* Responsive styles */
@media (max-width: 768px) {
  :deep(.finish-dialog) {
    width: 90vw !important;
  }

  .two-column-layout > .el-col {
    width: 100%;
    margin-bottom: 20px;
  }
}
</style>
