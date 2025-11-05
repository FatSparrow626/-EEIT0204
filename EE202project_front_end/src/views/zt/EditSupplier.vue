<template>
  <el-card class="edit-supplier-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold;">✏️ 編輯供應商</span>
    </template>

    <el-form ref="formRef" :model="supplier" label-width="120px" status-icon>
      <el-form-item label="供應商名稱" prop="supplierName">
        <el-input v-model="supplier.supplierName" required />
      </el-form-item>

      <el-form-item label="聯絡人 (PM)">
        <el-input v-model="supplier.pm" />
      </el-form-item>

      <el-form-item label="電話">
        <el-input v-model="supplier.supplierPhone" />
      </el-form-item>

      <el-form-item label="Email">
        <el-input v-model="supplier.supplierEmail" type="email" />
      </el-form-item>

      <el-form-item label="地址">
        <el-input v-model="supplier.supplierAddress" />
      </el-form-item>

      <el-form-item label="備註">
        <el-input v-model="supplier.supplierNote" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :icon="Check" @click="updateSupplier" :loading="submitting" :disabled="submitting">
          儲存變更
        </el-button>
        <el-button @click="$router.push('/zt/supplier/list')">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>



<script setup>
import {ref, onMounted} from 'vue'
import { useRoute, useRouter } from 'vue-router';
import { ElMessage,ElMessageBox } from 'element-plus';
import { Check } from '@element-plus/icons-vue'

import api from '@/services/api'
const route = useRoute() //可以抓動態參數ex:route.params.動態參數 和index.js的path相呼應
const router = useRouter() //類似遙控器主要是控制路由跳轉,所以也可以用來切換頁面router.put('路徑')
const formRef = ref()
const submitting = ref(false)
const supplier = ref({
 supplierId : null,
 pm : '',
 supplierPhone : '',
 supplierEmail : '',
 supplierAddress : '',
 supplierNote : ''
})

//去抓後端資料
const fetchSuppliers = async ()=>{
  //去拿我目前網址的id 透過route.params.id 而這邊的id可以抓是因為index.js 的path 使用:id
  const res = await api.get(`/api/supplier/${route.params.id}`)
  supplier.value = res.data
}

const updateSupplier = async ()=>{
  try{
  const valid = await formRef.value.validate()
    if (!valid) return
  }catch{
    return
  }

  try{
    await ElMessageBox.confirm('確定要更新這筆資料嗎?','確認',{
        type : 'warning',
        confirmButtonText : '確認',
        cancelButtonText : '取消',
    })
  }catch{
    return
  }
  submitting.value = true
  try{
       await api.put('/api/supplier/update',supplier.value,{
      headers :{'Content-Type':'application/json'}
     })
     ElMessage.success({
      message:'更新成功',
      showClose: true,
      duration:1500,
     })
     router.push('/zt/supplier/list')
    }finally{
      submitting.value = false
    }
}

onMounted(()=>{
  fetchSuppliers()
})
</script>

<style scoped>
.edit-supplier-card {
  margin: 20px;
}
</style>
