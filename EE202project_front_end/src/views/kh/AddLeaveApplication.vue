<template>
  <el-card class="leave-application-card kh-view-upscaled">
    <template #header>
      <span style="font-size: 20px; font-weight: bold;">📝 請假申請表單</span>
    </template>

    <el-form v-bind:model="form" v-bind:rules="rules" ref="formRef" label-width="120px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="假別" prop="leaveTypeId">
            <el-select v-model="form.leaveTypeId" placeholder="請選擇假別" style="width: 100%;">
              <el-option v-for="type in leaveTypes" :key="type.id" :label="type.name" :value="type.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="職務代理人" prop="agentId">
            <el-autocomplete v-model="agentNameDisplay" :fetch-suggestions="queryAgentsAsync"
              placeholder="請輸入代理人姓名搜尋 (可選填)" value-key="fullName" @select="handleAgentSelect" @clear="handleAgentClear"
              style="width: 100%;" clearable />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 新增：顯示剩餘特休時數 -->
      <el-form-item v-if="form.leaveTypeId === annualLeaveTypeId && annualLeaveBalance !== null" label=" ">
        <el-alert :title="`剩餘特休時數: ${annualLeaveBalance} 小時`" type="info" :closable="false" show-icon
          style="width: 100%;" />
      </el-form-item>

      <el-form-item label="請假事由" prop="reason">
        <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="概略說明請假事由，上限200字" />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="開始時間" prop="startDatetime">
            <el-date-picker v-model="form.startDatetime" type="datetime" placeholder="選擇開始日期與時間"
              format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" :disabled-minutes="disabledMinutes"
              :editable="false" style="width: 100%;" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="結束時間" prop="endDatetime">
            <el-date-picker v-model="form.endDatetime" type="datetime" placeholder="選擇結束日期與時間" format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DDTHH:mm:ss" :disabled-minutes="disabledMinutes" :editable="false"
              style="width: 100%;" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="請假時數" prop="hours">
        <el-input-number v-model="form.hours" :min="0" :step="0.5" disabled style="width: 120px;" />
        <el-tooltip content="系統會根據您的起訖時間，自動扣除例假日、國定假日與午休時間後計算，無需手動修改。" placement="top">
          <el-icon style="margin-left: 8px; color: #909399; vertical-align: middle;">
            <InfoFilled />
          </el-icon>
        </el-tooltip>
      </el-form-item>

      <!-- Attachment Section -->
      <el-divider content-position="left">📎 上傳附件</el-divider>
      <el-form-item label="附加檔案">
        <el-upload ref="uploadRef" :auto-upload="false" v-model:file-list="fileList" multiple :limit="5"
          :on-exceed="handleExceed" :before-upload="handleBeforeUpload">
          <el-button type="primary" icon="Upload">選擇檔案</el-button>
          <template #tip>
            <div class="el-upload__tip">
              總檔案不超過 50MB，最多同時上傳 5 個檔案。
            </div>
          </template>
        </el-upload>
      </el-form-item>
      <!-- End Attachment Section -->

      <el-form-item>
        <el-button type="primary" icon="Check" @click="submitForm" :loading="isSubmitting">提交申請</el-button>
        <el-button type="success" plain @click="fillSickLeaveData">一鍵填寫</el-button>
        <el-button @click="goBack">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import api from '@/services/api';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/AuthStore';

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref(null);
const uploadRef = ref(null);
const fileList = ref([]);
const isSubmitting = ref(false);

// 代理人搜尋相關
const agentNameDisplay = ref('');

// 特休相關的響應式變數
const annualLeaveBalance = ref(null);
const annualLeaveTypeId = ref(null);

const disabledMinutes = () => {
  return Array.from({ length: 60 }, (v, k) => k).filter(minute => minute !== 0 && minute !== 30);
};

// 表單資料
const form = ref({
  agentId: null,
  leaveTypeId: null,
  reason: '',
  startDatetime: '',
  endDatetime: '',
  hours: 0.0,
});

// 假別選項
const leaveTypes = ref([]);

// 驗證: 代理人不能是自己
const validateAgent = (rule, value, callback) => {
  // 當 form.agentId 有值時 (使用者已從下拉選單選擇)，才進行驗證
  if (form.value.agentId && authStore.currentUser && form.value.agentId === authStore.currentUser.employeeId) {
    callback(new Error('職務代理人不能選擇本人'));
  } else {
    callback();
  }
};

// 表單驗證規則
const rules = {
  leaveTypeId: [{ required: true, message: '請選擇假別', trigger: 'change' }],
  reason: [{ required: true, message: '請說明請假事由', trigger: 'blur' }],
  startDatetime: [{ required: true, message: '開始日期不為空', trigger: 'change' }],
  endDatetime: [{ required: true, message: '結束日期不為空', trigger: 'change' }],
  agentId: [{ validator: validateAgent, trigger: 'change' }], // 代理人為選填，但若填寫則觸發驗證
};

// 搜尋代理人
const queryAgentsAsync = async (queryString, cb) => {
  if (queryString) {
    try {
      const { data } = await api.get('/api/hr/employees/search', { params: { name: queryString } });
      cb(data);
    } catch (error) {
      console.error('查詢代理人失敗:', error);
      cb([]);
    }
  } else {
    cb([]);
  }
};

// 選擇代理人
const handleAgentSelect = (item) => {
  form.value.agentId = item.employeeId;
  agentNameDisplay.value = item.fullName;
  formRef.value.validateField('agentId');
};

// 清除代理人
const handleAgentClear = () => {
  form.value.agentId = null;
  agentNameDisplay.value = '';
};

