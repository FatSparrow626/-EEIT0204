import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useAuthStore } from '../stores/AuthStore' // Import the auth store

// 引入 zt 功能頁面
import AddOrder from '@/views/zt/AddOrder.vue'
import OrderList from '@/views/zt/OrderList.vue'
import SupplierList from '@/views/zt/SupplierList.vue'
import AddSupplier from '@/views/zt/AddSupplier.vue'
import EditSupplier from '@/views/zt/EditSupplier.vue'
import EditOrder from '@/views/zt/EditOrder.vue'
import Report from '@/views/zt/Report.vue'

// 引入 kh 功能頁面
import AddLeaveApplication from '@/views/kh/AddLeaveApplication.vue'
import LeaveListView from '@/views/kh/LeaveListView.vue'
import EditLeaveView from '@/views/kh/EditLeaveView.vue'

// 引入 ch 功能路由
import MyAssignments from '@/views/ch/assignments/MyAssignments.vue'
import MaintenanceAdminList from '@/views/ch/maintenance/MaintenanceAdminList.vue'
import MaintenanceUserList from '@/views/ch/maintenance/MaintenanceUserList.vue'
import MaintenanceForm from '@/views/ch/maintenance/MaintenanceForm.vue'
import RepairAdminList from '@/views/ch/repair/RepairAdminList.vue'
import RepairUserList from '@/views/ch/repair/RepairUserList.vue'
import RepairForm from '@/views/ch/repair/RepairForm.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true }, // Protect this route
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'), // Lazy load LoginView
    },

    {
      path: '/products',
      name: 'products',
      // route level code-splitting
      // this generates a separate chunk (Products.[hash].js) for this route
      // which lazy-loaded when the route is visited.
      component: () => import('../views/ProductsView.vue'),
      meta: { requiresAuth: true }, // Protect this route
    },
    //物料庫存表
    {
      path: '/materials',
      name: 'Materials',
      component: () => import('../views/MaterialsList.vue'),
    },
    {
      path: '/depot',
      name: 'Depot',
      component: () => import('../views/MaterialsList.vue'),
    },
    //入庫單列表
    {
      path: '/inbound-receipts',
      name: 'InboundReceipts',
      component: () => import('../views/InboundReceiptList.vue'),
    },

    {
      path: '/outbound',
      name: 'outbound',
      component: () => import('../views/OutboundView.vue'),
      meta: { requiresAuth: true }, // Protect this route
    },
    {
      path: '/inventory-logs',
      name: 'inventory-logs',
      component: () => import('../views/InventoryLogView.vue'),
      meta: { requiresAuth: true }, // Protect this route
    },

    {
      path: '/users',
      name: 'users',
      component: () => import('../views/UserView.vue'),
      meta: { requiresAuth: true }, // Protect this route
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/UserProfileView.vue'),
      meta: { requiresAuth: true }, // Protect this route
    },
 {
      path: '/personal-center',
      name: 'personal-center',
      component: () => import('../views/EmployeeProfile.vue'),
      meta: { requiresAuth: true } // Protect this route
    },

    {
      path: '/permission-management',
      name: 'PermissionManagement',
      component: () => import('../views/PermissionManagement.vue'),
      meta: { requiresAuth: true }, // Protect this route
    },
    // ===  zt 功能路由 (全部保留原本網址) ===
    {
      path: '/zt/order/insert',
      name: 'AddOrder',
      component: AddOrder,
      meta: { requiresAuth: true },
    },
    {
      path: '/zt/order/list',
      name: 'OrderList',
      component: OrderList,
      meta: { requiresAuth: true },
    },
    {
      path: '/zt/supplier/list',
      name: 'SupplierList',
      component: SupplierList,
      meta: { requiresAuth: true },
    },
    {
      path: '/zt/supplier/add',
      name: 'AddSupplier',
      component: AddSupplier,
      meta: { requiresAuth: true },
    },
    {
      path: '/zt/order/edit/:orderId',
      name: 'EditOrder',
      component: EditOrder,
      meta: { requiresAuth: true },
    },
    {
      path: '/zt/supplier/edit/:id',
      name: 'EditSupplier',
      component: EditSupplier,
      meta: { requiresAuth: true },
    },
    {
      path: '/zt/report',
      name: 'Report',
      component: Report,
      meta: { requiresAuth: true },
    },
    //通知
    {
      path: '/notifications',
      name: 'Notifications',
      component: () => import('../views/NotificationList.vue'),
    },
    // ===  kh 功能路由 (全部保留原本網址) ===
    {
      path: '/kh/leave-application',
      name: 'AddLeaveApplication',
      component: AddLeaveApplication,
      meta: { requiresAuth: true },
    },
    {
      path: '/kh/leave/list',
      name: 'LeaveListView',
      component: LeaveListView,
      meta: { requiresAuth: true },
    },
    {
      path: '/kh/leave/edit/:uuid',
      name: 'EditLeaveView',
      component: EditLeaveView,
      meta: { requiresAuth: true },
    },
    // ===  ch 功能路由 (全部保留原本網址) ===
   
    // 後端機台管理
    {
      path: '/machine/adminmachine',
      name: 'machine-admin',
      component: () => import('../views/ch/machine/MachineAdmin.vue'),
      meta: { requiresAuth: true },
    },
    // 後端機台新增
    {
      path: '/machine/machineform',
      name: 'machine-form',
      component: () => import('../views/ch/machine/MachineForm.vue'),
      meta: { requiresAuth: true },
    },
    // 前端機台管理
    {
      path: '/machine/usermachine',
      name: 'machine-user',
      component: () => import('../views/ch/machine/MachineUser.vue'),
      meta: { requiresAuth: true },
    },
    // 維修表單
    {
      path: '/repair/repairform',
      name: 'repair-form',
      component: () => import('../views/ch/repair/RepairForm.vue'),
      meta: { requiresAuth: true },
    },
    // 維修列表
    {
      path: '/repair/repairuserlist',
      name: 'repair-userlist',
      component: () => import('../views/ch/repair/RepairUserList.vue'),
      meta: { requiresAuth: true },
    },
    // 管理員維修列表
    {
      path: '/repair/repairadminlist',
      name: 'repair-adminlist',
      component: () => import('../views/ch/repair/RepairAdminList.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/maintenance/adminmaintenanceform',
      name: 'maintance-adminmaintenanceform',
      component: () => import('../views/ch/maintenance/MaintenanceForm.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/maintenance/maintenanceuserlist',
      name: 'maintance-maintenanceuserlist',
      component: () => import('../views/ch/maintenance/MaintenanceUserList.vue'),
    },
    {
      path: '/maintenance/maintenanceadminlist',
      name: 'maintance-maintenanceadminlist',
      component: () => import('../views/ch/maintenance/MaintenanceAdminList.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/workorderfinish',
      name: 'workorderfinish',
      component: () => import('../views/ch/WorkOrder/WorkOrderView.vue'),
      meta: { requiresAuth: true },
    }, 
    {
      path: '/my-assignments',
      name: 'my-assignments',
      component: MyAssignments,
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const publicPages = ['/login'] // Pages that don't require authentication
  const authRequired = !publicPages.includes(to.path)
  const loggedIn = authStore.loggedIn

  if (authRequired && !loggedIn) {
    next('/login') // Redirect to login page if not logged in and trying to access a protected page
  } else {
    next() // Continue to the requested page
  }
})

export default router
