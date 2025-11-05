<template>
  <div class="products-view">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span>產品與物料管理</span>
          <div class="header-actions">
            <el-input
              v-model="searchQuery"
              placeholder="搜尋產品名稱或類別"
              clearable
              style="width: 240px; margin-right: 12px"
            />
            <el-button type="primary" :icon="Plus" @click="openAddProductDialog">新增產品</el-button>
          </div>
        </div>
      </template>

      <div class="table-container">
        <el-table :data="filteredProducts" style="width: 100%" stripe>
          <el-table-column prop="name" label="產品名稱" min-width="180" sortable />
          <el-table-column prop="price" label="價格" width="120" sortable />
          <el-table-column prop="category" label="類別" width="150" sortable />
          <el-table-column label="BOM" width="120" align="center">
            <template #default="scope">
              <el-button size="small" @click="handleRowClick(scope.row)">查看BOM</el-button>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right" align="center">
            <template #default="scope">
              <el-button type="primary" size="small" @click="openProductionDialog(scope.row)">建立工單</el-button>
              <el-button type="danger" size="small" @click="handleDeleteProduct(scope.row)">刪除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="BOM 表列表" width="700px">
      <el-button type="primary" style="margin-bottom: 16px" @click="openAddBomDialog">新增BOM組件</el-button>
      <el-table :data="bomComponents" style="width: 100%">
        <el-table-column label="組件名稱">
          <template #default="scope">
            <span>{{ getMaterialNameById(scope.row.componentMaterialId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="數量" width="100" />
        <el-table-column prop="notes" label="備註" />
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">編輯</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">刪除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="bomFormDialogVisible" :title="isEditMode ? '編輯BOM組件' : '新增BOM組件'" width="600px">
      <el-form :model="currentBomComponent" label-width="100px">
        <el-form-item label="主物料">
          <el-input :value="getMaterialNameById(currentBomComponent.parentMaterialId)" disabled />
        </el-form-item>
        <el-form-item label="組件物料">
          <el-select
            v-model="currentBomComponent.componentMaterialId"
            :multiple="!isEditMode"
            placeholder="從倉庫選擇物料"
            style="width: 100%"
          >
            <el-option
              v-for="material in depotMaterials"
              :key="material.materialId"
              :label="material.materialName"
              :value="material.materialId"
              :disabled="isMaterialDisabled(material.materialId)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="數量">
          <el-input-number v-model="currentBomComponent.quantity" :min="1" />
        </el-form-item>
        <el-form-item label="備註">
          <el-input v-model="currentBomComponent.notes" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="bomFormDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveBomComponent">儲存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="productionDialogVisible" title="建立生產工單" width="700px">
      <el-form v-if="selectedProduct" label-width="100px">
        <el-form-item label="生產產品">
          <el-input :value="selectedProduct.name" disabled />
        </el-form-item>
        <el-form-item label="生產數量">
          <el-input-number v-model="productionQuantity" :min="1" @change="updateRequiredMaterials" />
        </el-form-item>
      </el-form>
      <el-divider />
      <h3>所需物料</h3>
      <el-table :data="requiredMaterials" style="width: 100%">
        <el-table-column prop="name" label="物料名稱" />
        <el-table-column prop="required" label="需求數量" />
        <el-table-column prop="available" label="可用庫存" />
        <el-table-column label="狀態" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.available >= scope.row.required ? 'success' : 'danger'">
              {{ scope.row.available >= scope.row.required ? '庫存足夠' : '庫存不足' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="isStockInsufficient" class="stock-warning">警告：一種或多種物料庫存不足。</div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="productionDialogVisible = false">取消</el-button>
          <el-button type="primary" :disabled="isStockInsufficient" @click="confirmProduction">確認建立工單</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="addProductDialogVisible" title="新增產品" width="700px">
      <el-form :model="newMaterial" label-width="100px">
        <el-form-item label="產品名稱">
          <el-input v-model="newMaterial.materialName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newMaterial.materialDescription" type="textarea" />
        </el-form-item>
        <el-form-item label="價格">
          <el-input-number v-model="newMaterial.price" :min="0" />
        </el-form-item>
        <el-form-item label="類別">
          <el-select v-model="newMaterial.category" placeholder="選擇或建立類別" allow-create filterable style="width: 100%">
            <el-option v-for="item in uniqueCategories" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-divider>BOM 組件</el-divider>
        <el-form-item label="選擇物料">
          <el-select v-model="newProductBomMaterials" multiple placeholder="選擇產品物料" style="width: 100%">
            <el-option
              v-for="material in depotMaterials"
              :key="material.materialId"
              :label="material.materialName"
              :value="material.materialId"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-for="materialId in newProductBomMaterials"
          :key="materialId"
          :label="`數量: ${getMaterialNameById(materialId)}`"
        >
          <el-input-number v-model="newProductBomQuantities[materialId]" :min="1" />
          <el-button
            type="danger"
            :icon="Delete"
            circle
            style="margin-left: 10px"
            @click="removeNewProductBomMaterial(materialId)"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addProductDialogVisible = false">取消</el-button>
          <el-button @click="fillWithSampleData">一鍵輸入</el-button>
          <el-button type="primary" @click="saveNewProduct">儲存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Delete, Plus } from '@element-plus/icons-vue';
import api from '../http-common';

// --- Type Definitions ---
interface Product {
  id: number;
  name: string;
  price: number;
  category: string;
}

interface NewMaterial {
  materialName: string;
  materialDescription: string;
  price: number;
  category: string;
  materialType: string;
}

interface Material {
  materialId: number;
  materialName: string;
  materialType: string;
  stockCurrent: number;
  price: number;
  category: string;
}

interface BomComponent {
  bomComponentId: number | null;
  parentMaterialId: number | null;
  componentMaterialId: number | number[] | null;
  quantity: number;
  notes: string;
}

interface RequiredMaterial {
  id: number;
  name: string;
  required: number;
  available: number;
}

interface ProductionOrderRequest {
  woNumber: string;
  materialId: number;
  requiredQuantity: number;
  status: string;
}

// --- Reactive State ---
const products = ref<Material[]>([]);
const allMaterials = ref<Material[]>([]);
const bomComponents = ref<BomComponent[]>([]);
const depotMaterials = ref<Material[]>([]);
const requiredMaterials = ref<RequiredMaterial[]>([]);
const selectedMaterialId = ref<number | null>(null);
const searchQuery = ref(''); // [新增] 搜尋關鍵字

// Dialog visibility
const dialogVisible = ref(false);
const bomFormDialogVisible = ref(false);
const addProductDialogVisible = ref(false);
const productionDialogVisible = ref(false);

// Form models and state
const isEditMode = ref(false);
const selectedProduct = ref<Material | null>(null);
const productionQuantity = ref(1);

const currentBomComponent = ref<BomComponent>({
  bomComponentId: null,
  parentMaterialId: null,
  componentMaterialId: null,
  quantity: 1,
  notes: '',
});

const newMaterial = ref<NewMaterial>({
  materialName: '',
  materialDescription: '',
  price: 0,
  category: '',
  materialType: 'PRODUCT',
});

const newProductBomMaterials = ref<number[]>([]);
const newProductBomQuantities = ref<Record<number, number>>({});

// --- Computed Properties ---
// [新增] 過濾後的產品列表
const filteredProducts = computed(() => {
  if (!searchQuery.value) {
    return products.value;
  }
  const lowerCaseQuery = searchQuery.value.toLowerCase();
  return products.value.filter(
    (product) =>
      product.name.toLowerCase().includes(lowerCaseQuery) ||
      product.category.toLowerCase().includes(lowerCaseQuery)
  );
});

const isStockInsufficient = computed(() => requiredMaterials.value.some((m) => m.available < m.required));

const uniqueCategories = computed(() => {
  const categories = allMaterials.value.map((m) => m.category);
  return [...new Set(categories)].filter((category) => category);
});

const materialIdToNameMap = computed(() => {
  if (!Array.isArray(depotMaterials.value)) return new Map();
  return new Map(depotMaterials.value.map((m) => [m.materialId, m.materialName]));
});

// --- Methods ---
const getMaterialNameById = (materialId: number | null | undefined): string => {
  if (materialId === null || materialId === undefined) return '未知物料';
  return materialIdToNameMap.value.get(materialId) || `未知物料 (ID: ${materialId})`;
};

const handleError = (error: unknown, message = '操作失敗') => {
  console.error(`${message}:`, error);
  const err = error as any;
  if (err !== 'cancel') {
    ElMessage.error(err.response?.data?.message || err.message || message);
  }
};

const refreshAllData = async () => {
  await fetchAllProducts();
  await fetchDepotMaterials();
};

const fetchAllProducts = async () => {
  try {
    const response = await api.get<Material[]>('/depot/materials?materialType=PRODUCT');
    if (Array.isArray(response.data)) {
      products.value = response.data.map((m) => ({ ...m, id: m.materialId, name: m.materialName }));
      allMaterials.value = response.data;
    } else {
      products.value = [];
      allMaterials.value = [];
    }
  } catch (error) {
    handleError(error, '獲取產品列表失敗');
    products.value = [];
    allMaterials.value = [];
  }
};

const fetchDepotMaterials = async () => {
  try {
    const response = await api.get<Material[]>('/depot/materials');
    depotMaterials.value = response.data || [];
  } catch (error) {
    handleError(error, '獲取物料庫存失敗');
    depotMaterials.value = [];
  }
};

const fetchBomComponents = async (materialId: number) => {
  try {
    const response = await api.get<BomComponent[]>(`/boms/material/${materialId}`);
    bomComponents.value = response.data;
    return response.data;
  } catch (error) {
    handleError(error, '獲取BOM組件失敗');
    return [];
  }
};

const handleRowClick = async (row: Product) => {
  selectedMaterialId.value = row.id;
  await fetchBomComponents(row.id);
  dialogVisible.value = true;
};

// BOM Component Actions
const openAddBomDialog = () => {
  if (!selectedMaterialId.value) return;
  isEditMode.value = false;
  currentBomComponent.value = {
    bomComponentId: null,
    parentMaterialId: selectedMaterialId.value,
    componentMaterialId: [],
    quantity: 1,
    notes: '',
  };
  bomFormDialogVisible.value = true;
};

const handleEdit = (row: BomComponent) => {
  isEditMode.value = true;
  currentBomComponent.value = { ...row };
  bomFormDialogVisible.value = true;
};

const handleDelete = async (row: BomComponent) => {
  try {
    await ElMessageBox.confirm('您確定要刪除此BOM組件嗎？', '警告', { type: 'warning' });
    await api.delete(`/boms/${row.bomComponentId as number}`);
    ElMessage.success('BOM組件刪除成功！');
    if (selectedMaterialId.value) {
      await fetchBomComponents(selectedMaterialId.value);
    }
  } catch (error) {
    handleError(error, '刪除BOM組件失敗');
  }
};

const saveBomComponent = async () => {
  if (!selectedMaterialId.value) return;
  try {
    if (isEditMode.value) {
      await api.put(`/boms/update`, currentBomComponent.value);
      ElMessage.success('BOM組件更新成功！');
    } else {
      const componentIds = currentBomComponent.value.componentMaterialId as number[];
      if (!componentIds || componentIds.length === 0) {
        ElMessage.error('請選擇至少一個組件物料。');
        return;
      }
      const payloads = componentIds.map((id) => ({
        parentMaterialId: selectedMaterialId.value,
        componentMaterialId: id,
        quantity: currentBomComponent.value.quantity,
        notes: currentBomComponent.value.notes,
      }));
      await Promise.all(payloads.map((p) => api.post(`/boms/add`, p)));
      ElMessage.success('BOM組件新增成功！');
    }
    bomFormDialogVisible.value = false;
    await fetchBomComponents(selectedMaterialId.value);
  } catch (error) {
    handleError(error, '儲存BOM組件失敗');
  }
};

// Product Actions
const openAddProductDialog = () => {
  newMaterial.value = {
    materialName: '',
    materialDescription: '',
    price: 0,
    category: '',
    materialType: 'PRODUCT',
  };
  newProductBomMaterials.value = [];
  newProductBomQuantities.value = {};
  addProductDialogVisible.value = true;
};

const fillWithSampleData = () => {
  newMaterial.value = {
    materialName: '8k螢幕',
    materialDescription: '8K螢幕',
    price: 1000,
    category: '螢幕與電子產品類',
    materialType: 'PRODUCT',
  };
};

const saveNewProduct = async () => {
  try {
    const materialPayload = {
      ...newMaterial.value,
      materialType: 'PRODUCT',
      stockCurrent: 0,
      unit: '個',
      location: '未指定',
      safetyStock: 0,
      reorderLevel: 0,
    };
    const response = await api.post('/depot/materials', materialPayload);
    const createdMaterial = response.data;
    ElMessage.success('產品新增成功！');

    if (newProductBomMaterials.value.length > 0) {
      const bomPayloads = newProductBomMaterials.value.map((id) => ({
        parentMaterialId: createdMaterial.materialId,
        componentMaterialId: id,
        quantity: newProductBomQuantities.value[id] || 1,
        notes: '',
      }));
      await Promise.all(bomPayloads.map((p) => api.post(`/boms/add`, p)));
      ElMessage.success('BOM 組件已關聯！');
    }
    addProductDialogVisible.value = false;
    await refreshAllData();
  } catch (error) {
    handleError(error, '新增產品失敗');
  }
};

const handleDeleteProduct = async (row: Product) => {
  try {
    await ElMessageBox.confirm(`確定要刪除產品 ${row.name} 嗎？`, '警告', { type: 'warning' });
    await api.delete(`/depot/materials/${row.id}`);
    ElMessage.success('產品刪除成功！');
    await refreshAllData();
  } catch (error) {
    handleError(error, '刪除產品失敗');
  }
};

const removeNewProductBomMaterial = (materialIdToRemove: number) => {
  const index = newProductBomMaterials.value.indexOf(materialIdToRemove);
  if (index > -1) {
    newProductBomMaterials.value.splice(index, 1);
  }
};

// Production Order Actions
const openProductionDialog = async (product: Product) => {
  selectedProduct.value = product;
  productionQuantity.value = 1;
  await updateRequiredMaterials();
  productionDialogVisible.value = true;
};

const updateRequiredMaterials = async () => {
  if (!selectedProduct.value) return;
  const bom = await fetchBomComponents(selectedProduct.value.id);
  requiredMaterials.value = bom.map((component) => {
    const depotMat = depotMaterials.value.find((m) => m.materialId === component.componentMaterialId);
    return {
      id: component.componentMaterialId as number,
      name: getMaterialNameById(component.componentMaterialId as number),
      required: component.quantity * productionQuantity.value,
      available: depotMat ? depotMat.stockCurrent : 0,
    };
  });
};

const confirmProduction = async () => {
  if (!selectedProduct.value) return;
  const woNumber = `WO-${selectedProduct.value.name.replace(/\s/g, '')}-${Date.now()}`;
  const workOrderPayload: ProductionOrderRequest = {
    woNumber,
    materialId: selectedProduct.value.id,
    requiredQuantity: productionQuantity.value,
    status: 'PENDING',
  };
  try {
    await api.post('/workorder', workOrderPayload);
    ElMessage.success(`已為 ${selectedProduct.value.name} 建立工單！`);
    productionDialogVisible.value = false;
  } catch (error) {
    handleError(error, '建立工單失敗');
  }
};

// Misc
const isMaterialDisabled = (materialId: number): boolean => {
  if (materialId === selectedMaterialId.value) return true;
  if (isEditMode.value && materialId === currentBomComponent.value.componentMaterialId) return false;
  return bomComponents.value.some((c) => c.componentMaterialId === materialId);
};

// --- Lifecycle Hooks ---
onMounted(refreshAllData);

watch(
  newProductBomMaterials,
  (newVal) => {
    newVal.forEach((materialId) => {
      if (!(materialId in newProductBomQuantities.value)) {
        newProductBomQuantities.value[materialId] = 1;
      }
    });
    for (const materialId in newProductBomQuantities.value) {
      if (!newVal.includes(Number(materialId))) {
        delete newProductBomQuantities.value[materialId];
      }
    }
  },
  { deep: true }
);
</script>

<style scoped>
/* --- 1. 建立設計規範 (CSS 變數) --- */
.products-view {
  --border-radius-base: 12px;
  --border-radius-button: 8px;
  --box-shadow-light: 0 6px 20px rgba(0, 0, 0, 0.07);
  --transition-base: all 0.25s ease-in-out;

  padding: 24px;
  background-color: #f7f8fc;
}

/* --- 2. 主要卡片與標題優化 --- */
.main-card {
  border-radius: var(--border-radius-base);
  border: none;
  box-shadow: var(--box-shadow-light);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eef0f5;
  font-size: 1.1rem;
  font-weight: 600;
}

/* [新增] 標題右側操作區的樣式 */
.header-actions {
  display: flex;
  align-items: center;
}

/* --- 3. 按鈕圓角與互動效果 --- */
:deep(.el-button) {
  border-radius: var(--border-radius-button) !important;
  transition: var(--transition-base);
  font-weight: 500;
}

:deep(.el-button:not(.is-link):not(.is-text):hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

:deep(.el-button--primary:hover) {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* --- 4. 其他細節優化 --- */
.table-container {
  width: 100%;
  overflow-x: auto;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.stock-warning {
  color: #d9534f;
  margin-top: 16px;
  padding: 10px 16px;
  background-color: rgba(242, 222, 222, 0.6);
  border-radius: 6px;
  font-weight: 500;
  border: 1px solid rgba(235, 204, 204, 0.8);
}

.el-form-item {
  margin-bottom: 22px;
}

@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 95vw !important;
    border-radius: var(--border-radius-base);
  }
}
</style>
