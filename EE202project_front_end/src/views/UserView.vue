<template>
  <div class="user-view">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>員工使用者列表</span>
          <div class="header-actions">
            <el-input
              v-model="searchText"
              placeholder="搜尋員工姓名或編號"
              prefix-icon="Search"
              style="width: 250px; margin-right: 12px"
              clearable
            />
            <el-select
              v-model="departmentFilter"
              placeholder="篩選部門"
              clearable
              style="width: 150px; margin-right: 12px"
            >
              <el-option label="全部部門" value="" />
              <el-option
                v-for="dept in availableDepartments"
                :key="dept.departmentId"
                :label="dept.description || dept.departmentName"
                :value="dept.description || dept.departmentName"
              />
            </el-select>
            <el-button type="primary" @click="openAddUserDialog">
              <el-icon><Plus /></el-icon>
              新增員工使用者
            </el-button>
            <el-button type="success" @click="exportEmployeeData">
              <el-icon><Download /></el-icon>
              匯出資料
            </el-button>
          </div>
        </div>
      </template>
      <div class="table-container">
        <el-table
          :data="filteredEmployees"
          v-loading="loadingUsers"
          table-layout="auto"
          class="responsive-table"
        >
          <el-table-column
            prop="employeeNumber"
            label="員工編號"
            class-name="col-employee-id"
            min-width="10%"
          >
            <template #default="scope">
              <span class="text-truncate">{{ scope.row.employeeNumber }}</span>
            </template>
          </el-table-column>
          <el-table-column label="姓名" class-name="col-name" min-width="15%">
            <template #default="scope">
              <span class="text-truncate">{{
                `${scope.row.firstName} ${scope.row.lastName}`
              }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="email"
            label="電子信箱"
            class-name="col-email"
            min-width="15%"
          >
            <template #default="scope">
              <span class="text-truncate">{{ scope.row.email || '未設定' }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="departmentName"
            label="部門"
            class-name="col-department"
            min-width="12%"
          >
            <template #default="scope">
              <span class="text-truncate">{{ scope.row.departmentName || '未分配' }}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="職位"
            class-name="col-position"
            min-width="12%"
          >
            <template #default="scope">
              <span class="text-truncate">{{ getPositionName(scope.row.employeePositionId) }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="hireDate"
            label="入職日期"
            class-name="col-hire-date"
            min-width="12%"
          >
            <template #default="scope">
              <span class="text-truncate">{{ formatDate(scope.row.hireDate) || '未設定' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="isActive" label="狀態" class-name="col-status" min-width="8%">
            <template #default="scope">
              <el-tag :type="scope.row.isActive ? 'success' : 'danger'" size="small">
                {{ scope.row.isActive ? '啟用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" class-name="col-actions" min-width="15%">
            <template #default="scope">
              <div class="action-buttons" >
                <el-tooltip content="查看詳細資料" placement="top">
                  <el-button 
                    circle 
                    size="small" 
                    type="info" 
                    @click="viewUser(scope.row)"
                  >
                    <el-icon><View /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="編輯" placement="top">
                  <el-button 
                    circle 
                    size="small" 
                    type="primary" 
                    @click="editUser(scope.row)"
                  >
                    <el-icon><Edit /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="刪除" placement="top">
                  <el-button
                    circle
                    size="small"
                    type="danger"
                    @click="deleteUser(scope.row.employeeUserId)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 新增/編輯員工使用者 Dialog -->
    <el-dialog
      v-model="showAddUserDialog"
      :title="isEditMode ? '編輯員工使用者' : '新增員工使用者'"
      width="600"
      @close="resetForm"
    >
     <!-- 一鍵資料 -->
      <div class="demo-section" v-if="!isEditMode">
        <div style="display: flex; justify-content: flex-end; margin-bottom: 16px">
          <el-button type="primary" size="default" @click="quickFillSampleData">
            一鍵資料
          </el-button>
        </div>
        <el-divider />
      </div>
      <!-- 以上 -->
      <el-form
        :model="currentEmployeeUser"
        :rules="formRules"
        ref="userFormRef"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="名字" prop="firstName">
              <el-input v-model="currentEmployeeUser.firstName"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓氏" prop="lastName">
              <el-input v-model="currentEmployeeUser.lastName"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="員工編號" prop="employeeNumber">
              <el-input v-model="currentEmployeeUser.employeeNumber"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="員工類型" prop="employeeType">
              <el-select
                v-model="currentEmployeeUser.employeeType"
                placeholder="選擇員工類型"
                style="width: 100%"
              >
                <el-option label="內部" value="INTERNAL"></el-option>
                <el-option label="供應商" value="SUPPLIER"></el-option>
                <el-option label="承包商" value="CONTRACTOR"></el-option>
                <el-option label="系統管理員" value="SYSTEM_ADMIN"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker
                v-model="currentEmployeeUser.birthDate"
                type="date"
                placeholder="選擇日期"
                style="width: 100%"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="電子郵件" prop="email">
              <el-input v-model="currentEmployeeUser.email"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="電話">
              <el-input v-model="currentEmployeeUser.phone"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="照片路徑">
              <el-input v-model="currentEmployeeUser.photoPath"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部門">
              <el-select
                v-model="currentEmployeeUser.employeeDepartmentId"
                placeholder="選擇部門"
                style="width: 100%"
              >
                <el-option
                  v-for="dept in availableDepartments"
                  :key="dept.departmentId"
                  :label="dept.description || dept.departmentName"
                  :value="dept.departmentId"
                >
                  <span>{{ dept.description || dept.departmentName }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{
                    dept.departmentName
                  }}</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="職位">
              <el-select
                v-model="currentEmployeeUser.employeePositionId"
                placeholder="選擇職位"
                style="width: 100%"
              >
                <el-option
                  v-for="position in availablePositions"
                  :key="position.positionId"
                  :label="getPositionChineseName(position.positionName)"
                  :value="position.positionId"
                >
                  <span>{{ getPositionChineseName(position.positionName) }}</span>
                  <span v-if="getPositionChineseDescription(position.positionName)" style="float: right; color: #8492a6; font-size: 13px">{{ getPositionChineseDescription(position.positionName) }}</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="主管">
              <el-select
                v-model="currentEmployeeUser.managerEmployeeUserId"
                placeholder="選擇主管"
                style="width: 100%"
                clearable
              >
                <el-option :label="'無主管'" :value="null">
                  <span>無主管</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">無需主管</span>
                </el-option>
                <el-option
                  v-for="manager in availableManagers"
                  :key="manager.employeeUserId"
                  :label="`${manager.firstName} ${manager.lastName} (${getPositionName(manager.employeePositionId)})`"
                  :value="manager.employeeUserId"
                >
                  <span>{{ `${manager.firstName} ${manager.lastName}` }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">({{ getPositionName(manager.employeePositionId) }})</span>
                </el-option>
              </el-select>
              <div style="margin-top: 4px; color: #909399; font-size: 11px">
                只有經理以上職位可擔任主管
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="雇用日期">
              <el-date-picker
                v-model="currentEmployeeUser.hireDate"
                type="date"
                placeholder="選擇日期"
                style="width: 100%"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              ></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="解雇日期">
              <el-date-picker
                v-model="currentEmployeeUser.terminationDate"
                type="date"
                placeholder="選擇日期"
                style="width: 100%"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="使用者名稱" prop="username">
              <el-input v-model="currentEmployeeUser.username"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="密碼" v-if="!isEditMode" prop="passwordHash">
          <el-input
            v-model="currentEmployeeUser.passwordHash"
            type="password"
            show-password
          ></el-input>
        </el-form-item>
        <!-- 【已修正】增加 prop，使其在編輯模式下也能被驗證 -->
        <el-form-item label="密碼" v-if="isEditMode && showPasswordEdit" prop="passwordHash">
          <el-input
            v-model="currentEmployeeUser.passwordHash"
            type="password"
            show-password
            placeholder="若不修改請留空"
          ></el-input>

          <el-button type="text" @click="showPasswordEdit = false">取消修改密碼</el-button>
        </el-form-item>
        <el-form-item v-if="isEditMode && !showPasswordEdit">
          <el-button type="text" @click="showPasswordEdit = true">修改密碼</el-button>
        </el-form-item>
        <div style="margin-top: -10px;margin-left: 120px; color: #909399; font-size: 11px">
          密碼長度至少6位、必須包含字母和數字、禁止使用常見弱密碼
        </div>
        <el-form-item label="是否啟用">
          <el-switch v-model="currentEmployeeUser.isActive"></el-switch>
        </el-form-item>

        
        <el-divider content-position="left">角色管理</el-divider>

        <el-form-item label="角色選擇">
          <el-select
            v-model="employeeRoles"
            multiple
            placeholder="選擇員工角色"
            style="width: 100%"
            @change="updatePermissionPreview"
          >
            <el-option
              v-for="role in availableRoles"
              :key="role.roleId"
              :label="getRoleDisplayName(role.roleName)"
              :value="role.roleName"
            >
              <div style="display: flex; flex-direction: column; padding: 4px 0">
                <div style="display: flex; justify-content: space-between; align-items: center">
                  <span style="font-weight: 500">{{ getRoleDisplayName(role.roleName) }}</span>
                  <span style="color: #8492a6; font-size: 11px">[{{ role.roleName }}]</span>
                </div>
                <span style="color: #909399; font-size: 12px; margin-top: 2px">
                  {{ getRoleDescription(role.roleName) }}
                </span>
              </div>
            </el-option>
          </el-select>
          <!-- <div style="margin-top: 8px; color: #909399; font-size: 12px">
            選擇適合的角色為員工分配基礎權限，建立後可在權限管理頁面進行詳細權限調整
          </div> -->
        </el-form-item>
        
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddUserDialog = false">取消</el-button>
          <el-button type="primary" @click="saveEmployeeUser">
            {{ isEditMode ? '更新' : '新增' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看員工詳細資料 Dialog -->
    <el-dialog
      v-model="showViewUserDialog"
      title="員工詳細資料"
      width="700"
      @close="resetViewData"
    >
      <el-descriptions :column="2" size="large" border v-if="viewUserData">
        <el-descriptions-item label="員工編號">
          {{ viewUserData.employeeNumber }}
        </el-descriptions-item>
        <el-descriptions-item label="使用者名稱">
          {{ viewUserData.username }}
        </el-descriptions-item>
        <el-descriptions-item label="姓名">
          {{ `${viewUserData.firstName} ${viewUserData.lastName}` }}
        </el-descriptions-item>
        <el-descriptions-item label="員工類型">
          <el-tag :type="getEmployeeTypeTagType(viewUserData.employeeType)" size="small">
            {{ getEmployeeTypeDisplay(viewUserData.employeeType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="電子郵件">
          <el-link 
            :href="`mailto:${viewUserData.email}`" 
            type="primary" 
            v-if="viewUserData.email"
          >
            {{ viewUserData.email }}
          </el-link>
          <span v-else>未設定</span>
        </el-descriptions-item>
        <el-descriptions-item label="聯絡電話">
          {{ viewUserData.phone || '未設定' }}
        </el-descriptions-item>
        <el-descriptions-item label="所屬部門">
          <el-tag type="info">{{ viewUserData.departmentName || '未分配' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="狀態">
          <el-tag :type="viewUserData.isActive ? 'success' : 'danger'" size="small">
            {{ viewUserData.isActive ? '啟用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="出生日期">
          {{ formatDate(viewUserData.birthDate) || '未設定' }}
        </el-descriptions-item>
        <el-descriptions-item label="到職日期">
          {{ formatDate(viewUserData.hireDate) || '未設定' }}
        </el-descriptions-item>
        <el-descriptions-item label="職位">
          {{ getPositionName(viewUserData.employeePositionId) || '未設定' }}
        </el-descriptions-item>
        <el-descriptions-item label="直屬主管">
          {{ getManagerName(viewUserData.managerEmployeeUserId) }}
        </el-descriptions-item>
        <el-descriptions-item label="建立時間" :span="2">
          {{ formatDateTime(viewUserData.createdAt) || '未知' }}
        </el-descriptions-item>
        <el-descriptions-item label="最後更新" :span="2">
          {{ formatDateTime(viewUserData.updatedAt) || '未知' }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 角色資訊區塊 -->
      <el-divider content-position="left">角色資訊</el-divider>
      <div class="roles-section">
        <div v-if="viewUserData && viewUserData.roles && viewUserData.roles.length > 0" class="roles-container">
          <el-tag
            v-for="role in viewUserData.roles"
            :key="role"
            :type="getRoleTagType(role)"
            class="role-tag"
            size="default"
          >
            {{ getRoleDisplayName(role) }}
          </el-tag>
        </div>
        <el-empty v-else description="未分配任何角色" :image-size="60" />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showViewUserDialog = false">關閉</el-button>
          <el-button type="primary" @click="editUserFromView">編輯此員工</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive, computed } from 'vue'
import http from '../http-common'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { View, Edit, Delete, Plus, Download } from '@element-plus/icons-vue'

interface EmployeeUserDto {
  employeeUserId?: number
  firstName: string
  lastName: string
  employeeNumber: string
  employeeType: 'INTERNAL' | 'SUPPLIER' | 'CONTRACTOR' | 'SYSTEM_ADMIN'
  birthDate?: string
  email?: string
  phone?: string
  photoPath?: string
  employeeDepartmentId?: number
  employeePositionId?: number
  managerEmployeeUserId?: number
  hireDate?: string
  terminationDate?: string
  username?: string
  passwordHash?: string
  isActive: boolean
  lastLogin?: string
  createdAt?: string
  updatedAt?: string
  departmentName?: string // 新增部門名稱欄位
  positionName?: string // 新增職位名稱欄位
  roles?: string[] // 新增角色列表欄位
}

interface DepartmentDto {
  departmentId: number
  departmentName: string
  description?: string
}

interface PositionDto {
  positionId: number
  positionName: string
  description?: string
  createdAt?: string
  updatedAt?: string
}


interface RoleDto {
  roleId: number
  roleName: string
  displayName: string
  description: string
  isActive: boolean
}

interface UpdateEmployeeRolesRequest {
  roleNames: string[]
  updatedBy: string
}
// 一件輸入
const quickFillSampleData = () => {
  currentEmployeeUser.value = {
    firstName: '小明',
    lastName: '王',
    employeeNumber: 'DEMO001',
    employeeType: 'INTERNAL',
    birthDate: '1990-05-15',
    email: 'demo@company.com',
    phone: '0912345678',
    employeeDepartmentId: 7,
    employeePositionId: 1,
    managerEmployeeUserId: undefined,
    hireDate: '2023-01-15',
    username: 'demo001',
    passwordHash: 'demo123',
    isActive: true,
    photoPath: '',
    terminationDate: undefined,
  }

  // 設定角色
  employeeRoles.value = ['EMPLOYEE_ACCOUNT_MANAGER']
  updatePermissionPreview()
}

//以上

const employeeUsers = ref<EmployeeUserDto[]>([])
const loadingUsers = ref(false)
const showAddUserDialog = ref(false)
const isEditMode = ref(false)
const showPasswordEdit = ref(false)
const showViewUserDialog = ref(false)
const viewUserData = ref<EmployeeUserDto | null>(null)

// 搜尋和篩選
const searchText = ref('')
const departmentFilter = ref('')
const availableDepartments = ref<DepartmentDto[]>([])
const availablePositions = ref<PositionDto[]>([])
const availableManagers = ref<EmployeeUserDto[]>([]) // 可擔任主管的員工列表


const availableRoles = ref<RoleDto[]>([]) // 所有可用角色
const employeeRoles = ref<string[]>([]) // 當前員工的角色名稱
const calculatedPermissions = ref<string[]>([]) // 計算出的權限預覽

// 機台角色檢測
const hasMachineRoles = computed(() => {
  return employeeRoles.value.some(
    (role) => role === 'MACHINE_EMPLOYEE' || role === 'MACHINE_MANAGER',
  )
})

// 機台角色資訊
const machineRoleInfo = computed(() => {
  const machineRoles = employeeRoles.value.filter(
    (role) => role === 'MACHINE_EMPLOYEE' || role === 'MACHINE_MANAGER',
  )

  if (machineRoles.length === 0) return null

  return {
    roles: machineRoles,
    level: machineRoles.includes('MACHINE_MANAGER') ? '管理員' : '員工',
    module: '機台模組',
  }
})



const userFormRef = ref<FormInstance>()

// 計算屬性 - 篩選後的員工列表 (增強版)
const filteredEmployees = computed(() => {
  return employeeUsers.value.filter((employee) => {
    const fullName = `${employee.firstName} ${employee.lastName}`

    // 搜尋匹配 - 支援員工編號、姓名、使用者名稱、郵件
    const matchesSearch =
      !searchText.value ||
      fullName.toLowerCase().includes(searchText.value.toLowerCase()) ||
      employee.employeeNumber.toLowerCase().includes(searchText.value.toLowerCase()) ||
      (employee.username &&
        employee.username.toLowerCase().includes(searchText.value.toLowerCase())) ||
      (employee.email && employee.email.toLowerCase().includes(searchText.value.toLowerCase()))

    // 部門篩選匹配
    const matchesDepartment =
      !departmentFilter.value || employee.departmentName === departmentFilter.value

    return matchesSearch && matchesDepartment
  })
})

const currentEmployeeUser = ref<EmployeeUserDto>({
  firstName: '',
  lastName: '',
  employeeNumber: '',
  employeeType: 'INTERNAL',
  birthDate: undefined,
  email: '',
  phone: '',
  photoPath: '',
  employeeDepartmentId: undefined,
  employeePositionId: undefined,
  managerEmployeeUserId: undefined,
  hireDate: undefined,
  terminationDate: undefined,
  username: '',
  passwordHash: '',
  isActive: true,
})

// 【已修正】增加自訂驗證器，讓密碼在編輯模式下為非必填
const validatePassword = (rule: any, value: any, callback: any) => {
  // 在「新增」模式下，密碼為必填
  if (!isEditMode.value && !value) {
    callback(new Error('請輸入密碼'))
    return
  }

  // 如果有密碼值，進行強化驗證
  if (value && value.length > 0) {
    // 1. 密碼長度至少6位
    if (value.length < 6) {
      callback(new Error('密碼長度至少6位'))
      return
    }

    // 2. 密碼必須包含字母和數字
    const hasLetter = /[A-Za-z]/.test(value)
    const hasNumber = /\d/.test(value)
    if (!hasLetter || !hasNumber) {
      callback(new Error('密碼必須包含字母和數字'))
      return
    }

    // 3. 禁止使用常見弱密碼
    const weakPasswords = [
      '123456',
      '111111',
      '222222',
      '333333',
      '444444',
      '555555',
      '666666',
      '777777',
      '888888',
      '999999',
      '000000',
      'password',
      'admin123',
      'abc123',
      'qwerty',
      '123abc',
      'admin',
      'user123',
      'pass123',
      '1234567',
      'abcdefg',
    ]

    if (weakPasswords.includes(value.toLowerCase())) {
      callback(new Error('請避免使用常見弱密碼'))
      return
    }
  }

  // 在「編輯」模式下，如果密碼為空則通過驗證
  callback()
}

const formRules = reactive<FormRules>({
  firstName: [{ required: true, message: '請輸入名字', trigger: 'blur' }],
  lastName: [{ required: true, message: '請輸入姓氏', trigger: 'blur' }],
  employeeNumber: [{ required: true, message: '請輸入員工編號', trigger: 'blur' }],
  username: [{ required: true, message: '請輸入使用者名稱', trigger: 'blur' }],
  email: [{ type: 'email', message: '請輸入有效的電子郵件地址', trigger: ['blur', 'change'] }],
  // 【已修正】使用自訂驗證器
  passwordHash: [{ validator: validatePassword, trigger: 'blur' }],
})

const API_BASE_URL = '/employee-users'

// 獲取部門列表 - 使用真實 API
const fetchDepartments = async () => {
  try {
    const response = await http.get<DepartmentDto[]>('/departments')
    availableDepartments.value = response.data
    console.log('Available departments loaded:', response.data)
  } catch (error: any) {
    console.error('Error loading departments:', error)
    ElMessage.error(`載入部門資料失敗: ${error.message}`)
    
    // 如果 API 失敗，回退到 mock 數據作為備用
    const fallbackDepartments: DepartmentDto[] = [
      { departmentId: 1, departmentName: 'Warehouse', description: '倉庫管理部' },
      { departmentId: 2, departmentName: 'Production', description: '工單管理部' },
      { departmentId: 3, departmentName: 'Procurement', description: '採購管理部' },
      { departmentId: 4, departmentName: 'Equipment', description: '機台管理部' },
      { departmentId: 5, departmentName: 'Personnel', description: '人員管理部' },
      { departmentId: 6, departmentName: 'Leave', description: '請假管理部' },
    ]
    availableDepartments.value = fallbackDepartments
    console.log('Using fallback departments data')
  }
}

// 獲取職位列表 - 使用真實 API
const fetchPositions = async () => {
  try {
    const response = await http.get<PositionDto[]>('/positions')
    availablePositions.value = response.data
    console.log('Available positions loaded:', response.data)
  } catch (error: any) {
    console.error('Error loading positions:', error)
    ElMessage.error(`載入職位資料失敗: ${error.message}`)
    
    // 如果 API 失敗，回退到 mock 數據作為備用
    const fallbackPositions: PositionDto[] = [
      { positionId: 1, positionName: 'Manager', description: 'Manages a team' },
      { positionId: 2, positionName: 'Staff', description: 'General staff member' },
      { positionId: 3, positionName: 'System Administrator', description: 'Manages the system' },
    ]
    availablePositions.value = fallbackPositions
    console.log('Using fallback positions data')
  }
}


// 獲取所有可用角色
const fetchAvailableRoles = async () => {
  try {
    const response = await http.get<RoleDto[]>(`${API_BASE_URL}/available-roles`)
    availableRoles.value = response.data
    console.log('Available roles loaded:', response.data)
  } catch (error: any) {
    console.error('Error fetching available roles:', error)
    ElMessage.error(`獲取可用角色失敗: ${error.response?.data || error.message}`)
  }
}

// 獲取員工角色
const fetchEmployeeRoles = async (employeeId: number) => {
  // 驗證員工ID的有效性
  if (!employeeId || typeof employeeId !== 'number' || employeeId <= 0) {
    console.error('無效的員工ID:', employeeId)
    employeeRoles.value = []
    calculatedPermissions.value = []
    return
  }

  try {
    const response = await http.get<string[]>(`${API_BASE_URL}/${employeeId}/roles`)

    // 確保回應數據是陣列
    if (Array.isArray(response.data)) {
      employeeRoles.value = response.data
      updatePermissionPreview()
      console.log('Employee roles loaded:', response.data)
    } else {
      console.warn('員工角色回應格式不正確:', response.data)
      employeeRoles.value = []
      calculatedPermissions.value = []
    }
  } catch (error: any) {
    console.error('Error fetching employee roles:', error)
    ElMessage.error(
      `獲取員工角色失敗: ${error.response?.data?.message || error.response?.data || error.message}`,
    )
    employeeRoles.value = []
    calculatedPermissions.value = []
  }
}

// 獲取當前用戶名稱
const getCurrentUser = (): string => {
  // 嘗試從 localStorage 獲取用戶資訊
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      return user.username || user.name || 'unknown_user'
    }
  } catch (error) {
    console.warn('無法從 localStorage 獲取用戶資訊:', error)
  }

  // 嘗試從其他可能的來源獲取
  const token = localStorage.getItem('token')
  if (token) {
    try {
      // 如果 token 是 JWT，可以解析獲取用戶資訊
      const payload = JSON.parse(atob(token.split('.')[1]))
      return payload.sub || payload.username || 'token_user'
    } catch (error) {
      console.warn('無法解析 token:', error)
    }
  }

  // 如果都無法獲取，使用預設值
  return 'system_admin'
}

// 更新員工角色
const updateEmployeeRoles = async (employeeId: number, roleNames: string[]) => {
  // 驗證參數的有效性
  if (!employeeId || typeof employeeId !== 'number' || employeeId <= 0) {
    throw new Error('無效的員工ID')
  }

  if (!Array.isArray(roleNames)) {
    throw new Error('角色名稱必須是陣列')
  }

  try {
    const currentUser = getCurrentUser()
    const request: UpdateEmployeeRolesRequest = {
      roleNames: roleNames.filter((name) => typeof name === 'string' && name.trim() !== ''), // 過濾無效的角色名稱
      updatedBy: currentUser,
    }

    await http.put(`${API_BASE_URL}/${employeeId}/roles`, request)
    console.log('Employee roles updated successfully by:', currentUser, 'roles:', request.roleNames)
  } catch (error: any) {
    console.error('Error updating employee roles:', error)
    throw error // 重新拋出錯誤以便調用方處理
  }
}

// 優化權限預覽，突出機台模組
const updatePermissionPreview = () => {
  const roleDetails = employeeRoles.value.map((roleName) => {
    const isMachineRole = roleName === 'MACHINE_EMPLOYEE' || roleName === 'MACHINE_MANAGER'

    return {
      roleName,
      displayName: getRoleDisplayName(roleName),
      description: getRoleDescription(roleName),
      module: isMachineRole ? '機台模組' : '其他模組',
      level: roleName.includes('MANAGER') || roleName === 'ADMIN' ? '管理員' : '員工',
      isMachine: isMachineRole,
    }
  })

  // 為了保持向後相容，仍然提供簡單的顯示名稱數組
  const roleDescriptions = roleDetails.map((detail) => detail.displayName)
  calculatedPermissions.value = roleDescriptions
}


// 獲取員工類型顯示文字
const getEmployeeTypeDisplay = (type: string): string => {
  const typeMap: Record<string, string> = {
    INTERNAL: '內部',
    SUPPLIER: '供應商',
    CONTRACTOR: '承包商',
    SYSTEM_ADMIN: '系統管理員',
  }
  return typeMap[type] || type
}

// 獲取員工類型標籤顏色
const getEmployeeTypeTagType = (type: string): string => {
  const typeMap: Record<string, string> = {
    INTERNAL: 'primary',
    SUPPLIER: 'success',
    CONTRACTOR: 'warning',
    SYSTEM_ADMIN: 'danger',
  }
  return typeMap[type] || 'info'
}

// 獲取角色標籤顏色（與 PermissionManagement 保持一致）
const getRoleTagType = (role: string): string => {
  const typeMap: Record<string, string> = {
    ADMIN: 'danger',
    WAREHOUSE_MANAGER: 'warning',
    WAREHOUSE_STAFF: 'info',
    WORKORDER_MANAGER: 'success',
    PURCHASE_MANAGER: 'warning',
    MACHINE_EMPLOYEE: 'info',
    MACHINE_MANAGER: 'warning',
    LEAVE_MANAGER: 'primary',
    EMPLOYEE_ACCOUNT_MANAGER: 'primary',
  }
  return typeMap[role] || 'info'
}

//角色描述
const getRoleDescription = (roleName: string): string => {
  const descriptionMap: Record<string, string> = {
    ADMIN: '系統最高權限管理員',
    WAREHOUSE_STAFF: '倉庫作業人員',
    WAREHOUSE_MANAGER: '倉庫全權管理',
    WORKORDER_MANAGER: '工單與物料管理',
    PURCHASE_MANAGER: '採購與供應商管理',
    EMPLOYEE_ACCOUNT_MANAGER: '員工帳號權限管理',
    MACHINE_EMPLOYEE: '機台操作與維護申請',
    MACHINE_MANAGER: '設備維護與管理',
    LEAVE_MANAGER: '請假與出勤管理',
  }
  return descriptionMap[roleName] || '角色描述'
}
// 獲取角色顯示名稱
// 確保 getRoleDisplayName 函數正確
const getRoleDisplayName = (roleName: string): string => {
  const roleDisplayMap: Record<string, string> = {
    ADMIN: '系統管理員',
    WAREHOUSE_STAFF: '倉庫人員',
    WAREHOUSE_MANAGER: '倉庫管理員',
    WORKORDER_MANAGER: '工單管理員',
    PURCHASE_MANAGER: '採購管理員',
    EMPLOYEE_ACCOUNT_MANAGER: '員工帳號管理員',
    MACHINE_EMPLOYEE: '機台員工',
    MACHINE_MANAGER: '機台管理員',
    LEAVE_MANAGER: '請假管理員',
  }
  return roleDisplayMap[roleName] || roleName
}

// 根據部門ID獲取部門名稱
const getDepartmentName = (departmentId?: number): string => {
  if (!departmentId) return '未分配'
  const department = availableDepartments.value.find((dept) => dept.departmentId === departmentId)
  return department ? department.description || department.departmentName : '未知部門'
}

// 職位英文到中文對照表
const positionTranslationMap: Record<string, { chinese: string; description: string }> = {
  'Manager': { chinese: '經理', description: '管理團隊和業務' },
  'Staff': { chinese: '專員', description: '一般員工職位' },
  'System Administrator': { chinese: '系統管理員', description: '管理系統和技術事務' }
}

// 獲取職位中文名稱
const getPositionChineseName = (englishName: string): string => {
  return positionTranslationMap[englishName]?.chinese || englishName
}

// 獲取職位中文描述
const getPositionChineseDescription = (englishName: string): string => {
  return positionTranslationMap[englishName]?.description || ''
}

// 根據職位ID獲取職位名稱（中文）
const getPositionName = (positionId?: number): string => {
  if (!positionId) return '未設定'
  const position = availablePositions.value.find((pos) => pos.positionId === positionId)
  if (!position) return '未知職位'
  return getPositionChineseName(position.positionName)
}

// 獲取符合條件的主管候選人（只有 Manager 或 System Administrator）
const fetchAvailableManagers = async () => {
  try {
    // 獲取所有員工資料
    const response = await http.get<EmployeeUserDto[]>(API_BASE_URL)
    const allEmployees = response.data
    
    // 過濾出符合條件的主管
    const managers = allEmployees.filter(employee => {
      if (!employee.employeePositionId) return false
      
      const position = availablePositions.value.find(pos => pos.positionId === employee.employeePositionId)
      if (!position) return false
      
      // 只有 Manager 或 System Administrator 可以擔任主管
      return position.positionName === 'Manager' || position.positionName === 'System Administrator'
    })
    
    // 為每個主管加入職位名稱資訊
    const managersWithPositions = managers.map(manager => ({
      ...manager,
      positionName: getPositionName(manager.employeePositionId)
    }))
    
    availableManagers.value = managersWithPositions
    console.log('Available managers loaded:', managersWithPositions)
  } catch (error: any) {
    console.error('Error loading available managers:', error)
    ElMessage.error(`載入主管候選人失敗: ${error.message}`)
    availableManagers.value = []
  }
}

// 根據主管ID獲取主管姓名
const getManagerName = (managerId?: number): string => {
  if (!managerId) return '無主管'
  const manager = availableManagers.value.find(mgr => mgr.employeeUserId === managerId)
  if (!manager) {
    // 如果在主管列表中找不到，嘗試從所有員工中尋找
    const employee = employeeUsers.value.find(emp => emp.employeeUserId === managerId)
    return employee ? `${employee.firstName} ${employee.lastName}` : '未知主管'
  }
  return `${manager.firstName} ${manager.lastName}`
}

// Fetch all employee users
const fetchEmployeeUsers = async () => {
  loadingUsers.value = true
  try {
    const response = await http.get<any[]>(API_BASE_URL)

    const transformedData = await Promise.all(
      response.data.map(async (user) => {
        if (user.isActive === undefined && user.active !== undefined) {
          user.isActive = user.active
        }
        // 根據部門ID添加部門名稱
        user.departmentName = getDepartmentName(user.employeeDepartmentId)
        
        // 根據職位ID添加職位名稱
        user.positionName = getPositionName(user.employeePositionId)

        // 載入員工角色資訊
        if (user.employeeUserId && typeof user.employeeUserId === 'number') {
          try {
            const rolesResponse = await http.get<string[]>(
              `${API_BASE_URL}/${user.employeeUserId}/roles`,
            )
            user.roles = Array.isArray(rolesResponse.data) ? rolesResponse.data : []
          } catch (roleError) {
            console.warn(`無法載入員工 ${user.employeeUserId} 的角色:`, roleError)
            user.roles = []
          }
        } else {
          user.roles = []
        }

        return user
      }),
    )

    employeeUsers.value = transformedData as EmployeeUserDto[]
  } catch (error: any) {
    console.error('Error fetching employee users:', error)
    ElMessage.error(`獲取員工使用者列表失敗: ${error.response?.data || error.message}`)
  } finally {
    loadingUsers.value = false
  }
}

const resetForm = () => {
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
}

// Open add employee user dialog
const openAddUserDialog = () => {
  isEditMode.value = false
  showPasswordEdit.value = true
  currentEmployeeUser.value = {
    firstName: '',
    lastName: '',
    employeeNumber: '',
    employeeType: 'INTERNAL',
    birthDate: undefined,
    email: '',
    phone: '',
    photoPath: '',
    employeeDepartmentId: undefined,
    employeePositionId: undefined,
    managerEmployeeUserId: undefined,
    hireDate: undefined,
    terminationDate: undefined,
    username: '',
    passwordHash: '',
    isActive: true,
  }

  // ===== 新增：角色管理功能開始 =====
  // 重置角色相關數據
  employeeRoles.value = []
  calculatedPermissions.value = []
  // ===== 新增：角色管理功能結束 =====

  showAddUserDialog.value = true
  userFormRef.value?.clearValidate()
}

// Save (add/update) employee user
const saveEmployeeUser = async () => {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const payload: Partial<EmployeeUserDto> = { ...currentEmployeeUser.value }

        if (isEditMode.value && !payload.passwordHash) {
          delete payload.passwordHash
        }

        // 確保日期格式正確傳送給後端
        if (payload.birthDate) {
          payload.birthDate = toDateString(payload.birthDate)
        }
        if (payload.hireDate) {
          payload.hireDate = toDateString(payload.hireDate)
        }
        if (payload.terminationDate) {
          payload.terminationDate = toDateString(payload.terminationDate)
        }

        if (isEditMode.value) {
          // 確保 employeeUserId 存在且為有效數字
          if (!payload.employeeUserId || typeof payload.employeeUserId !== 'number') {
            throw new Error('員工ID無效，無法更新員工資料')
          }

          await http.put(`${API_BASE_URL}/${payload.employeeUserId}`, payload)

          
          // 更新員工角色
          if (
            payload.employeeUserId &&
            typeof payload.employeeUserId === 'number' &&
            Array.isArray(employeeRoles.value)
          ) {
            try {
              await updateEmployeeRoles(payload.employeeUserId, employeeRoles.value)
              ElMessage.success('員工使用者和角色更新成功')
            } catch (roleError: any) {
              console.error('角色更新失敗:', roleError)
              const errorMsg =
                roleError.response?.data?.message ||
                roleError.response?.data ||
                roleError.message ||
                '未知錯誤'
              ElMessage.warning('員工資料更新成功，但角色更新失敗: ' + errorMsg)
            }
          } else {
            console.warn('員工ID無效或角色數據無效，跳過角色更新')
            ElMessage.success('員工使用者更新成功')
          }
          
        } else {
          // 新增員工
          const response = await http.post<EmployeeUserDto>(API_BASE_URL, payload)

          // 新增員工成功後，指派角色
          if (
            response.data?.employeeUserId &&
            Array.isArray(employeeRoles.value) &&
            employeeRoles.value.length > 0
          ) {
            try {
              await updateEmployeeRoles(response.data.employeeUserId, employeeRoles.value)
              ElMessage.success('員工使用者和角色新增成功')
            } catch (roleError: any) {
              console.error('角色指派失敗:', roleError)
              const errorMsg =
                roleError.response?.data?.message ||
                roleError.response?.data ||
                roleError.message ||
                '未知錯誤'
              ElMessage.warning('員工資料新增成功，但角色指派失敗: ' + errorMsg)
            }
          } else {
            ElMessage.success('員工使用者新增成功')
          }
        }
        showAddUserDialog.value = false
        // 重新載入員工資料以確保部門名稱和角色正確顯示
        await fetchEmployeeUsers()
      } catch (error: any) {
        console.error('Error saving employee user:', error)
        if (error.response && error.response.status === 409) {
          const conflictMessage =
            error.response.data?.message || '員工編號、使用者名稱或Email已存在。'
          ElMessage.error(`儲存失敗: ${conflictMessage}`)
        } else {
          const errorMessage = error.response?.data?.message || error.message
          ElMessage.error(`儲存員工使用者失敗: ${errorMessage}`)
        }
      }
    } else {
      ElMessage.error('表單驗證失敗，請檢查輸入的資料！')
      return false
    }
  })
}

// Edit employee user
const editUser = async (user: EmployeeUserDto) => {
  try {
    isEditMode.value = true
    showPasswordEdit.value = false
    currentEmployeeUser.value = {
      ...user,
      passwordHash: '',
      birthDate: toDateString(user.birthDate),
      hireDate: toDateString(user.hireDate),
      terminationDate: toDateString(user.terminationDate),
      // 確保部門資訊正確設定
      departmentName: getDepartmentName(user.employeeDepartmentId),
    }

    
    // 載入員工角色（加入錯誤處理）
    if (user.employeeUserId && typeof user.employeeUserId === 'number') {
      try {
        await fetchEmployeeRoles(user.employeeUserId)
      } catch (roleError: any) {
        console.error('載入員工角色失敗:', roleError)
        ElMessage.warning('員工資料載入成功，但角色資料載入失敗，角色管理功能可能受影響')
        // 重置角色數據以避免顯示錯誤的舊數據
        employeeRoles.value = []
        calculatedPermissions.value = []
      }
    } else {
      console.warn('員工ID無效，無法載入角色資料')
      employeeRoles.value = []
      calculatedPermissions.value = []
    }
    

    showAddUserDialog.value = true
    userFormRef.value?.clearValidate()
  } catch (error: any) {
    console.error('編輯員工時發生錯誤:', error)
    ElMessage.error('載入員工資料失敗: ' + (error.message || '未知錯誤'))
  }
}

// Delete employee user
const deleteUser = async (id?: number) => {
  if (!id) return
  ElMessageBox.confirm('確定要刪除此員工使用者嗎？', '警告', {
    confirmButtonText: '確定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await http.delete(`${API_BASE_URL}/${id}`)
        ElMessage.success('員工使用者刪除成功')
        await fetchEmployeeUsers()
      } catch (error: any) {
        console.error('Error deleting employee user:', error)
        ElMessage.error(`刪除員工使用者失敗: ${error.response?.data || error.message}`)
      }
    })
    .catch(() => {
      ElMessage.info('已取消刪除')
    })
}

// 查看員工詳細資料
const viewUser = (user: EmployeeUserDto) => {
  viewUserData.value = { ...user }
  showViewUserDialog.value = true
}

// 重置查看資料
const resetViewData = () => {
  viewUserData.value = null
}

// 從查看視窗直接編輯
const editUserFromView = () => {
  if (viewUserData.value) {
    showViewUserDialog.value = false
    editUser(viewUserData.value)
  }
}

// 匯出員工資料
const exportEmployeeData = () => {
  try {
    // 準備匯出資料
    const exportData = filteredEmployees.value.map(user => ({
      '員工編號': user.employeeNumber,
      '姓名': `${user.firstName} ${user.lastName}`,
      '使用者名稱': user.username,
      '電子郵件': user.email || '',
      '聯絡電話': user.phone || '',
      '部門': user.departmentName || '未分配',
      '職位': getPositionName(user.employeePositionId) || '未設定',
      '直屬主管': getManagerName(user.managerEmployeeUserId),
      '員工類型': getEmployeeTypeDisplay(user.employeeType),
      '角色': user.roles ? user.roles.map(role => getRoleDisplayName(role)).join(', ') : '無',
      '狀態': user.isActive ? '啟用' : '停用',
      '出生日期': formatDate(user.birthDate) || '',
      '到職日期': formatDate(user.hireDate) || '',
      '建立時間': formatDateTime(user.createdAt) || '',
      '最後更新': formatDateTime(user.updatedAt) || ''
    }))

    // 轉換為 CSV 格式
    const headers = Object.keys(exportData[0] || {})
    const csvContent = [
      headers.join(','), // 標題行
      ...exportData.map(row => 
        headers.map(header => `"${row[header] || ''}"`).join(',')
      )
    ].join('\n')

    // 建立下載連結
    const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    
    // 產生檔案名稱
    const now = new Date()
    const dateStr = toDateString(now)
    link.download = `員工資料_${dateStr}.csv`
    
    // 觸發下載
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success(`已匯出 ${exportData.length} 筆員工資料`)
  } catch (error: any) {
    console.error('匯出資料時發生錯誤:', error)
    ElMessage.error('匯出失敗: ' + (error.message || '未知錯誤'))
  }
}

// 日期格式化 - 避免時區轉換問題
const formatDate = (dateString?: string): string => {
  if (!dateString) return ''
  try {
    // 如果是 YYYY-MM-DD 格式，直接處理不經過 Date 物件避免時區問題
    if (typeof dateString === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
      const [year, month, day] = dateString.split('-')
      return `${year}/${month}/${day}`
    }
    // 其他格式使用原方法
    return new Date(dateString).toLocaleDateString('zh-TW')
  } catch {
    return ''
  }
}

// 安全的日期轉換函數 - 確保 YYYY-MM-DD 格式
const toDateString = (date: any): string => {
  if (!date) return ''
  
  // 如果已經是 YYYY-MM-DD 格式的字串
  if (typeof date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(date)) {
    return date
  }
  
  // 如果是 Date 物件，轉換為本地日期字串（避免 UTC 轉換）
  if (date instanceof Date) {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }
  
  return ''
}

// 日期時間格式化
const formatDateTime = (dateString?: string): string => {
  if (!dateString) return ''
  try {
    return new Date(dateString).toLocaleString('zh-TW')
  } catch {
    return ''
  }
}

onMounted(async () => {
  // 先載入部門、職位和角色資料，再載入員工資料
  await fetchDepartments() // 載入部門列表
  await fetchPositions() // 載入職位列表
  await fetchAvailableRoles() // 載入可用角色
  

  // 載入主管候選人（需要在職位資料載入後）
  await fetchAvailableManagers()

  // 最後載入員工資料，確保部門名稱、職位名稱和主管名稱能正確顯示
  fetchEmployeeUsers()
})
</script>

<style scoped>
.user-view {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden; /* 防止內容溢出 */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.header-actions {
  display: flex;
  align-items: center;
}

.dialog-footer button:first-child {
  margin-right: 10px;
}

/* 響應式表格容器 */
.table-container {
  width: 100%;
  overflow-x: auto;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 基本表格樣式 */
:deep(.responsive-table) {
  width: 100% !important;
  min-width: 800px;
}

:deep(.responsive-table .el-table__header-wrapper) {
  overflow: visible;
}

:deep(.responsive-table .el-table__body-wrapper) {
  overflow: visible;
}

:deep(.responsive-table th) {
  background-color: #fafafa;
  font-weight: 600;
  color: #606266;
  padding: 12px 8px;
  text-align: center;
}

:deep(.responsive-table td) {
  padding: 10px 8px;
  text-align: center;
}

/* 文字截斷樣式 */
.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  display: inline-block;
}

/* 角色標籤容器 */
.roles-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: center;
  align-items: center;
}

.role-tag {
  font-size: 12px;
  margin: 1px;
}

.no-roles {
  color: #909399;
  font-size: 12px;
  font-style: italic;
}

/* 操作按鈕容器 */
.action-buttons {
  display: flex;
  gap: 6px;
  justify-content: center;
  align-items: center;
}

/* 桌面版樣式 (>1200px) */
@media (min-width: 1201px) {
  :deep(.responsive-table) {
    min-width: 1200px;
  }

  .roles-container {
    justify-content: flex-start;
  }
}

/* 手機版樣式 (<768px) */
@media (max-width: 768px) {
  .hidden-mobile {
    display: none !important;
  }

  .table-container {
    margin: 0 -10px;
    border-radius: 0;
  }

  :deep(.responsive-table) {
    min-width: 600px;
  }

  :deep(.col-employee-id) {
    min-width: 12% !important;
  }

  :deep(.col-name) {
    min-width: 20% !important;
  }

  :deep(.col-email) {
    min-width: 18% !important;
  }

  :deep(.col-department) {
    min-width: 15% !important;
  }

  :deep(.col-status) {
    min-width: 10% !important;
  }

  :deep(.col-actions) {
    min-width: 15% !important;
  }

  :deep(.responsive-table th),
  :deep(.responsive-table td) {
    padding: 8px 4px;
    font-size: 12px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}

/* 超小螢幕優化 (<480px) */
@media (max-width: 480px) {
  :deep(.responsive-table) {
    min-width: 500px;
  }

  :deep(.responsive-table th),
  :deep(.responsive-table td) {
    padding: 6px 2px;
    font-size: 11px;
  }

  .text-truncate {
    max-width: 80px;
  }
}

/* 保留一些通用樣式 */
:deep(.el-button--small) {
  padding: 4px 8px;
  font-size: 12px;
  min-width: auto;
}

/* 圓形按鈕樣式優化 */
:deep(.el-button.is-circle) {
  width: 28px !important;
  height: 28px !important;
  padding: 0;
}

/* 響應式 - 操作按鈕 */
@media (max-width: 768px) {
  .action-buttons {
    gap: 4px;
  }
  
  :deep(.el-button.is-circle) {
    width: 24px !important;
    height: 24px !important;
  }
}



/* 查看對話框樣式 */
.roles-section {
  margin-top: 16px;
}

.roles-section .roles-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.roles-section .role-tag {
  margin: 0;
}

/* 圓形按鈕樣式優化 */
:deep(.el-button.is-circle) {
  width: 28px !important;
  height: 28px !important;
  padding: 0;
}

/* 響應式 - 操作按鈕 */
@media (max-width: 768px) {
  .action-buttons {
    gap: 4px;
  }
  
  :deep(.el-button.is-circle) {
    width: 24px !important;
    height: 24px !important;
  }
}

/* 查看對話框樣式 */
.roles-section {
  margin-top: 16px;
}

.roles-section .roles-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.roles-section .role-tag {
  margin: 0;
}
</style>
