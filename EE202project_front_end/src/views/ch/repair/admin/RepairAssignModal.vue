<template>
  <el-dialog :model-value="visible" title="派工" width="400px" @close="onClose">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="派工時間" prop="repairAssignTime">
        <el-date-picker 
          v-model="form.repairAssignTime" 
          type="datetime" 
          placeholder="選擇派工時間" 
          style="width: 100%"
          value-format="YYYY-MM-DD HH:mm:ss"
          format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="維修人員編號" prop="repairEmployeeId">
        <el-input v-model="form.repairEmployeeId" placeholder="請輸入維修人員編號" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="onClose">取消</el-button>
      <el-button type="primary" @click="handleAssign">派工</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import api from '@/services/api'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: Boolean,
  repair: Object
})
const emit = defineEmits(['close', 'assigned'])

const formRef = ref()
const form = ref({
  repairAssignTime: '',
  repairEmployeeId: ''
})

const rules = {
  repairAssignTime: [
    { required: true, message: '請選擇派工時間', trigger: 'change' }
  ],
  repairEmployeeId: [
    { required: true, message: '請輸入維修人員編號', trigger: 'blur' }
  ]
}

watch(() => props.visible, (val) => {
  if (val && props.repair) {
    const now = new Date()
    const isoString = now.toISOString().slice(0, 19)
    
    form.value = {
      repairAssignTime: isoString.replace('T', ' '),
      repairEmployeeId: ''
    }
  }
})

const formatLocalDateTime = (dateTime) => {
  if (!dateTime) return null
  
  if (typeof dateTime === 'string' && dateTime.includes('T')) {
    return dateTime
  }
  
  if (typeof dateTime === 'string') {
    return dateTime.replace(' ', 'T')
  }
  
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

const handleAssign = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();

    if (!props.repair?.repairId) {
      ElMessage.error('維修記錄ID無效');
      return;
    }

    if (!form.value.repairEmployeeId) {
      ElMessage.error('請輸入維修人員編號');
      return;
    }

    if (!form.value.repairAssignTime) {
      ElMessage.error('請選擇派工時間');
      return;
    }

    const assignData = {
      taskType: '維修',
      taskId: parseInt(props.repair.repairId),
      employeeId: parseInt(form.value.repairEmployeeId),
      assignedTime: formatLocalDateTime(form.value.repairAssignTime),
    };

    console.log('準備傳送的派工資料：', assignData);

    // 呼叫派工 API
    const response1 = await api.post('/api/task-assignments', assignData);
    console.log('派工 API 回應：', response1.data);

    // 傳遞完整的派工資料給父元件
    emit('assigned', {
      employeeId: parseInt(form.value.repairEmployeeId),
      repairAssignTime: formatLocalDateTime(form.value.repairAssignTime),
    });

    ElMessage.success('派工成功');
  } catch (error) {
    console.error('派工錯誤詳細資訊：', error);

    if (error.response) {
      const errorMessage =
        error.response.data?.message ||
        error.response.data?.error ||
        `伺服器錯誤 (${error.response.status})`;
      ElMessage.error(`操作失敗：${errorMessage}`);
    } else if (error.request) {
      ElMessage.error('操作失敗：無法連接到伺服器');
    } else {
      ElMessage.error(`操作失敗：${error.message}`);
    }
  }
};

const onClose = () => {
  emit('close')
}
</script>

<style scoped>
.el-dialog,
.el-dialog * {
  font-size: 15px !important;
}
</style>