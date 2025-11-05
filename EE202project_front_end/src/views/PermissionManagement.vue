<template>
  <div class="permission-management">
    <!-- 頂部工具欄 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-title">
          <h3>權限管理</h3>
          <p>統一管理員工角色與權限</p>
        </div>
        <div class="toolbar-actions">
          <el-input
            v-model="searchText"
            placeholder="搜尋員工姓名或編號"
            prefix-icon="Search"
            style="width: 280px; margin-right: 12px"
            clearable
            @keyup.enter="focusFirstEmployee"
          />
        </div>
      </div>
    </el-card>

    <!-- 主要內容區域 -->
    <div class="main-content">
      <!-- 左側員工列表 -->
      <el-card class="employee-list-card">
        <template #header>
          <div class="card-header">
            <span>員工列表</span>
            <div class="employee-stats">
              <el-badge :value="filteredEmployees.length" class="item" type="primary" />
              <span v-if="searchText" class="filter-info">
                (共 {{ employeeUsers.length }} 位員工)
              </span>
            </div>
          </div>
        </template>

        <div class="employee-list" v-loading="loadingUsers">
          <div
            v-for="employee in filteredEmployees"
            :key="employee.employeeUserId"
            class="employee-item"
            :class="{ selected: selectedEmployee?.employeeUserId === employee.employeeUserId }"
            @click="selectEmployee(employee)"
          >
            <div class="employee-info">
              <div class="employee-name">{{ employee.firstName }} {{ employee.lastName }}</div>
              <div class="employee-details">
                <span class="employee-number">{{ employee.employeeNumber }}</span>
                <el-tag
                  :type="employee.isActive ? 'success' : 'danger'"
                  size="small"
                  style="margin-left: 8px"
                >
                  {{ employee.isActive ? '啟用' : '停用' }}
                </el-tag>
              </div>
              <div class="employee-roles">
                <el-tag
                  v-for="role in employee.roles || []"
                  :key="role"
                  :type="getRoleTagType(role)"
                  size="small"
                  style="margin: 2px 4px 2px 0"
                  :title="getRoleShortDescription(role)"
                >
                  {{ getRoleDisplayName(role) }}
                </el-tag>
                <span v-if="!employee.roles || employee.roles.length === 0" class="no-roles">
                  尚未分配角色
                </span>
              </div>
            </div>
          </div>

          <div v-if="filteredEmployees.length === 0" class="no-data">
            <el-empty description="無符合條件的員工" />
          </div>
        </div>
      </el-card>

      <!-- 右側權限編輯區 -->
      <el-card class="permission-edit-card">
        <template #header>
          <div class="card-header">
            <span>權限編輯</span>
            <div v-if="selectedEmployee" class="permission-actions">
              <el-button
                type="primary"
                size="small"
                @click="saveEmployeeRoles"
                :loading="savingRoles"
                :disabled="!hasPermissionChanges"
              >
                儲存變更
              </el-button>
              <el-button size="small" @click="resetEmployeeRoles" :disabled="!hasPermissionChanges">
                重置
              </el-button>
            </div>
          </div>
        </template>

        <div v-if="!selectedEmployee" class="no-selection">
          <el-empty description="請選擇左側員工以編輯權限" />
        </div>

        <div v-else class="permission-editor">
          <!-- 第二區：權限編輯 (右上) - 員工基本資訊 -->
          <div class="employee-summary">
            <h4>員工基本資訊</h4>
            <el-descriptions :column="2" size="small" border>
              <el-descriptions-item label="姓名">
                {{ selectedEmployee.firstName }} {{ selectedEmployee.lastName }}
              </el-descriptions-item>
              <el-descriptions-item label="員工編號">
                {{ selectedEmployee.employeeNumber }}
              </el-descriptions-item>
              <el-descriptions-item label="電子郵件">
                {{ selectedEmployee.email || '未設定' }}
              </el-descriptions-item>
              <el-descriptions-item label="狀態">
                <el-tag :type="selectedEmployee.isActive ? 'success' : 'danger'" size="small">
                  {{ selectedEmployee.isActive ? '啟用' : '停用' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 第三區：權限變更偵測 (右側中間) -->
          <div class="permission-changes-section">
            <!-- 權限變更偵測 -->
            <div v-if="hasPermissionChanges" class="permission-changes">
              <el-alert
                title="偵測到權限變更"
                type="warning"
                :description="`將為 ${selectedEmployee.firstName} ${selectedEmployee.lastName} 更新角色配置`"
                show-icon
                :closable="false"
              />
            </div>

            <!-- 系統管理員特殊權限提示 -->
            <div v-if="isAdminSelected" class="admin-special-notice">
              <el-alert
                title="系統管理員特殊權限"
                type="success"
                :description="`${selectedEmployee.firstName} ${selectedEmployee.lastName} 擁有系統最高管理權限，自動包含以下所有模組的管理員權限`"
                show-icon
                :closable="false"
                class="admin-alert"
              />

              <div class="auto-enabled-modules">
                <h5>自動啟用的管理員權限：</h5>
                <div class="modules-grid">
                  <div
                    v-for="module in autoEnabledModules"
                    :key="module.role"
                    class="auto-module-item"
                  >
                    <el-tag type="success" size="default" class="auto-module-tag">
                      {{ module.name }}管理員
                    </el-tag>
                  </div>
                </div>
                <div class="admin-note">
                  <span>系統管理員無需額外選擇其他模組權限，已自動擁有所有管理權限</span>
                </div>
              </div>
            </div>

            <!-- 當前角色顯示 -->
            <div v-if="selectedRoles.length > 0" class="current-roles-display">
              <h5>當前分配的角色：</h5>
              <div class="current-roles-tags">
                <el-tag
                  v-for="roleName in selectedRoles"
                  :key="roleName"
                  :type="getRoleTagType(roleName)"
                  size="small"
                  class="role-tag-inline"
                >
                  {{ getRoleDisplayName(roleName) }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 第四區：模組權限分配 (右下) - 新設計 -->
          <div class="module-permission-section">
            <h4>模組權限分配</h4>
            <div class="permission-cards-grid">
              <!-- 機台管理模組 -->
              <el-card class="permission-module-card" shadow="hover">
                <div class="module-header">
                  <span class="module-title">機台管理</span>
                </div>
                <div class="permission-buttons">
                  <el-radio-group
                    v-model="modulePermissions.machine"
                    @change="updateSelectedRoles"
                    class="permission-radio-group"
                  >
                    <el-radio-button value="">無權限</el-radio-button>
                    <el-radio-button value="MACHINE_EMPLOYEE">員工</el-radio-button>
                    <el-radio-button value="MACHINE_MANAGER">管理員</el-radio-button>
                  </el-radio-group>
                </div>
              </el-card>

              <!-- 倉庫管理模組 -->
              <el-card class="permission-module-card" shadow="hover">
                <div class="module-header">
                  <span class="module-title">倉庫管理</span>
                </div>
                <div class="permission-buttons">
                  <el-radio-group
                    v-model="modulePermissions.warehouse"
                    @change="updateSelectedRoles"
                    class="permission-radio-group"
                  >
                    <el-radio-button value="">無權限</el-radio-button>
                    <el-radio-button value="WAREHOUSE_STAFF">員工</el-radio-button>
                    <el-radio-button value="WAREHOUSE_MANAGER">管理員</el-radio-button>
                  </el-radio-group>
                </div>
              </el-card>

              <!-- 工單管理模組 -->
              <el-card class="permission-module-card" shadow="hover">
                <div class="module-header">
                  <span class="module-title">工單管理</span>
                </div>
                <div class="permission-buttons">
                  <el-radio-group
                    v-model="modulePermissions.workorder"
                    @change="updateSelectedRoles"
                    class="permission-radio-group"
                  >
                    <el-radio-button value="">無權限</el-radio-button>
                    <el-radio-button value="WORKORDER_MANAGER">管理員</el-radio-button>
                  </el-radio-group>
                </div>
              </el-card>

              <!-- 採購管理模組 -->
              <el-card class="permission-module-card" shadow="hover">
                <div class="module-header">
                  <span class="module-title">採購管理</span>
                </div>
                <div class="permission-buttons">
                  <el-radio-group
                    v-model="modulePermissions.purchase"
                    @change="updateSelectedRoles"
                    class="permission-radio-group"
                  >
                    <el-radio-button value="">無權限</el-radio-button>
                    <el-radio-button value="PURCHASE_MANAGER">管理員</el-radio-button>
                  </el-radio-group>
                </div>
              </el-card>

              <!-- 請假管理模組 -->
              <el-card class="permission-module-card" shadow="hover">
                <div class="module-header">
                  <span class="module-title">請假管理</span>
                </div>
                <div class="permission-buttons">
                  <el-radio-group
                    v-model="modulePermissions.leave"
                    @change="updateSelectedRoles"
                    class="permission-radio-group"
                  >
                    <el-radio-button value="">無權限</el-radio-button>
                    <el-radio-button value="LEAVE_EMPLOYEE">員工</el-radio-button>
                    <el-radio-button value="LEAVE_MANAGER">主管</el-radio-button>
                    <el-radio-button value="SUPER_LEAVE_MANAGER">超級管理員</el-radio-button>
                  </el-radio-group>
                </div>
              </el-card>

              <!-- 行銷管理模組--展示用 -->
              <el-card class="permission-module-card" shadow="hover">
                <div class="module-header">
                  <span class="module-title">行銷管理 (展示)</span>
                </div>
                <div class="permission-buttons">
                  <el-radio-group
                    v-model="modulePermissions.marketing"
                    @change="updateSelectedRoles"
                    class="permission-radio-group"
                  >
                    <el-radio-button value="">無權限</el-radio-button>
                    <el-radio-button value="MARKETING_STAFF">員工</el-radio-button>
                    <el-radio-button value="MARKETING_MANAGER">管理員</el-radio-button>
                  </el-radio-group>
                </div>
              </el-card>

              <!-- 帳號管理模組 -->
              <el-card class="permission-module-card" shadow="hover">
                <div class="module-header">
                  <span class="module-title">帳號管理</span>
                </div>
                <div class="permission-buttons">
                  <el-radio-group
                    v-model="modulePermissions.account"
                    @change="updateSelectedRoles"
                    class="permission-radio-group"
                  >
                    <el-radio-button value="">無權限</el-radio-button>
                    <el-radio-button value="EMPLOYEE_ACCOUNT_MANAGER">管理員</el-radio-button>
                  </el-radio-group>
                </div>
              </el-card>

              <!-- 系統管理模組 -->
              <el-card class="permission-module-card" shadow="hover">
                <div class="module-header">
                  <span class="module-title">系統管理</span>
                </div>
                <div class="permission-buttons">
                  <el-radio-group
                    v-model="modulePermissions.admin"
                    @change="updateSelectedRoles"
                    class="permission-radio-group"
                  >
                    <el-radio-button value="">無權限</el-radio-button>
                    <el-radio-button value="ADMIN">管理員</el-radio-button>
                  </el-radio-group>
                </div>
              </el-card>
            </div>
            <div class="module-hint">每個模組只能選擇一個角色，可同時選擇多個模組</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, reactive } from 'vue'
import http from '../http-common'
import { ElMessage, ElMessageBox } from 'element-plus'

// 介面定義 - 參考 UserView.vue
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
  roles?: string[] // 新增角色欄位
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

// 響應式數據
const searchText = ref('')
const loadingUsers = ref(false)
const loadingRoles = ref(false)
const savingRoles = ref(false)

const employeeUsers = ref<EmployeeUserDto[]>([])
const availableRoles = ref<RoleDto[]>([])
const selectedEmployee = ref<EmployeeUserDto | null>(null)
const employeeRoles = ref<string[]>([])

// 模組化權限管理
const modulePermissions = ref({
  machine: '', // 機台管理
  warehouse: '', // 倉庫管理
  workorder: '', // 工單管理
  purchase: '', // 採購管理
  leave: '', // 請假管理
  marketing: '', // 行銷管理 (新增)
  account: '', // 帳號管理
  admin: '', // 系統管理
})

// API 基礎路徑 - 參考 UserView.vue
const API_BASE_URL = '/employee-users'

// 計算屬性
const filteredEmployees = computed(() => {
  return employeeUsers.value.filter((employee) => {
    const fullName = `${employee.firstName} ${employee.lastName}`
    const matchesSearch =
      !searchText.value ||
      fullName.toLowerCase().includes(searchText.value.toLowerCase()) ||
      employee.employeeNumber.toLowerCase().includes(searchText.value.toLowerCase())

    return matchesSearch
  })
})

// 選中的角色計算屬性
const selectedRoles = computed(() => {
  const roles: string[] = []
  Object.values(modulePermissions.value).forEach((role) => {
    if (role && role.trim() !== '') {
      roles.push(role)
    }
  })
  return roles
})

// 修正 selectedRoleDetails 計算屬性
const selectedRoleDetails = computed(() => {
  return selectedRoles.value.map((roleName) => {
    return {
      roleName,
      displayName: getRoleDisplayName(roleName),
      shortDescription: getRoleShortDescription(roleName),
      detailedDescription: getRoleDetailedDescription(roleName),
      permissionCount: getRolePermissions(roleName).length,
    }
  })
})

const hasPermissionChanges = computed(() => {
  if (!selectedEmployee.value || !selectedEmployee.value.roles) return false

  const originalRoles = [...(selectedEmployee.value.roles || [])]
  const currentRoles = [...selectedRoles.value]

  // 檢查角色是否有變更
  if (originalRoles.length !== currentRoles.length) return true

  const sortedOriginal = originalRoles.sort()
  const sortedCurrent = currentRoles.sort()

  return !sortedOriginal.every((role, index) => role === sortedCurrent[index])
})

// 檢查是否選擇了系統管理員
const isAdminSelected = computed(() => {
  return modulePermissions.value.admin === 'ADMIN'
})

// 獲取所有自動啟用的模組權限
const autoEnabledModules = computed(() => {
  if (!isAdminSelected.value) return []

  return [
    { name: '機台管理', role: 'MACHINE_MANAGER' },
    { name: '倉庫管理', role: 'WAREHOUSE_MANAGER' },
    { name: '工單管理', role: 'WORKORDER_MANAGER' },
    { name: '採購管理', role: 'PURCHASE_MANAGER' },
    { name: '請假管理', role: 'SUPER_LEAVE_MANAGER' }, // Changed to SUPER_LEAVE_MANAGER
    { name: '帳號管理', role: 'EMPLOYEE_ACCOUNT_MANAGER' },
  ]
})

// === 分層角色顯示系統 ===

// 1. 角色中文名稱
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
    LEAVE_EMPLOYEE: '請假員工',
    LEAVE_MANAGER: '請假主管',
    SUPER_LEAVE_MANAGER: '請假超級主管',
    MARKETING_STAFF: '行銷專員', // 新增
    MARKETING_MANAGER: '行銷經理', // 新增
  }
  return roleDisplayMap[roleName] || roleName
}

