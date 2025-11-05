<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'
import api from '@/services/api'

// Props
const props = defineProps({
  visible: { type: Boolean, default: false }, // 控制開關
  repair: { type: Object, default: () => ({}) },
})

// Emits
const emit = defineEmits(['close', 'deleted'])

// Loading 狀態
const loading = ref(false)

// 確定刪除
async function confirmDelete() {
  if (!props.repair?.repairId) return

  loading.value = true
  try {
    await api.delete(`/api/repair/${props.repair.repairId}`)
    ElMessage.success('刪除成功')
    emit('deleted') // 通知父元件刷新列表
    emit('close')
  } catch (error) {
    console.error(error)
    if (error.response?.status === 401) {
      ElMessage.error('驗證已過期，請重新登入。')
    } else if (error.response?.status === 403) {
      ElMessage.error('您的權限不足。')
    } else {
      ElMessage.error('刪除失敗，請稍後再試')
    }
  } finally {
    loading.value = false
  }
}

// 取消
function cancelDelete() {
  emit('close')
}
</script>

<template>
  <el-dialog
    v-model="props.visible"
    title="確認刪除"
    width="450px"
    :before-close="cancelDelete"
    :close-on-click-modal="false"
    v-loading="loading"
  >
    <template #header>
      <span style="color: #e74c3c; font-size: 18px; font-weight: bold">❗ 確認刪除</span>
    </template>

    <div style="text-align: center; padding: 20px 0">
      <el-icon size="60" color="#f56c6c" style="margin-bottom: 20px">
        <WarningFilled />
      </el-icon>

      <p style="font-size: 16px; margin-bottom: 20px">
        你確定要刪除維修單 <strong style="color: #e74c3c">#{{ repair.repairId }}</strong> 嗎？
      </p>

      <el-descriptions :column="1" border style="margin: 20px 0">
        <el-descriptions-item label="機台編號">
          {{ repair.machineId }}
        </el-descriptions-item>
        <el-descriptions-item label="狀態">
          <el-tag size="small" :type="repair.repairStatus === '已完成' ? 'success' : 'warning'">
            {{ repair.repairStatus || repair.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述">
          {{ repair.repairDescription || repair.description }}
        </el-descriptions-item>
      </el-descriptions>

      <p style="font-size: 13px; color: #909399; margin-top: 15px">此操作無法復原</p>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="cancelDelete" :disabled="loading">取消</el-button>
        <el-button type="danger" :loading="loading" @click="confirmDelete">
          {{ loading ? '刪除中...' : '確定刪除' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.el-dialog,
.el-dialog * {
  font-size: 15px !important;
}
</style>
