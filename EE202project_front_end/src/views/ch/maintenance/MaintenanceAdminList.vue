<template>
  <div class="maintenance-admin">
    <div class="header">
      <h1>🔧 保養管理系統</h1>
    </div>

    <!-- 搜尋組件 -->
    <div class="section">
      <MaintenanceSearch
        @search-complete="handleSearchComplete"
        @search-clear="handleSearchClear"
      />
    </div>

    <!-- 原本列表，搜尋時隱藏 -->
    <div class="section" v-if="showOriginalList">
      <MaintenanceList />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import MaintenanceList from './admin/AdminMaintenanceList.vue'
import MaintenanceSearch from './admin/AdminMaintenanceSearch.vue'

const showOriginalList = ref(true)

const handleSearchComplete = () => {
  showOriginalList.value = false
}
const handleSearchClear = () => {
  showOriginalList.value = true
}
</script>

<style scoped>
.section {
  margin-bottom: 20px;
}
</style>
