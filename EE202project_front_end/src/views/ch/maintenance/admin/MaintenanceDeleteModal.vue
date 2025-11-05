<template>
  <el-dialog
    v-model="dialogVisible"
    title="刪除保養單"
    width="400px"
    :before-close="handleClose"
    append-to-body
  >
    <div class="delete-content">
      <el-icon class="warning-icon" size="48" color="#E6A23C">
        <WarningFilled />
      </el-icon>
      <p class="delete-message">
        確定要刪除保養單 <strong>#{{ props.maintenance?.scheduleId }}</strong> 嗎？
      </p>
      <p class="delete-warning">此操作無法恢復，請謹慎操作。</p>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="danger" :loading="deleting" @click="handleConfirmDelete">
          確認刪除
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { WarningFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'

const props = defineProps({
  maintenance: Object,
})

const emit = defineEmits(['close', 'deleted'])

const dialogVisible = ref(true)
const deleting = ref(false)

// 關閉對話框
const handleClose = () => {
  dialogVisible.value = false
  nextTick(() => {
    emit('close')
  })
}

// 確認刪除
const handleConfirmDelete = async () => {
  try {
    deleting.value = true
    await api.delete(`/api/maintenance/${props.maintenance.scheduleId}`)

    ElMessage.success('刪除成功')
    emit('deleted')
    handleClose()
  } catch (error) {
    console.error('刪除失敗：', error)
    ElMessage.error('刪除失敗，請稍後再試')
  } finally {
    deleting.value = false
  }
}
</script>

<style scoped>
.delete-content {
  text-align: center;
  padding: 20px 0;
}

.warning-icon {
  margin-bottom: 16px;
}

.delete-message {
  font-size: 16px;
  margin: 16px 0 8px 0;
  color: #303133;
}

.delete-warning {
  font-size: 14px;
  color: #909399;
  margin: 8px 0 0 0;
}

.dialog-footer {
  text-align: right;
}

.el-dialog,
.el-dialog * {
  font-size: 15px !important;
}
</style>
