<template>
  <div class="employee-profile">
    <!-- 個人資訊卡片 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="profile-header">
            <div class="avatar-container">
              <div 
                class="avatar-upload-area"
                @click="triggerFileInput"
                :class="{ 'avatar-loading': avatarUploading }"
              >
                <el-avatar 
                  :size="120" 
                  :src="previewImage || getAvatarUrl(currentEmployee?.photoPath) || ''" 
                  class="profile-avatar"
                >
                  <el-icon><User /></el-icon>
                </el-avatar>
                <!-- 懸停遮罩層 -->
                <div class="avatar-overlay">
                  <el-icon class="upload-icon" v-if="!avatarUploading"><Camera /></el-icon>
                  <el-icon class="loading-icon" v-else><Loading /></el-icon>
                  <span class="upload-text" v-if="!avatarUploading">點擊上傳照片</span>
                  <span class="upload-text" v-else>上傳中...</span>
                </div>
              </div>
              <el-button
                class="edit-avatar-btn"
                circle
                size="small"
                type="primary"
                @click="triggerFileInput"
                :loading="avatarUploading"
              >
                <el-icon><Camera /></el-icon>
              </el-button>
              
              <!-- 隱藏的檔案輸入 -->
              <input
                ref="fileInputRef"
                type="file"
                accept="image/jpeg,image/jpg,image/png"
                @change="handleFileSelect"
                style="display: none"
              />
            </div>
            <div class="profile-info">
              <h2 class="employee-name">
                {{ currentEmployee?.firstName }} {{ currentEmployee?.lastName }}
              </h2>
              <p class="employee-number">員工編號：{{ currentEmployee?.employeeNumber }}</p>
              <el-tag :type="getEmployeeTypeTagType(currentEmployee?.employeeType)" size="large">
                {{ getEmployeeTypeDisplay(currentEmployee?.employeeType) }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>基本資訊</span>
              <div class="header-actions">
                <el-button 
                  v-if="!editMode" 
                  type="primary" 
                  size="small" 
                  @click="toggleEditMode"
                >
                  <el-icon><Edit /></el-icon>
                  編輯資料
                </el-button>
                <div v-else class="edit-actions">
                  <el-button 
                    size="small" 
                    @click="cancelEdit"
                  >
                    取消
                  </el-button>
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="saveProfile"
                    :loading="savingProfile"
                  >
                    儲存
                  </el-button>
                </div>
              </div>
            </div>
          </template>

          <!-- 顯示模式 -->
          <el-descriptions v-if="!editMode" :column="2" size="large" border>
            <el-descriptions-item label="姓名">
              {{ currentEmployee?.firstName }} {{ currentEmployee?.lastName }}
            </el-descriptions-item>
            <el-descriptions-item label="員工編號">
              {{ currentEmployee?.employeeNumber }}
            </el-descriptions-item>
            <el-descriptions-item label="電子郵件">
              <span>{{ currentEmployee?.email || '未設定' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="聯絡電話">
              {{ currentEmployee?.phone || '未設定' }}
            </el-descriptions-item>
            <el-descriptions-item label="所屬部門">
              <el-tag type="info">{{ departmentInfo?.displayName || '未分配' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="職位">
              {{ positionInfo?.positionName || '未設定' }}
            </el-descriptions-item>
            <el-descriptions-item label="直屬主管">
              <span v-if="managerInfo">
                {{ managerInfo.firstName }} {{ managerInfo.lastName }}
              </span>
              <span v-else>未設定</span>
            </el-descriptions-item>
            <el-descriptions-item label="到職日期">
              {{ formatDate(currentEmployee?.hireDate) || '未設定' }}
            </el-descriptions-item>
            <el-descriptions-item label="服務年資">
              <el-tag type="success">{{ calculateWorkingYears() }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="出生日期">
              {{ formatDate(currentEmployee?.birthDate) || '未設定' }}
            </el-descriptions-item>
          </el-descriptions>

          <!-- 編輯模式 -->
          <el-form 
            v-else
            :model="editForm"
            :rules="editFormRules"
            ref="editFormRef"
            label-width="120px"
            class="edit-form"
          >
            <!-- 不可編輯的基本資訊 -->
            <div class="readonly-info">
              <el-alert
                title="部分資訊受保護"
                type="info"
                :closable="false"
                show-icon
              >
                <template #default>
                  姓名、員工編號、部門等資訊需由管理員修改
                </template>
              </el-alert>
            </div>

            <!-- 不可編輯資訊展示 -->
            <el-row :gutter="20" class="readonly-fields">
              <el-col :span="12">
                <el-form-item label="姓名">
                  <el-input 
                    :value="`${currentEmployee?.firstName} ${currentEmployee?.lastName}`" 
                    disabled
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="員工編號">
                  <el-input :value="currentEmployee?.employeeNumber" disabled />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20" class="readonly-fields">
              <el-col :span="12">
                <el-form-item label="所屬部門">
                  <el-input :value="departmentInfo?.displayName || '未分配'" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="職位">
                  <el-input :value="positionInfo?.positionName || '未設定'" disabled />
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">可編輯資訊</el-divider>
            
            <!-- 可編輯欄位 -->
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="電子郵件" prop="email">
                  <el-input 
                    v-model="editForm.email" 
                    placeholder="請輸入電子郵件"
                    prefix-icon="Message"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="聯絡電話" prop="phone">
                  <el-input 
                    v-model="editForm.phone" 
                    placeholder="請輸入聯絡電話"
                    prefix-icon="Phone"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="出生日期">
                  <el-date-picker
                    v-model="editForm.birthDate"
                    type="date"
                    placeholder="選擇出生日期"
                    style="width: 100%"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <!-- 預留空間或添加其他可編輯欄位 -->
              </el-col>
            </el-row>

            <!-- 變更提示 -->
            <div v-if="hasProfileChanges" class="changes-alert">
              <el-alert
                title="偵測到資料變更"
                type="warning"
                :closable="false"
                show-icon
              >
                <template #default>
                  已修改 {{ changedFields.length }} 個欄位：{{ changedFields.join('、') }}
                </template>
              </el-alert>
            </div>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 權限與操作區 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <!-- 統一的權限展示區塊 -->
        <el-card class="unified-permissions-card">
          <template #header>
            <div class="card-header">
              <span>我的權限</span>
              <div class="permission-summary">
                <el-tag size="small" type="success">{{ activeModuleCount }}個模組</el-tag>
                <el-tag size="small" type="info">{{ employeeRoles.length }}個角色</el-tag>
                <el-button type="text" @click="refreshPermissions" size="small">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </div>
          </template>
          
          <!-- 角色標籤區 -->
          <div v-if="employeeRoles.length > 0" class="role-badges-section">
            <span class="section-title">擁有角色：</span>
            <div class="role-badges">
              <el-tag v-for="role in employeeRoles" 
                      :key="role" 
                      :type="getRoleTagType(role)" 
                      size="small"
                      class="role-badge">
                {{ getRoleDisplayName(role) }}
              </el-tag>
            </div>
          </div>
          
          <el-divider v-if="employeeRoles.length > 0" />
          
          <!-- 詳細權限列表 -->
          <div class="permissions-grid">
            <!-- 機台模組 -->
            <div v-if="modulePermissions.machine.canAccess" class="permission-module">
              <div class="module-header">
                <!-- <el-icon class="module-icon"><Tools /></el-icon> -->
                <span class="module-name">機台管理</span>
                <el-tag :type="modulePermissions.machine.isManager ? 'warning' : 'info'" size="small">
                  {{ modulePermissions.machine.level }}
                </el-tag>
              </div>
              <p class="module-description">{{ modulePermissions.machine.description }}</p>
            </div>
            
            <!-- 倉庫模組 -->
            <div v-if="modulePermissions.warehouse.canAccess" class="permission-module">
              <div class="module-header">
                <!-- <el-icon class="module-icon"><House /></el-icon> -->
                <span class="module-name">倉庫管理</span>
                <el-tag :type="modulePermissions.warehouse.isManager ? 'warning' : 'info'" size="small">
                  {{ modulePermissions.warehouse.level }}
                </el-tag>
              </div>
              <p class="module-description">{{ modulePermissions.warehouse.description }}</p>
            </div>
            
            <!-- 工單模組 -->
            <div v-if="modulePermissions.workorder.canAccess" class="permission-module">
              <div class="module-header">
                <!-- <el-icon class="module-icon"><Document /></el-icon> -->
                <span class="module-name">工單管理</span>
                <el-tag type="warning" size="small">管理員</el-tag>
              </div>
              <p class="module-description">可以建立和管理工單、物料需求</p>
            </div>
            
            <!-- 採購模組 -->
            <div v-if="modulePermissions.purchase.canAccess" class="permission-module">
              <div class="module-header">
                <!-- <el-icon class="module-icon"><ShoppingCart /></el-icon> -->
                <span class="module-name">採購管理</span>
                <el-tag type="warning" size="small">管理員</el-tag>
              </div>
              <p class="module-description">可以管理採購需求、供應商關係</p>
            </div>
            
            <!-- 請假模組 -->
            <div v-if="modulePermissions.leave.canAccess" class="permission-module">
              <div class="module-header">
                <!-- <el-icon class="module-icon"><Calendar /></el-icon> -->
                <span class="module-name">請假管理</span>
                <el-tag type="warning" size="small">管理員</el-tag>
              </div>
              <p class="module-description">可以處理請假申請、出勤管理</p>
            </div>
            
            <!-- 人員模組 -->
            <div v-if="modulePermissions.employee.canAccess" class="permission-module">
              <div class="module-header">
                <!-- <el-icon class="module-icon"><UserFilled /></el-icon> -->
                <span class="module-name">人員管理</span>
                <el-tag :type="modulePermissions.employee.isManager ? 'warning' : 'info'" size="small">
                  {{ modulePermissions.employee.level }}
                </el-tag>
              </div>
              <p class="module-description">{{ modulePermissions.employee.description }}</p>
            </div>
            
            <!-- 無權限提示 -->
            <div v-if="!hasAnyModuleAccess" class="no-permissions">
              <el-empty description="暫無功能模組權限" :image-size="60">
                <el-button type="primary" size="small" @click="contactAdmin">聯絡管理員</el-button>
              </el-empty>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <!-- 簡化的快速操作 -->
        <el-card class="quick-actions-card">
          <template #header>
            <div class="card-header">
              <span>快速操作</span>
              <el-tag :type="websocketConnected ? 'success' : 'danger'" size="small">
                {{ websocketConnected ? 'WebSocket 已連接' : 'WebSocket 未連接' }}
              </el-tag>
            </div>
          </template>

          <div class="quick-actions">
            <el-button type="success" size="large" @click="viewContacts" class="action-btn">
              <el-icon><User /></el-icon>
              公司通訊錄
            </el-button>

            <el-button type="primary" size="large" @click="toggleFloatingChat" class="action-btn">
              <el-icon><Message /></el-icon>
              即時聊天
              <el-badge v-if="unreadChatMessages > 0" :value="unreadChatMessages" class="chat-badge" />
            </el-button>

            <el-button type="warning" size="large" @click="changePassword" class="action-btn">
              <el-icon><Lock /></el-icon>
              修改密碼
            </el-button>

            <el-button type="info" size="large" @click="testWebSocketNotification" class="action-btn">
              <el-icon><Bell /></el-icon>
              測試通知
            </el-button>

          </div>
          
          <!-- 通知列表 -->
          <div v-if="notifications.length > 0" class="notifications-section">
            <el-divider>最近通知</el-divider>
            <div class="notifications-list">
              <div 
                v-for="(notification, index) in notifications.slice(0, 5)" 
                :key="index"
                class="notification-item"
              >
                <el-icon class="notification-icon"><Bell /></el-icon>
                <span class="notification-text">{{ notification }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 可摺疊的最近活動 -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="activity-card">
          <template #header>
            <div class="card-header">
              <span>最近活動</span>
              <el-button 
                type="text" 
                size="small" 
                @click="showRecentActivities = !showRecentActivities"
                class="collapse-btn"
              >
                <el-icon><ArrowDown v-if="!showRecentActivities" /><ArrowUp v-else /></el-icon>
                {{ showRecentActivities ? '收起' : '展開' }}
              </el-button>
            </div>
          </template>

          <el-collapse-transition>
            <div v-show="showRecentActivities">
              <el-timeline>
                <el-timeline-item
                  v-for="(activity, index) in recentActivities"
                  :key="index"
                  :timestamp="activity.timestamp"
                  :type="activity.type"
                >
                  {{ activity.description }}
                </el-timeline-item>
              </el-timeline>

              <div v-if="recentActivities.length === 0" class="no-activity">
                <el-empty description="暫無最近活動記錄" :image-size="60" />
              </div>
            </div>
          </el-collapse-transition>
        </el-card>
      </el-col>
    </el-row>

    <!-- 通訊錄模態框 -->
    <el-dialog
      v-model="showContactsDialog"
      title="公司通訊錄"
      width="90%"
      top="5vh"
      class="contacts-dialog"
      :close-on-click-modal="false"
    >
      <div class="contacts-dialog-content">
        <!-- 搜尋和篩選區域 -->
        <div class="contacts-search-section">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-input
                v-model="contactsSearchKeyword"
                placeholder="搜尋姓名..."
                prefix-icon="Search"
                clearable
                @input="handleContactsSearch"
              />
            </el-col>
            <el-col :span="8">
              <el-select
                v-model="contactsSelectedDepartment"
                placeholder="選擇部門"
                clearable
                @change="handleContactsDepartmentFilter"
                style="width: 100%"
              >
                <el-option
                  v-for="dept in departmentsList"
                  :key="dept.departmentId"
                  :label="dept.displayName"
                  :value="dept.departmentId"
                />
              </el-select>
            </el-col>
            <el-col :span="8">
              <div class="contacts-search-info">
                <el-tag type="info" size="large">
                  共 {{ allFilteredEmployees.length }} 位員工
                </el-tag>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 員工表格列表 -->
        <div class="contacts-table-container">
          <el-table
            :data="filteredEmployees"
            v-loading="contactsLoading"
            stripe
            size="small"
            :empty-text="contactsLoading ? '載入中...' : '沒有找到符合條件的員工'"
            class="contacts-table"
            :row-style="{ height: '40px' }"
            :cell-style="{ padding: '8px 12px' }"
            :header-cell-style="{ 
              backgroundColor: '#f8fafc', 
              color: '#1f2937', 
              fontWeight: '600',
              fontSize: '14px',
              padding: '12px',
              borderBottom: '2px solid #e5e7eb'
            }"
          >
            <el-table-column 
              prop="name" 
              label="姓名" 
              width="160"
              sortable
              :sort-method="sortByName"
            >
              <template #default="scope">
                <span class="contact-name-text">
                  {{ scope.row.firstName }} {{ scope.row.lastName }}
                </span>
              </template>
            </el-table-column>

            <el-table-column 
              prop="department" 
              label="部門" 
              width="160"
              sortable
              :sort-method="sortByDepartment"
            >
              <template #default="scope">
                <el-tag 
                  size="small" 
                  type="info" 
                  effect="light"
                  class="contact-department-tag"
                >
                  {{ getContactDepartmentName(scope.row.employeeDepartmentId) }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column 
              prop="position" 
              label="職位" 
              width="120"
              sortable
              :sort-method="sortByPosition"
            >
              <template #default="scope">
                <span class="contact-position-text">
                  {{ getContactPositionName(scope.row.employeePositionId) }}
                </span>
              </template>
            </el-table-column>

            <el-table-column 
              prop="email" 
              label="電子郵件" 
              min-width="200"
            >
              <template #default="scope">
                <div class="contact-email-cell">
                  <span v-if="scope.row.email" class="contact-email-text">
                    <el-icon class="contact-email-icon"><Message /></el-icon>
                    {{ scope.row.email }}
                  </span>
                  <span v-else class="contact-no-email-text">
                    <el-icon class="contact-email-icon disabled"><Message /></el-icon>
                    未設定
                  </span>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分頁 -->
          <div class="contacts-pagination-wrapper" v-if="allFilteredEmployees.length > contactsPageSize">
            <el-pagination
              v-model:current-page="contactsCurrentPage"
              v-model:page-size="contactsPageSize"
              :page-sizes="[20, 50, 100, 200]"
              :total="allFilteredEmployees.length"
              layout="total, sizes, prev, pager, next, jumper"
              class="contacts-pagination"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 修改密碼對話框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密碼"
      width="400px"
      @close="resetPasswordForm"
    >
      <el-form
        :model="passwordForm"
        :rules="passwordRules"
        ref="passwordFormRef"
        label-width="100px"
      >
        <el-form-item label="舊密碼" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="請輸入舊密碼"
          />
        </el-form-item>
        <el-form-item label="新密碼" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="請輸入新密碼"
          />
        </el-form-item>
        <el-form-item label="確認密碼" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="請再次輸入新密碼"
          />
        </el-form-item>
        
      </el-form>
       <div style="margin-left: 30px; color: #909399; font-size: 12px">
          密碼長度至少6位、必須包含字母和數字、禁止使用常見弱密碼
        </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button type="primary" @click="submitPasswordChange" :loading="changingPassword">
            確認修改
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 浮動聊天按鈕 -->
    <div class="floating-chat-button" @click="toggleFloatingChat" v-if="!floatingChatVisible">
      <el-badge :value="unreadChatMessages" :hidden="unreadChatMessages === 0" class="chat-badge">
        <el-button type="primary" size="large" circle>
          <el-icon size="24"><Message /></el-icon>
        </el-button>
      </el-badge>
    </div>

    <!-- 浮動聊天窗口 -->
    <div 
      v-show="floatingChatVisible" 
      class="floating-chat-window"
      :class="{ minimized: floatingChatMinimized }"
    >
      <!-- 聊天窗口標題欄 -->
      <div class="chat-window-header" @click="toggleChatMinimized">
        <div class="header-left">
          <el-icon class="header-icon"><Message /></el-icon>
          <span class="header-title">即時聊天</span>
          <el-tag :type="websocketConnected ? 'success' : 'danger'" size="small">
            {{ websocketConnected ? '已連接' : '未連接' }}
          </el-tag>
        </div>
        <div class="header-actions">
          <el-button type="text" size="small" @click.stop="refreshOnlineUsers" :loading="refreshingUsers">
            <el-icon><Refresh /></el-icon>
          </el-button>
          <el-button type="text" size="small" @click.stop="toggleChatMinimized">
            <el-icon><ArrowDown v-if="!floatingChatMinimized" /><ArrowUp v-else /></el-icon>
          </el-button>
          <el-button type="text" size="small" @click.stop="closeFloatingChat">
            <el-icon>×</el-icon>
          </el-button>
        </div>
      </div>

      <!-- 聊天窗口內容 -->
      <div v-show="!floatingChatMinimized" class="chat-window-content">
        <!-- 在線用戶列表 -->
        <div class="floating-users-section" v-if="!selectedChatUser">
          <div class="section-title">在線用戶</div>
          <div class="floating-users-list">
            <div v-if="onlineUsers.length === 0" class="no-users-mini">
              暫無在線用戶
            </div>
            <div v-else>
              <div 
                v-for="user in onlineUsers" 
                :key="user"
                class="floating-user-item"
                :class="{ 'has-unread': getUserUnreadCount(user) > 0 }"
                @click="selectChatUser(user)"
              >
                <span class="user-name-mini">{{ user }}</span>
                <el-badge 
                  v-if="getUserUnreadCount(user) > 0" 
                  :value="getUserUnreadCount(user)" 
                  class="user-badge-mini"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 聊天區域 -->
        <div v-else class="floating-chat-area">
          <!-- 聊天頭部 -->
          <div class="floating-chat-header">
            <el-button type="text" size="small" @click="backToUserList">
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <span class="chat-user-name-mini">{{ selectedChatUser }}</span>
            <div class="chat-header-actions">
              <el-button 
                type="text" 
                size="small" 
                @click="clearChatHistory"
                :disabled="!getCurrentUserMessages().length"
              >
                清空
              </el-button>
            </div>
          </div>

          <!-- 聊天訊息列表 -->
          <div class="floating-messages-container" ref="messagesContainer">
            <div v-if="getCurrentUserMessages().length === 0" class="no-messages-mini">
              開始聊天吧！
            </div>
            <div v-else class="floating-messages-list">
              <div 
                v-for="(message, index) in getCurrentUserMessages()" 
                :key="index"
                class="floating-message-item"
                :class="{ 'own-message': message.isOwn }"
              >
                <div class="floating-message-bubble">
                  <div class="message-text">{{ message.message }}</div>
                  <div class="message-time-mini">{{ formatChatTime(message.timestamp) }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 聊天輸入區 -->
          <div class="floating-input-container">
            <el-input
              v-model="chatMessageInput"
              placeholder="輸入訊息..."
              class="floating-message-input"
              @keydown="handleChatKeydown"
              :disabled="!websocketConnected"
              size="small"
            >
              <template #append>
                <el-button 
                  type="primary" 
                  @click="sendChatMessage"
                  :disabled="!chatMessageInput.trim() || !websocketConnected"
                  size="small"
                >
                  <el-icon><Message /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, reactive, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/AuthStore'
import http from '@/http-common'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Camera, Edit, Lock, Refresh, Tools, House, Document, ShoppingCart, Calendar, UserFilled, ArrowDown, ArrowUp, ArrowLeft, Message, Loading, Bell } from '@element-plus/icons-vue'

// 介面定義
interface EmployeeInfo {
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
  isActive: boolean
  lastLogin?: string
  createdAt?: string
  updatedAt?: string
}

interface DepartmentInfo {
  departmentId: number
  departmentName: string
  displayName: string
  description?: string
}

interface PositionInfo {
  positionId: number
  positionName: string
  description?: string
}

interface Activity {
  timestamp: string
  type: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  description: string
}

interface ChatMessage {
  fromUserId: string
  toUserId: string
  message: string
  timestamp: number
  isOwn: boolean
}

// 響應式數據
const router = useRouter()
const authStore = useAuthStore()

const currentEmployee = ref<EmployeeInfo | null>(null)
const employeeRoles = ref<string[]>([])
const departmentInfo = ref<DepartmentInfo | null>(null)
const departmentsList = ref<DepartmentInfo[]>([])
const positionInfo = ref<PositionInfo | null>(null)
const managerInfo = ref<EmployeeInfo | null>(null)
const loadingProfile = ref(false)
const showRecentActivities = ref(false) // 預設收起最近活動


const showPasswordDialog = ref(false)
const changingPassword = ref(false)
const passwordFormRef = ref<FormInstance>()

// WebSocket 相關變數
const websocketConnected = ref(false)
const notifications = ref<string[]>([])
let websocket: WebSocket | null = null
let reconnectAttempts = 0
const maxReconnectAttempts = 5

// 聊天相關變數
const floatingChatVisible = ref(false)
const floatingChatMinimized = ref(false)
const selectedChatUser = ref<string>('')
const chatMessageInput = ref('')
const onlineUsers = ref<string[]>([])
const refreshingUsers = ref(false)
const chatMessages = ref<Record<string, ChatMessage[]>>({}) // 按用戶分組的聊天記錄
const unreadChatMessages = ref(0)
const messagesContainer = ref<HTMLElement>()

// 頭像上傳相關變數
const fileInputRef = ref<HTMLInputElement>()
const avatarUploading = ref(false)
const previewImage = ref<string>('')

// 表單數據
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// 編輯模式數據
const editMode = ref(false)
const savingProfile = ref(false)
const editFormRef = ref<FormInstance>()

// 編輯表單數據 (只包含可編輯欄位)
const editForm = reactive({
  email: '',
  phone: '',
  birthDate: ''
})

// 原始數據備份
const originalData = ref<any>({})

// 模擬最近活動數據
// const recentActivities = ref<Activity[]>([
//   {
//     timestamp: '2024-01-08 14:30',
//     type: 'primary',
//     description: '登入系統',
//   },
//   {
//     timestamp: '2024-01-08 09:15',
//     type: 'success',
//     description: '更新個人資料',
//   },
//   {
//     timestamp: '2024-01-07 16:45',
//     type: 'info',
//     description: '查看工作報表',
//   },
// ])
// [新增] 時間格式化函數，用於將後端的 LocalDateTime 格式轉換為前端顯示格式
const formatDateTime = (dateTime: string): string => {
  if (!dateTime) return ''
  try {
    const date = new Date(dateTime)
    return date.toLocaleString('zh-TW', {
      year: 'numeric',
      month: '2-digit', 
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    console.warn('時間格式轉換失敗:', dateTime, error)
    return dateTime
  }
}

// [修改] 改用從API資料動態生成的最近活動數據，取代原本的假資料
// 使用 computed 屬性根據 currentEmployee 的時間戳自動生成活動記錄
const recentActivities = computed<Activity[]>(() => {
  if (!currentEmployee.value) {
    return []
  }

  const activities: Activity[] = []

  // 根據 API 回傳的真實時間戳生成活動記錄
  if (currentEmployee.value.lastLogin) {
    activities.push({
      timestamp: formatDateTime(currentEmployee.value.lastLogin),
      type: 'primary',
      description: '最後登入系統'
    })
  }

  if (currentEmployee.value.updatedAt) {
    activities.push({
      timestamp: formatDateTime(currentEmployee.value.updatedAt),
      type: 'success', 
      description: '個人資料更新'
    })
  }

  if (currentEmployee.value.createdAt) {
    activities.push({
      timestamp: formatDateTime(currentEmployee.value.createdAt),
      type: 'info',
      description: '帳號建立'
    })
  }

  // 按時間排序，最新的在前面
  return activities.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
})

// 權限相關數據結構
const modulePermissions = ref({
  machine: {
    canAccess: false,
    isManager: false,
    level: '',
    description: ''
  },
  warehouse: {
    canAccess: false,
    isManager: false,
    level: '',
    description: ''
  },
  workorder: {
    canAccess: false,
    isManager: false,
    level: '',
    description: ''
  },
  purchase: {
    canAccess: false,
    isManager: false,
    level: '',
    description: ''
  },
  leave: {
    canAccess: false,
    isManager: false,
    level: '',
    description: ''
  },
  employee: {
    canAccess: false,
    isManager: false,
    level: '',
    description: ''
  }
})

// 計算屬性
const currentUser = computed(() => authStore.currentUser)

const hasAnyModuleAccess = computed(() => {
  return Object.values(modulePermissions.value).some(module => module.canAccess)
})

const activeModuleCount = computed(() => {
  return Object.values(modulePermissions.value).filter(module => module.canAccess).length
})

// 編輯模式計算屬性 (只檢查可編輯欄位)
const hasProfileChanges = computed(() => {
  if (!editMode.value) return false
  
  const current = editForm
  const original = originalData.value
  
  return (
    current.email !== original.email ||
    current.phone !== original.phone ||
    current.birthDate !== original.birthDate
  )
})

const changedFields = computed(() => {
  const fields = []
  const current = editForm
  const original = originalData.value
  
  if (current.email !== original.email) fields.push('電子郵件')
  if (current.phone !== original.phone) fields.push('聯絡電話')
  if (current.birthDate !== original.birthDate) fields.push('出生日期')
  
  return fields
})

// 表單驗證規則
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('兩次輸入的密碼不一致'))
  } else {
    callback()
  }
}

// 與 UserView.vue 相同的密碼驗證規則
const validateNewPassword = (rule: any, value: any, callback: any) => {
  if (!value) {
    callback(new Error('請輸入新密碼'))
    return
  }
  
  // 1. 密碼長度至少6位
  if (value.length < 6) {
    callback(new Error('密碼長度至少6位'))
    return
  }
  
  // 2. 密碼必須包含字母和數字
  const hasLetter = /[A-Za-z]/.test(value)
  const hasNumber = /\d/.test(value)
  if (!hasLetter || !hasNumber) {
    callback(new Error('密碼必須包含字母和數字'))
    return
  }
  
  // 3. 禁止使用常見弱密碼
  const weakPasswords = [
    '123456', '111111', '222222', '333333', '444444', '555555',
    '666666', '777777', '888888', '999999', '000000',
    'password', 'admin123', 'abc123', 'qwerty', '123abc',
    'admin', 'user123', 'pass123', '1234567', 'abcdefg'
  ]
  
  if (weakPasswords.includes(value.toLowerCase())) {
    callback(new Error('請避免使用常見弱密碼'))
    return
  }
  
  callback()
}

const passwordRules = reactive<FormRules>({
  oldPassword: [{ required: true, message: '請輸入舊密碼', trigger: 'blur' }],
  newPassword: [{ validator: validateNewPassword, trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '請確認新密碼', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
})

// 編輯表單驗證規則 (只對可編輯欄位進行驗證)
const editFormRules = reactive<FormRules>({
  email: [
    { type: 'email', message: '請輸入有效的電子郵件地址', trigger: ['blur', 'change'] }
  ],
  phone: [
    { pattern: /^09\d{8}$/, message: '請輸入有效的手機號碼(格式: 09xxxxxxxx)', trigger: 'blur' }
  ]
})

// 工具方法
const getAvatarUrl = (photoPath?: string): string => {
  if (!photoPath) return ''
  
  // 如果已經是完整的 URL，直接回傳
  if (photoPath.startsWith('http://') || photoPath.startsWith('https://')) {
    return photoPath
  }
  
  // 如果是相對路徑，轉換為完整的 URL
  // 移除開頭的斜線（如果有的話）
  const cleanPath = photoPath.startsWith('/') ? photoPath.substring(1) : photoPath
  
  // 構建完整的 URL
  const fullUrl = `http://localhost:8082/${cleanPath}`
  
  // 除錯日誌
  console.log('Avatar URL conversion:', {
    original: photoPath,
    cleaned: cleanPath,
    fullUrl: fullUrl
  })
  
  return fullUrl
}

const getEmployeeTypeDisplay = (type?: string): string => {
  const typeMap: Record<string, string> = {
    INTERNAL: '內部員工',
    SUPPLIER: '供應商',
    CONTRACTOR: '承包商',
    SYSTEM_ADMIN: '系統管理員',
  }
  return type ? typeMap[type] || type : '未設定'
}

const getEmployeeTypeTagType = (type?: string): string => {
  const typeMap: Record<string, string> = {
    INTERNAL: 'primary',
    SUPPLIER: 'success',
    CONTRACTOR: 'warning',
    SYSTEM_ADMIN: 'danger',
  }
  return type ? typeMap[type] || 'info' : 'info'
}

// === 角色顯示方法（複製自權限管理） ===

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
    LEAVE_MANAGER: '請假管理員',
  }
  return roleDisplayMap[roleName] || roleName
}

// 2. 簡短描述
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
    LEAVE_MANAGER: '請假與出勤管理',
  }
  return shortDescriptionMap[roleName] || '角色管理'
}

// 3. 角色標籤類型
const getRoleTagType = (role: string): string => {
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
  }
  return typeMap[role] || 'info'
}

const formatDate = (dateString?: string): string => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('zh-TW')
}

const calculateWorkingYears = (): string => {
  if (!currentEmployee.value?.hireDate) return '未知'

  const hireDate = new Date(currentEmployee.value.hireDate)
  const now = new Date()
  const diffTime = Math.abs(now.getTime() - hireDate.getTime())
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  const years = Math.floor(diffDays / 365)
  const months = Math.floor((diffDays % 365) / 30)

  if (years > 0) {
    return months > 0 ? `${years}年${months}個月` : `${years}年`
  } else {
    return months > 0 ? `${months}個月` : '未滿1個月'
  }
}

// API 方法
const fetchEmployeeProfile = async () => {
  if (!currentUser.value) {
    ElMessage.error('用戶未登入')
    return
  }

  loadingProfile.value = true
  try {
    // 獲取員工基本資訊 - 使用新的 profile API
    const response = await http.get('/profile/me')
    currentEmployee.value = response.data

    // 獲取員工角色 - 重新設計的權限推斷邏輯
    if (currentUser.value?.authorities) {
      const authorities = currentUser.value.authorities
      const userRoles = []

      console.log('用戶權限清單:', authorities)

      // 1. 優先檢查是否為系統管理員角色 (ROLE_1 = ADMIN 角色)
      if (authorities.includes('ROLE_1')) {
        userRoles.push('ADMIN')
        console.log('檢測到系統管理員角色')
      } else {
        // 2. 根據實際管理權限推斷具體管理員角色
        
        // 員工帳號管理員：有員工管理權限
        if (authorities.includes('EMPLOYEE_MANAGE')) {
          userRoles.push('EMPLOYEE_ACCOUNT_MANAGER')
        }

        // 機台管理員：有機台管理權限
        if (authorities.includes('MACHINE_MANAGE')) {
          userRoles.push('MACHINE_MANAGER')
        } else if (authorities.some((auth) => auth.startsWith('MACHINE_'))) {
          userRoles.push('MACHINE_EMPLOYEE')
        }

        // 倉庫管理員：有庫存管理權限 (後端實際使用 INVENTORY_MANAGE)
        if (authorities.includes('INVENTORY_MANAGE')) {
          userRoles.push('WAREHOUSE_MANAGER')
        } else if (authorities.some((auth) => auth.startsWith('INVENTORY_'))) {
          userRoles.push('WAREHOUSE_STAFF')
        }

        // 工單管理員：有工單管理權限
        if (authorities.includes('WORKORDER_MANAGE')) {
          userRoles.push('WORKORDER_MANAGER')
        }

        // 採購管理員：有供應商管理權限 (後端實際使用 SUPPLIER_MANAGE)
        if (authorities.includes('SUPPLIER_MANAGE')) {
          userRoles.push('PURCHASE_MANAGER')
        }

        // 請假管理員：
        if (authorities.includes('LEAVE_MANAGE_ALL')) {
          userRoles.push('SUPER_LEAVE_MANAGER');
        } else if (authorities.includes('LEAVE_APPROVE')) {
          userRoles.push('LEAVE_MANAGER');
        } else if (authorities.includes('LEAVE_APPLY_SELF')) {
          userRoles.push('LEAVE_EMPLOYEE');
        }
      }

      employeeRoles.value = userRoles; // [FIX] Remove fallback to ['EMPLOYEE']
      console.log('最終識別的角色:', employeeRoles.value);
    } else {
      console.warn('缺少用戶權限資料')
      employeeRoles.value = []
    }

    // 獲取部門資訊
    if (currentEmployee.value?.employeeDepartmentId) {
      departmentInfo.value = getDepartmentInfo(currentEmployee.value.employeeDepartmentId)
    }

    // 獲取職位資訊（模擬數據）
    if (currentEmployee.value?.employeePositionId) {
      positionInfo.value = getPositionInfo(currentEmployee.value.employeePositionId)
    }

    // 獲取主管資訊（如果有的話）- 使用個人檔案專用 API
    try {
      const managerResponse = await http.get('/profile/me/manager')
      managerInfo.value = managerResponse.data
      console.log('主管資訊獲取結果:', managerInfo.value)
    } catch (error: any) {
      console.warn('無法獲取主管資訊:', error)
      if (error.response?.status === 500) {
        console.error('伺服器內部錯誤:', error.response.data)
      }
      managerInfo.value = null
    }
  } catch (error: any) {
    console.error('獲取個人資料失敗:', error)
    ElMessage.error('獲取個人資料失敗: ' + (error.response?.data?.message || error.message))
  } finally {
    loadingProfile.value = false
  }
}

// 獲取部門列表
const fetchDepartments = async () => {
  try {
    const response = await http.get<DepartmentInfo[]>('/departments')
    departmentsList.value = response.data.map(dept => ({
      ...dept,
      displayName: dept.description || dept.departmentName
    }))
    console.log('Departments loaded in profile:', departmentsList.value)
  } catch (error: any) {
    console.error('Error loading departments in profile:', error)
    // 如果 API 失敗，使用備用數據
    departmentsList.value = [
      { departmentId: 1, departmentName: 'Warehouse', displayName: '倉庫管理部' },
      { departmentId: 2, departmentName: 'Production', displayName: '工單管理部' },
      { departmentId: 3, departmentName: 'Procurement', displayName: '採購管理部' },
      { departmentId: 4, departmentName: 'Equipment', displayName: '機台管理部' },
      { departmentId: 5, departmentName: 'Personnel', displayName: '人員管理部' },
      { departmentId: 6, departmentName: 'Leave', displayName: '請假管理部' },
    ]
    console.log('Using fallback departments data in profile')
  }
}

// 根據部門ID獲取部門資訊
const getDepartmentInfo = (departmentId: number): DepartmentInfo | null => {
  return departmentsList.value.find(dept => dept.departmentId === departmentId) || null
}

const getPositionInfo = (positionId: number): PositionInfo | null => {
  const positions: Record<number, PositionInfo> = {
    1: { positionId: 1, positionName: '經理', description: '部門經理' },
    2: { positionId: 2, positionName: '專員', description: '一般專員' },
    3: { positionId: 3, positionName: '系統管理員', description: '系統管理員' },
  }
  return positions[positionId] || null
}

// 編輯模式相關方法
const toggleEditMode = () => {
  if (editMode.value) {
    // 如果有未儲存的變更，詢問用戶
    if (hasProfileChanges.value) {
      ElMessageBox.confirm(
        '您有未儲存的變更，確定要取消編輯嗎？',
        '確認取消',
        {
          confirmButtonText: '確定取消',
          cancelButtonText: '繼續編輯',
          type: 'warning',
        }
      ).then(() => {
        cancelEdit()
      }).catch(() => {
        // 用戶選擇繼續編輯，什麼都不做
      })
    } else {
      cancelEdit()
    }
  } else {
    startEdit()
  }
}

const startEdit = () => {
  if (!currentEmployee.value) {
    ElMessage.error('無法載入員工資料')
    return
  }

  // 備份原始數據 (只備份可編輯欄位)
  originalData.value = {
    email: currentEmployee.value.email || '',
    phone: currentEmployee.value.phone || '',
    birthDate: currentEmployee.value.birthDate || ''
  }

  // 初始化編輯表單 (只初始化可編輯欄位)
  editForm.email = originalData.value.email
  editForm.phone = originalData.value.phone
  editForm.birthDate = originalData.value.birthDate

  editMode.value = true
  
  // 清除驗證錯誤
  setTimeout(() => {
    editFormRef.value?.clearValidate()
  }, 100)
}

const cancelEdit = () => {
  editMode.value = false
  
  // 重置表單數據
  Object.assign(editForm, originalData.value)
  
  // 清除驗證錯誤
  editFormRef.value?.clearValidate()
}

const saveProfile = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      if (!hasProfileChanges.value) {
        ElMessage.info('沒有資料變更')
        editMode.value = false
        return
      }

      savingProfile.value = true
      
      try {
        // 準備更新數據 (只包含可編輯欄位)
        const updateData = {
          email: editForm.email,
          phone: editForm.phone,
          birthDate: editForm.birthDate
        }

        // 調用 API 更新資料
        await http.put('/profile/me', updateData)
        
        // 更新本地數據 (只更新可編輯欄位)
        if (currentEmployee.value) {
          currentEmployee.value.email = editForm.email
          currentEmployee.value.phone = editForm.phone
          currentEmployee.value.birthDate = editForm.birthDate
        }

        ElMessage.success('個人資料更新成功')
        editMode.value = false
        
      } catch (error: any) {
        console.error('更新個人資料失敗:', error)
        ElMessage.error('更新失敗: ' + (error.response?.data?.message || error.message))
      } finally {
        savingProfile.value = false
      }
    } else {
      ElMessage.error('請檢查表單資料')
    }
  })
}