// 2. 簡短描述（用於下拉選單等緊湊空間）
const getRoleShortDescription = (roleName: string): string => {
  const shortDescriptionMap: Record<string, string> = {
    ADMIN: '系統最高權限管理',
    WAREHOUSE_STAFF: '倉庫日常作業執行',
    WAREHOUSE_MANAGER: '倉庫全面管理監督',
    WORKORDER_MANAGER: '工單與生產管理',
    PURCHASE_MANAGER: '採購與供應商管理',
    EMPLOYEE_ACCOUNT_MANAGER: '員工帳號權限管理',
    MACHINE_EMPLOYEE: '機台操作與維護申請',
    MACHINE_MANAGER: '設備維護與管理',
    LEAVE_EMPLOYEE: '請假申請與查詢',
    LEAVE_MANAGER: '請假審核與部門查詢',
    SUPER_LEAVE_MANAGER: '所有請假記錄管理',
    MARKETING_STAFF: '行銷活動執行與客戶開發', // 新增
    MARKETING_MANAGER: '行銷策略規劃與團隊管理', // 新增
  }
  return shortDescriptionMap[roleName] || '角色管理'
}

// 3. 詳細描述（用於詳情頁面）
const getRoleDetailedDescription = (roleName: string): string => {
  const detailedDescriptionMap: Record<string, string> = {
    ADMIN: '擁有系統最高管理權限，負責整體系統架構設定、用戶權限分配、所有功能模組的完整存取權限。',
    WAREHOUSE_STAFF:
      '執行倉庫日常作業管理，包含物料收發作業、庫存盤點執行、貨品分類整理、庫存異動記錄、出入庫單據處理等基礎倉儲管理工作。',
    WAREHOUSE_MANAGER:
      '倉庫管理監督職責，負責倉儲策略制定、庫存政策規劃、倉庫人員調度、作業流程優化、安全庫存管控、倉儲成本控制等高階倉庫管理業務。',
    WORKORDER_MANAGER:
      '工作訂單全程管理，負責生產工單建立、任務派工分配、物料需求申請審核、生產進度追蹤監控、工單狀態更新、生產效率分析等工單相關管理業務。',
    PURCHASE_MANAGER:
      '採購業務全面管理，包含供應商開發評選、採購申請審核、訂單下達追蹤、採購預算控制、供應商績效評估、採購成本分析等採購供應鏈管理。',
    EMPLOYEE_ACCOUNT_MANAGER:
      '員工系統帳號管理專責，負責新進員工帳號建立、離職帳號停用、權限角色設定、密碼政策管理、登入安全監控、帳號權限稽核等資訊安全管理。',
    MACHINE_EMPLOYEE:
      '機台基本操作執行，負責日常機台操作作業、設備狀態查看、維修申請提出、保養需求回報、操作異常記錄等基礎機台相關作業。',
    MACHINE_MANAGER:
      '生產設備綜合管理，包含機台設備建檔、預防保養排程、故障維修管理、設備狀態監控、設備效能分析等設備管理業務。',
    LEAVE_EMPLOYEE: '負責提交個人請假申請、查詢個人請假記錄、修改和刪除自己的請假申請。',
    LEAVE_MANAGER: '負責審核部門員工的請假申請、查看部門請假記錄，並擁有請假員工的所有權限。',
    SUPER_LEAVE_MANAGER: '擁有所有員工請假記錄的完整管理權限，包括查看、修改、刪除任何請假申請，並可管理假期制度。',
    MARKETING_STAFF: '負責執行指定的行銷活動，開發並維護潛在客戶名單，回報活動成效。擁有查看行銷活動與潛在客戶的權限。', // 新增
    MARKETING_MANAGER: '負責規劃與監督所有行銷活動，管理潛在客戶數據，並查看分析報告。擁有行銷模組的完整管理權限。', // 新增
  }
  return detailedDescriptionMap[roleName] || '此角色職責描述待補充'
}

