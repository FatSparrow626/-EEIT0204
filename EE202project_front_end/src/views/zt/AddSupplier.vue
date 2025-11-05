<template>
  <el-card class="add-supplier-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold">➕ 新增供應商</span>
    </template>

    <el-form ref="formRef" :model="form" label-width="120px">
      <el-form-item label="供應商名稱" prop="supplierName">
        <el-input v-model="form.supplierName" />
      </el-form-item>

      <el-form-item label="聯絡人">
        <el-input v-model="form.pm" />
      </el-form-item>

      <el-form-item label="電話">
        <el-input v-model="form.supplierPhone" />
      </el-form-item>

      <el-form-item label="Email">
        <el-input v-model="form.supplierEmail" type="email" />
      </el-form-item>

      <el-form-item label="地址">
        <el-input v-model="form.supplierAddress" />
      </el-form-item>

      <el-form-item label="備註">
        <el-input v-model="form.supplierNote" />
      </el-form-item>

      <el-form-item>
        <el-button type="warning" :icon="MagicStick" @click="fillDemo" :disabled="submitting"
          >一鍵填入</el-button
        >
        <el-button
          type="primary"
          :icon="Check"
          @click="submitForm"
          :loading="submitting"
          :disabled="submitting"
          >送出</el-button
        >
        <el-button @click="$router.push('/zt/supplier/list')">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, MagicStick } from '@element-plus/icons-vue'
const router = useRouter()
const formRef = ref()
const submitting = ref(false)

const form = ref({
  supplierName: '',
  pm: '',
  supplierPhone: '',
  supplierEmail: '',
  supplierAddress: '',
  supplierNote: '',
})

const fillDemo = () => {
  Object.assign(form.value, {
    supplierName: '台達電子工業股份有限公司',
    pm: '林小姐',
    supplierPhone: '886-2-8797-2088',
    supplierEmail: '2308@deltaww.com',
    supplierAddress: '台北市內湖區瑞光路186號',
    supplierNote: '展示用範例資料',
  })
  ElMessage.success('已填入範例資料')
}
const submitForm = async () => {
  // 驗證
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
  } catch {
    return
  }

  // 二次確認
  try {
    await ElMessageBox.confirm('確定要新增這個供應商嗎？', '確認', {
      type: 'warning',
      confirmButtonText: '確認',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  // 送出
  submitting.value = true
  try {
    await api.post('/api/supplier/add', form.value, {
      headers: { 'Content-Type': 'application/json' },
    })
    ElMessage.success({ message: '新增成功', showClose: true, duration: 1500 })
    router.push('/zt/supplier/list')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.add-supplier-card {
  margin: 20px;
}
</style>
