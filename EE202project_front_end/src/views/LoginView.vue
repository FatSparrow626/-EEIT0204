<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>登入</span>
        </div>
      </template>
      <el-form @submit.prevent="handleLogin">
        <el-form-item label="使用者名稱">
          <el-input v-model="username" autocomplete="username"></el-input>
        </el-form-item>
        <el-form-item label="密碼">
          <el-input type="password" v-model="password" autocomplete="current-password"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" class="login-button">登入</el-button>
        </el-form-item>
        <el-form-item>
          <el-button
            type="text"
            @click="showForgotPasswordDialog = true"
            class="forgot-password-button"
          >
            忘記密碼？
          </el-button>
        </el-form-item>
      </el-form>
      <div v-if="message" class="error-message">{{ message }}</div>
    </el-card>

    <!-- 忘記密碼模態視窗 -->
    <el-dialog
      v-model="showForgotPasswordDialog"
      title="忘記密碼"
      width="500"
      @close="resetForgotPasswordForm"
    >

      <!-- 一鍵 -->
      <div class="demo-section" style="margin-bottom: 16px">
        <div style="display: flex; justify-content: flex-end">
          <el-button type="primary" size="small" @click="quickFillForgotPasswordForm" plain>
            一鍵
          </el-button>
        </div>
        <el-divider style="margin: 12px 0" />
      </div>

      <!-- 以上 -->

      <el-form
        :model="forgotPasswordForm"
        :rules="forgotPasswordRules"
        ref="forgotPasswordFormRef"
        label-width="120px"
      >
        <el-form-item label="員工編號" prop="employeeNumber">
          <el-input
            v-model="forgotPasswordForm.employeeNumber"
            placeholder="請輸入員工編號"
          ></el-input>
        </el-form-item>
        <el-form-item label="電子信箱" prop="email">
          <el-input
            v-model="forgotPasswordForm.email"
            type="email"
            placeholder="請輸入電子信箱"
          ></el-input>
        </el-form-item>
        <el-form-item label="入職日期" prop="hireDate">
          <el-date-picker
            v-model="forgotPasswordForm.hireDate"
            type="date"
            placeholder="請選擇入職日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="新密碼" prop="newPassword">
          <el-input
            v-model="forgotPasswordForm.newPassword"
            type="password"
            placeholder="請輸入新密碼"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="確認密碼" prop="confirmPassword">
          <el-input
            v-model="forgotPasswordForm.confirmPassword"
            type="password"
            placeholder="請再次輸入密碼"
            show-password
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showForgotPasswordDialog = false">取消</el-button>
          <el-button type="primary" @click="handleResetPassword" :loading="resettingPassword">
            重設密碼
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/AuthStore'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import http from '../http-common'

const username = ref('')
const password = ref('')
const message = ref('')
const authStore = useAuthStore()
const router = useRouter()

// 一鍵
const quickFillForgotPasswordForm = () => {
  forgotPasswordForm.employeeNumber = 'DEMO001'
  forgotPasswordForm.email = 'demo@company.com'
  forgotPasswordForm.hireDate = '2023-01-15'
  forgotPasswordForm.newPassword = 'newDemo123'
  forgotPasswordForm.confirmPassword = 'newDemo123'
}
//以上

// 忘記密碼相關變數
const showForgotPasswordDialog = ref(false)
const resettingPassword = ref(false)
const forgotPasswordFormRef = ref<FormInstance>()

interface ForgotPasswordForm {
  employeeNumber: string
  email: string
  hireDate: string
  newPassword: string
  confirmPassword: string
}

const forgotPasswordForm = reactive<ForgotPasswordForm>({
  employeeNumber: '',
  email: '',
  hireDate: '',
  newPassword: '',
  confirmPassword: '',
})

