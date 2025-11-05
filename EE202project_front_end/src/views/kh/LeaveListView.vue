<template>
  <el-card class="leave-list-card kh-view-upscaled">
    <template v-slot:header>
      <div class="card-header">
        <span style="font-size: 20px; font-weight: bold;">📋 請假紀錄管理</span>
        <div class="header-actions">
          <el-button
            type="primary"
            icon="Plus"
            @click="addRecord"
            v-if="authStore.currentUser?.authorities.includes('LEAVE_APPLY_SELF')"
          >
            新增請假申請
          </el-button>
        </div>
      </div>
    </template>

    <!-- Tabs and filters remain the same -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane v-if="isSuperManager" label="全公司紀錄" name="companyAll"></el-tab-pane>
      <el-tab-pane v-if="isManager" label="待部門審核" name="pendingApproval"></el-tab-pane>
      <el-tab-pane label="我的請假" name="myRequests"></el-tab-pane>
      <el-tab-pane v-if="isManager" label="已處理紀錄" name="processed"></el-tab-pane>
    </el-tabs>

    <div class="filter-container" style="margin-bottom: 15px; display: flex; gap: 15px; align-items: center;">
      <el-select v-if="activeTab === 'myRequests'" v-model="statusFilter" placeholder="依狀態篩選" style="width: 140px;" size="small" @change="fetchLeaveRecords">
        <el-option v-for="item in filterOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <template v-if="activeTab !== 'myRequests'">
        <el-autocomplete v-model="searchName" :fetch-suggestions="querySearchAsync" placeholder="請輸入員工姓名搜尋" @select="handleNameSelect" @clear="handleNameClear" clearable style="width: 240px;" value-key="fullName" />
        <el-date-picker v-model="searchDateRange" type="daterange" range-separator="-" start-placeholder="請假開始日期" end-placeholder="請假結束日期" @change="fetchLeaveRecords" style="width: 280px;" :clearable="true" />
      </template>
      <el-select v-if="isSuperManager && activeTab === 'processed'" v-model="processedViewScope" style="width: 180px;" size="small" @change="fetchLeaveRecords">
        <el-option label="部門已處理資料" value="department" />
        <el-option label="全公司已處理資料" value="company" />
      </el-select>
    </div>

    <!-- Table remains the same -->
    <el-table ref="tableRef" :data="leaveRecords" stripe border v-loading="loading" style="width: 100%" @row-click="handleRowClick" :row-class-name="tableRowClassName" @sort-change="handleSortChange">
      <el-table-column type="expand">
        <template #default="{ row }">
          <div v-if="row.statusCode === 'REJECTED' && row.rejectionReason" class="rejection-reason-box">
            <strong>駁回原因：</strong> {{ row.rejectionReason }}
          </div>
          <div v-if="(activeTab === 'processed' || activeTab === 'companyAll') && row.reviewedAt" class="reviewed-at-box">
            <strong>審核時間：</strong> {{ formatDateTime(null, null, row.reviewedAt) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="employeeName" label="員工姓名" width="110" v-if="activeTab !== 'myRequests'" />
      <el-table-column prop="leaveTypeName" label="假別" width="80" />
      <el-table-column prop="reason" label="事由" min-width="200" show-overflow-tooltip />
      <el-table-column prop="startDatetime" label="開始時間" width="165" :formatter="formatDateTime" sortable="custom" />
      <el-table-column prop="endDatetime" label="結束時間" width="165" :formatter="formatDateTime" sortable="custom" />
      <el-table-column prop="hours" label="時數" width="80" />
      <el-table-column prop="statusName" label="狀態" width="100" />
      <el-table-column label="操作" width="180" fixed="right">
        <template v-slot:default="{ row }">
          <el-button type="info" icon="View" circle @click.stop="viewRecord(row.uuid)" title="查看詳情" />
          <template v-if="(isManager || isSuperManager) && row.statusCode === 'PENDING' && row.employeeId !== authStore.currentUser.employeeId">
            <el-button type="success" icon="Check" circle @click.stop="handleApproval(row.uuid, 'APPROVED')" title="核准" />
            <el-button type="danger" icon="Close" circle @click.stop="handleApproval(row.uuid, 'REJECTED')" title="駁回" />
          </template>
          <el-button type="primary" icon="Edit" circle @click.stop="editRecord(row.uuid)" title="編輯" v-if="row.statusCode === 'PENDING' && row.employeeId === authStore.currentUser.employeeId" />
          <el-button type="danger" icon="Delete" circle @click.stop="deleteRecord(row.uuid)" title="刪除" v-if="row.statusCode === 'PENDING' && row.employeeId === authStore.currentUser.employeeId" />
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination remains the same -->
    <div class="pagination-container">
      <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[10, 20, 50, 100]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="totalRecords" />
    </div>
  </el-card>

  <!-- Details Dialog -->
  <el-dialog v-model="dialogVisible" title="請假申請詳情" width="60%" :before-close="handleClose">
    <div v-loading="isLoadingDetails">
      <div v-if="selectedRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申請人">{{ selectedRecord.employeeName }}</el-descriptions-item>
          <el-descriptions-item label="假別">{{ selectedRecord.leaveTypeName }}</el-descriptions-item>
          <el-descriptions-item label="開始時間">{{ formatDialogDateTime(selectedRecord.startDatetime) }}</el-descriptions-item>
          <el-descriptions-item label="結束時間">{{ formatDialogDateTime(selectedRecord.endDatetime) }}</el-descriptions-item>
          <el-descriptions-item label="總時數">{{ selectedRecord.hours }} 小時</el-descriptions-item>
          <el-descriptions-item label="狀態">
            <el-tag :type="statusTagType(selectedRecord.statusCode)">{{ selectedRecord.statusName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="職務代理人">{{ selectedRecord.agentName || '未指定' }}</el-descriptions-item>
          <el-descriptions-item label="審核時間" v-if="selectedRecord.reviewedAt">{{ formatDialogDateTime(selectedRecord.reviewedAt) }}</el-descriptions-item>
          <el-descriptions-item label="事由" :span="2">{{ selectedRecord.reason }}</el-descriptions-item>
          <el-descriptions-item label="駁回原因" :span="2" v-if="selectedRecord.statusCode === 'REJECTED'">
            <el-alert :title="selectedRecord.rejectionReason" type="error" :closable="false" />
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">📎 附件列表</el-divider>
        
        <div v-if="!selectedRecord.attachments || selectedRecord.attachments.length === 0">
            <el-empty description="無附件" :image-size="60"></el-empty>
        </div>
        <div v-else>
            <!-- Image Attachments -->
            <div v-if="imageAttachments.length > 0" class="attachment-gallery">
                <el-image
                    v-for="(file, index) in imageAttachments"
                    :key="file.id"
                    style="width: 100px; height: 100px; border-radius: 6px; margin-right: 10px;"
                    :src="imageSrcs[file.id]"
                    :preview-src-list="imagePreviewList"
                    :initial-index="index"
                    fit="cover"
                    hide-on-click-modal
                />
            </div>

            <!-- Other Attachments -->
            <div v-if="otherAttachments.length > 0" class="attachment-list">
                <el-tag 
                    v-for="file in otherAttachments" 
                    :key="file.id" 
                    class="attachment-tag"
                    @click="downloadFile(file.downloadUrl, file.fileName)"
                >
                    <el-icon><Paperclip /></el-icon> {{ file.fileName }} ({{ (file.fileSize / 1024).toFixed(2) }} KB)
                </el-tag>
            </div>
        </div>

      </div>
      <div v-else>
        <el-alert title="無法載入資料" type="error" :closable="false" />
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">關閉</el-button>
      </span>
    </template>
  </el-dialog>

</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAuthStore } from '@/stores/AuthStore';
import { Paperclip } from '@element-plus/icons-vue';

const router = useRouter();
const leaveRecords = ref([]);
const loading = ref(true);
const authStore = useAuthStore();
const tableRef = ref(null);

// --- Dialog State ---
const dialogVisible = ref(false);
const isLoadingDetails = ref(false);
const selectedRecord = ref(null);
const imageSrcs = ref({}); // For storing blob URLs

// --- Attachment Computed Properties ---
const imageAttachments = computed(() => {
    if (!selectedRecord.value || !selectedRecord.value.attachments) return [];
    return selectedRecord.value.attachments.filter(file => file.fileType.startsWith('image/'));
});

const otherAttachments = computed(() => {
    if (!selectedRecord.value || !selectedRecord.value.attachments) return [];
    return selectedRecord.value.attachments.filter(file => !file.fileType.startsWith('image/'));
});

const imagePreviewList = computed(() => {
    // Use the blob URLs for the preview list
    return imageAttachments.value.map(file => imageSrcs.value[file.id]).filter(Boolean);
});

// --- Computed properties for roles ---
const isSuperManager = computed(() => authStore.currentUser?.authorities.includes('LEAVE_MANAGE_ALL'));
const isManager = computed(() => authStore.currentUser?.authorities.includes('LEAVE_VIEW_DEPARTMENT'));

// --- State for UI controls ---
const activeTab = ref(isSuperManager.value ? 'companyAll' : (isManager.value ? 'pendingApproval' : 'myRequests'));
const statusFilter = ref('ALL');
const searchName = ref('');
const searchDateRange = ref([]);
const processedViewScope = ref('department');

// --- Pagination and Sorting state ---
const totalRecords = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const sortField = ref('createdAt');
const sortOrder = ref('desc');

const filterOptions = [
  { value: 'ALL', label: '全部' },
  { value: 'PENDING', label: '待審核' },
  { value: 'APPROVED', label: '已核准' },
  { value: 'REJECTED', label: '已駁回' },
];

// --- Methods ---

const formatDateTime = (row, column, cellValue) => {
  if (!cellValue) return '';
  return cellValue.replace('T', ' ').substring(0, 16);
};

const formatDialogDateTime = (dateTimeString) => {
  if (!dateTimeString) return '';
  return dateTimeString.replace('T', ' ').substring(0, 16);
};

const statusTagType = (statusCode) => {
  switch (statusCode) {
    case 'APPROVED': return 'success';
    case 'REJECTED': return 'danger';
    case 'PENDING': return 'warning';
    default: return 'info';
  }
};

const tableRowClassName = ({ row }) => {
  const hasDetails = (row.statusCode === 'REJECTED' && row.rejectionReason) || 
                     ((activeTab.value === 'processed' || activeTab.value === 'companyAll') && row.reviewedAt);
  return hasDetails ? '' : 'hide-expand-icon';
};

const handleRowClick = (row) => {
  const hasDetails = (row.statusCode === 'REJECTED' && row.rejectionReason) || 
                     ((activeTab.value === 'processed' || activeTab.value === 'companyAll') && row.reviewedAt);
  if (hasDetails) {
    tableRef.value?.toggleRowExpansion(row);
  }
};

const fetchLeaveRecords = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sort: `${sortField.value},${sortOrder.value}`,
    };

    if (isSuperManager.value && activeTab.value === 'companyAll') params.viewMode = 'companyAll';
    else if (isManager.value && activeTab.value === 'pendingApproval') params.viewMode = 'departmentPending';
    else if (isManager.value && activeTab.value === 'processed') {
      if (isSuperManager.value && processedViewScope.value === 'company') params.viewMode = 'companyProcessed';
      else params.viewMode = 'departmentProcessed';
    } else params.viewMode = 'myRequests';

    if (activeTab.value === 'myRequests') {
      params.statusFilter = statusFilter.value;
    } else {
      params.employeeName = searchName.value;
      if (searchDateRange.value && searchDateRange.value.length === 2) {
        params.startDate = searchDateRange.value[0].toISOString().split('T')[0];
        params.endDate = searchDateRange.value[1].toISOString().split('T')[0];
      } else {
        params.startDate = null;
        params.endDate = null;
      }
    }

    const res = await api.get('/api/leave/records', { params });
    leaveRecords.value = res.data.content;
    totalRecords.value = res.data.totalElements;

  } catch (error) {
    console.error('獲取請假列表失敗:', error);
    ElMessage.error('無法載入請假列表。');
  } finally {
    loading.value = false;
  }
};

