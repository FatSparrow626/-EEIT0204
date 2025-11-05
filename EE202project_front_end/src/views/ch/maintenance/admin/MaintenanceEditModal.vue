<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`編輯保養單 #${form.scheduleId}`"
    width="500px"
    :before-close="handleClose"
    append-to-body
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" label-position="right">
      <el-form-item label="保養狀態" prop="maintenanceStatus">
        <el-select v-model="form.maintenanceStatus" placeholder="請選擇狀態" style="width: 100%">
          <el-option
            v-for="status in statusOptions"
            :key="status.status_code || status.statusCode"
            :label="status.status_label || status.statusLabel"
            :value="status.status_code || status.statusCode"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="保養描述" prop="maintenanceDescription">
        <el-input
          v-model="form.maintenanceDescription"
          type="textarea"
          :rows="4"
          placeholder="請輸入保養描述"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave"> 儲存 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'

const props = defineProps({
  maintenance: Object,
  statusOptions: Array,
})

const emit = defineEmits(['close', 'updated'])

const dialogVisible = ref(true)
const formRef = ref()
const saving = ref(false)

const form = ref({
  scheduleId: '',
  maintenanceStatus: '',
  maintenanceDescription: '',
  machineId: null,
  employeeId: null,
  scheduleDate: null,
})

const rules = {
  maintenanceStatus: [{ required: true, message: '請選擇保養狀態', trigger: 'change' }],
  maintenanceDescription: [
    { required: true, message: '請輸入保養描述', trigger: 'blur' },
    { min: 1, max: 500, message: '描述長度應在 1 到 500 個字符', trigger: 'blur' },
  ],
}

watch(
  () => props.maintenance,
  (newVal) => {
    if (newVal) {
      form.value = {
        scheduleId: newVal.scheduleId,
        maintenanceStatus: newVal.maintenanceStatus || '',
        maintenanceDescription: newVal.maintenanceDescription || '',
        machineId: newVal.machineId || null,
        employeeId: newVal.employeeId || null,
        scheduleDate: newVal.scheduleDate || null,
      }
    }
  },
  { immediate: true },
)

const handleClose = () => {
  dialogVisible.value = false
  nextTick(() => {
    emit('close')
  })
}

const handleSave = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    saving.value = true

    const updateData = {
      scheduleId: form.value.scheduleId,
      maintenanceStatus: form.value.maintenanceStatus,
      maintenanceDescription: form.value.maintenanceDescription,
      machineId: form.value.machineId,
      employeeId: form.value.employeeId,
      scheduleDate: form.value.scheduleDate,
    }

    const token = localStorage.getItem('token')
    await api.put(`/api/maintenance/${form.value.scheduleId}`, updateData, {
      headers: { Authorization: `Bearer ${token}` }
    })

    ElMessage.success('更新成功')
    emit('updated')
    handleClose()
  } catch (error) {
    console.error('更新失敗：', error)
    ElMessage.error('更新失敗，請稍後再試')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
.el-dialog,
.el-dialog * {
  font-size: 15px !important;
}
</style>