// 事件處理方法

// 頭像上傳相關方法
const triggerFileInput = () => {
  if (avatarUploading.value) return
  fileInputRef.value?.click()
}

const validateFile = (file: File): boolean => {
  // 檢查檔案類型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支援 JPG、JPEG、PNG 格式的圖片')
    return false
  }
  
  // 檢查檔案大小 (2MB = 2 * 1024 * 1024 bytes)
  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('圖片大小不能超過 2MB')
    return false
  }
  
  return true
}

const handleFileSelect = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return
  
  // 驗證檔案
  if (!validateFile(file)) {
    // 清除選擇的檔案
    target.value = ''
    return
  }
  
  try {
    // 顯示預覽
    const reader = new FileReader()
    reader.onload = (e) => {
      previewImage.value = e.target?.result as string
    }
    reader.readAsDataURL(file)
    
    // 開始上傳
    await uploadAvatar(file)
    
  } catch (error) {
    console.error('處理檔案時發生錯誤:', error)
    ElMessage.error('處理檔案時發生錯誤')
    previewImage.value = ''
  } finally {
    // 清除檔案輸入的值，以便下次可以選擇同一個檔案
    target.value = ''
  }
}

const uploadAvatar = async (file: File) => {
  avatarUploading.value = true
  
  try {
    // 準備 FormData
    const formData = new FormData()
    formData.append('file', file)
    
    // 發送上傳請求 (這裡假設後端有對應的 API)
    const response = await http.post('/profile/me/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    // 更新員工資料中的照片路徑
    if (currentEmployee.value && response.data.photoPath) {
      const newPhotoPath = response.data.photoPath
      currentEmployee.value.photoPath = newPhotoPath
      previewImage.value = '' // 清除預覽，使用新的 photoPath
      
      console.log('頭像上傳成功，新路徑:', newPhotoPath)
      console.log('轉換後的完整 URL:', getAvatarUrl(newPhotoPath))
    }
    
    ElMessage.success('頭像上傳成功')
    
  } catch (error: any) {
    console.error('頭像上傳失敗:', error)
    ElMessage.error('頭像上傳失敗: ' + (error.response?.data?.message || error.message))
    previewImage.value = '' // 清除預覽
  } finally {
    avatarUploading.value = false
  }
}

// 通訊錄模態框相關
const showContactsDialog = ref(false)
const contactsLoading = ref(false)
const contactsDataReady = ref(false) // 數據載入完成標記
const allEmployees = ref<EmployeeData[]>([])
const availablePositions = ref<PositionInfo[]>([])
const contactsSearchKeyword = ref('')
const contactsSelectedDepartment = ref<number | null>(null)
const contactsCurrentPage = ref(1)
const contactsPageSize = ref(50)

// 通訊錄相關介面定義
interface EmployeeData {
  employeeUserId: number
  firstName: string
  lastName: string
  employeeNumber: string
  employeeType: string
  email?: string
  phone?: string
  photoPath?: string
  employeeDepartmentId?: number
  employeePositionId?: number
  isActive: boolean
}

// 職位翻譯映射
const positionTranslationMap: Record<string, { chinese: string; description: string }> = {
  'Manager': { chinese: '經理', description: '管理團隊和業務' },
  'Staff': { chinese: '專員', description: '一般員工職位' },
  'System Administrator': { chinese: '系統管理員', description: '管理系統和技術事務' }
}

// 獲取職位中文名稱
const getPositionChineseName = (englishName: string): string => {
  return positionTranslationMap[englishName]?.chinese || englishName
}

// 根據職位ID獲取職位名稱（中文）
const getContactPositionName = (positionId?: number): string => {
  if (!positionId) return '未設定'
  const position = availablePositions.value.find((pos) => pos.positionId === positionId)
  if (!position) return '未知職位'
  return getPositionChineseName(position.positionName)
}

// 根據部門ID獲取部門名稱
const getContactDepartmentName = (departmentId?: number): string => {
  if (!departmentId) return '未分配'
  const department = departmentsList.value.find((dept) => dept.departmentId === departmentId)
  return department?.displayName || '未知部門'
}

// 篩選後的員工列表（不含分頁）
const allFilteredEmployees = computed(() => {
  // 只有當數據載入完成且有員工數據時才進行篩選
  if (!contactsDataReady.value || allEmployees.value.length === 0) {
    return []
  }

  let filtered = allEmployees.value.filter(emp => emp.isActive) // 只顯示啟用的員工

  // 按搜尋關鍵字篩選
  if (contactsSearchKeyword.value.trim()) {
    const keyword = contactsSearchKeyword.value.trim().toLowerCase()
    filtered = filtered.filter(emp => {
      const fullName = `${emp.firstName} ${emp.lastName}`.toLowerCase()
      const departmentName = getContactDepartmentName(emp.employeeDepartmentId).toLowerCase()
      return fullName.includes(keyword) || departmentName.includes(keyword)
    })
  }

  // 按部門篩選 - 修正條件判斷
  if (contactsSelectedDepartment.value) {
    filtered = filtered.filter(emp => emp.employeeDepartmentId === contactsSelectedDepartment.value)
  }

  // 按姓名排序
  return filtered.sort((a, b) => {
    const nameA = `${a.firstName} ${a.lastName}`
    const nameB = `${b.firstName} ${b.lastName}`
    return nameA.localeCompare(nameB, 'zh-TW')
  })
})

// 分頁後的員工列表
const filteredEmployees = computed(() => {
  const start = (contactsCurrentPage.value - 1) * contactsPageSize.value
  const end = start + contactsPageSize.value
  return allFilteredEmployees.value.slice(start, end)
})

// 排序方法
const sortByName = (a: EmployeeData, b: EmployeeData): number => {
  const nameA = `${a.firstName} ${a.lastName}`
  const nameB = `${b.firstName} ${b.lastName}`
  return nameA.localeCompare(nameB, 'zh-TW')
}

const sortByDepartment = (a: EmployeeData, b: EmployeeData): number => {
  const deptA = getContactDepartmentName(a.employeeDepartmentId)
  const deptB = getContactDepartmentName(b.employeeDepartmentId)
  return deptA.localeCompare(deptB, 'zh-TW')
}

const sortByPosition = (a: EmployeeData, b: EmployeeData): number => {
  const posA = getContactPositionName(a.employeePositionId)
  const posB = getContactPositionName(b.employeePositionId)
  return posA.localeCompare(posB, 'zh-TW')
}

// 分頁事件處理
const handleSizeChange = (newSize: number) => {
  contactsPageSize.value = newSize
  contactsCurrentPage.value = 1
}

const handleCurrentChange = (newPage: number) => {
  contactsCurrentPage.value = newPage
}

// 搜尋和篩選事件處理
const handleContactsSearch = () => {
  if (contactsSearchKeyword.value.trim()) {
    contactsSelectedDepartment.value = null
  }
  contactsCurrentPage.value = 1
}

const handleContactsDepartmentFilter = () => {
  contactsCurrentPage.value = 1
}

// API 調用
const fetchContactsEmployees = async () => {
  contactsLoading.value = true
  try {
    const response = await http.get<EmployeeData[]>('/employee-users')
    allEmployees.value = response.data
    console.log('員工資料載入成功:', allEmployees.value.length)
  } catch (error: any) {
    console.error('載入員工資料失敗:', error)
    ElMessage.error('載入員工資料失敗: ' + (error.response?.data?.message || error.message))
  } finally {
    contactsLoading.value = false
  }
}

const fetchContactsPositions = async () => {
  try {
    const response = await http.get<PositionInfo[]>('/positions')
    availablePositions.value = response.data
    console.log('職位資料載入成功:', availablePositions.value.length)
  } catch (error: any) {
    console.error('載入職位資料失敗:', error)
    // 使用備用數據
    availablePositions.value = [
      { positionId: 1, positionName: 'Manager', description: '經理' },
      { positionId: 2, positionName: 'Staff', description: '專員' },
      { positionId: 3, positionName: 'System Administrator', description: '系統管理員' },
    ]
    console.log('使用備用職位資料')
  }
}

// 重置通訊錄篩選條件
const resetContactsFilters = () => {
  // 清空搜尋關鍵字
  contactsSearchKeyword.value = ''
  
  // 重置部門選擇為 null
  contactsSelectedDepartment.value = null
  
  // 重置分頁到第一頁
  contactsCurrentPage.value = 1
  
  // 重置頁面大小為預設值
  contactsPageSize.value = 50
  
  // 重置數據狀態
  contactsDataReady.value = false
}

const viewContacts = async () => {
  try {
    // 1. 重置篩選條件和狀態
    resetContactsFilters()
    
    // 2. 開啟載入狀態（但不顯示模態框）
    contactsLoading.value = true
    
    // 3. 並行加載數據
    await Promise.all([
      fetchContactsPositions(),
      fetchContactsEmployees()
    ])
    
    // 4. 數據載入完成，設置狀態
    contactsDataReady.value = true
    
    // 5. 現在才顯示模態框
    showContactsDialog.value = true
    
  } catch (error) {
    console.error('載入通訊錄數據失敗:', error)
    ElMessage.error('載入通訊錄失敗，請稍後再試')
  } finally {
    contactsLoading.value = false
  }
}



const changePassword = () => {
  showPasswordDialog.value = true
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.resetFields()
}

const submitPasswordChange = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      changingPassword.value = true
      try {
        // 準備密碼修改資料
        const passwordChangeData = {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword,
          confirmPassword: passwordForm.confirmPassword
        }

        // 調用後端 API
        await http.put('/profile/me/password', passwordChangeData)

        ElMessage.success('密碼修改成功')
        showPasswordDialog.value = false
        resetPasswordForm()
      } catch (error: any) {
        console.error('密碼修改失敗:', error)
        const errorMessage = error.response?.data || error.message || '未知錯誤'
        ElMessage.error('密碼修改失敗: ' + errorMessage)
      } finally {
        changingPassword.value = false
      }
    }
  })
}

