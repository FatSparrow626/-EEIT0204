<template>
  <div class="common-layout">
    <el-container style="height: 100vh">
      <el-aside
        v-if="loggedIn"
        :width="isCollapsed ? '80px' : '200px'"
        class="sidebar-container"
        :class="{ 'is-collapsed': isCollapsed }"
      >
        <!-- Sidebar Header -->
        <div class="sidebar-header">
          <div v-if="!isCollapsed" class="sidebar-title">工業管理系統</div>
          <div v-else class="sidebar-title-collapsed">
            <el-icon><Box /></el-icon>
          </div>
        </div>

        <!-- Scrollable Menu -->
        <div class="sidebar-menu-container">
          <div
            v-if="
              !isCollapsed &&
              (hasAnyPermission(['INVENTORY_MANAGE', 'INVENTORY_VIEW']) || isAdmin())
            "
            class="menu-subtitle"
          >
            倉管管理 模組
          </div>
          <el-menu
            :collapse="isCollapsed"
            :default-active="$route.path"
            class="el-menu-vertical-demo"
            router
          >
            <el-menu-item index="/">
              <el-icon><House /></el-icon>
              <template #title>首頁</template>
            </el-menu-item>

            <el-menu-item index="/personal-center">
              <el-icon><Setting /></el-icon>
              <template #title>個人中心</template>
            </el-menu-item>

            <el-menu-item
              index="/depot"
              v-if="
                (hasPermission('INVENTORY_VIEW')) || isAdmin()
              "
            >
              <el-icon><Box /></el-icon>
              <template #title>庫存管理</template>
            </el-menu-item>
            <el-menu-item index="/inbound-receipts" v-if="hasPermission('INVENTORY_VIEW')|| isAdmin() " >
              <el-icon><List /></el-icon>
              <template #title>入庫單列表</template>
            </el-menu-item>
            <el-menu-item
              index="/products"
              v-if="
                (hasPermission('INVENTORY_VIEW') ) || isAdmin()
              "
            >
              <el-icon><Goods /></el-icon>
              <template #title>成品生產</template>
            </el-menu-item>

            <el-menu-item
              index="/outbound"
              v-if="
                (hasPermission('WORKORDER_MANAGER')) || isAdmin()
              "
            >
              <el-icon><Goods /></el-icon>
              <template #title>派工工單</template>
            </el-menu-item>

            <el-menu-item index="/users" v-if="hasAnyPermission(['ADMIN', 'EMPLOYEE_VIEW'])">
              <el-icon><User /></el-icon>
              <template #title>使用者管理</template>
            </el-menu-item>

            <el-menu-item
              index="/permission-management"
              v-if="hasAnyPermission(['ADMIN', 'EMPLOYEE_MANAGE'])"
            >
              <el-icon><Lock /></el-icon>
              <template #title>權限管理</template>
            </el-menu-item>
            <el-menu-item
              index="/inventory-logs"
              class="menu-item-divider"
              v-if="hasPermission('SYSTEM_LOG_VIEW')"
            >
              <el-icon><Compass /></el-icon>
              <template #title>歷程記錄</template>
            </el-menu-item>
            <!-- === ZT 模組路由 === -->
            <div
              v-if="
                !isCollapsed &&
                (hasPermission('SUPPLIER_VIEW') ||
                  hasPermission('SUPPLIER_MANAGE') ||
                  hasPermission('REPORT_VIEW'))
              "
              class="menu-subtitle"
            >
              採購清單 模組
            </div>
            <el-menu-item index="/zt/order/insert" v-if="hasPermission('SUPPLIER_MANAGE')">
              <el-icon><Edit /></el-icon>
              <template #title>新增訂單</template>
            </el-menu-item>
            <el-menu-item index="/zt/order/list" v-if="hasPermission('SUPPLIER_VIEW')">
              <el-icon><List /></el-icon>
              <template #title>訂單列表</template>
            </el-menu-item>
            <el-menu-item index="/zt/supplier/list" v-if="hasPermission('SUPPLIER_VIEW')">
              <el-icon><OfficeBuilding /></el-icon>
              <template #title>供應商列表</template>
            </el-menu-item>
            <el-menu-item index="/zt/report" v-if="hasPermission('REPORT_VIEW')">
              <el-icon><DataAnalysis /></el-icon>
              <template #title>統計表</template>
            </el-menu-item>
            <!--  === KH 模組路由 === -->
            <div v-if="!isCollapsed && (hasPermission('LEAVE_APPLY_SELF') || hasPermission('LEAVE_VIEW_SELF') || hasPermission('LEAVE_VIEW_DEPARTMENT') || hasPermission('LEAVE_APPROVE'))" class="menu-subtitle">人力資源 模組</div>
            <el-menu-item index="/kh/leave/list" v-if="hasPermission('LEAVE_VIEW_SELF') || hasPermission('LEAVE_VIEW_DEPARTMENT')">
              <el-icon><Memo /></el-icon>
              <template #title>請假列表</template>
            </el-menu-item>

            <!-- === Marketing Department Demo Module === -->
            <div 
              v-if="!isCollapsed && hasAnyPermission(['MARKETING_CAMPAIGN_VIEW', 'MARKETING_CAMPAIGN_MANAGE', 'MARKETING_LEADS_VIEW', 'MARKETING_REPORT_VIEW'])"
              class="menu-subtitle"
            >
              行銷管理 模組 (展示)
            </div>
            <el-menu-item index="/marketing/campaigns" v-if="hasAnyPermission(['MARKETING_CAMPAIGN_VIEW', 'MARKETING_CAMPAIGN_MANAGE'])">
              <el-icon><DataLine /></el-icon>
              <template #title>行銷活動</template>
            </el-menu-item>
            <el-menu-item index="/marketing/leads" v-if="hasPermission('MARKETING_LEADS_VIEW')">
              <el-icon><User /></el-icon>
              <template #title>潛在客戶</template>
            </el-menu-item>
            <el-menu-item index="/marketing/reports" v-if="hasPermission('MARKETING_REPORT_VIEW')">
              <el-icon><PieChart /></el-icon>
              <template #title>分析報告</template>
            </el-menu-item>

            <!-- === ch 模組路由 === -->
            
            <!-- === 機台管理 === -->
            <div
              v-if="
                !isCollapsed && (hasAnyPermission(['MACHINE_MANAGE', 'MACHINE_VIEW']) || isAdmin())
              "
              class="menu-subtitle"
            >
              機台管理 模組
            </div>

            <el-menu-item
              index="/machine/machineform"
              v-if="hasPermission('MACHINE_MANAGE') || isAdmin()"
            >
              <el-icon><Edit /></el-icon>
              <template #title>新增機台</template>
            </el-menu-item>

            <el-menu-item
              index="/machine/adminmachine"
              v-if="hasPermission('MACHINE_MANAGE') || isAdmin()"
            >
              <el-icon><Setting /></el-icon>
              <template #title>後台機台管理</template>
            </el-menu-item>

            <el-menu-item
              index="/machine/usermachine"
              v-if="
                (hasPermission('MACHINE_VIEW') && !hasPermission('MACHINE_MANAGE')) || isAdmin()
              "
            >
              <el-icon><Monitor /></el-icon>
              <template #title>前台機台查詢</template>
            </el-menu-item>

            <!-- === 維修管理 === -->
            <div
              v-if="
                !isCollapsed && (hasAnyPermission(['MACHINE_MANAGE', 'MACHINE_VIEW']) || isAdmin())
              "
              class="menu-subtitle"
            >
              維修管理 模組
            </div>

            <el-menu-item
              index="/repair/repairform"
              v-if="
                (hasPermission('MACHINE_VIEW') && !hasPermission('MACHINE_MANAGE')) || isAdmin()
              "
            >
              <el-icon><Tools /></el-icon>
              <template #title>維修表單</template>
            </el-menu-item>

            <el-menu-item
              index="/repair/repairuserlist"
              v-if="
                (hasPermission('MACHINE_VIEW') && !hasPermission('MACHINE_MANAGE')) || isAdmin()
              "
            >
              <el-icon><List /></el-icon>
              <template #title>使用者維修列表</template>
            </el-menu-item>

            <el-menu-item
              index="/repair/repairadminlist"
              v-if="hasPermission('MACHINE_MANAGE') || isAdmin()"
            >
              <el-icon><Briefcase /></el-icon>
              <template #title>管理員維修列表</template>
            </el-menu-item>

            <!-- === 保養管理 === -->
            <div
              v-if="
                !isCollapsed && (hasAnyPermission(['MACHINE_MANAGE', 'MACHINE_VIEW']) || isAdmin())
              "
              class="menu-subtitle"
            >
              保養管理 模組
            </div>

            <el-menu-item
              index="/maintenance/adminmaintenanceform"
              v-if="hasPermission('MACHINE_MANAGE') || isAdmin()"
            >
              <el-icon><Edit /></el-icon>
              <template #title>新增保養單</template>
            </el-menu-item>

            <el-menu-item
              index="/maintenance/maintenanceuserlist"
              v-if="
                (hasPermission('MACHINE_VIEW') && !hasPermission('MACHINE_MANAGE')) || isAdmin()
              "
            >
              <el-icon><List /></el-icon>
              <template #title>使用者保養列表</template>
            </el-menu-item>

            <el-menu-item
              index="/maintenance/maintenanceadminlist"
              v-if="hasPermission('MACHINE_MANAGE') || isAdmin()"
            >
              <el-icon><Briefcase /></el-icon>
              <template #title>管理員保養列表</template>
            </el-menu-item>

            <el-menu-item index="/workorderfinish" v-if="hasPermission('MACHINE_MANAGE') || isAdmin()">
              <el-icon><Briefcase /></el-icon>
              <template #title>完成工單</template>
            </el-menu-item>
            <el-menu-item
              index="/my-assignments"
              v-if="hasPermission('MACHINE_VIEW') || isAdmin()"
            >
              <el-icon><Briefcase /></el-icon>
              <template #title>我的任務</template>
            </el-menu-item>
          </el-menu>
        </div>

        <!-- Sidebar Footer -->
        <div class="sidebar-footer">
          <div v-if="!isCollapsed">
            <span>{{ currentUser?.username }}</span>
          </div>
          <el-icon v-else><User /></el-icon>
        </div>
      </el-aside>

      <el-container>
        <el-header v-if="loggedIn" class="main-header">
          <div class="header-content">
            <div class="header-left-panel">
              <el-button
                @click="isCollapsed = !isCollapsed"
                text
                circle
                class="collapse-icon-button"
              >
                <el-icon size="20">
                  <component :is="isCollapsed ? ArrowRight : ArrowLeft" />
                </el-icon>
              </el-button>
              <div class="header-title">
                <span></span>
              </div>
            </div>
            <div class="header-right-panel">
              <div class="header-info">
                <span>{{ currentTime }}</span>
                <el-button type="text" @click="router.push('/personal-center')">
                  <span>{{ currentUser?.username }}</span>
                </el-button>
              </div>
              <el-input
                v-model="searchQuery"
                placeholder="Search..."
                :prefix-icon="Search"
                class="header-search-input"
              />
              <NotificationBell />

              <el-button @click="isDarkMode = !isDarkMode" text circle>
                <el-icon size="20">
                  <component :is="isDarkMode ? Sunny : Moon" />
                </el-icon>
              </el-button>
              <el-button @click="handleLogout" type="danger" text circle>
                <el-icon size="20"><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, onMounted, onUnmounted, computed } from 'vue'
