<template>
  <el-card class="edit-leave-card kh-view-upscaled" v-loading="loading">
    <template #header>
      <div class="card-header">
        <span style="font-size: 20px; font-weight: bold;">
          {{ isEditing ? '✏️ 編輯請假單' : '📄 請假單詳情' }}
        </span>
        <div>
          <el-button v-if="!isEditing && canEdit" type="primary" icon="Edit" @click="isEditing = true">編輯</el-button>
          <el-button v-if="isEditing" @click="cancelEdit">取消</el-button>
        </div>
      </div>
    </template>

    <el-form v-if="form" :model="form" :rules="rules" ref="formRef" label-width="120px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="員工編號">{{ form.employeeId }}</el-descriptions-item>
        <el-descriptions-item label="員工姓名">{{ form.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="代理人編號">{{ form.agentId || '無' }}</el-descriptions-item>
        <el-descriptions-item label="代理人姓名">{{ form.agentName || '無' }}</el-descriptions-item>
        <el-descriptions-item label="假別">{{ form.leaveTypeName }}</el-descriptions-item>
        <el-descriptions-item label="狀態">
          <el-tag :type="statusTagType(form.statusCode)">{{ form.statusName }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <el-form-item label="請假事由" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="3"
          :readonly="!isEditing"
          placeholder="概略說明請假事由，上限200字"
        />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="開始時間" prop="startDatetime">
            <el-date-picker
              v-model="form.startDatetime"
              type="datetime"
              placeholder="選擇開始日期與時間"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DDTHH:mm:ss"
              :readonly="!isEditing"
              :disabled-minutes="disabledMinutes"
              :editable="false"
              style="width: 100%;"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="結束時間" prop="endDatetime">
            <el-date-picker
              v-model="form.endDatetime"
              type="datetime"
              placeholder="選擇結束日期與時間"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DDTHH:mm:ss"
              :readonly="!isEditing"
              :disabled-minutes="disabledMinutes"
              :editable="false"
              style="width: 100%;"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="請假時數" prop="hours">
        <el-input-number v-model="form.hours" :min="0" :step="0.5" :disabled="!isEditing" />
      </el-form-item>

      <!-- Attachment Section -->
      <el-divider content-position="left">📎 附件管理</el-divider>

      <el-form-item label="已上傳附件">
        <div v-if="displayedAttachments.length === 0" style="color: #909399;">
          無
        </div>
        <el-table v-else :data="displayedAttachments" style="width: 100%" size="small" :show-header="false">
          <el-table-column prop="fileName" label="檔案名稱" />
          <el-table-column label="操作" width="120" align="right">
            <template #default="{ row }">
              <el-button type="primary" link icon="Download" @click.prevent="downloadFile(row.downloadUrl, row.fileName)" title="下載"></el-button>
              <el-button v-if="isEditing" type="danger" link icon="Delete" @click="handleDeleteAttachment(row)" title="刪除"></el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form-item>

      <el-form-item v-if="isEditing" label="新增附件">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          v-model:file-list="filesToAdd"
          multiple
          :limit="uploadLimit"
          :on-exceed="handleExceed"
          :before-upload="handleBeforeUpload"
        >
          <el-button type="primary" icon="Upload" :disabled="uploadLimit === 0">點擊上傳</el-button>
          <template #tip>
            <div class="el-upload__tip">
              單一檔案不超過 50MB，還可上傳 {{ uploadLimit }} 個檔案 (總數最多5個)。
            </div>
          </template>
        </el-upload>
      </el-form-item>
      <!-- End Attachment Section -->

      <el-divider v-if="isEditing" />

      <el-form-item v-if="isEditing">
        <el-button type="primary" icon="Check" @click="submitForm">儲存更新</el-button>
        <el-button @click="cancelEdit">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/AuthStore'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const uploadRef = ref(null)
const authStore = useAuthStore()

const loading = ref(true)
const isEditing = ref(false)
const form = ref(null)
const originalForm = ref(null)

const filesToAdd = ref([]) 
const filesToDelete = ref([]) 
const originalPersistedAttachments = ref([]) 

const canEdit = computed(() => {
  if (!form.value || !authStore.currentUser) return false;
  const isOwner = form.value.employeeId === authStore.currentUser.employeeId;
  const isPending = form.value.statusCode === 'PENDING';
  return isOwner && isPending;
});

const displayedAttachments = computed(() => {
  const filteredOriginals = originalPersistedAttachments.value.filter(
    (attachment) => !filesToDelete.value.includes(attachment.storedFileName)
  );
  const newFiles = filesToAdd.value.map(f => ({...f, fileName: f.name, fileSize: f.size, downloadUrl: '' }));
  return [...filteredOriginals, ...newFiles];
});

const uploadLimit = computed(() => {
  const limit = 5 - displayedAttachments.value.length;
  return limit > 0 ? limit : 0;
});

const currentTotalSize = computed(() => {
    return displayedAttachments.value.reduce((total, file) => total + (file.fileSize || 0), 0);
});

const statusTagType = (statusCode) => {
  switch (statusCode) {
    case 'PENDING': return 'warning';
    case 'APPROVED': return 'success';
    case 'REJECTED': return 'danger';
    default: return 'info';
  }
};

const disabledMinutes = () => {
  return Array.from({ length: 60 }, (v, k) => k).filter(minute => minute !== 0 && minute !== 30);
}

const rules = {
  reason: [{ required: true, message: '請說明請假事由', trigger: 'blur' }],
  startDatetime: [{ required: true, message: '開始日期不為空', trigger: 'change' }],
  endDatetime: [{ required: true, message: '結束日期不為空', trigger: 'change' }],
}

const fetchRecord = async () => {
  loading.value = true
  try {
    const uuid = route.params.uuid
    const res = await api.get(`/api/leave/records/${uuid}`)
    form.value = res.data
    originalForm.value = JSON.parse(JSON.stringify(res.data))
    originalPersistedAttachments.value = JSON.parse(JSON.stringify(res.data.attachments || []))
    filesToAdd.value = []
    filesToDelete.value = []
  } catch (error) {
    console.error('獲取請假單詳情失敗:', error)
    ElMessage.error('無法載入請假單資料。')
    router.push('/kh/leave/list')
  } finally {
    loading.value = false
  }
}

const cancelEdit = () => {
  isEditing.value = false
  form.value = JSON.parse(JSON.stringify(originalForm.value))
  originalPersistedAttachments.value = JSON.parse(JSON.stringify(originalForm.value.attachments || []))
  filesToAdd.value = []
  filesToDelete.value = []
}

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const uuid = route.params.uuid;
        const formData = new FormData();
        const updatePayload = {
          reason: form.value.reason,
          startDatetime: form.value.startDatetime,
          endDatetime: form.value.endDatetime,
          hours: form.value.hours,
          attachmentsToDelete: filesToDelete.value,
        };
        const updatePayloadBlob = new Blob([JSON.stringify(updatePayload)], {
          type: 'application/json'
        });
        formData.append('updateRequest', updatePayloadBlob);

        for (const file of filesToAdd.value) {
          formData.append('newAttachments', file.raw);
        }

        const response = await api.put(`/api/leave/records/${uuid}`, formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        });

        const updatedRecord = response.data;
        form.value = updatedRecord;
        originalForm.value = JSON.parse(JSON.stringify(updatedRecord));
        originalPersistedAttachments.value = JSON.parse(JSON.stringify(updatedRecord.attachments || []));

        filesToAdd.value = [];
        filesToDelete.value = [];

        ElMessage.success('請假單已成功更新！');
        isEditing.value = false;

      } catch (error) {
        console.error('更新失敗', error);
        ElMessage.error(error.response?.data?.message || '更新失敗，請檢查資料或聯繫管理員。');
      } finally {
        loading.value = false;
      }
    } else {
      ElMessage.warning('請檢查表單必填欄位。');
      return false;
    }
  });
};

