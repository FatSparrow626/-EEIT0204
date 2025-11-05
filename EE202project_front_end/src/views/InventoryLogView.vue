<template>
  <div class="inventory-log-view">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>庫存異動紀錄</span>
        </div>
      </template>

      <!-- 搜尋框 -->
      <el-input
        v-model="searchQuery"
        placeholder="依物料名稱、類型、備註搜尋"
        clearable
        @clear="searchQuery = ''"
        style="margin-bottom: 20px; width: 300px;"
      >
        <template #prepend>🔍</template>
      </el-input>

      <!-- 表格 -->
      <el-table :data="paginatedData" style="width: 100%" v-loading="loadingTransactions">
        <el-table-column prop="transactionId" label="交易ID" width="80"></el-table-column>
        <el-table-column prop="material.materialName" label="物料名稱"></el-table-column>
        <el-table-column label="交易類型">
          <template #default="scope">
            <el-tag :type="getTransactionTypeTag(scope.row.transactionType)" disable-transitions>
              {{ translateTransactionType(scope.row.transactionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="異動數量"></el-table-column>
        <el-table-column prop="transactionDate" label="交易日期"></el-table-column>
                <el-table-column label="參考表">
          <template #default="scope">
            {{ translateReferenceTable(scope.row.referenceTable) }}
          </template>
        </el-table-column>
        <el-table-column prop="referenceId" label="參考ID"></el-table-column>
        <el-table-column prop="notes" label="備註"></el-table-column>
      </el-table>

      <!-- 分頁器 -->
      <el-pagination
        v-if="totalItems > 0"
        background
        layout="prev, pager, next, sizes, total"
        :total="totalItems"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        :current-page="currentPage"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end;"
      >
      </el-pagination>

    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import http from '../http-common'
import { ElMessage } from 'element-plus'

interface Material {
  materialId: number;
  materialName: string;
}

interface InventoryTransaction {
  transactionId: number;
  material: Material;
  transactionType: string;
  quantity: number;
  transactionDate: string;
  referenceTable: string;
  referenceId: number;
  notes: string;
}

const inventoryTransactions = ref<InventoryTransaction[]>([])
const loadingTransactions = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const API_BASE_URL = '/depot/transactions'

// 翻譯交易類型
const translateTransactionType = (type: string): string => {
  const typeMap: { [key: string]: string } = {
    'PRODUCTION_INBOUND': '生產入庫',
    'PRODUCTION_OUTBOUND': '生產出庫',
    'MANUAL_ADJUSTMENT': '手動調整',
    'SHIPMENT_OUTBOUND': '出貨',
    'INITIAL_STOCK': '初始庫存'
  };
  return typeMap[type] || type;
};

// 翻譯參考表
const translateReferenceTable = (table: string): string => {
  const tableMap: { [key: string]: string } = {
    'work_orders': '工單',
    'materials': '物料',
    'inbound_receipts': '入庫單',
    'outbound_orders': '出庫單'
  };
  return tableMap[table] || table;
};

// 根據交易類型回傳標籤顏色
const getTransactionTypeTag = (type: string): string => {
  if (type.includes('INBOUND')) return 'success';
  if (type.includes('OUTBOUND')) return 'warning';
  return 'info';
};

// 搜尋過濾後的資料
const filteredData = computed(() => {
  if (!searchQuery.value) {
    return inventoryTransactions.value;
  }
  const lowerCaseQuery = searchQuery.value.toLowerCase();
  return inventoryTransactions.value.filter(transaction => {
    const translatedType = translateTransactionType(transaction.transactionType).toLowerCase();
    return (
      transaction.material?.materialName?.toLowerCase().includes(lowerCaseQuery) ||
      transaction.transactionType?.toLowerCase().includes(lowerCaseQuery) ||
      translatedType.includes(lowerCaseQuery) ||
      transaction.notes?.toLowerCase().includes(lowerCaseQuery)
    );
  });
});

// 分頁後的資料
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredData.value.slice(start, end);
});

// 總項目數
const totalItems = computed(() => filteredData.value.length);

// 處理分頁大小變更
const handleSizeChange = (val: number) => {
  pageSize.value = val;
  currentPage.value = 1; // Reset to first page
};

// 處理目前頁面變更
const handleCurrentChange = (val: number) => {
  currentPage.value = val;
};

// Fetch all inventory transactions
const fetchInventoryTransactions = async () => {
  loadingTransactions.value = true
  try {
    const response = await http.get<InventoryTransaction[]>(API_BASE_URL)
    inventoryTransactions.value = response.data.map(transaction => ({
      ...transaction,
      transactionDate: new Date(transaction.transactionDate).toLocaleString() // Format date
    })).sort((a, b) => b.transactionId - a.transactionId); // Sort by ID descending
  } catch (error) {
    console.error('Error fetching inventory transactions:', error)
    ElMessage.error('獲取庫存異動紀錄失敗')
  } finally {
    loadingTransactions.value = false
  }
}

onMounted(() => {
  fetchInventoryTransactions()
})
</script>

<style scoped>
.inventory-log-view {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
</style>