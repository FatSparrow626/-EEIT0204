<template>
  <el-dialog
    :model-value="modelValue"
    title="完成工單"
    width="500px"
    @close="handleClose"
  >
    <el-form 
      ref="formRef" 
      :model="formData" 
      label-width="120px"
    >
      <el-form-item label="工單編號">
        <el-input v-model="workOrder.woNumber" disabled />
      </el-form-item>
      
      <el-form-item label="材料名稱">
        <el-input v-model="workOrder.materialName" disabled />
      </el-form-item>
      
      <el-form-item label="成功數量" required>
        <el-input-number 
          v-model="formData.quantityDone" 
          :min="0" 
          :max="workOrder.requiredQuantity"
          style="width: 100%"
        />
        <div class="form-tip">需求數量: {{ workOrder.requiredQuantity }}</div>
      </el-form-item>

      <el-form-item label="失敗數量" required>
        <el-input-number 
          v-model="formData.quantityFailed" 
          :min="0" 
          :max="workOrder.requiredQuantity"
          style="width: 100%"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="submitForm" :loading="loading">確認完成</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  workOrder: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'update:modelValue', 'submit'])

const formRef = ref()
const loading = ref(false)

// 表單資料
const formData = ref({
  quantityDone: props.workOrder.requiredQuantity || 0,
  quantityFailed: 0
})

// watch 成功數量，自動計算失敗數
watch(() => formData.value.quantityDone, val => {
  const max = props.workOrder.requiredQuantity || 0
  formData.value.quantityFailed = Math.max(0, max - val)
})

// 當 dialog 關閉時重置表單
watch(() => props.modelValue, val => {
  if (val === false) resetForm()
})

const resetForm = () => {
  formData.value.quantityDone = props.workOrder.requiredQuantity || 0
  formData.value.quantityFailed = 0
}

// 驗證
const validateForm = () => {
  const max = props.workOrder.requiredQuantity || 0
  const total = Number(formData.value.quantityDone) + Number(formData.value.quantityFailed)
  if (total > max) {
    ElMessage.error('成功+失敗不能超過要求數量')
    return false
  }
  if (formData.value.quantityDone < 0 || formData.value.quantityFailed < 0) {
    ElMessage.error('數量不能為負數')
    return false
  }
  return true
}

const handleClose = () => {
  emit('update:modelValue', false)
  emit('close')
}

// 提交表單
const submitForm = () => {
  if (!validateForm()) return
  loading.value = true

  const submitData = {
    woId: props.workOrder.woId,
    quantityDone: formData.value.quantityDone,
    quantityFailed: formData.value.quantityFailed
  }

  // 模擬 API 調用
  setTimeout(() => {
    emit('submit', submitData)
    loading.value = false
    emit('update:modelValue', false) // 關閉對話框
  }, 500)
}
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
