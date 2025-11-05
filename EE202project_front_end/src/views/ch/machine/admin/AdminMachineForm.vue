<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'

const formRef = ref(null)
const loading = ref(false)

// 表單資料
const form = ref({
  machineName: '',
  serialNumber: '',
  statusCode: { statusCode: '' }, // Initialize statusCode as an object
  
  machineModel: '',
  machineBrand: '',
  machineManufacturer: '',
  machinePurchaseDate: '',
  machineServiceLife: '',
  machineRemark: '',
})
const testData = ({
   machineName: '黃光機', 
  serialNumber: 'EXP-001',  
  machineLocation: '北廠', 
   machineModel: 'EX-5000', 
   machineBrand: 'PCB-Tech', 
    machineManufacturer: 'PCB Corp.', 
     machineServiceLife: 8, 
      machineRemark: '曝光機，用於光阻圖案轉印。', 
     })
     const fillTestData = () => { Object.assign(form.value, testData) }

// 狀態選項
const statusOptions = ref([])

// 驗證規則
const rules = {
  machineName: [{ required: true, message: '機台名稱不能為空', trigger: 'blur' }],
  serialNumber: [{ required: true, message: '出廠編號不能為空', trigger: 'blur' }],
  statusCode: [{ required: true, message: '請選擇運行狀態', trigger: 'change' }],
  machineLocation: [{ required: true, message: '機台位置不能為空', trigger: 'blur' }],
  machineModel: [{ required: false, message: '型號不能為空', trigger: 'blur' }],
  machineBrand: [{ required: false, message: '品牌不能為空', trigger: 'blur' }],
  machineManufacturer: [{ required: false, message: '製造商不能為空', trigger: 'blur' }],
  machinePurchaseDate: [{ required: false, message: '購置日期不能為空', trigger: 'blur' }],
  machineServiceLife: [{ required: false, message: '建議使用年限不能為空', trigger: 'blur' }],
  machineRemark: [{ required: false, message: '備註不能為空', trigger: 'blur' }],
}

// 提交表單
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const userJson = localStorage.getItem('user')
        const user = userJson ? JSON.parse(userJson) : null
        const token = user?.token
        if (!token) throw new Error('未找到驗證權杖，請先登入。')

        await api.post('/api/machines', form.value, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })

        ElMessage.success('✅ 機台新增成功！')
        form.value = {
          machineName: '',
          serialNumber: '',
          statusCode: { statusCode: '' },
          machineLocation: '',
          machineModel: '',
          machineBrand: '',
          machineManufacturer: '',
          machinePurchaseDate: '',
          machineServiceLife: '',
          machineRemark: '',
        }
      } catch (error) {
        if (error.response && error.response.data) {
          ElMessage.error(error.response.data) // 顯示後端訊息
        } else {
          ElMessage.error('新增失敗')
        }
      } finally {
        loading.value = false
      }
    } else {
      ElMessage.warning('請檢查表單必填欄位。')
      return false
    }
  })
}

const goBack = () => {
  window.history.back()
}

const fetchStatusOptions = async () => {
  const res = await api.get('/api/status-codes/machine')
  statusOptions.value = res.data.map(item => ({
    label: item.statusLabel,
    value: item.statusCode
  }))
}

onMounted(() => {
  fetchStatusOptions()
})
</script>

<template>
  <el-card class="leave-application-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold">🛠️ 新增機台</span>
    </template>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="機台名稱" prop="machineName">
            <el-input v-model="form.machineName" placeholder="輸入機台名稱" :disabled="loading" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="出廠編號" prop="serialNumber">
            <el-input v-model="form.serialNumber" placeholder="輸入出廠編號" :disabled="loading" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="型號" prop="machineModel">
            <el-input v-model="form.machineModel" placeholder="輸入型號" :disabled="loading" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="品牌" prop="machineBrand">
            <el-input v-model="form.machineBrand" placeholder="輸入品牌" :disabled="loading" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="製造商" prop="machineManufacturer">
            <el-input v-model="form.machineManufacturer" placeholder="輸入製造商" :disabled="loading" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="購置日期" prop="machinePurchaseDate">
            <el-date-picker
              v-model="form.machinePurchaseDate"
              type="date"
              placeholder="選擇購置日期"
              style="width: 100%"
              :disabled="loading"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="建議使用年限" prop="machineServiceLife">
            <el-input
              v-model="form.machineServiceLife"
              type="number"
              placeholder="輸入建議使用年限（年）"
              :disabled="loading"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="備註" prop="machineRemark">
            <el-input
              v-model="form.machineRemark"
              type="textarea"
              placeholder="輸入備註"
              :disabled="loading"
              rows="2"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="運行狀態" prop="statusCode.statusCode">
            <el-select
              v-model="form.statusCode.statusCode"
              placeholder="請選擇運行狀態"
              :disabled="loading"
              style="width: 100%"
            >
              <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="機台位置" prop="machineLocation">
            <el-input v-model="form.machineLocation" placeholder="輸入機台位置" :disabled="loading" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item>
        <el-button type="primary" icon="Check" @click="submitForm" :loading="loading">
          新增資料
        </el-button>
        <el-button type="info" @click="fillTestData"> 一鍵填入測試資料 </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
.leave-application-card {
  max-width: 900px;
  margin: 0 auto;
}

/* 將整個 el-card 內文字基於原本字體再放大 25px */
.leave-application-card,
.leave-application-card * {
  font-size: 15px !important;
}
</style>
