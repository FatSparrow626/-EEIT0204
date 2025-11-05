<template>
  <div class="dashboard-container">
    <!-- Header -->
    <div class="dashboard-header">
      <h1>工廠營運總覽</h1>
      <span v-if="lastUpdated" class="last-updated">最後更新: {{ lastUpdated }}</span>
    </div>

    <!-- Global KPIs -->
    <el-row :gutter="24" class="global-kpi-row">
      <el-col :xs="24" :sm="12" :lg="8">
        <div class="global-kpi-card kpi-oee">
          <div class="kpi-value">{{ dashboardData.global.oee }}<span class="kpi-unit">%</span></div>
          <div class="kpi-title">產線綜合效率 (OEE)</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="8">
        <div class="global-kpi-card kpi-otd">
          <div class="kpi-value">{{ dashboardData.global.onTimeDelivery }}<span class="kpi-unit">%</span></div>
          <div class="kpi-title">準時交付率 (OTD)</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="24" :lg="8">
        <div class="global-kpi-card kpi-yield">
          <div class="kpi-value">{{ dashboardData.global.firstPassYield }}<span class="kpi-unit">%</span></div>
          <div class="kpi-title">一次通過率 (FPY)</div>
        </div>
      </el-col>
    </el-row>

    <!-- Module Details -->
    <el-row :gutter="24">
      <!-- Production Core -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="kpi-card card-production" @click="navigateTo('/workorderfinish')">
          <template #header><div class="card-header"><span><el-icon><Cpu /></el-icon> 生產核心</span></div></template>
          <el-row>
            <el-col :span="8"><el-statistic title="運行中機台" :value="dashboardData.production.runningMachines" /></el-col>
            <el-col :span="8"><el-statistic title="待處理維修" :value="dashboardData.production.pendingRepairs" /></el-col>
            <el-col :span="8"><el-statistic title="今日已完工" :value="dashboardData.production.completedToday" /></el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- Warehouse -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="kpi-card card-warehouse" @click="navigateTo('/inventory-logs')">
          <template #header><div class="card-header"><span><el-icon><Box /></el-icon> 倉儲物流</span></div></template>
          <el-row>
            <el-col :span="8"><el-statistic title="今日入庫數" :value="dashboardData.warehouse.inboundToday" /></el-col>
            <el-col :span="8"><el-statistic title="今日出庫數" :value="dashboardData.warehouse.outboundToday" /></el-col>
            <el-col :span="8"><el-statistic title="低庫存物料" :value="dashboardData.warehouse.lowStockItems">
              <template #value="{ value }"><span :class="{ 'blinking-warning': value > lowStockThreshold }">{{ value }}</span></template>
            </el-statistic></el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" style="margin-top: 24px;">
      <!-- Supply Chain -->
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="kpi-card card-procurement" @click="navigateTo('/zt/supplier/list')">
          <template #header><div class="card-header"><span><el-icon><Collection /></el-icon> 供應鏈</span></div></template>
          <el-row>
            <el-col :span="12"><el-statistic title="合作供應商" :value="dashboardData.procurement.suppliers" /></el-col>
            <el-col :span="12"><el-statistic title="待審批採購" :value="dashboardData.procurement.pendingOrders" /></el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- Human Resources -->
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="kpi-card card-hr" @click="navigateTo('/kh/leave/list')">
          <template #header><div class="card-header"><span><el-icon><User /></el-icon> 人事行政</span></div></template>
          <el-row>
            <el-col :span="12"><el-statistic title="今日在班" :value="dashboardData.hr.employeesOnDuty" /></el-col>
            <el-col :span="12"><el-statistic title="待審批假單" :value="dashboardData.hr.pendingLeaves" /></el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- System -->
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="kpi-card card-system" @click="navigateTo('/permission-management')">
          <template #header><div class="card-header"><span><el-icon><Setting /></el-icon> 系統狀態</span></div></template>
          <el-row>
            <el-col :span="12"><el-statistic title="活躍使用者" :value="dashboardData.system.activeUsers" /></el-col>
            <el-col :span="12"><el-statistic title="系統日誌" :value="dashboardData.system.logCount" /></el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Cpu, Box, Collection, User, Setting } from '@element-plus/icons-vue';