const loadAttachmentPreviews = async (attachments) => {
  if (!attachments || attachments.length === 0) return;

  const imagePromises = attachments
    .filter(file => file.fileType.startsWith('image/'))
    .map(async (file) => {
      try {
        const imageUrl = file.downloadUrl.split('?')[0];
        const response = await api.get(imageUrl, { responseType: 'blob' });
        const blob = new Blob([response.data], { type: response.headers['content-type'] });
        imageSrcs.value[file.id] = URL.createObjectURL(blob);
      } catch (error) {
        console.error(`無法載入圖片預覽: ${file.fileName}`, error);
        imageSrcs.value[file.id] = ''; // Or a placeholder
      }
    });

  await Promise.all(imagePromises);
};

const viewRecord = async (uuid) => {
  selectedRecord.value = null;
  dialogVisible.value = true;
  isLoadingDetails.value = true;
  try {
    const response = await api.get(`/api/leave/records/${uuid}`);
    selectedRecord.value = response.data;
    if (response.data.attachments) {
      await loadAttachmentPreviews(response.data.attachments);
    }
  } catch (err) {
    console.error('獲取請假詳情失敗:', err);
    ElMessage.error(err.response?.data?.message || '無法載入資料');
    dialogVisible.value = false;
  } finally {
    isLoadingDetails.value = false;
  }
};

