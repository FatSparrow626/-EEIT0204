<template>
  <div class="depot-view">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>入庫單列表</span>
          <el-button type="primary" @click="openCreateInboundDialog">建立入庫單</el-button>
        </div>
      </template>

      <el-table :data="inboundReceipts" style="width: 100%" v-loading="loadingInboundReceipts">
        <el-table-column prop="inboundId" label="ID" width="80" />
        <el-table-column prop="inboundDate" label="入庫日期" width="140">
          <template #default="{ row }">{{ row.inboundDate }}</template>
        </el-table-column>
        <el-table-column prop="purchaseOrderId" label="訂單編號" width="120" />
        <el-table-column prop="status" label="狀態" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="經手人" width="140">
          <template #default="{ row }">
            {{ users.find(u => u.employeeUserId === row.handledBy?.employeeUserId)?.username || row.handledBy?.username || 'N/A' }}
          </template>
        </el-table-column>
        <el-table-column prop="note" label="備註" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewInboundReceiptDetails(row)">詳情</el-button>
            <el-button link type="danger" size="small" @click="deleteInboundReceipt(row.inboundId)">刪除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 建立入庫單 -->
    <el-dialog v-model="showCreateInboundReceiptDialog" title="建立入庫單" width="1100">
      <el-form :model="newInboundReceipt" label-width="120px">
        <el-form-item label="入庫日期">
          <el-date-picker
            v-model="newInboundReceipt.inboundDate"
            type="date"
            placeholder="選擇日期"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="關聯訂單">
          <el-select
            v-model="selectedOrderId"
            placeholder="選擇訂單（可選）"
            clearable
            filterable
            style="width: 100%"
            @change="fetchOrderItems"
          >
            <el-option
              v-for="order in pendingOrders"
              :key="order.orderId"
              :label="`訂單編號 ${order.orderId}`"
              :value="order.orderId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="狀態">
          <el-select v-model="newInboundReceipt.status" placeholder="選擇狀態" style="width: 100%">
            <el-option label="部分到貨" value="DRAFT" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>

        <el-form-item label="經手人">
          <el-select
            v-model="newInboundReceipt.handledBy"
            placeholder="選擇經手人"
            style="width: 100%"
            clearable
            value-key="employeeUserId"
            filterable
          >
            <el-option
              v-for="user in users"
              :key="user.employeeUserId"
              :label="user.username"
              :value="user"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="備註">
          <el-input v-model="newInboundReceipt.note" type="textarea" placeholder="輸入備註" />
        </el-form-item>

        <el-form-item label="入庫項目">
          <el-table :data="newInboundReceipt.items" style="width: 100%; margin-top: 10px">
            <el-table-column prop="materialName" label="物料名稱" />
            <el-table-column label="訂購數量" width="120">
              <template #default="{ row }">{{ row.orderedQuantity }}</template>
            </el-table-column>
            <el-table-column label="已收貨數量" width="120">
              <template #default="{ row }">{{ row.receivedQuantitySoFar }}</template>
            </el-table-column>
            <el-table-column label="尚未收貨數量" width="120">
              <template #default="{ row }">{{ row.remainingQuantity }}</template>
            </el-table-column>
            <el-table-column label="本次收貨數量" width="180">
              <template #default="{ row }">
                <el-input-number v-model="row.inboundQuantity" :min="0" :max="row.remainingQuantity" />
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateInboundReceiptDialog = false">取消</el-button>
          <el-button type="primary" @click="createInboundReceipt">建立</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 入庫單詳情 -->
    <el-dialog v-model="showInboundReceiptDetailsDialog" title="入庫單詳情" width="650">
      <div v-if="selectedInboundReceipt" class="inbound-details-content">
        <p><strong>ID：</strong>{{ selectedInboundReceipt.inboundId }}</p>
        <p><strong>對應的訂單ID：</strong>{{ selectedInboundReceipt.purchaseOrderId ?? '—' }}</p>
        <p><strong>入庫日期：</strong>{{ selectedInboundReceipt.inboundDate }}</p>
        <p>
          <strong>狀態：</strong>
          <el-tag :type="statusTagType(selectedInboundReceipt.status)">
            {{ statusLabel(selectedInboundReceipt.status) }}
          </el-tag>
        </p>
        <p v-if="selectedInboundReceipt.handledBy">
          <strong>經手人：</strong>
          {{
            users.find(u => u.employeeUserId === selectedInboundReceipt.handledBy?.employeeUserId)?.username
            || selectedInboundReceipt.handledBy?.username
            || 'N/A'
          }}
        </p>
        <p v-if="selectedInboundReceipt.note"><strong>備註：</strong>{{ selectedInboundReceipt.note }}</p>

        <h4>入庫項目</h4>
        <el-table :data="selectedInboundReceipt.items" style="width: 100%">
          <el-table-column prop="materialId" label="物料ID" width="120" />
          <el-table-column prop="materialName" label="物料名稱" />
          <el-table-column prop="receivedQuantity" label="數量" width="120" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="showInboundReceiptDetailsDialog = false">關閉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/http-common'
