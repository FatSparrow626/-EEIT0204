<template>
  <el-card class="edit-order-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold;">🛠 編輯訂單</span>
    </template>

    <el-form ref="orderFormRef" :model="order" :rules="rules" label-width="120px">
      <el-form-item label="供應商" prop="supplierId">
        <el-select v-model="order.supplierId" placeholder="請選擇供應商">
          <el-option
            v-for="s in suppliers"
            :key="s.supplierId"
            :label="s.supplierName"
            :value="s.supplierId"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="訂單日期" prop="orderDate">
        <el-date-picker
          v-model="order.orderDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="選擇日期"
          style="width: 100%;"
        />
      </el-form-item>

      <el-form-item label="狀態" prop="orderStatus">
        <el-select v-model="order.orderStatus" placeholder="選擇狀態">
          <el-option label="待處理" value="PENDING" />
          <!-- <el-option label="已下單" value="ORDERED" /> -->
          <!-- <el-option label="已收貨" value="RECEIVED" /> -->
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </el-form-item>

      <h3 style="margin: 20px 0 10px;">訂單明細</h3>
      <el-table :data="order.itemList" border stripe style="margin-bottom: 20px;">
        <el-table-column label="物料" min-width="200">
          <template #default="{ row }">
            <el-select v-model="row.materialId" placeholder="選擇物料">
              <el-option
                v-for="m in materials"
                :key="m.materialId"
                :label="m.materialName"
                :value="m.materialId"
              />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="數量" width="200">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" />
          </template>
        </el-table-column>

        <el-table-column label="單價" width="200">
          <template #default="{ row }">
            <el-input-number v-model="row.unitPrice" :min="0" :step="0.01" />
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
      <el-form-item label-width="0">
        <el-button type="primary" icon="Plus" @click="addItem" >新增物料</el-button>
        <el-button type="success" icon="Check" @click="updateOrder" :loading="submitting" :disabled="submitting">更新訂單</el-button>
        <el-button @click="$router.push('/zt/order/list')">取消</el-button>
      </el-form-item>
       </div>
    </el-form>

  </el-card>
</template>


<script setup>
import{ ref,onMounted,watch } from 'vue'
import { useRoute, useRouter } from 'vue-router';
import { ElMessage,ElMessageBox } from 'element-plus';
import api from '@/services/api'

const route = useRoute()
const router = useRouter()
const orderFormRef = ref()
const order = ref({
  orderId : null,
  supplierId : null,
  orderDate : '',
  orderStatus :'PENDING',
  itemList : []
})
const suppliers = ref([])
const materials = ref([])
const submitting = ref(false)

const rules ={
  supplierId : [{ required:true, message:"請選擇供應商",trigger:'change'}],
  orderDate : [{ required:true, message:"請選擇日期",trigger:'change'}] ,
  orderStatus : [{ required:true, message:"請選擇狀態",trigger:'change'}]
}
//載入初始資料
const fetchOrders = async ()=>{
  const id = route.params.orderId
  const res =  await api.get(`/api/order/edit/${id}`)
  const data = res.data

  order.value={
    orderId : data.order.orderId,
    supplierId: data.supplier?.supplierId || '',
    orderDate : data.orderDate,
    orderStatus : data.orderStatus,
    itemList : data.items
  }
  suppliers.value = data.suppliers
  materials.value = data.materials
}
//更新訂單資料
const updateOrder = async ()=>{
  const valid = await orderFormRef.value.validate()
    if (!valid) return
    try{
      await ElMessageBox.confirm('確定要更新這筆訂單嗎?','確認',{
        type : 'warning',
        confirmButtonText : '確認',
        cancelButtonText : '取消',
      })
    }catch{
      return //使用者取消
    }

    submitting.value = true

    const payload = {
     orderId : order.value.orderId,
     supplierId: order.value.supplierId,
     orderDate : order.value.orderDate,
     orderStatus : order.value.orderStatus,
     materialIds: order.value.itemList.map(i => String(i.materialId)),
     quantities: order.value.itemList.map(i => String(i.quantity)),
     unitPrices: order.value.itemList.map(i => String(i.unitPrice))
     }
     await api.put('/api/order/update',payload,{
      headers :{'Content-Type':'application/json'}
     })
     ElMessage.success({
      message:'更新成功',
      showClose: true,
      duration:1500,
     })
     router.push('/zt/order/list')
}
const addItem = () => {
  order.value.itemList.push({ materialId: '', quantity: 1, unitPrice: 0 })
}

onMounted(fetchOrders)
watch(() => route.fullPath, () => {
  fetchOrders()
})
</script>

<style scoped>
.edit-order-card {
  margin: 20px;
}
.pagination-container{
  display: flex;
  justify-content: flex-start;
  margin-top: 20px;
}
</style>