const fetchCalculatedHours = async (start, end) => {
  try {
    const response = await api.get('/api/leave/calculate-hours', {
      params: { start, end },
    });
    if (response.data && typeof response.data.calculatedHours === 'number') {
      form.value.hours = response.data.calculatedHours;
    }
  } catch (error) {
    console.error('計算請假時數失敗:', error);
    ElMessage.error('計算請假時數失敗，可能是時間範圍不合理，請檢查。');
    form.value.hours = 0;
  }
};

const fetchAnnualLeaveBalance = async () => {
  annualLeaveBalance.value = '查詢中...';
  try {
    const response = await api.get('/api/leave/annual-leave-balance');
    if (response.data && typeof response.data.balanceHours === 'number') {
      annualLeaveBalance.value = response.data.balanceHours;
    }
  } catch (error) {
    console.error('查詢剩餘特休失敗:', error);
    ElMessage.error('查詢剩餘特休失敗，請稍後再試。');
    annualLeaveBalance.value = '查詢失敗';
  }
};

// --- 一鍵填寫功能 ---
const fillSickLeaveData = () => {
  // 1. 設定假別為 "病假"
  const sickLeaveType = leaveTypes.value.find(type => type.name === '病假');
  if (sickLeaveType) {
    form.value.leaveTypeId = sickLeaveType.id;
  } else {
    ElMessage.warning('找不到「病假」選項，請確認後台資料。');
    return;
  }

  // 2. 設定時間為今天 09:00 到 18:00
  const today = new Date();
  const year = today.getFullYear();
  const month = (today.getMonth() + 1).toString().padStart(2, '0');
  const day = today.getDate().toString().padStart(2, '0');

  form.value.startDatetime = `${year}-${month}-${day}T09:00:00`;
  form.value.endDatetime = `${year}-${month}-${day}T18:00:00`;

  // 3. 設定請假事由
  form.value.reason = '一早起來肚子痛，等看完醫生待補證明';

  // 4. 設定代理人模糊查詢
  agentNameDisplay.value = '陳';
  form.value.agentId = null; // 清空ID，讓使用者手動選擇

  // 5. 時數會由 watch 自動觸發計算，無需手動調用

  ElMessage.success('已自動填寫病假申請資訊。');
};

watch([() => form.value.startDatetime, () => form.value.endDatetime], ([newStart, newEnd]) => {
  if (newStart && newEnd && new Date(newEnd) > new Date(newStart)) {
    fetchCalculatedHours(newStart, newEnd);
  } else {
    form.value.hours = 0;
  }
}, { immediate: false });

watch(() => form.value.leaveTypeId, (newId) => {
  if (newId === annualLeaveTypeId.value) {
    fetchAnnualLeaveBalance();
  } else {
    annualLeaveBalance.value = null;
  }
});

onMounted(async () => {
  try {
    const res = await api.get('/api/leave/form-data');
    leaveTypes.value = res.data.leaveTypes;
    const annualLeave = leaveTypes.value.find(type => type.name === '特休');
    if (annualLeave) {
      annualLeaveTypeId.value = annualLeave.id;
    }
  } catch (error) {
    console.error('獲取假別資料失敗', error);
    ElMessage.error('無法載入頁面所需資料，請稍後再試。');
  }
});

const handleExceed = (files, uploadFiles) => {
  ElMessage.warning(
    `目前限制選擇 5 個檔案，本次選擇了 ${files.length} 個檔案，共 ${files.length + uploadFiles.length} 個檔案`,
  );
};

const handleBeforeUpload = (rawFile) => {
  const limit = 50; // MB
  if (rawFile.size / 1024 / 1024 > limit) {
    ElMessage.error(`單一檔案大小不能超過 ${limit}MB!`);
    return false;
  }
  return true;
};

const submitForm = async () => {
  if (!formRef.value || isSubmitting.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      isSubmitting.value = true;
      let recordUuid = null;

      try {
        // Step 1: Create the leave record
        const createResponse = await api.post('/api/leave/records', form.value);
        recordUuid = createResponse.data.uuid;

        // Step 2: Upload attachments if any
        if (fileList.value.length > 0 && recordUuid) {
          const uploadPromises = fileList.value.map(file => {
            const formData = new FormData();
            formData.append('file', file.raw);
            console.log("上傳檔案:", file.raw);
            console.log("FormData內容:", formData.get('file'));

            return api.post(`/api/leave/${recordUuid}/attachments`, formData);
          });
          await Promise.all(uploadPromises);
        }

        ElMessage.success('請假申請已成功送出！');
        router.push('/kh/leave/list');

      } catch (error) {
        console.error('新增失敗', error);
        if (recordUuid) {
          ElMessage.error('申請已建立，但附件上傳失敗，請至編輯頁面補傳。');
          router.push(`/kh/leave/edit/${recordUuid}`);
        } else {
          const errorMsg = error.response?.data?.message || '申請失敗，請檢查輸入資料或聯繫管理員。';
          ElMessage.error(errorMsg);
        }
      } finally {
        isSubmitting.value = false;
      }
    } else {
      ElMessage.warning('請檢查表單必填欄位。');
      return false;
    }
  });
};

const goBack = () => {
  router.back();
};
</script>

<style scoped>
.leave-application-card {
  margin: 20px;
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

/* 放大提示訊息 */
.kh-view-upscaled :deep(.el-alert__title) {
  font-size: 16px;
}

.kh-view-upscaled :deep(.el-upload__tip) {
  font-size: 14px;
  margin-top: 5px;
}
</style>