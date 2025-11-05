<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api' // ✅ 假設你有統一封裝的 axios 實例

const formRef = ref()
const form = reactive({
  machineId: '',
  maintenanceDescription: '',
  employeeId: '',
  scheduleDate: '', // 新增排程日期欄位
})

const rules = {
  machineId: [
    { required: true, message: '請輸入機台編號', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        const id = parseInt(value)
        if (isNaN(id) || id <= 0) {
          callback(new Error('機台編號必須是有效的正整數'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  maintenanceDescription: [
    { required: true, message: '請輸入保養描述', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value.length > 500) {
          callback(new Error('保養描述不能超過500字元'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  employeeId: [
    { required: true, message: '請輸入員工編號', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        const id = parseInt(value)
        if (isNaN(id) || id <= 0) {
          callback(new Error('員工編號必須是有效的正整數'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  scheduleDate: [
    { required: true, message: '請選擇排程日期', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!value) {
          callback(new Error('排程日期必須選擇'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const loading = ref(false)

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    loading.value = true
    // 1. 新增保養單
    const maintenanceRes = await api.post('/api/maintenance', {
      machineId: parseInt(form.machineId),
      maintenanceDescription: form.maintenanceDescription,
      scheduleDate: form.scheduleDate,
      employeeId: parseInt(form.employeeId),
      maintenanceStatus: 'WAIT_MAINTENANCE'
    })
    // 加入這行，確認回傳內容
    console.log('maintenanceRes:', maintenanceRes)
    console.log('maintenanceRes.data:', maintenanceRes.data)
    const maintenanceId = Number(maintenanceRes.data.scheduleId)
    console.log('maintenanceId:', maintenanceId) // 再確認一次 id

    // 2. 派工
    await api.post('/api/task-assignments', {
      taskType: '保養',
      taskId: maintenanceId,
      employeeId: form.employeeId,
      assignedTime: form.scheduleDate // 派工時間與排程時間一致
    })

    ElMessage.success('✅ 保養排程新增成功！')
    resetForm()
  } catch (error) {
    if (error.name === 'ValidationError') return

    console.error(error)
    if (error.response?.status === 401) {
      ElMessage.error('驗證已過期，請重新登入。')
    } else if (error.response?.status === 403) {
      ElMessage.error('您的權限不足。')
    } else {
      ElMessage.error('❌ 新增失敗，請稍後再試')
    }
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.machineId = ''
  form.maintenanceDescription = ''
  form.employeeId = ''
  form.scheduleDate = '' // 重設排程日期
}
</script>

<template>
  <el-card shadow="hover">
    <template #header>
      <span>🛠️ 新增保養排程</span>
    </template>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" @submit.prevent>
      <el-form-item label="機台編號" prop="machineId">
        <el-input v-model="form.machineId" placeholder="請輸入機台編號" />
      </el-form-item>

      <el-form-item label="保養描述" prop="maintenanceDescription">
        <el-input
          type="textarea"
          v-model="form.maintenanceDescription"
          placeholder="請輸入描述"
          :autosize="{ minRows: 3, maxRows: 6 }"
        />
      </el-form-item>

      <el-form-item label="員工編號" prop="employeeId">
        <el-input v-model="form.employeeId" placeholder="請輸入員工編號" />
      </el-form-item>

      <el-form-item label="排程日期" prop="scheduleDate">
        <el-date-picker
          v-model="form.scheduleDate"
          type="datetime"
          placeholder="請選擇排程日期"
          :clearable="true"
          :default-time="new Date()"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSubmit">送出</el-button>
        <el-button @click="resetForm">重設</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
.el-card,
.el-card * {
  font-size: 15px !important;
}
</style>