// 4. 權限範圍說明（具體權限列表）
const getRolePermissions = (roleName: string): string[] => {
  const permissionsMap: Record<string, string[]> = {
    ADMIN: [
      '系統設定與配置管理',
      '用戶權限完整控制',
      '所有功能模組存取',
      '管理所有員工請假記錄', // Explicitly add for clarity
      //'系統日誌與稽核',
      //'資料備份與還原',
      //'安全政策管理',
    ],
    WAREHOUSE_STAFF: [
      '庫存即時查詢',
      '物料出入庫作業',
      '庫存盤點執行',
      '異動記錄維護',
      '基礎報表查看',
    ],
    WAREHOUSE_MANAGER: [
      '倉庫全權管理',
      '庫存策略制定',
      '倉庫人員管理',
      '作業流程優化',
      '庫存分析報表',
      '成本控制管理',
    ],
    WORKORDER_MANAGER: [
      '工單建立與維護',
      '派工任務分配',
      '物料申請審核',
      '生產進度監控',
      '工單狀態管理',
      '生產效率分析',
    ],
    PURCHASE_MANAGER: [
      '供應商管理',
      '採購申請審核',
      '採購訂單管理',
      '採購預算控制',
      '供應商評估',
      '採購分析報表',
    ],
    EMPLOYEE_ACCOUNT_MANAGER: [
      '員工帳號建立',
      '權限角色設定',
      '密碼政策管理',
      '帳號安全監控',
      //'登入記錄查看',
      '權限稽核管理',
    ],
    MACHINE_EMPLOYEE: [
      '設備狀態查看',
      '維修申請提出',
      '保養需求回報',
      '操作記錄維護',
      '基本設備操作',
    ],
    MACHINE_MANAGER: [
      '設備基本資料管理',
      '保養計畫排程',
      '維修記錄管理',
      '設備狀態監控',
      //'維修成本分析',
      //'設備效能報表',
    ],
    LEAVE_EMPLOYEE: [
      '提交個人請假申請',
      '查詢個人請假記錄',
      '修改自己的請假申請',
      '刪除自己的請假申請',
    ],
    LEAVE_MANAGER: [
      '審核部門員工請假申請',
      '查看部門請假記錄',
      '擁有請假員工所有權限', // Implies LEAVE_EMPLOYEE permissions
    ],
    SUPER_LEAVE_MANAGER: [
      '管理所有員工請假記錄',
      '查看所有請假記錄',
      '修改所有請假記錄',
      '刪除所有請假記錄',
      '管理假期制度', // Based on description in DataLoader
    ],
    MARKETING_STAFF: ['查看行銷活動', '查看潛在客戶'], // 新增
    MARKETING_MANAGER: ['管理行銷活動', '管理潛在客戶', '查看分析報告'], // 新增
  }
  return permissionsMap[roleName] || ['基本系統權限']
}

