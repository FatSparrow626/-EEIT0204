<script setup>
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'

const props = defineProps({
  visible: { type: Boolean, default: false },
  repair: { type: Object, default: () => ({}) },
  statusOptions: { type: Array, default: () => [] },
})

const emit = defineEmits(['close', 'updated'])

const formRef = ref()
const form = ref({
  repairId: '',
  repairStatus: '',
  repairDescription: '',
})

const rules = {
  repairStatus: [{ required: true, message: '請選擇狀態', trigger: 'change' }],
  repairDescription: [
    { required: true, message: '請輸入問題描述', trigger: 'blur' },
    { max: 500, message: '描述不能超過500字元', trigger: 'blur' },
  ],
}

const loading = ref(false)

watch(
  () => props.repair,
  (newVal) => {
    if (newVal && Object.keys(newVal).length > 0) {
      form.value = {
        repairId: newVal.repairId || '',
        repairStatus: newVal.repairStatus || newVal.status || '',
        repairDescription: newVal.repairDescription || newVal.description || '',
      }
    }
  },
  { immediate: true, deep: true },
)

function close() {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  emit('close')
}

async function save() {
  if (!formRef.value) {
    ElMessage.error('表單初始化錯誤，請重試')
    return
  }
  if (!form.value.repairId) {
    ElMessage.error('維修單編號遺失')
    return
  }
  try {
    const valid = await formRef.value.validate()
    if (!valid) {
      ElMessage.warning('請檢查輸入資料')
      return
    }
  } catch (validationError) {
    ElMessage.error('表單驗證失敗')
    return
  }
  loading.value = true
  try {
    await api.put(`/api/repair/${form.value.repairId}/status`, null, {
      params: { newStatus: form.value.repairStatus }
    })
    ElMessage.success('狀態更新成功')
    emit('updated')
    close()
  } catch (error) {
    if (error.response?.status === 401) {
      ElMessage.error('驗證已過期，請重新登入')
    } else if (error.response?.status === 403) {
      ElMessage.error('權限不足，無法執行此操作')
    } else if (error.response?.status === 404) {
      ElMessage.error('找不到該維修單')
    } else if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('更新失敗，請稍後再試')
    }
  } finally {
    loading.value = false
  }
}

watch(
  () => props.visible,
  async (newVal) => {
    if (newVal) {
      await nextTick()
    }
  }
)
</script>

<template>
  <el-dialog
    v-model="props.visible"
    title="編輯維修單"
    width="500px"
    :before-close="close"
    :close-on-click-modal="false"
    v-loading="loading"
    loading-text="處理中..."
  >
    <template #header>
      <span style="font-size: 18px; font-weight: bold">編輯維修單 #{{ form.repairId }}</span>
    </template>

    <el-form 
      ref="formRef" 
      :model="form" 
      :rules="rules" 
      label-width="100px"
      @submit.prevent="save"
    >
      <el-form-item label="狀態" prop="repairStatus">
        <el-select 
          v-model="form.repairStatus" 
          placeholder="請選擇狀態" 
          style="width: 100%"
          clearable
        >
          <el-option
            v-for="status in props.statusOptions"
            :key="status.status_code || status.statusCode"
            :label="status.status_label || status.statusLabel"
            :value="status.status_code || status.statusCode"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="問題描述" prop="repairDescription">
        <el-input
          v-model="form.repairDescription"
          type="textarea"
          :rows="4"
          placeholder="請輸入問題描述"
          show-word-limit
          maxlength="500"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div>
        <el-button @click="close">取消</el-button>
        <el-button 
          type="primary" 
          :loading="loading" 
          @click="save"
          :disabled="!form.repairId"
        >
          {{ loading ? '儲存中...' : '儲存' }}
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