// 權限相關方法
const refreshPermissions = async () => {
  try {
    ElMessage.info('正在刷新權限資訊...')
    
    // 重新計算模組權限
    updateModulePermissions()
    
    ElMessage.success('權限資訊已更新')
  } catch (error) {
    ElMessage.error('權限資訊更新失敗')
  }
}

const updateModulePermissions = () => {
  const authorities = currentUser.value?.authorities || []
  
  // 重設所有權限
  Object.keys(modulePermissions.value).forEach(key => {
    (modulePermissions.value as any)[key] = {
      canAccess: false,
      isManager: false,
      level: '',
      description: ''
    }
  })
  
  // 如果是系統管理員，擁有所有權限
  if (authorities.includes('ROLE_1')) {
    modulePermissions.value.machine = {
      canAccess: true,
      isManager: true,
      level: '系統管理員',
      description: '擁有機台模組完整管理權限'
    }
    modulePermissions.value.warehouse = {
      canAccess: true,
      isManager: true,
      level: '系統管理員',
      description: '擁有倉庫模組完整管理權限'
    }
    modulePermissions.value.workorder = {
      canAccess: true,
      isManager: true,
      level: '系統管理員',
      description: '擁有工單模組完整管理權限'
    }
    modulePermissions.value.purchase = {
      canAccess: true,
      isManager: true,
      level: '系統管理員',
      description: '擁有採購模組完整管理權限'
    }
    modulePermissions.value.leave = {
      canAccess: true,
      isManager: true,
      level: '系統管理員',
      description: '擁有請假模組完整管理權限'
    }
    modulePermissions.value.employee = {
      canAccess: true,
      isManager: true,
      level: '系統管理員',
      description: '擁有人員模組完整管理權限'
    }
    return
  }
  
  // 機台模組權限
  if (authorities.includes('MACHINE_MANAGE')) {
    modulePermissions.value.machine = {
      canAccess: true,
      isManager: true,
      level: '管理員',
      description: '可以新增、修改機台設備，管理維修保養計劃'
    }
  } else if (authorities.some(auth => auth.startsWith('MACHINE_'))) {
    modulePermissions.value.machine = {
      canAccess: true,
      isManager: false,
      level: '員工',
      description: '可以查看機台資訊，申請維修保養'
    }
  }
  
  // 倉庫模組權限 (使用正確的權限名稱)
  if (authorities.includes('INVENTORY_MANAGE')) {
    modulePermissions.value.warehouse = {
      canAccess: true,
      isManager: true,
      level: '管理員',
      description: '可以管理庫存、處理入庫出庫作業'
    }
  } else if (authorities.some(auth => auth.startsWith('INVENTORY_'))) {
    modulePermissions.value.warehouse = {
      canAccess: true,
      isManager: false,
      level: '員工',
      description: '可以查看庫存資訊，執行領料作業'
    }
  }
  
  // 工單模組權限
  if (authorities.includes('WORKORDER_MANAGE')) {
    modulePermissions.value.workorder = {
      canAccess: true,
      isManager: true,
      level: '管理員',
      description: '可以建立和管理工單、物料需求'
    }
  } else if (authorities.some(auth => auth.startsWith('WORKORDER_'))) {
    modulePermissions.value.workorder = {
      canAccess: true,
      isManager: false,
      level: '查看者',
      description: '可以查看工單資訊'
    }
  }
  
  // 採購模組權限 (使用正確的權限名稱)
  if (authorities.includes('SUPPLIER_MANAGE')) {
    modulePermissions.value.purchase = {
      canAccess: true,
      isManager: true,
      level: '管理員',
      description: '可以管理採購需求、供應商關係'
    }
  } else if (authorities.some(auth => auth.startsWith('SUPPLIER_'))) {
    modulePermissions.value.purchase = {
      canAccess: true,
      isManager: false,
      level: '查看者',
      description: '可以查看供應商資訊'
    }
  }
  
  // 請假模組權限
  if (authorities.includes('LEAVE_MANAGE')) {
    modulePermissions.value.leave = {
      canAccess: true,
      isManager: true,
      level: '管理員',
      description: '可以處理請假申請、出勤管理'
    }
  } else if (authorities.some(auth => auth.startsWith('LEAVE_'))) {
    modulePermissions.value.leave = {
      canAccess: true,
      isManager: false,
      level: '查看者',
      description: '可以查看請假記錄'
    }
  }
  
  // 人員模組權限
  if (authorities.includes('EMPLOYEE_MANAGE')) {
    modulePermissions.value.employee = {
      canAccess: true,
      isManager: true,
      level: '管理員',
      description: '可以管理員工帳號、分配權限'
    }
  } else if (authorities.includes('EMPLOYEE_VIEW')) {
    modulePermissions.value.employee = {
      canAccess: true,
      isManager: false,
      level: '查看者',
      description: '可以查看員工資訊'
    }
  }
}