const downloadFile = async (url, fileName) => {
  try {
    const response = await api.get(url, {
      responseType: 'blob',
    });
    const blob = new Blob([response.data], { type: response.headers['content-type'] });
    const blobUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = blobUrl;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(blobUrl);
  } catch (error) {
    console.error('下載檔案失敗:', error);
    ElMessage.error('下載檔案失敗，請稍後再試。');
  }
};

const handleClose = () => {
    dialogVisible.value = false;
    selectedRecord.value = null;
    // Revoke all created blob URLs to prevent memory leaks
    Object.values(imageSrcs.value).forEach(url => {
        if (url) URL.revokeObjectURL(url);
    });
    imageSrcs.value = {}; // Reset for next time
}

const querySearchAsync = async (queryString, cb) => {
  if (queryString) {
    try {
      const { data } = await api.get('/api/hr/employees/search', { params: { name: queryString } });
      cb(data);
    } catch (error) {
      console.error('查詢員工姓名失敗:', error);
      cb([]);
    }
  } else {
    cb([]);
  }
};

const handleNameSelect = () => fetchLeaveRecords();
const handleNameClear = () => fetchLeaveRecords();

const handleTabChange = () => {
  currentPage.value = 1;
  searchName.value = '';
  searchDateRange.value = [];
  statusFilter.value = 'ALL';
  processedViewScope.value = 'department';
  fetchLeaveRecords();
};

