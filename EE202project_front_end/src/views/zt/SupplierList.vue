<template>
  <el-card class="supplier-list-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold;">🏭 供應商列表</span>
    </template>

    <el-table :data="suppliers" stripe border>
      <el-table-column prop="supplierId" label="ID" width="80" />
      <el-table-column prop="supplierName" label="供應商名稱" />
      <el-table-column prop="pm" label="聯絡人" />
      <el-table-column prop="supplierPhone" label="電話" />
      <el-table-column prop="supplierEmail" label="Email" />
      <el-table-column prop="supplierAddress" label="地址" />
      <el-table-column prop="supplierNote" label="備註">
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button
            type="primary"
            icon="Edit"
            circle
            @click="$router.push(`/zt/supplier/edit/${row.supplierId}`)"
          />
          <el-button
            type="danger"
            icon="Delete"
            circle
            @click="deleteSupplier(row.supplierId)"
          />
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增按鈕 -->
    <div class="mt-4" style="text-align: right;">
      <el-button
        type="success"
        icon="Plus"
        @click="$router.push('/zt/supplier/add')"
      >
        新增供應商
      </el-button>
    </div>
  </el-card>
</template>


<script setup>
import { ref, onMounted } from 'vue'
import api from '@/services/api'


const suppliers = ref([])

const fetchSuppliers = async () => {
  const res = await api.get('/api/supplier/list')//會發送GET請求這要跟後端的@RequestMapping和@GetMapping要有所對應
  // console.log('回傳內容:', res.data)
  suppliers.value = res.data  // 篩選有名字的
}

const deleteSupplier = async (id) => {
  if (confirm('確定要下架這筆供應商資料嗎？')) {
    await api.delete(`/api/supplier/${id}`)
    await fetchSuppliers()
  }
}

onMounted(() => {
  fetchSuppliers()
})
</script>

<style scoped>
.supplier-list-card {
  margin: 20px;
}
.mt-4 {
  margin-top: 1.5rem;
}
</style>