const contactAdmin = () => {
  ElMessage.info('請聯絡系統管理員申請相關功能權限')
}

// 浮動聊天相關方法
const toggleFloatingChat = () => {
  floatingChatVisible.value = true
  floatingChatMinimized.value = false
  refreshOnlineUsers()
}

const closeFloatingChat = () => {
  floatingChatVisible.value = false
  floatingChatMinimized.value = false
  selectedChatUser.value = ''
  chatMessageInput.value = ''
}

const toggleChatMinimized = () => {
  floatingChatMinimized.value = !floatingChatMinimized.value
}

const backToUserList = () => {
  selectedChatUser.value = ''
  chatMessageInput.value = ''
}

const selectChatUser = (userId: string) => {
  selectedChatUser.value = userId
  // 標記該用戶的訊息為已讀
  markUserMessagesAsRead(userId)
  // 滾動到最新訊息
  setTimeout(() => {
    scrollToLatestMessage()
  }, 100)
}

const sendChatMessage = () => {
  if (!chatMessageInput.value.trim() || !websocketConnected.value || !selectedChatUser.value) {
    return
  }

  const message = {
    action: 'private',
    toUserId: selectedChatUser.value,
    message: chatMessageInput.value.trim()
  }

  try {
    websocket?.send(JSON.stringify(message))
    
    // 添加到本地聊天記錄
    const chatMessage: ChatMessage = {
      fromUserId: authStore.currentUser?.username || 'me',
      toUserId: selectedChatUser.value,
      message: chatMessageInput.value.trim(),
      timestamp: Date.now(),
      isOwn: true
    }
    
    addChatMessage(selectedChatUser.value, chatMessage)
    chatMessageInput.value = ''
    
    setTimeout(() => {
      scrollToLatestMessage()
    }, 100)
    
  } catch (error) {
    console.error('發送聊天訊息失敗:', error)
    ElMessage.error('發送訊息失敗')
  }
}

