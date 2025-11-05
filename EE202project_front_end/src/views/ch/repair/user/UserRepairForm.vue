<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api' // ✅ 假設你有統一封裝的 axios 實例

const formRef = ref()
const form = reactive({
  employeeId: '',
  machineId: '',
  repairDescription: '',
})

const rules = {
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
  repairDescription: [
    { required: true, message: '請輸入維修描述', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value.length > 500) {
          callback(new Error('維修描述不能超過500字元'))
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
    await api.post('/api/repair', {
      employeeId: parseInt(form.employeeId),
      machineId: parseInt(form.machineId),
      repairDescription: form.repairDescription,
    })

    ElMessage.success('✅ 維修申請提交成功！')
    resetForm()
  } catch (error) {
    if (error.name === 'ValidationError') return

    console.error(error)
    if (error.response?.status === 401) {
      ElMessage.error('驗證已過期，請重新登入。')
    } else if (error.response?.status === 403) {
      ElMessage.error('您的權限不足。')
    } else {
      ElMessage.error('❌ 提交失敗，請稍後再試')
    }
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.employeeId = ''
  form.machineId = ''
  form.repairDescription = ''
}
</script>

<template>
  <el-card shadow="hover">
    <template #header>
      <span>🔧 機台報修申請</span>
    </template>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" @submit.prevent>
      <el-form-item label="員工編號" prop="employeeId">
        <el-input v-model="form.employeeId" placeholder="請輸入員工編號" />
      </el-form-item>

      <el-form-item label="機台編號" prop="machineId">
        <el-input v-model="form.machineId" placeholder="請輸入機台編號" />
      </el-form-item>

      <el-form-item label="維修描述" prop="repairDescription">
        <el-input
          type="textarea"
          v-model="form.repairDescription"
          placeholder="請描述故障狀況..."
          :autosize="{ minRows: 3, maxRows: 6 }"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSubmit">送出申請</el-button>
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