// 工具方法
const getRoleTagType = (role: string) => {
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
    MARKETING_MANAGER: 'success', // 新增
    MARKETING_STAFF: 'info', // 新增
  }
  return typeMap[role] || 'info'
}

const getEmployeeTypeDisplay = (type: string) => {
  const typeMap: Record<string, string> = {
    INTERNAL: '內部',
    SUPPLIER: '供應商',
    CONTRACTOR: '承包商',
    SYSTEM_ADMIN: '系統管理員',
  }
  return typeMap[type] || type
}

// API 方法 - 參考 UserView.vue 的實現
const fetchEmployeeUsers = async () => {
  loadingUsers.value = true
  try {
    const response = await http.get<EmployeeUserDto[]>(API_BASE_URL)

    // 為每個員工獲取角色資訊
    const employeesWithRoles = await Promise.all(
      response.data.map(async (user) => {
        try {
          if (user.employeeUserId) {
            const rolesResponse = await http.get<string[]>(
              `${API_BASE_URL}/${user.employeeUserId}/roles`,
            )
            user.roles = Array.isArray(rolesResponse.data) ? rolesResponse.data : []
          } else {
            user.roles = []
          }
        } catch (error) {
          console.warn(`無法載入員工 ${user.employeeUserId} 的角色:`, error)
          user.roles = []
        }
        return user
      }),
    )

    employeeUsers.value = employeesWithRoles
  } catch (error: any) {
    console.error('Error fetching employee users:', error)
    ElMessage.error(`獲取員工列表失敗: ${error.response?.data || error.message}`)
  } finally {
    loadingUsers.value = false
  }
}

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

