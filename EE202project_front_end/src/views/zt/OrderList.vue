<template>
  <el-card shadow="hover" class="mb-4">
    <template #header>
      <div class="card-header">
        <span>📋 訂單列表</span>
        <el-button type="primary"  @click="exportCsv" :loading="exporting" plain>匯出CSV</el-button>
      </div>

    </template>


    <el-row :gutter="20" class="mb-3">
      <el-col :span="8">
        <el-input
          v-model="searchText"
          placeholder="搜尋供應商或狀態"
          clearable
          @input="filterOrders"
        />
      </el-col>
      <el-col :span="8">
        <el-date-picker
          v-model="startDate"
          type="date"
          placeholder="開始日期"
          style="width: 100%"
          @change="filterOrders"
        />
      </el-col>
      <el-col :span="8">
        <el-date-picker
          v-model="endDate"
          type="date"
          placeholder="結束日期"
          style="width: 100%"
          @change="filterOrders"
        />
      </el-col>
    </el-row>
    <!--訂單+訂單明細-->
    <el-table :data="pagedOrders" stripe style="width: 100%">
        <el-table-column type=expand>
          <template #default="{ row }">
          <el-table :data="row.itemList" border style="width: 100%">
          <el-table-column prop="materialName" label="物料名稱" />
          <el-table-column prop="quantity" label="數量" />
          <el-table-column prop="unitPrice" label="單價" />
          <el-table-column prop="receivedQuantity" label="已收貨數量"></el-table-column>
          <el-table-column prop="deliveryStatus" label="狀態">
            <template #default="{ row }">
              <el-tag :type="getOrderStatusTagType(row.deliveryStatus)">
                {{ translateStatus(row.deliveryStatus) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        </template>
    </el-table-column>

      <el-table-column prop="orderId" label="訂單編號" width="100" />
      <el-table-column prop="supplier.supplierName" label="供應商" />
      <el-table-column prop="orderDate" label="訂單日期" />
      <el-table-column prop="orderStatus" label="狀態">
        <template #default="{ row }">
          <el-tag :type="getOrderStatusTagType(row.orderStatus)" effect="light">
          {{ translateStatus(row.orderStatus) }}
          </el-tag>
        </template>
      </el-table-column>

      <!--千分位-->
      <el-table-column prop="subTotal" label="小計">
        <template #default="{ row }">
        {{ formatNumber(row.subTotal) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="primary" icon="Edit" circle @click="editOrder(row)" />
          <el-button type="danger" icon="Delete" circle @click="deleteOrder(row.orderId)" />
        </template>
      </el-table-column>
    </el-table>

    <!--分頁功能-->
    <div class="pagination-container">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredOrders.length"
        :page-size="pageSize"
        :current-page="currentPage"
        @size-change= "handleSizeChange"
        @current-change= "handlePageChange"
        :page-sizes="[5, 10, 20, 50]"
        style="margin-top: 20px; text-align: right;"/>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/services/api'

const router = useRouter()
const orders = ref([])
const searchText = ref('')
const startDate = ref('')
const endDate = ref('')
const currentPage = ref(1)
const pageSize = ref(5)
const filterOrders = () => {
  resetPage()
}
const exporting = ref(false)


const fetchOrders = async () => {
  const res = await api.get('/api/order/list')
  orders.value = res.data
}
//千分位
const formatNumber = ((value)=>{
  if(typeof value === 'number'){
    return value.toLocaleString('zh-TW',{ style: 'currency', currency: 'TWD' }) //美式格式也等於千分位格式  貨幣使用
  }
  return value
})

onMounted(fetchOrders)

const translateStatus = (status) => {
  switch (status) {
    case 'PENDING': return '待處理'
    case 'RECEIVED': return '已收貨'
    case 'COMPLETED': return '已完成'
    case 'PARTIALLY_RECEIVED': return '部分到貨'
    case 'CANCELLED': return '已取消'
    case '出貨中': return '出貨中'
    case '部分到貨': return '部分到貨'
    case '已到貨': return '已到貨'
    default: return status
  }
}

const getOrderStatusTagType =(status)=>{
  switch (status) {
   case 'PENDING': return 'warning'
   case 'RECEIVED': return 'success'
   case 'COMPLETED': return 'success'
   case 'PARTIALLY_RECEIVED': return 'warning'
   case 'CANCELLED': return 'danger'
   case '出貨中': return 'info' // 預設顏色，非灰階
   case '部分到貨': return 'warning'
   case '已到貨': return 'success'
   default: return ''
  }
}

const filteredOrders = computed(() => {
  return orders.value.filter(order => {
    const keyword = searchText.value.toLowerCase()
    const matchText = order.supplier?.supplierName?.toLowerCase().includes(keyword) || order.orderStatus?.toLowerCase().includes(keyword)
    const date = new Date(order.orderDate)
    const startOK = !startDate.value || date >= new Date(startDate.value)
    const endOK = !endDate.value || date <= new Date(endDate.value)
    return matchText && startOK && endOK
  })
})
//分頁
const pagedOrders = computed( ()=>{
  const start = (currentPage.value -1)* pageSize.value
  const end = start + pageSize.value
  return filteredOrders.value.slice(start,end)
})
const resetPage = ()=>{
  currentPage.value = 1
}
const handleSizeChange = (val)=>{
  pageSize.value = val
  currentPage.value =1
}

const handlePageChange = (val)=>{
  currentPage.value=val
}

//刪除+訊息框
const deleteOrder = async (id) => {
  try{
      await ElMessageBox.confirm(
      `確定刪除訂單編號: ${id} 嗎?`,
    { confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  //點擊確定
    await api.delete(`/api/order/delete/${id}`)
    await fetchOrders()
    ElMessage.success('刪除成功')
  } catch(error){
    ElMessage.error('請先刪除關聯入庫單')
  }
}

const editOrder = (row) => {
  router.push(`/zt/order/edit/${row.orderId}`)
}

//CSV
async function exportCsv() {
  try {
    exporting.value = true
    const res = await api.get('/api/order/export/csv', { responseType: 'blob' })
    const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8;' })

    // 建立下載檔名（可從 header 取，也可自訂）
    const now = new Date()
    const pad = (n) => n.toString().padStart(2, '0')
    const filename = `orders_${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}.csv`

    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = filename
    document.body.appendChild(link)
    link.click()
    URL.revokeObjectURL(link.href)
    link.remove()
    ElMessage.success('匯出成功')
  } catch (error) {
    ElMessage.error('匯出失敗')
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  font-size: 20px;
  font-weight: bold;
}
.mb-3 {
  margin-bottom: 1rem;
}
.mt-4 {
  margin-top: 1.5rem;
}
.pagination-container{
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
