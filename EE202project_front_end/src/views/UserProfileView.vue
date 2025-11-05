<template>
  <div class="user-profile-view">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>個人資料</span>
        </div>
      </template>
      <el-form label-width="120px">
        <el-form-item label="員工ID">
          <el-input :value="currentUser?.employeeId" disabled></el-input>
        </el-form-item>
        <el-form-item label="使用者名稱">
          <el-input :value="currentUser?.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="電子郵件">
          <el-input :value="currentUser?.email" disabled></el-input>
        </el-form-item>
        <el-form-item label="員工類型">
          <el-input :value="currentUser?.employeeType" disabled></el-input>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useAuthStore } from '../stores/AuthStore';

const authStore = useAuthStore();
const currentUser = computed(() => authStore.currentUser);
</script>

<style scoped>
.user-profile-view {
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