const fetchEmployeeRoles = async (employeeId: number) => {
  if (!employeeId || typeof employeeId !== 'number' || employeeId <= 0) {
    console.error('無效的員工ID:', employeeId)
    employeeRoles.value = []
    convertRolesToModulePermissions([])
    return
  }

  loadingRoles.value = true
  try {
    const response = await http.get<string[]>(`${API_BASE_URL}/${employeeId}/roles`)

    if (Array.isArray(response.data)) {
      employeeRoles.value = response.data
      convertRolesToModulePermissions(response.data)
      console.log('Employee roles loaded:', response.data)
    } else {
      console.warn('員工角色回應格式不正確:', response.data)
      employeeRoles.value = []
      convertRolesToModulePermissions([])
    }
  } catch (error: any) {
    console.error('Error fetching employee roles:', error)
    ElMessage.error(
      `獲取員工角色失敗: ${error.response?.data?.message || error.response?.data || error.message}`,
    )
    employeeRoles.value = []
    convertRolesToModulePermissions([])
  } finally {
    loadingRoles.value = false
  }
}

const getCurrentUser = (): string => {
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      return user.username || user.name || 'unknown_user'
    }
  } catch (error) {
    console.warn('無法從 localStorage 獲取用戶資訊:', error)
  }

  const token = localStorage.getItem('token')
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      return payload.sub || payload.username || 'token_user'
    } catch (error) {
      console.warn('無法解析 token:', error)
    }
  }

  return 'system_admin'
}