const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  currentPage.value = 1;
  fetchLeaveRecords();
};

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage;
  fetchLeaveRecords();
};

const handleSortChange = ({ prop, order }) => {
  if (prop) {
    sortField.value = prop;
    sortOrder.value = order === 'ascending' ? 'asc' : 'desc';
  } else {
    sortField.value = 'createdAt';
    sortOrder.value = 'desc';
  }
  fetchLeaveRecords();
};

const addRecord = () => router.push('/kh/leave-application');
const editRecord = (uuid) => router.push(`/kh/leave/edit/${uuid}`);

const deleteRecord = async (uuid) => {
  await ElMessageBox.confirm('確定要刪除這筆請假紀錄嗎？', '警告', { type: 'warning' });
  await api.delete(`/api/leave/records/${uuid}`);
  ElMessage.success('刪除成功！');
  await fetchLeaveRecords();
};

const handleApproval = async (uuid, status) => {
  const actionText = status === 'APPROVED' ? '核准' : '駁回';
  try {
    if (status === 'REJECTED') {
      const { value } = await ElMessageBox.prompt('請輸入駁回原因：', '確認駁回', {
        confirmButtonText: '確定駁回',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '駁回原因不可為空',
      });
      await api.put(`/api/leave/records/${uuid}/status`, { status: 'REJECTED', reason: value });
      ElMessage.success('已成功駁回！');
    } else {
      await ElMessageBox.confirm(`確定要${actionText}這筆申請嗎？`, '確認', { type: 'info' });
      await api.put(`/api/leave/records/${uuid}/status`, { status: 'APPROVED' });
      ElMessage.success(`已成功${actionText}！`);
    }
    await fetchLeaveRecords();
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${actionText}操作失敗:`, error);
      ElMessage.error('操作失敗，請稍後再試。');
    } else {
      ElMessage.info('已取消操作');
    }
  }
};

onMounted(fetchLeaveRecords);
</script>

<style scoped>
.leave-list-card {
  margin: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-actions {
  display: flex;
  align-items: center;
}
.filter-container {
  margin-bottom: 15px;
  display: flex;
  gap: 15px;
  align-items: center;
}
.rejection-reason-box, .reviewed-at-box {
  padding: 8px 12px;
  border-left-width: 4px;
  border-left-style: solid;
  margin: 10px 0;
}
.rejection-reason-box {
  background-color: #fef0f0;
  color: #f56c6c;
  border-left-color: #f56c6c;
}
.reviewed-at-box {
  background-color: #e6f7ff;
  color: #1890ff;
  border-left-color: #1890ff;
}
:deep(.hide-expand-icon .el-table__expand-icon) {
  display: none;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.attachment-gallery {
    margin-bottom: 10px;
}
.attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.attachment-tag {
  cursor: pointer;
  padding: 10px 15px;
  height: auto;
  font-size: 14px;
}

.attachment-tag:hover {
  background-color: #ecf5ff;
  color: #409eff;
}

.attachment-tag .el-icon {
  vertical-align: middle;
  margin-right: 5px;
}

/* 放大檢視的通用樣式 */
.kh-view-upscaled :deep() {
  /* 通用字體放大 */
  --el-font-size-base: 16px;
  --el-dialog-font-size: 16px;
}

/* 放大卡片標題和內距 */
.kh-view-upscaled :deep(.el-card__header) {
  padding: 20px 24px;
}
.kh-view-upscaled :deep(.el-card__body) {
  padding: 24px;
}

/* 放大 Tabs 和篩選器 */
.kh-view-upscaled :deep(.el-tabs__item) {
  font-size: 16px;
  height: 48px;
}
.kh-view-upscaled :deep(.el-input__inner) {
  font-size: 16px;
}
.kh-view-upscaled :deep(.el-button) {
  font-size: 16px;
  padding: 10px 18px;
}

/* --- 修正：恢復圓形按鈕的樣式 --- */
.kh-view-upscaled :deep(.el-button.is-circle) {
  padding: 12px; /* 確保 padding 上下左右相等 */
}

/* 放大表格 */
.kh-view-upscaled :deep(.el-table th.el-table__cell),
.kh-view-upscaled :deep(.el-table td.el-table__cell) {
  padding: 14px 0;
}
.kh-view-upscaled :deep(.el-table) {
  font-size: 16px;
}

/* 放大分頁 */
.kh-view-upscaled :deep(.el-pagination) {
  --el-pagination-font-size: 16px;
}

/* 放大彈出對話框 */
.kh-view-upscaled :deep(.el-dialog) {
    --el-dialog-title-font-size: 22px;
}
.kh-view-upscaled :deep(.el-descriptions__label),
.kh-view-upscaled :deep(.el-descriptions__content) {
  font-size: 16px;
}
</style>