const downloadFile = async (url, fileName) => {
  try {
    // Ensure we request the file for download, not inline preview
    const downloadUrl = url.split('?')[0];
    const response = await api.get(downloadUrl, {
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
}

const handleDeleteAttachment = async (attachment) => {
  try {
    await ElMessageBox.confirm(
      `確定要刪除附件 "${attachment.fileName}" 嗎？`,
      '警告',
      {
        confirmButtonText: '確定刪除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    if (attachment.raw) { // New files have a .raw property
      filesToAdd.value = filesToAdd.value.filter(file => file.uid !== attachment.uid);
    } else { // Existing attachment
      filesToDelete.value.push(attachment.storedFileName);
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('刪除附件操作失敗:', error);
    }
  }
};

const handleExceed = (files) => {
  ElMessage.warning(`附件總數已達5個上限，無法再新增。`);
}

const handleBeforeUpload = (rawFile) => {
  const singleFileLimitMB = 50;
  const totalSizeLimitMB = 50;
  const totalSizeLimitBytes = totalSizeLimitMB * 1024 * 1024;

  if (rawFile.size / 1024 / 1024 > singleFileLimitMB) {
    ElMessage.error(`單一檔案大小不能超過 ${singleFileLimitMB}MB!`);
    return false;
  }

  if (rawFile.size + currentTotalSize.value > totalSizeLimitBytes) {
    ElMessage.error(`上傳此檔案後，附件總大小將超過 ${totalSizeLimitMB}MB 上限!`);
    return false;
  }

  return true;
};

onMounted(fetchRecord)
</script>

<style scoped>
.edit-leave-card {
  margin: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 放大檢視的通用樣式 */
.kh-view-upscaled :deep() {
  /* 通用字體放大 */
  --el-font-size-base: 16px;
}

/* 放大卡片標題和內距 */
.kh-view-upscaled :deep(.el-card__header) {
  padding: 20px 24px;
}
.kh-view-upscaled :deep(.el-card__body) {
  padding: 24px;
}

/* 放大描述列表 */
.kh-view-upscaled :deep(.el-descriptions__label),
.kh-view-upscaled :deep(.el-descriptions__content) {
  font-size: 16px;
}

/* 放大表單標籤和輸入框 */
.kh-view-upscaled :deep(.el-form-item__label) {
  font-size: 16px;
}
.kh-view-upscaled :deep(.el-form-item) {
  margin-bottom: 22px;
}
.kh-view-upscaled :deep(.el-input__inner),
.kh-view-upscaled :deep(.el-textarea__inner) {
  font-size: 16px;
}

/* 放大按鈕 */
.kh-view-upscaled :deep(.el-button) {
  font-size: 16px;
  padding: 12px 20px;
}

/* 放大表格和上傳提示 */
.kh-view-upscaled :deep(.el-table) {
  font-size: 16px;
}
.kh-view-upscaled :deep(.el-upload__tip) {
  font-size: 14px;
  margin-top: 5px;
}
</style>