const updateEmployeeRoles = async (employeeId: number, roleNames: string[]) => {
  if (!employeeId || typeof employeeId !== 'number' || employeeId <= 0) {
    throw new Error('無效的員工ID')
  }

  if (!Array.isArray(roleNames)) {
    throw new Error('角色名稱必須是陣列')
  }

  try {
    const currentUser = getCurrentUser()
    const request: UpdateEmployeeRolesRequest = {
      roleNames: roleNames.filter((name) => typeof name === 'string' && name.trim() !== ''),
      updatedBy: currentUser,
    }

    await http.put(`${API_BASE_URL}/${employeeId}/roles`, request)
    console.log('Employee roles updated successfully by:', currentUser, 'roles:', request.roleNames)
  } catch (error: any) {
    console.error('Error updating employee roles:', error)
    throw error
  }
}

// 模組權限轉換方法
const convertRolesToModulePermissions = (roles: string[]) => {
  // 重設所有模組權限
  modulePermissions.value = {
    machine: '',
    warehouse: '',
    workorder: '',
    purchase: '',
    leave: '',
    marketing: '', // 新增
    account: '',
    admin: '',
  }

  // 根據角色設定模組權限
  // Handle 'leave' module with priority
  if (roles.includes('SUPER_LEAVE_MANAGER')) {
    modulePermissions.value.leave = 'SUPER_LEAVE_MANAGER'
  } else if (roles.includes('LEAVE_MANAGER')) {
    modulePermissions.value.leave = 'LEAVE_MANAGER'
  } else if (roles.includes('LEAVE_EMPLOYEE')) {
    modulePermissions.value.leave = 'LEAVE_EMPLOYEE'
  }

  // Handle other modules (assuming they are mutually exclusive or simpler)
  roles.forEach((role) => {
    switch (role) {
      case 'MACHINE_EMPLOYEE':
      case 'MACHINE_MANAGER':
        modulePermissions.value.machine = role
        break
      case 'WAREHOUSE_STAFF':
      case 'WAREHOUSE_MANAGER':
        modulePermissions.value.warehouse = role
        break
      case 'WORKORDER_MANAGER':
        modulePermissions.value.workorder = role
        break
      case 'PURCHASE_MANAGER':
        modulePermissions.value.purchase = role
        break
      case 'MARKETING_STAFF': // 新增
      case 'MARKETING_MANAGER': // 新增
        modulePermissions.value.marketing = role
        break
      // 'leave' is handled above, so no 'case' for LEAVE roles here
      case 'EMPLOYEE_ACCOUNT_MANAGER':
        modulePermissions.value.account = role
        break
      case 'ADMIN':
        modulePermissions.value.admin = role
        break
    }
  })
}

// 更新選中角色
const updateSelectedRoles = () => {
  employeeRoles.value = selectedRoles.value
}

// 頁面操作方法
const selectEmployee = async (employee: EmployeeUserDto) => {
  selectedEmployee.value = employee

  if (employee.employeeUserId) {
    await fetchEmployeeRoles(employee.employeeUserId)
  } else {
    employeeRoles.value = []
    // 重設模組權限
    convertRolesToModulePermissions([])
  }
}

const saveEmployeeRoles = async () => {
  if (!selectedEmployee.value || !selectedEmployee.value.employeeUserId) {
    ElMessage.error('請選擇有效的員工')
    return
  }

  savingRoles.value = true
  try {
    const rolesToSave = selectedRoles.value
    await updateEmployeeRoles(selectedEmployee.value.employeeUserId, rolesToSave)

    // 更新本地員工數據
    const employeeIndex = employeeUsers.value.findIndex(
      (emp) => emp.employeeUserId === selectedEmployee.value!.employeeUserId,
    )
    if (employeeIndex !== -1) {
      employeeUsers.value[employeeIndex].roles = [...rolesToSave]
      // 同時更新 selectedEmployee 以保持一致性
      selectedEmployee.value.roles = [...rolesToSave]
    }

    // 更新 employeeRoles 以保持同步
    employeeRoles.value = [...rolesToSave]

    ElMessage.success('員工角色更新成功')
  } catch (error: any) {
    console.error('更新員工角色失敗:', error)
    const errorMsg =
      error.response?.data?.message || error.response?.data || error.message || '未知錯誤'
    ElMessage.error('更新員工角色失敗: ' + errorMsg)
  } finally {
    savingRoles.value = false
  }
}

const resetEmployeeRoles = () => {
  if (!selectedEmployee.value || !selectedEmployee.value.roles) return

  employeeRoles.value = [...selectedEmployee.value.roles]
  convertRolesToModulePermissions(selectedEmployee.value.roles)
  ElMessage.info('已重置為原始角色配置')
}

const focusFirstEmployee = () => {
  if (filteredEmployees.value.length > 0) {
    selectEmployee(filteredEmployees.value[0])
  }
}