const router = useRouter();

// --- Data and State ---
const dashboardData = ref({
  global: { oee: 0, onTimeDelivery: 0, firstPassYield: 0 },
  production: { runningMachines: 0, pendingRepairs: 0, completedToday: 0 },
  warehouse: { inboundToday: 0, outboundToday: 0, lowStockItems: 0 },
  procurement: { suppliers: 0, pendingOrders: 0 },
  hr: { employeesOnDuty: 0, pendingLeaves: 0 },
  system: { activeUsers: 0, logCount: 0 },
});
const lowStockThreshold = 10;
const lastUpdated = ref('');
let pollingInterval: number | null = null;

// --- API & Data Fetching ---
const fetchDashboardData = async () => {
  console.log('Fetching comprehensive dashboard data...');
  await new Promise(resolve => setTimeout(resolve, 500));
  dashboardData.value = {
    global: { oee: 85.2, onTimeDelivery: 98.5, firstPassYield: 96.8 },
    production: { runningMachines: 18, pendingRepairs: 2, completedToday: 134 },
    warehouse: { inboundToday: 45, outboundToday: 88, lowStockItems: Math.floor(Math.random() * 10) + 8 },
    procurement: { suppliers: 62, pendingOrders: 4 },
    hr: { employeesOnDuty: 208, pendingLeaves: 3 },
    system: { activeUsers: 15, logCount: 1024 },
  };
  lastUpdated.value = new Date().toLocaleTimeString('it-IT');
};

// --- Navigation ---
const navigateTo = (path: string) => {
  console.log(`Navigating to ${path}...`);
  router.push(path);
};

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchDashboardData();
  pollingInterval = window.setInterval(fetchDashboardData, 30000);
});

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval);
});
</script>

<style scoped>
/* Import previous styles and add new ones */
.dashboard-container {
  padding: 24px 36px;
  background-color: #f5f7fa;
  font-family: Roboto, "Noto Sans", "思源黑體", sans-serif;
  min-height: 100vh;
  background-image: linear-gradient(rgba(0,0,0,0.02) 1px, transparent 1px), linear-gradient(90deg, rgba(0,0,0,0.02) 1px, transparent 1px);
  background-size: 20px 20px;
}
.dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.dashboard-header h1 { font-size: 28px; font-weight: 600; color: #303133; margin: 0; }
.last-updated { font-size: 14px; color: #909399; }

/* Global KPI Cards */
.global-kpi-row { margin-bottom: 24px; }
.global-kpi-card { border-radius: 10px; padding: 20px; color: white; text-align: center; }
.kpi-oee { background: linear-gradient(135deg, #2980b9, #3498db); }
.kpi-otd { background: linear-gradient(135deg, #27ae60, #2ecc71); }
.kpi-yield { background: linear-gradient(135deg, #8e44ad, #9b59b6); }
.kpi-value { font-size: 36px; font-weight: bold; }
.kpi-unit { font-size: 20px; margin-left: 4px; }
.kpi-title { font-size: 16px; opacity: 0.8; margin-top: 8px; }

/* Module KPI Cards */
.kpi-card { --card-border-color: #E4E7ED; border: 1px solid #E4E7ED; border-left: 5px solid var(--card-border-color); border-radius: 8px; transition: all 0.2s ease-in-out; cursor: pointer; }
.kpi-card:hover { transform: translateY(-4px); filter: brightness(1.02); box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08), 0 0 0 2px var(--card-border-color, #409eff) inset; }
.card-header { font-weight: 600; color: #606266; }
.card-header .el-icon { vertical-align: middle; margin-right: 8px; }

/* Module Color Definitions */
.card-production { --card-border-color: #16a085; }
.card-warehouse { --card-border-color: #f39c12; }
.card-procurement { --card-border-color: #2980b9; }
.card-hr { --card-border-color: #8e44ad; }
.card-system { --card-border-color: #7f8c8d; }

:deep(.el-statistic__title) { font-size: 14px !important; color: #909399 !important; }
:deep(.el-statistic__content) { font-size: 26px !important; font-weight: 600 !important; color: #303133 !important; }

@keyframes blink-animation { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }
.blinking-warning { color: #c0392b !important; animation: blink-animation 1.5s infinite; }
</style>