const handleChatKeydown = (event: KeyboardEvent) => {
  if (event.ctrlKey && event.key === 'Enter') {
    event.preventDefault()
    sendChatMessage()
  }
}

const addChatMessage = (userId: string, message: ChatMessage) => {
  if (!chatMessages.value[userId]) {
    chatMessages.value[userId] = []
  }
  chatMessages.value[userId].push(message)
  
  // 限制聊天記錄數量（保留最近 100 條）
  if (chatMessages.value[userId].length > 100) {
    chatMessages.value[userId] = chatMessages.value[userId].slice(-100)
  }
  
  // 自動滾動到最新訊息
  nextTick(() => {
    scrollToLatestMessage()
  })
}

const getCurrentUserMessages = (): ChatMessage[] => {
  if (!selectedChatUser.value) return []
  return chatMessages.value[selectedChatUser.value] || []
}

const getUserUnreadCount = (userId: string): number => {
  // 簡化實現，實際應該追蹤每個用戶的未讀訊息
  return 0
}

const markUserMessagesAsRead = (userId: string) => {
  // 實現標記用戶訊息為已讀的邏輯
  // 這裡可以更新未讀計數等
}

const clearChatHistory = () => {
  if (selectedChatUser.value) {
    ElMessageBox.confirm(
      `確定要清空與 ${selectedChatUser.value} 的聊天記錄嗎？`,
      '確認操作',
      {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    ).then(() => {
      chatMessages.value[selectedChatUser.value] = []
      ElMessage.success('聊天記錄已清空')
    }).catch(() => {
      // 用戶取消
    })
  }
}

const refreshOnlineUsers = async () => {
  if (!websocketConnected.value) {
    ElMessage.warning('WebSocket 未連接，無法獲取在線用戶')
    return
  }

  refreshingUsers.value = true
  
  try {
    // 方法1: 通過 WebSocket 請求在線用戶列表
    const message = {
      action: 'get_online_users'
    }
    websocket?.send(JSON.stringify(message))
    
    // 方法2: 通過 HTTP API 獲取（備用方案）
    setTimeout(async () => {
      try {
        const response = await http.get('/api/websocket/online-users')
        if (response.data && Array.isArray(response.data)) {
          onlineUsers.value = response.data.filter(user => user !== authStore.currentUser?.username)
        }
      } catch (error) {
        console.error('獲取在線用戶失敗:', error)
      } finally {
        refreshingUsers.value = false
      }
    }, 1000)
    
  } catch (error) {
    console.error('請求在線用戶失敗:', error)
    refreshingUsers.value = false
  }
}

const scrollToLatestMessage = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatChatTime = (timestamp: number): string => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) { // 小於1分鐘
    return '剛剛'
  } else if (diff < 3600000) { // 小於1小時
    return `${Math.floor(diff / 60000)}分鐘前`
  } else if (date.toDateString() === now.toDateString()) { // 今天
    return date.toLocaleTimeString('zh-TW', { hour: '2-digit', minute: '2-digit' })
  } else {
    return date.toLocaleString('zh-TW', { 
      month: 'short', 
      day: 'numeric', 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  }
}

// WebSocket 相關方法
const connectWebSocket = () => {
  try {
    // 檢查是否已有連接存在
    if (websocket && (websocket.readyState === WebSocket.CONNECTING || websocket.readyState === WebSocket.OPEN)) {
      console.log('WebSocket 已存在連接，跳過重複連接')
      return
    }
    
    console.log('正在初始化原生 WebSocket 連接...')
    
    // 使用原生 WebSocket API，連接到原生 WebSocket 端點
    const wsUrl = 'ws://localhost:8082/websocket'
    websocket = new WebSocket(wsUrl)
    
    // 連接開啟事件
    websocket.onopen = (event) => {
      console.log('WebSocket 連接成功:', event)
      websocketConnected.value = true
      reconnectAttempts = 0
      ElMessage.success('WebSocket 連接成功')
      
      // 發送身份識別消息
      const identifyMessage = {
        action: 'identify',
        userId: authStore.currentUser?.username || 'anonymous'
      }
      websocket?.send(JSON.stringify(identifyMessage))
      
      // 發送訂閱消息到服務端
      const subscribeMessage = {
        action: 'subscribe',
        topic: '/topic/notifications'
      }
      websocket?.send(JSON.stringify(subscribeMessage))
    }
    
    // 接收消息事件
    websocket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        console.log('收到 WebSocket 消息:', data)
        
        // 處理不同類型的消息
        if (data.type === 'notification') {
          const notification = data.data?.message || data.message
          console.log('收到通知:', notification)
          
          // 將通知添加到列表
          notifications.value.unshift(notification)
          
          // 只保留最新的10條通知
          if (notifications.value.length > 10) {
            notifications.value = notifications.value.slice(0, 10)
          }
          
          // 顯示 Element Plus 通知
          ElMessage({
            message: `📢 ${notification}`,
            type: 'info',
            duration: 5000,
            showClose: true
          })
        } else if (data.type === 'message' && data.source === 'private') {
          // 處理私人聊天訊息
          const messageData = data.data
          console.log('收到私人訊息:', messageData)
          
          const chatMessage: ChatMessage = {
            fromUserId: messageData.fromUserId,
            toUserId: authStore.currentUser?.username || '',
            message: messageData.message,
            timestamp: messageData.timestamp,
            isOwn: false
          }
          
          // 添加到聊天記錄
          addChatMessage(messageData.fromUserId, chatMessage)
          
          // 如果聊天窗口未開啟或未選中該用戶，增加未讀計數
          if (!floatingChatVisible.value || selectedChatUser.value !== messageData.fromUserId) {
            unreadChatMessages.value++
          }
          
          // 顯示聊天通知
          ElMessage({
            message: `💬 來自 ${messageData.fromUserId}: ${messageData.message}`,
            type: 'success',
            duration: 4000,
            showClose: true
          })
        } else if (data.type === 'online-users') {
          // 處理在線用戶列表更新
          console.log('收到在線用戶列表:', data.data)
          if (data.data && data.data.users) {
            onlineUsers.value = data.data.users.filter(user => user !== authStore.currentUser?.username)
          }
        } else if (data.type === 'user-status') {
          // 處理用戶上線/下線狀態
          const statusData = data.data
          console.log('用戶狀態更新:', statusData)
          
          if (statusData.status === 'online') {
            if (!onlineUsers.value.includes(statusData.userId) && statusData.userId !== authStore.currentUser?.username) {
              onlineUsers.value.push(statusData.userId)
            }
          } else if (statusData.status === 'offline') {
            onlineUsers.value = onlineUsers.value.filter(user => user !== statusData.userId)
          }
        }
      } catch (error) {
        // 如果不是 JSON 格式，直接作為文本通知處理
        const notification = event.data
        console.log('收到文本通知:', notification)
        
        notifications.value.unshift(notification)
        if (notifications.value.length > 10) {
          notifications.value = notifications.value.slice(0, 10)
        }
        
        ElMessage({
          message: `📢 ${notification}`,
          type: 'info',
          duration: 5000,
          showClose: true
        })
      }
    }
    
    // 連接關閉事件
    websocket.onclose = (event) => {
      console.log('WebSocket 連接已關閉:', event)
      websocketConnected.value = false
      
      // 只有在非正常關閉時才嘗試重連（正常關閉代碼為1000）
      if (event.code !== 1000 && reconnectAttempts < maxReconnectAttempts) {
        reconnectAttempts++
        console.log(`嘗試重新連接 (${reconnectAttempts}/${maxReconnectAttempts})`)
        
        // 清理當前連接引用
        websocket = null
        
        setTimeout(() => {
          connectWebSocket()
        }, 3000) // 3秒後重試
      } else if (event.code !== 1000) {
        ElMessage.error('WebSocket 連接已斷開，已達到最大重連次數')
        websocket = null
      } else {
        // 正常關閉，清理連接引用
        websocket = null
      }
    }
    
    // 連接錯誤事件
    websocket.onerror = (error) => {
      console.error('WebSocket 發生錯誤:', error)
      websocketConnected.value = false
      ElMessage.error('WebSocket 連接發生錯誤')
    }
    
  } catch (error) {
    console.error('WebSocket 初始化失敗:', error)
    ElMessage.error('WebSocket 初始化失敗: ' + error)
  }
}