import {
  House,
  InfoFilled,
  Goods,
  User,
  ArrowLeft,
  ArrowRight,
  Box,
  Search,
  Sunny,
  Moon,
  Switch,
  Compass,
  Cpu,
  Setting,
} from '@element-plus/icons-vue'
import NotificationBell from '@/components/NotificationBell.vue'
import { useAuthStore } from './stores/AuthStore'
import { useRouter } from 'vue-router'

const isCollapsed = ref(false)
const searchQuery = ref('')
const isDarkMode = ref(false)
const currentTime = ref('')

const authStore = useAuthStore()
const router = useRouter()

const loggedIn = computed(() => authStore.loggedIn)
const currentUser = computed(() => authStore.currentUser)

const isAdmin = () => currentUser.value?.employeeType === 'SYSTEM_ADMIN'

const hasPermission = (permission: string) => {
  return currentUser.value?.authorities?.includes(permission) || false
}
const hasAnyPermission = (permissions: string[]) => {
  return permissions.some((permission) => hasPermission(permission))
}

// console.log(' 除錯 ')
// console.log('currentUser:', currentUser.value)
// console.log('employeeType:', currentUser.value?.employeeType)
// console.log('isAdmin():', isAdmin())
// console.log('hasPermission("MACHINE_VIEW"):', hasPermission('MACHINE_VIEW'))
// console.log('hasPermission("MACHINE_MANAGE"):', hasPermission('MACHINE_MANAGE'))
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString()
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