// 初始化
onMounted(() => {
  fetchEmployeeUsers()
  fetchAvailableRoles()
})
</script>

<style scoped>
.permission-management {
  padding: 20px;
  min-height: 100vh;
}

/* 頂部工具欄 */
.toolbar-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.toolbar-title h3 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.toolbar-title p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 主要內容區域 */
.main-content {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 20px;
  min-height: calc(100vh - 200px);
  align-items: start;
}

/* 左側員工列表 */
.employee-list-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.employee-stats {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-info {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.employee-list {
  max-height: calc(100vh - 280px);
  overflow-y: auto;
  padding-right: 4px;
}

.employee-item {
  padding: 16px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 8px;
  margin-bottom: 8px;
}

.employee-item:hover {
  background: #f5f7fa;
  transform: translateX(2px);
}

.employee-item.selected {
  background: #e6f7ff;
  border-left: 4px solid #409eff;
  transform: translateX(4px);
}

.employee-info {
  width: 100%;
}

.employee-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.employee-details {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.employee-number {
  color: #606266;
  font-size: 13px;
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 12px;
  font-family: monospace;
}

.employee-roles {
  margin-top: 8px;
  min-height: 24px;
}

.no-roles {
  color: #c0c4cc;
  font-size: 12px;
  font-style: italic;
}

.no-data {
  padding: 60px 20px;
  text-align: center;
}

/* 右側權限編輯區 */
.permission-edit-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  height: fit-content;
  max-height: calc(100vh - 180px);
}

.permission-edit-card :deep(.el-card__body) {
  max-height: calc(100vh - 240px);
  overflow-y: auto;
  padding-right: 4px;
}

.no-selection {
  padding: 100px 20px;
  text-align: center;
}

.permission-editor {
  padding: 0;
}

/* 第二區：員工基本資訊 (右上) */
.employee-summary {
  margin-bottom: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
}

.employee-summary h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.employee-summary :deep(.el-descriptions) {
  font-size: 13px;
}

.employee-summary :deep(.el-descriptions__label) {
  font-size: 12px;
  font-weight: 500;
}

.employee-summary :deep(.el-descriptions__content) {
  font-size: 12px;
}

/* 第三區：權限變更偵測 (右側中間) */
.permission-changes-section {
  margin-bottom: 20px;
}

.permission-changes {
  margin-bottom: 16px;
}

.current-roles-display {
  margin-top: 16px;
  padding: 12px;
  background: #f0f7ff;
  border-radius: 6px;
  border-left: 3px solid #409eff;
}

.current-roles-display h5 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 13px;
  font-weight: 600;
}

.current-roles-tags {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.role-tag-inline {
  margin: 0;
  font-size: 11px;
  font-weight: 500;
}

/* 第四區：模組權限分配 (右下) */
.module-permission-section {
  margin-bottom: 20px;
}

.module-permission-section h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

/* 權限卡片網格佈局：2列 x 3行 */
/* .permission-cards-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  grid-template-rows: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
  align-items: stretch;
} */

.permission-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
  align-items: stretch;
}

/* 權限模組卡片樣式 */
.permission-module-card {
  height: 120px;
  width: 100%;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: all 0.3s ease;
  background: #ffffff;
  overflow: hidden;
}

.permission-module-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-1px);
}

/* 確保 el-card 的內部樣式 */
.permission-module-card :deep(.el-card__body) {
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

/* 模組標題在左上角 */
.module-header {
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  flex: 0 0 auto;
  margin-bottom: 8px;
}

.module-title {
  color: #303133;
  font-size: 15px;
  font-weight: 600;
  margin: 0;
  text-align: left;
}

/* 權限按鈕區域在卡片底部 */
.permission-buttons {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  flex: 1;
  padding-top: 8px;
}

.permission-radio-group {
  display: flex;
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* el-radio-button 樣式重新設計 */
.permission-radio-group :deep(.el-radio-button) {
  margin: 0;
  border: none;
  flex: 1;
}

.permission-radio-group :deep(.el-radio-button__inner) {
  background-color: #f3f3f3ea;
  border: none;
  border-right: 1px solid #e4e7ed;
  color: #606266;
  font-size: 13px;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 0;
  transition: all 0.3s ease;
  width: 100%;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
}

/* 最後一個按鈕去除右邊框 */
.permission-radio-group :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-right: none;
  border-radius: 0 5px 5px 0;
}
/* 第一個按鈕左側圓角 */
.permission-radio-group :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 5px 0 0 5px;
}
/* 懸停效果 */
.permission-radio-group :deep(.el-radio-button__inner):hover {
  background-color: #e6f7ff;
  color: #409eff;
  transform: translateY(-1px);
}

/* 選中狀態：藍色背景 */
.permission-radio-group :deep(.el-radio-button.is-active .el-radio-button__inner) {
  background-color: #409eff;
  color: #ffffff;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.3);
  border-color: #409eff;
}

