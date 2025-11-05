<template>
  <el-card class="machine-list-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold">📋 機台列表</span>
    </template>

    <el-table v-loading="loading" :data="machineList" stripe border style="width: 100%">
      <el-table-column prop="machineId" label="機台ID" width="100" />
      <el-table-column prop="machineName" label="機台名稱" />
      <el-table-column prop="serialNumber" label="出廠編號">
        <template #default="{ row }">
          <el-tag type="info">{{ row.serialNumber }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="mstatus" label="運行狀態">
        <template #default="{ row }">
          <span>
            {{ statusLabelMap[row.statusCode?.statusCode] || row.statusCode?.statusCode || '未知狀態' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="machineLocation" label="機台位置">
        <template #default="{ row }">📍 {{ row.machineLocation }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button type="primary" @click="goToDetail(row.machineId)">詳情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && machineList.length === 0" description="📂 沒有機台資料" />
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '@/services/api'

// 定義 emit
const emit = defineEmits(['show-detail'])

const machineList = ref([])
const loading = ref(false)
const statusLabelMap = ref({})

const fetchMachines = async () => {
  loading.value = true
  try {
    // 如需權限可加 token header
    const userJson = localStorage.getItem('user')
    const user = userJson ? JSON.parse(userJson) : null
    const token = user?.token
    const res = await api.get('/api/machines', {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
    machineList.value = res.data || []
  } catch (error) {
    machineList.value = []
  } finally {
    loading.value = false
  }
}

const fetchStatusOptions = async () => {
  const res = await api.get('/api/status-codes/machine')
  statusLabelMap.value = Object.fromEntries(res.data.map(item => [item.statusCode, item.statusLabel]))
}

const goToDetail = (id) => {
  emit('show-detail', id)
}

onMounted(() => {
  fetchMachines()
  fetchStatusOptions()
})
</script>

<style scoped>
.machine-list-card,
.machine-list-card * {
  font-size: 15px !important;
}
</style>
