<template>
  <el-card class="add-order-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold;">📝 新增訂單</span>
    </template>

    <el-form :model="order" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="供應商">
            <el-select v-model="order.supplierId" placeholder="請選擇供應商" clearable required>
              <el-option
                v-for="s in suppliers"
                :key="s.supplierId"
                :label="s.supplierName"
                :value="s.supplierId"
              />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="訂單日期">
            <el-date-picker
              v-model="order.orderDate"
              type="date"
              placeholder="選擇日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              required
              style="width: 100%;"
            />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="狀態">
            <el-select v-model="order.orderStatus" placeholder="選擇狀態">
              <el-option label="待處理" value="PENDING" />
              <!-- <el-option label="已下單" value="ORDERED" /> -->
              <!-- <el-option label="已收貨" value="RECEIVED" /> -->
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <!-- 明細列表 -->
    <el-table :data="order.itemList" style="margin-top: 20px;">
      <el-table-column label="編號" type="index" width="60" />
      <el-table-column label="物料">
        <template #default="{ row }">
          <el-select v-model="row.materialId" placeholder="選擇物料" required>
            <el-option
              v-for="m in materials"
              :key="m.materialId"
              :label="m.materialName"
              :value="m.materialId"
            />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="數量">
        <template #default="{ row }">
          <el-input-number v-model="row.quantity" :min="1" />
        </template>
      </el-table-column>
      <el-table-column label="單價">
        <template #default="{ row }">
          <el-input-number v-model="row.unitPrice" :min="0" :step="1" />
        </template>
      </el-table-column>
      <el-table-column label="小計">
        <template #default="{ row }">
          {{ row.quantity * row.unitPrice }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ $index }">
          <el-button type="danger" icon="Delete" circle @click="removeItem($index)" />
        </template>
      </el-table-column>
    </el-table>

    <div class="mt-4" style="display: flex; justify-content: space-between; align-items: center; margin-top: 20px;">
      <div>
        <el-button type="primary" icon="Plus" @click="addItem">新增明細</el-button>
        <el-button type="success" icon="Check" @click="submitOrder">提交訂單</el-button>
      </div>
      <div style="font-size: 18px; font-weight: bold;">
        總金額：${{ totalAmount }}
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'


const router = useRouter()

const suppliers = ref([])
const materials = ref([])

const order = ref({
  supplierId: '',
  orderDate: '',
  orderStatus: 'PENDING',
  itemList: [{ materialId: '', quantity: 1, unitPrice: 0 }]
})

const fetchFormData = async () => {
  const res = await api.get('/api/order/addForm')
  suppliers.value = res.data.suppliers
  materials.value = res.data.materials
}

onMounted(fetchFormData)

const addItem = () => {
  order.value.itemList.push({ materialId: '', quantity: 1, unitPrice: 0 })
}

const removeItem = (index) => {
  order.value.itemList.splice(index, 1)
}

const totalAmount = computed(() =>
  order.value.itemList.reduce((sum, item) => sum + item.quantity * item.unitPrice, 0)
)

const submitOrder = async () => {
  const payload = {
    supplierId: order.value.supplierId,
    orderDate: order.value.orderDate,
    orderStatus: order.value.orderStatus,
    materialIds: order.value.itemList.map(i => String(i.materialId)),
    quantities: order.value.itemList.map(i => String(i.quantity)),
    unitPrices: order.value.itemList.map(i => String(i.unitPrice))
  }

  await api.post('/api/order/insert', payload)
  ElMessage.success('訂單新增成功！')
  router.push('/zt/order/list')
}
</script>

<style scoped>
.add-order-card {
  margin: 20px;
}
</style>