/* 去除 Element Plus 預設的按鈕組樣式 */
/* .permission-radio-group :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 0;
} */

.permission-radio-group :deep(.el-radio-button__orig-radio) {
  opacity: 0;
  outline: none;
  position: absolute;
  z-index: -1;
}
/* 確保按鈕文字居中 */
.permission-radio-group :deep(.el-radio-button__inner span) {
  display: block;
  width: 100%;
  text-align: center;
}
/* 提示文字 */
.module-hint {
  margin-top: 12px;
  color: #909399;
  font-size: 12px;
  text-align: center;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 6px;
  line-height: 1.4;
  border: 1px solid #e4e7ed;
}

/* 響應式設計 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 320px 1fr;
  }

  .permission-cards-grid {
    gap: 16px;
  }

  .permission-module-card {
    height: 130px;
  }

  .module-title {
    font-size: 15px;
  }

  .permission-radio-group :deep(.el-radio-button__inner) {
    font-size: 12px;
    padding: 8px 12px;
  }
}

@media (max-width: 768px) {
  .permission-management {
    padding: 12px;
  }

  .main-content {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .toolbar {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .toolbar-actions {
    width: 100%;
    justify-content: space-between;
  }

  .employee-list {
    max-height: 400px;
  }

  .permission-edit-card {
    max-height: none;
  }

  .permission-edit-card :deep(.el-card__body) {
    max-height: none;
    overflow-y: visible;
  }

  .permission-cards-grid {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
    gap: 12px;
  }

  .permission-module-card {
    height: 120px;
  }

  .permission-module-card :deep(.el-card__body) {
    padding: 12px;
  }

  .module-title {
    font-size: 14px;
  }

  .permission-radio-group :deep(.el-radio-button__inner) {
    font-size: 12px;
    padding: 8px 10px;
    min-width: 50px;
  }

  .module-title {
    font-size: 13px;
  }

  .module-hint {
    font-size: 12px;
  }

  .role-cards-grid {
    grid-template-columns: 1fr;
    gap: 6px;
  }

  .current-roles-display {
    margin-top: 12px;
    padding: 10px;
  }

  .current-roles-tags {
    justify-content: flex-start;
  }

  .employee-summary {
    padding: 12px;
  }

  .admin-special-notice {
    padding: 12px;
  }
}

@media (max-width: 480px) {
  .permission-cards-grid {
    gap: 10px;
  }

  .permission-module-card {
    height: 110px;
  }

  .permission-module-card :deep(.el-card__body) {
    padding: 10px;
  }

  .module-title {
    font-size: 13px;
  }

  .permission-radio-group :deep(.el-radio-button__inner) {
    font-size: 11px;
    padding: 6px 8px;
    min-width: 40px;
  }

  .module-hint {
    font-size: 11px;
    padding: 3px;
  }

  .role-tag-inline {
    font-size: 10px;
  }
}

/* 滾動條樣式 */
.employee-list::-webkit-scrollbar,
.permission-edit-card :deep(.el-card__body)::-webkit-scrollbar {
  width: 6px;
}

.employee-list::-webkit-scrollbar-track,
.permission-edit-card :deep(.el-card__body)::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.employee-list::-webkit-scrollbar-thumb,
.permission-edit-card :deep(.el-card__body)::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.employee-list::-webkit-scrollbar-thumb:hover,
.permission-edit-card :deep(.el-card__body)::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 動畫效果 */
.employee-item {
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 按鈕組樣式 */
.permission-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.permission-changes {
  margin-bottom: 16px;
}

/* Element Plus 組件樣式調整 */
:deep(.el-descriptions__body .el-descriptions__table) {
  border-radius: 8px;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-card__header) {
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-divider__text) {
  background: #fff;
  font-weight: 600;
  color: #303133;
}

:deep(.el-alert) {
  border-radius: 8px;
}

/* 系統管理員特殊提示樣式 */
.admin-special-notice {
  margin-top: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 1px solid #0ea5e9;
  border-radius: 8px;
}

.admin-alert {
  margin-bottom: 16px;
  border-radius: 8px;
}

.admin-alert :deep(.el-alert__title) {
  font-size: 16px;
  font-weight: 600;
}

.auto-enabled-modules h5 {
  margin: 0 0 12px 0;
  color: #059669;
  font-size: 14px;
  font-weight: 600;
}

.modules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 8px;
  margin-bottom: 16px;
}

.auto-module-item {
  display: flex;
  justify-content: center;
}

.auto-module-tag {
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 6px;
}

.admin-note {
  padding: 12px;
  background: rgba(5, 150, 105, 0.1);
  border: 1px solid rgba(5, 150, 105, 0.2);
  border-radius: 6px;
  color: #059669;
  font-size: 13px;
  text-align: center;
}

/* 響應式設計調整 */
@media (max-width: 768px) {
  .modules-grid {
    grid-template-columns: 1fr;
  }

  .admin-special-notice {
    padding: 16px;
  }

  .auto-module-tag {
    padding: 4px 8px;
    font-size: 12px;
  }
}
</style>