const disconnectWebSocket = () => {
  if (websocket) {
    console.log('正在關閉 WebSocket 連接...')
    
    // 移除所有事件監聽器，防止重連
    websocket.onopen = null
    websocket.onmessage = null
    websocket.onclose = null
    websocket.onerror = null
    
    // 只有在連接狀態下才關閉
    if (websocket.readyState === WebSocket.OPEN || websocket.readyState === WebSocket.CONNECTING) {
      websocket.close(1000, '用戶主動關閉連接')
    }
    
    websocket = null
    websocketConnected.value = false
    reconnectAttempts = 0 // 重置重連次數
    ElMessage.info('WebSocket 連接已關閉')
  }
}

const testWebSocketNotification = async () => {
  try {
    const testMessage = `測試通知 - ${new Date().toLocaleTimeString()}`
    
    // 方法1: 通過 WebSocket 直接發送
    if (websocket && websocketConnected.value) {
      const message = {
        action: 'send',
        topic: '/topic/notifications',
        message: testMessage
      }
      websocket.send(JSON.stringify(message))
      ElMessage.success('測試通知已通過 WebSocket 發送')
    } else {
      // 方法2: 通過 HTTP API 發送（備用方案）
      const response = await http.post('/api/websocket/notify', testMessage, {
        headers: {
          'Content-Type': 'application/json'
        }
      })
      
      console.log('通知發送成功:', response.data)
      ElMessage.success('測試通知已通過 API 發送')
    }
  } catch (error: any) {
    console.error('發送通知失敗:', error)
    ElMessage.error('發送通知失敗: ' + (error.response?.data || error.message))
  }
}