import { ElMessage, ElMessageBox } from 'element-plus'

/** ==== 型別 ==== */
interface UserDto {
  employeeUserId: number
  username: string
  fullName: string
}

interface Material {
  materialId: number
  materialName: string
}

interface InboundReceiptItem {
  materialId: number
  materialName?: string
  orderedQuantity?: number
  receivedQuantitySoFar?: number
  remainingQuantity?: number
  inboundQuantity: number
  // 詳情用
  receivedQuantity?: number
}

interface InboundReceipt {
  inboundId?: number
  inboundDate: string
  status: string
  handledBy?: { employeeUserId: number; username: string } | null
  note?: string
  items: InboundReceiptItem[]
  purchaseOrderId?: number | null
}

interface PurchaseOrder {
  orderId: number
  orderStatus?: string
}

/** ==== 狀態 ==== */
const API_BASE_URL = 'http://localhost:8082/api/depot'
const USER_API_BASE_URL = 'http://localhost:8082/api/employee-users'

const users = ref<UserDto[]>([])
const inboundReceipts = ref<InboundReceipt[]>([])
const loadingInboundReceipts = ref(false)

const showCreateInboundReceiptDialog = ref(false)
const newInboundReceipt = ref<InboundReceipt>({
  inboundDate: '',
  status: 'COMPLETED',
  handledBy: null,
  note: '',
  items: []
})
const selectedInboundReceipt = ref<InboundReceipt | null>(null)
const showInboundReceiptDetailsDialog = ref(false)

const pendingOrders = ref<PurchaseOrder[]>([])
const selectedOrderId = ref<number | null>(null)

const availableMaterialsForInbound = ref<Material[]>([])

/** ==== Utils ==== */
function fmtDateToYmd(d?: string | Date): string {
  const date = d ? new Date(d) : new Date()
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
}

function toTwDate(d?: string): string {
  if (!d) return ''
  const dt = new Date(d)
  return isNaN(dt.getTime()) ? d : dt.toLocaleDateString('zh-TW')
}

const statusLabel = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: '部分到貨',
    COMPLETED: '已完成',
    CANCELLED: '取消'
  }
  return map[status] || status
}

const statusTagType = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

/** ==== API ==== */
async function fetchUsersForDropdown() {
  try {
    const res = await http.get<UserDto[]>(USER_API_BASE_URL)
    users.value = res.data
  } catch (e: any) {
    ElMessage.error(`獲取使用者列表失敗：${e.response?.data?.message || e.message}`)
  }
}

async function fetchAvailableMaterialsForInbound() {
  try {
    const res = await http.get<Material[]>(`${API_BASE_URL}/materials`)
    availableMaterialsForInbound.value = res.data
  } catch (e: any) {
    ElMessage.error(`獲取物料失敗：${e.response?.data || e.message}`)
  }
}

async function fetchInboundReceipts() {
  loadingInboundReceipts.value = true
  try {
    const res = await http.get<any>(`${API_BASE_URL}/inbound-receipts`)
    const data = res.data.data || res.data
    inboundReceipts.value = (Array.isArray(data) ? data : []).map((r: any) => {
      const date = r.inboundDate || r.receiptDate
      return {
        inboundId: r.inboundId || 0,
        inboundDate: date ? toTwDate(date) : '',
        purchaseOrderId: r.purchaseOrderId ?? null,
        status: r.status || 'N/A',
        handledBy: r.handledBy || null,
        note: r.note || '',
        items: []
      }
    })
  } catch (e: any) {
    ElMessage.error(`獲取入庫單列表失敗：${e.response?.data?.message || e.message}`)
  } finally {
    loadingInboundReceipts.value = false
  }
}

async function fetchPendingOrders() {
  try {
    const res = await http.get('/order/list')
    pendingOrders.value = res.data.filter((o: any) =>
      o.orderStatus === 'PENDING' ||
      o.orderStatus === 'DRAFT' ||
      o.orderStatus === 'PARTIALLY_RECEIVED'
    )
  } catch (e: any) {
    // 非致命
  }
}

