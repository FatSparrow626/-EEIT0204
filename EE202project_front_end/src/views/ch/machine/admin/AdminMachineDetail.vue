<template>
  <el-card class="machine-detail-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold">🔍 機台詳情</span>
    </template>
    <el-form :model="machine" label-width="120px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="機台名稱">
            <el-input v-model="machine.machineName" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="出廠編號">
            <el-input v-model="machine.serialNumber" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="型號">
            <el-input v-model="machine.machineModel" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="品牌">
            <el-input v-model="machine.machineBrand" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="製造商">
            <el-input v-model="machine.machineManufacturer" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="購置日期">
            <el-date-picker v-model="machine.machinePurchaseDate" type="date" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="建議使用年限">
            <el-input v-model="machine.machineServiceLife" type="number" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="備註">
            <el-input v-model="machine.machineRemark" type="textarea" rows="2" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="運行狀態">
            <el-select v-model="machine.statusCode.statusCode" placeholder="請選擇狀態" style="width: 100%">
              <el-option
                v-for="opt in statusOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="機台位置">
            <el-input v-model="machine.machineLocation" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <div style="margin-top: 30px; display: flex; gap: 20px;">
      <el-button type="primary" @click="updateMachine">修改</el-button>
      <el-button type="danger" @click="deleteMachine">刪除</el-button>
      <el-button type="info" @click="emit('show-record')">維修保養紀錄</el-button>
      <el-button @click="handleBack">返回</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/services/api'
import { useRouter } from 'vue-router'

const props = defineProps({
  machineId: {
    type: Number,
    required: true,
  },
})

// 定義 emit
const emit = defineEmits(['back', 'machine-deleted'])

const machine = ref({
  statusCode: { statusCode: '' } // Initialize statusCode
});
const router = useRouter()

const statusOptions = ref([])

const fetchStatusOptions = async () => {
  const res = await api.get('/api/status-codes/machine')
  statusOptions.value = res.data.map(item => ({
    label: item.statusLabel,
    value: item.statusCode
  }))
}

const fetchMachine = async () => {
  try {
    const res = await api.get(`/api/machines/${props.machineId}`)
    machine.value = {
      ...res.data,
      statusCode: res.data.statusCode || { statusCode: '' }
    }
  } catch (error) {
    ElMessage.error('載入失敗')
  }
}

const updateMachine = async () => {
  try {
    await api.put(`/api/machines/${props.machineId}`, machine.value)
    ElMessage.success('修改成功')
    emit('back')
  } catch (error) {
    if (error.response && error.response.data) {
      ElMessage.error(error.response.data) // 顯示後端訊息
    } else {
      ElMessage.error('修改失敗')
    }
  }
}

const deleteMachine = async () => {
  try {
    await ElMessageBox.confirm('確定要刪除這台機台嗎？', '警告', { type: 'warning' })
    await api.delete(`/api/machines/${props.machineId}`)
    ElMessage.success('刪除成功')
    emit('machine-deleted')
    emit('back')
  } catch (error) {
    if (error.response && error.response.data) {
      ElMessage.error(error.response.data) // 顯示後端訊息
    } else if (error !== 'cancel') {
      ElMessage.error('刪除失敗')
    }
  }
}

// 維修保養紀錄仍使用 router（因為這是跳轉到其他頁面）
const goToRepairRecord = () => {
  router.push(`/machine/${props.machineId}/repair-record`)
}

// 改為 emit 事件而不是直接操作 router
const handleBack = () => {
  emit('back')
}

onMounted(() => {
  fetchMachine()
  fetchStatusOptions()
})
</script>

<style scoped>
/* 將整個 el-card 內文字基於原本字體再放大 25px */
.machine-detail-card,
.machine-detail-card * {
  font-size: 15px !important;
}
</style>