// 初始化
onMounted(async () => {
  // 先載入部門資料
  await fetchDepartments()
  
  // 再載入員工資料
  await fetchEmployeeProfile()
  
  // 初始化權限顯示
  updateModulePermissions()
  
  // 連接 WebSocket
  setTimeout(() => {
    connectWebSocket()
  }, 1000) // 延遲1秒確保頁面完全載入
})

// 組件卸載時斷開 WebSocket 連接
onUnmounted(() => {
  console.log('組件即將卸載，斷開 WebSocket 連接')
  disconnectWebSocket()
})
</script>

<style scoped>
.employee-profile {
  padding: 20px;
  min-height: 100vh;
}

/* 個人資訊卡片 */
.profile-card {
  text-align: center;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.profile-header {
  padding: 20px 0;
}

.avatar-container {
  position: relative;
  display: inline-block;
  margin-bottom: 20px;
}

/* 頭像上傳區域 */
.avatar-upload-area {
  position: relative;
  display: inline-block;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.avatar-upload-area:hover {
  transform: scale(1.02);
}

.avatar-upload-area:hover .avatar-overlay {
  opacity: 1;
}

.avatar-upload-area.avatar-loading {
  cursor: not-allowed;
}

.profile-avatar {
  border: 4px solid #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

/* 懸停遮罩層 */
.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: white;
  font-size: 12px;
  text-align: center;
}

.avatar-overlay .upload-icon {
  font-size: 24px;
  margin-bottom: 4px;
  color: #ffffff;
}

.avatar-overlay .loading-icon {
  font-size: 24px;
  margin-bottom: 4px;
  color: #ffffff;
  animation: rotate 2s linear infinite;
}

.avatar-overlay .upload-text {
  font-size: 11px;
  font-weight: 500;
  line-height: 1.2;
  max-width: 80px;
  word-wrap: break-word;
}

/* 旋轉動畫 */
@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.edit-avatar-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  border: 2px solid #ffffff;
}

.employee-name {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.employee-number {
  margin: 0 0 16px 0;
  color: #909399;
  font-size: 14px;
}

/* 基本資訊卡片 */
.info-card {
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


/* 統一權限卡片 */
.unified-permissions-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  min-height: 400px;
}

.permission-summary {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 角色標籤區 */
.role-badges-section {
  margin-bottom: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.section-title {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
  margin-right: 12px;
}

.role-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.role-badge {
  transition: all 0.2s ease;
}

.role-badge:hover {
  transform: scale(1.05);
}

/* 權限模組網格 */
.permissions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  margin-top: 8px;
}

.permission-module {
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.2s ease;
  position: relative;
}

.permission-module:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.module-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.module-icon {
  font-size: 18px;
  color: #409eff;
}

.module-name {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
  flex: 1;
}

.module-description {
  margin: 0;
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
}

/* 快速操作卡片 */
.quick-actions-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  justify-content: flex-start;
  text-align: left;
  height: 48px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.action-btn:hover {
  transform: translateX(4px);
}

.action-btn .el-icon {
  margin-right: 8px;
}

/* 活動卡片 */
.activity-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.no-activity {
  text-align: center;
  padding: 20px 0;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .employee-profile {
    padding: 12px;
  }

  .profile-header {
    padding: 16px 0;
  }

  .profile-avatar {
    width: 100px !important;
    height: 100px !important;
  }

  .employee-name {
    font-size: 20px;
  }

  .quick-actions {
    gap: 8px;
  }

  .action-btn {
    height: 40px;
    font-size: 14px;
  }
  
  /* 手機版權限區塊 */
  .permissions-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .permission-module {
    padding: 12px;
  }
  
  .role-badges-section {
    padding: 12px;
    margin-bottom: 12px;
  }
  
  .role-badges {
    gap: 6px;
  }
  
  .permission-summary {
    flex-wrap: wrap;
    gap: 6px;
  }
  
  .unified-permissions-card {
    min-height: auto;
  }
}

/* Element Plus 組件樣式調整 */
:deep(.el-descriptions) {
  margin-top: 0;
}

:deep(.el-descriptions__body .el-descriptions__table) {
  border-radius: 8px;
}

:deep(.el-card__header) {
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
}

/* 確保卡片標題中的按鈕可見 */
.card-header .header-actions .el-button {
  background-color: #409eff !important;
  border-color: #409eff !important;
  color: #ffffff !important;
}

.card-header .header-actions .el-button:hover {
  background-color: #66b1ff !important;
  border-color: #66b1ff !important;
}

.card-header .edit-actions .el-button {
  border: 1px solid #dcdfe6 !important;
  background-color: #ffffff !important;
  color: #606266 !important;
}

.card-header .edit-actions .el-button--primary {
  background-color: #409eff !important;
  border-color: #409eff !important;
  color: #ffffff !important;
}

.card-header .edit-actions .el-button:hover {
  background-color: #ecf5ff !important;
  border-color: #b3d8ff !important;
  color: #409eff !important;
}

.card-header .edit-actions .el-button--primary:hover {
  background-color: #66b1ff !important;
  border-color: #66b1ff !important;
  color: #ffffff !important;
}

:deep(.el-timeline) {
  padding-left: 0;
}

:deep(.el-badge__content) {
  border: 2px solid #ffffff;
}

/* 滾動條樣式 */
.roles-container::-webkit-scrollbar {
  width: 6px;
}

.roles-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.roles-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.roles-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 收起展開按鈕 */
.collapse-btn {
  color: #409eff;
  transition: all 0.2s ease;
}

.collapse-btn:hover {
  color: #66b1ff;
}

.no-permissions {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  grid-column: 1 / -1;
}

/* 編輯模式樣式 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.readonly-info {
  margin-bottom: 20px;
}

.readonly-fields {
  margin-bottom: 16px;
}

.readonly-fields :deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  color: #6c757d;
}

.readonly-fields :deep(.el-form-item__label) {
  color: #6c757d;
  font-weight: 500;
}

.edit-actions {
  display: flex;
  gap: 8px;
}

.edit-form {
  padding: 16px 0;
}

.edit-form .el-form-item {
  margin-bottom: 20px;
}

.changes-alert {
  margin-top: 16px;
}

.changes-alert .el-alert {
  border-radius: 8px;
}

/* 編輯表單動畫 */
.edit-form {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 編輯模式表單樣式優化 */
.edit-form :deep(.el-input__wrapper) {
  border-radius: 6px;
}

.edit-form :deep(.el-select) {
  width: 100%;
}

.edit-form :deep(.el-date-editor) {
  width: 100%;
}

/* 通訊錄模態框樣式 */
:deep(.contacts-dialog) {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e7ed;
}

:deep(.contacts-dialog .el-dialog__header) {
  background: #fafafa;
  color: #303133;
  border-radius: 12px 12px 0 0;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.contacts-dialog .el-dialog__title) {
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

:deep(.contacts-dialog .el-dialog__close) {
  color: #909399;
  font-size: 18px;
  transition: color 0.2s ease;
}

:deep(.contacts-dialog .el-dialog__close:hover) {
  color: #606266;
}

:deep(.contacts-dialog .el-dialog__body) {
  padding: 24px;
  max-height: 70vh;
  overflow: hidden;
}

.contacts-dialog-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.contacts-search-section {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.contacts-search-info {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 32px;
}

.contacts-table-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.contacts-table {
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  font-size: 14px;
  flex: 1;
}

/* 表格樣式優化 - 緊湊設計 */
:deep(.contacts-table .el-table__header) {
  background: #fafafa;
}

:deep(.contacts-table .el-table th) {
  background: transparent;
  color: #303133;
  font-weight: 600;
  font-size: 14px;
  text-align: center;
  border-bottom: 2px solid #e4e7ed;
  height: 48px;
}

:deep(.contacts-table .el-table th .cell) {
  padding: 0 8px;
  white-space: nowrap;
}

:deep(.contacts-table .el-table td) {
  height: 40px;
  border-bottom: 1px solid #f3f4f6;
  vertical-align: middle;
}

:deep(.contacts-table .el-table td .cell) {
  padding: 0 4px;
  line-height: 1.4;
}

/* 斑馬紋效果 */
:deep(.contacts-table .el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #f9fafb;
}

:deep(.contacts-table .el-table__body tr:nth-child(even) td) {
  background-color: #f9fafb;
}

/* hover 效果 */
:deep(.contacts-table .el-table__body tr:hover > td) {
  background-color: #f5f7fa !important;
  transition: background-color 0.2s ease;
}

/* 聯繫人內容樣式 */
.contact-name-text {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.contact-department-tag {
  border-radius: 4px;
  font-size: 12px;
  padding: 2px 8px;
  font-weight: 500;
}

.contact-position-text {
  color: #606266;
  font-weight: 500;
  font-size: 13px;
}

.contact-email-cell {
  display: flex;
  align-items: center;
}

.contact-email-text {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
}

.contact-email-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.contact-email-icon.disabled {
  color: #c0c4cc;
}

.contact-no-email-text {
  color: #c0c4cc;
  font-style: italic;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.contacts-pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding: 16px 0;
  background: #fafafa;
  border-top: 1px solid #e4e7ed;
  border-radius: 0 0 8px 8px;
}

.contacts-pagination {
  background: white;
}

:deep(.contacts-pagination .el-pager li) {
  min-width: 32px;
  height: 32px;
  line-height: 30px;
}

:deep(.contacts-pagination .btn-prev),
:deep(.contacts-pagination .btn-next) {
  padding: 0 8px;
  height: 32px;
  line-height: 30px;
}

/* 響應式設計 - 通訊錄模態框 */
@media (max-width: 768px) {
  :deep(.contacts-dialog) {
    width: 95% !important;
    margin: 0 auto;
  }
  
  :deep(.contacts-dialog .el-dialog__body) {
    padding: 16px;
    max-height: 75vh;
  }
  
  .contacts-search-section {
    padding: 12px;
  }
  
  .contacts-search-section .el-row {
    flex-direction: column;
    gap: 12px;
  }
  
  .contacts-search-info {
    justify-content: center;
  }
}

/* 通知相關樣式 */
.notifications-section {
  margin-top: 16px;
}

.notifications-list {
  max-height: 200px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  margin-bottom: 4px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 3px solid #409eff;
  font-size: 13px;
  line-height: 1.4;
}

.notification-icon {
  color: #409eff;
  font-size: 14px;
  flex-shrink: 0;
}

.notification-text {
  color: #606266;
  word-break: break-word;
}

.notification-item:hover {
  background: #f0f7ff;
}

/* 響應式 - 編輯模式 */
@media (max-width: 768px) {
  .header-actions {
    flex-direction: column;
    gap: 6px;
    align-items: flex-end;
  }
  
  .edit-actions {
    flex-direction: row;
    justify-content: flex-end;
  }
  
  .edit-form {
    padding: 12px 0;
  }
  
  .edit-form .el-form-item {
    margin-bottom: 16px;
  }
  
  .edit-form :deep(.el-form-item__label) {
    font-size: 14px;
  }
  
  .notifications-section {
    margin-top: 12px;
  }
  
  .notification-item {
    padding: 6px 8px;
    font-size: 12px;
  }
}

/* 浮動聊天樣式 */
.floating-chat-button {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
  transition: all 0.3s ease;
}

.floating-chat-button:hover {
  transform: scale(1.1);
}

.floating-chat-window {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 300px;
  height: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
}

.floating-chat-window.minimized {
  height: 50px;
}

.chat-window-header {
  background: #409eff;
  color: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 16px;
}

.header-title {
  font-weight: 500;
  font-size: 12px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-actions .el-button {
  color: white;
  border: none;
  background: transparent;
}

.header-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.1);
}

.chat-window-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 用戶列表樣式 */
.floating-users-section {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.section-title {
  padding: 12px 16px;
  font-weight: 500;
  font-size: 14px;
  color: #303133;
  border-bottom: 1px solid #f0f0f0;
}

.floating-users-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.no-users-mini {
  text-align: center;
  color: #909399;
  font-size: 12px;
  padding: 20px;
}

.floating-user-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  border-radius: 6px;
  transition: background-color 0.2s;
  position: relative;
}

.floating-user-item:hover {
  background-color: #f5f7fa;
}

.floating-user-item.has-unread {
  background-color: #fef0f0;
}

.user-name-mini {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-badge-mini {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
}

/* 聊天區域樣式 */
.floating-chat-area {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.floating-chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.chat-user-name-mini {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.chat-header-actions {
  display: flex;
  gap: 4px;
}

.floating-messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  scroll-behavior: smooth;
}

.no-messages-mini {
  text-align: center;
  color: #909399;
  font-size: 12px;
  padding: 20px;
}

.floating-messages-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.floating-message-item {
  display: flex;
  width: 100%;
}

.floating-message-item.own-message {
  justify-content: flex-end;
}

.floating-message-bubble {
  max-width: 80%;
  padding: 8px 12px;
  border-radius: 12px;
  background: #e8e8e8;
  color: #2c3e50;
  word-wrap: break-word;
  border: 1px solid #d0d0d0;
}

.floating-message-item.own-message .floating-message-bubble {
  background: #409eff;
  color: white;
  border: 1px solid #3a8ee6;
}

.message-text {
  font-size: 13px;
  line-height: 1.4;
  font-weight: 500;
}

.message-time-mini {
  font-size: 10px;
  opacity: 0.75;
  margin-top: 2px;
  font-weight: 400;
}

/* 確保自己訊息中的時間文字在白色背景下可見 */
.floating-message-item.own-message .message-time-mini {
  opacity: 0.85;
}

/* 為對方訊息添加更好的視覺區分 */
.floating-message-bubble {
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.2s ease;
}

.floating-message-bubble:hover {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.floating-message-item.own-message .floating-message-bubble {
  box-shadow: 0 1px 3px rgba(64, 158, 255, 0.3);
}

.floating-message-item.own-message .floating-message-bubble:hover {
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.4);
}

.floating-input-container {
  padding: 12px;
  border-top: 1px solid #f0f0f0;
  background: white;
}

.floating-message-input {
  width: 100%;
}

/* 滾動條樣式 */
.floating-users-list::-webkit-scrollbar,
.floating-messages-container::-webkit-scrollbar {
  width: 4px;
}

.floating-users-list::-webkit-scrollbar-track,
.floating-messages-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.floating-users-list::-webkit-scrollbar-thumb,
.floating-messages-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.floating-users-list::-webkit-scrollbar-thumb:hover,
.floating-messages-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 浮動聊天響應式設計 */
@media (max-height: 600px) {
  .floating-chat-window {
    height: 350px;
  }
  
  .floating-messages-container {
    padding: 8px;
  }
  
  .floating-input-container {
    padding: 8px;
  }
}

@media (max-width: 768px) {
  .floating-chat-button {
    bottom: 15px;
    right: 15px;
  }
  
  .floating-chat-window {
    bottom: 15px;
    right: 15px;
    width: 280px;
  }
}

@media (max-width: 480px) {
  .floating-chat-window {
    width: calc(100vw - 30px);
    right: 15px;
    left: 15px;
  }
}
</style>