let timer: number
onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
})

watch(isDarkMode, (newVal) => {
  if (newVal) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
})
</script>

<style>
/* Reset body and app styles for full-screen layout */
body {
  margin: 0;
  background-color: #fcfcfc;
}

#app {
  max-width: 100vw;
  padding: 0;
  margin: 0;
}

.sidebar-container {
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 20px);
  margin: 10px 0 10px 10px;
  border-radius: 10px;
  background-color: #f5f5f5;
  box-sizing: border-box;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 10px;
  text-align: center;
  flex-shrink: 0;
  transition: padding 0.3s ease;
}

.sidebar-container.is-collapsed .sidebar-header {
  padding: 10px 0;
}

.sidebar-title {
  padding: 10px 0;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-title-collapsed {
  padding: 10px 0;
  font-size: 28px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #303133;
}

.sidebar-menu-container {
  flex-grow: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 0 10px;
}

.menu-subtitle {
  font-size: 12px;
  color: #909399;
  padding: 10px 20px;
  transition: all 0.3s ease;
}

.sidebar-container.is-collapsed .menu-subtitle {
  text-align: center;
  padding: 10px 0;
}

.sidebar-footer {
  padding: 20px;
  text-align: center;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.el-menu {
  border-right: none !important;
  background-color: transparent !important;
  padding-top: 8px;
}

.el-menu-item {
  margin: 8px 0;
  border-radius: 8px;
  height: 48px;
  line-height: 48px;
  transition: all 0.3s ease;
}

.el-menu-item:hover {
  background-color: #dcdfe6;
}

.el-menu-item.is-active {
  background-color: #dcdfe6;
}

.el-menu-item .el-icon {
  font-size: 20px;
  color: #303133;
}

.el-menu-item.is-active .el-icon {
  /* Color will be inherited */
}

.el-menu--collapse .el-menu-item {
  height: 44px;
  width: 44px;
  margin: 8px auto;
  padding: 0 !important;
}

.el-menu--collapse .el-menu-item .el-tooltip__trigger {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
}

.el-menu--collapse ~ .sidebar-footer {
  padding: 16px 0;
}

.sidebar-footer .el-icon {
  font-size: 22px;
}

.main-header {
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background-color: #fff;
  border-radius: 10px;
  margin: 10px 10px 0 10px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.header-left-panel,
.header-right-panel {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: bold;
}

.header-logo {
  width: 32px;
  height: 32px;
  margin-right: 10px;
}

.collapse-icon-button {
  font-size: 24px;
}

.header-search-input {
  width: 240px;
}

.el-main {
  padding: 20px;
  background-color: #fcfcfc;
  border-radius: 10px;
  margin: 10px;
}

.menu-item-divider {
  border-bottom: 1px solid #dcdfe6;
  margin-bottom: 5px;
  padding-bottom: 5px;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

/* Professional Brown Theme Palette with Higher Specificity */
html.dark body {
  background-color: var(--color-background);
  color: var(--color-text);
}

html.dark .sidebar-container {
  background-color: #e0e0e0;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-right: none;
}

html.dark .sidebar-header,
html.dark .sidebar-footer {
  border-color: var(--color-border);
}

html.dark .sidebar-title,
html.dark .sidebar-title-collapsed,
html.dark .sidebar-footer {
  color: var(--color-heading);
}

html.dark .el-menu-item {
  color: var(--color-text);
}

html.dark .el-menu-item .el-icon {
  color: var(--color-text);
}

html.dark .el-menu-item:hover {
  background-color: var(--color-background-mute);
}

html.dark .el-menu-item.is-active {
  background-color: var(--color-background-mute);
  color: var(--color-heading);
}

html.dark .main-header {
  background-color: var(--color-background-soft);
  border-bottom-color: var(--color-border);
}

html.dark .header-title,
html.dark .el-input__inner,
html.dark .el-button {
  color: var(--color-text);
}

html.dark .el-input__wrapper {
  background-color: var(--color-background);
  box-shadow: none;
}

html.dark .el-main {
  background-color: var(--color-background);
}

.el-main {
  height: calc(100vh - 90px);
  overflow: auto;
  padding: 20px;
  background-color: #fcfcfc;
  border-radius: 10px;
  margin: 10px;
}

.el-main {
  height: calc(100vh - 90px);
  overflow: auto;
  padding: 20px;
  background-color: #fcfcfc;
  border-radius: 10px;
  margin: 10px;
}

html.dark .menu-item-divider {
  border-bottom-color: var(--color-border);
}

/* Brown Theme for El-Table */
html.dark .el-table {
  background-color: var(--color-background-soft) !important;
  color: var(--color-text) !important;
  border-radius: 10px;
}

html.dark .el-table th,
html.dark .el-table tr {
  background-color: transparent !important;
  color: var(--color-text);
}

html.dark .el-table td,
html.dark .el-table th.is-leaf {
  border-bottom: 1px solid var(--color-border) !important;
}

html.dark .el-table--enable-row-hover .el-table__body tr:hover > td {
  background-color: var(--color-background-mute) !important;
}

html.dark .el-table__header-wrapper th {
  background-color: var(--color-background-soft) !important;
  color: var(--color-heading) !important;
}

/* Brown Theme for El-Card */
html.dark .el-card {
  background-color: var(--color-background-soft) !important;
  color: var(--color-text) !important;
  border: 1px solid var(--color-border) !important;
  border-radius: 10px;
}

html.dark .el-card__header {
  border-bottom: 1px solid var(--color-border) !important;
  color: var(--color-heading) !important;
}
</style>