// 密碼驗證函數
const validatePassword = (rule: any, value: any, callback: any) => {
  if (!value) {
    callback(new Error('請輸入密碼'))
    return
  }

  // 密碼長度至少6位
  if (value.length < 6) {
    callback(new Error('密碼長度至少6位'))
    return
  }

  // 密碼必須包含字母和數字
  const hasLetter = /[A-Za-z]/.test(value)
  const hasNumber = /\d/.test(value)
  if (!hasLetter || !hasNumber) {
    callback(new Error('密碼必須包含字母和數字'))
    return
  }

  callback()
}

// 確認密碼驗證函數
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (!value) {
    callback(new Error('請再次輸入密碼'))
    return
  }

  if (value !== forgotPasswordForm.newPassword) {
    callback(new Error('兩次輸入的密碼不一致'))
    return
  }

  callback()
}

// 忘記密碼表單驗證規則
const forgotPasswordRules = reactive<FormRules>({
  employeeNumber: [{ required: true, message: '請輸入員工編號', trigger: 'blur' }],
  email: [
    { required: true, message: '請輸入電子信箱', trigger: 'blur' },
    { type: 'email', message: '請輸入正確的電子信箱格式', trigger: ['blur', 'change'] },
  ],
  hireDate: [{ required: true, message: '請選擇入職日期', trigger: 'change' }],
  newPassword: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
})

const handleLogin = async () => {
  message.value = ''
  try {
    await authStore.login({ username: username.value, password: password.value })
    router.push('/') // Redirect to home or dashboard after successful login
  } catch (error: any) {
    message.value =
      (error.response && error.response.data && error.response.data.message) ||
      error.message ||
      error.toString()
    ElMessage.error(message.value)
  }
}

// 重設表單
const resetForgotPasswordForm = () => {
  if (forgotPasswordFormRef.value) {
    forgotPasswordFormRef.value.resetFields()
  }
  forgotPasswordForm.employeeNumber = ''
  forgotPasswordForm.email = ''
  forgotPasswordForm.hireDate = ''
  forgotPasswordForm.newPassword = ''
  forgotPasswordForm.confirmPassword = ''
}

// 處理密碼重設
const handleResetPassword = async () => {
  if (!forgotPasswordFormRef.value) return

  await forgotPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      resettingPassword.value = true
      try {
        const payload = {
          employeeNumber: forgotPasswordForm.employeeNumber,
          email: forgotPasswordForm.email,
          hireDate: forgotPasswordForm.hireDate,
          newPassword: forgotPasswordForm.newPassword,
        }

        await http.post('/employee-users/reset-password', payload)

        ElMessage.success('密碼重設成功！請使用新密碼登入')
        showForgotPasswordDialog.value = false
        resetForgotPasswordForm()
      } catch (error: any) {
        console.error('Password reset error:', error)

        let errorMessage = '密碼重設失敗'

        if (error.response) {
          switch (error.response.status) {
            case 404:
              errorMessage = '找不到符合條件的員工資料，請確認輸入的資訊是否正確'
              break
            case 400:
              errorMessage = error.response.data?.message || '輸入的資料格式不正確'
              break
            case 403:
              errorMessage = '身份驗證失敗，請確認您的資料'
              break
            case 500:
              errorMessage = '伺服器錯誤，請稍後再試'
              break
            default:
              errorMessage = error.response.data?.message || '未知錯誤，請聯繫管理員'
          }
        } else if (error.request) {
          errorMessage = '網路連線錯誤，請檢查網路連線'
        } else {
          errorMessage = error.message || '未知錯誤'
        }

        ElMessage.error(errorMessage)
      } finally {
        resettingPassword.value = false
      }
    } else {
      ElMessage.error('請正確填寫所有必填欄位')
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.login-card {
  width: 400px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.card-header {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
}

.error-message {
  color: red;
  text-align: center;
  margin-top: 10px;
}

.login-button {
  width: 100%;
}

.forgot-password-button {
  width: 100%;
  text-align: center;
  color: #409eff;
  font-size: 14px;
}

.forgot-password-button:hover {
  color: #66b1ff;
}

.dialog-footer {
  text-align: right;
}

.dialog-footer .el-button {
  margin-left: 10px;
}
</style>