async function fetchOrderItems() {
  if (!selectedOrderId.value) {
    newInboundReceipt.value.items = []
    return
  }
  try {
    const res = await http.get(`/order/edit/${selectedOrderId.value}`)
    const items = res.data.items || []
    newInboundReceipt.value.items = items.map((item: any) => {
      const received = item.receivedQuantity ?? 0
      const remaining = (item.quantity ?? 0) - received
      const materialId = item.material?.materialId ?? item.materialId
      const matchedMaterial = availableMaterialsForInbound.value.find(m => m.materialId === materialId)
      return {
        materialId,
        materialName: item.material?.materialName ?? matchedMaterial?.materialName ?? '未知',
        orderedQuantity: item.quantity ?? 0,
        receivedQuantitySoFar: received,
        remainingQuantity: remaining > 0 ? remaining : 0,
        inboundQuantity: remaining > 0 ? remaining : 0
      }
    })
  } catch (e: any) {
    ElMessage.error(`載入訂單明細失敗：${e.response?.data?.message || e.message}`)
  }
}

async function createInboundReceipt() {
  try {
    if (!selectedOrderId.value) {
      ElMessage.warning('請選擇關聯訂單')
      return
    }

    const validItems = newInboundReceipt.value.items
      .filter(i => i.materialId > 0 && (i.inboundQuantity ?? 0) > 0)
      .map(i => ({ materialId: i.materialId, receivedQuantity: i.inboundQuantity }))

    if (validItems.length === 0) {
      ElMessage.warning('沒有可入庫的項目或本次收貨數量為 0')
      return
    }

    const payload: any = {
      purchaseOrderId: selectedOrderId.value,
      inboundDate: newInboundReceipt.value.inboundDate
        ? fmtDateToYmd(newInboundReceipt.value.inboundDate)
        : fmtDateToYmd(),
      status: newInboundReceipt.value.status,
      handledBy: newInboundReceipt.value.handledBy?.username || null, // 依你原後端接收 username
      note: newInboundReceipt.value.note,
      items: validItems
    }

    await http.post(`${API_BASE_URL}/inbound-receipts`, payload)
    ElMessage.success('入庫單建立成功')
    showCreateInboundReceiptDialog.value = false
    resetForm()
    await Promise.all([fetchInboundReceipts(), fetchPendingOrders()])
  } catch (e: any) {
    ElMessage.error(`建立入庫單失敗：${e.response?.data?.message || e.message}`)
  }
}

async function viewInboundReceiptDetails(receipt: InboundReceipt) {
  try {
    const res = await http.get<any>(`${API_BASE_URL}/inbound-receipts/${receipt.inboundId}`)
    const r = res.data
    const date = r.inboundDate || r.receiptDate
    selectedInboundReceipt.value = {
      inboundId: r.inboundId || 0,
      inboundDate: date ? toTwDate(date) : '',
      status: r.status || 'N/A',
      handledBy: r.handledBy || null,
      purchaseOrderId: r.purchaseOrderId ?? null,
      note: r.note || '',
      items: (r.items || []).map((it: any) => {
        const material = availableMaterialsForInbound.value.find(m => m.materialId === it.materialId)
        return {
          ...it,
          materialName: material ? material.materialName : '未知物料'
        }
      })
    }
    showInboundReceiptDetailsDialog.value = true
  } catch (e: any) {
    ElMessage.error(`獲取入庫單詳情失敗：${e.response?.data || e.message}`)
  }
}

async function deleteInboundReceipt(id?: number) {
  if (!id) return
  try {
    await ElMessageBox.confirm('確定要刪除此入庫單嗎？', '警告', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await http.delete(`${API_BASE_URL}/inbound-receipts/${id}`)
    ElMessage.success('入庫單刪除成功')
    fetchInboundReceipts()
  } catch (e: any) {
    ElMessage.error(`刪除入庫單失敗：${e.response?.data || e.message}`)
  }
}

/** ==== 其它 ==== */
function resetForm() {
  newInboundReceipt.value = {
    inboundDate: '',
    status: 'COMPLETED',
    handledBy: null,
    note: '',
    items: []
  }
  selectedOrderId.value = null
}

function openCreateInboundDialog() {
  resetForm()
  showCreateInboundReceiptDialog.value = true
}

/** ==== mounted ==== */
onMounted(() => {
  fetchInboundReceipts()
  fetchAvailableMaterialsForInbound()
  fetchUsersForDropdown()
  fetchPendingOrders()
})
</script>

<style scoped>
.depot-view {
  padding: 20px;
}
.box-card {
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}
.inbound-details-content p {
  margin-bottom: 6px;
}
.inbound-details-content h4 {
  margin: 14px 0 8px;
}
</style>
