<template>
  <el-card class="machine-detail-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold">🔍 機台詳情</span>
    </template>
    <el-form :model="machine" label-width="120px" disabled>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="機台名稱">
            <el-input v-model="machine.machineName" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="出廠編號">
            <el-input v-model="machine.serialNumber" disabled />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="型號">
            <el-input v-model="machine.machineModel" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="品牌">
            <el-input v-model="machine.machineBrand" disabled />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="製造商">
            <el-input v-model="machine.machineManufacturer" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="購置日期">
            <el-date-picker v-model="machine.machinePurchaseDate" type="date" style="width: 100%" disabled />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="建議使用年限">
            <el-input v-model="machine.machineServiceLife" type="number" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="備註">
            <el-input v-model="machine.machineRemark" type="textarea" rows="2" disabled />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="運行狀態">
            <el-input :model-value="statusLabelMap[machine.statusCode?.statusCode] || machine.statusCode?.statusCode || '未知狀態'" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="機台位置">
            <el-input v-model="machine.machineLocation" disabled />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <div style="margin-top: 30px; display: flex; justify-content: flex-end;">
      <el-button type="default" @click="handleBack">返回</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'

// 新增 emit
const emit = defineEmits(['back'])

const props = defineProps({
  machineId: {
    type: Number,
    required: true,
  },
})

const machine = ref({})
const router = useRouter()
const statusLabelMap = ref({})

const fetchMachine = async () => {
  try {
    const res = await api.get(`/api/machines/${props.machineId}`)
    machine.value = res.data
  } catch (error) {
    // 可加提示
  }
}

const fetchStatusOptions = async () => {
  const res = await api.get('/api/status-codes/machine')
  statusLabelMap.value = Object.fromEntries(res.data.map(item => [item.statusCode, item.statusLabel]))
}

// 新增返回方法
const handleBack = () => {
  emit('back')
}

onMounted(() => {
  fetchMachine()
  fetchStatusOptions()
})
</script>

<style scoped>
.machine-detail-card,
.machine-detail-card * {
  font-size: 15px !important;
}
</style>