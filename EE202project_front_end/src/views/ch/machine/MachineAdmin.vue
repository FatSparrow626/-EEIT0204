<template>
  <div class="machine-admin">
    <AdminMachineSearch 
      v-if="!showRecordPage"
      ref="searchComponentRef"
      @search-complete="handleSearchComplete" 
      @search-clear="handleSearchClear"
      @show-detail="handleShowDetail" 
    />

    <AdminMachineList 
      v-if="showOriginalList && !showDetailPage && !showRecordPage" 
      @show-detail="handleShowDetail" 
    />

    <!-- 詳細頁面 -->
    <div v-else-if="showDetailPage && !showRecordPage">
      <AdminMachineDetail 
        :machineId="detailMachineId" 
        @back="handleBackFromDetail"
        @machine-deleted="handleMachineDeleted"
        @show-record="handleShowRecord"
      />
    </div>

    <!-- 維修保養紀錄頁面 -->
    <div v-else-if="showRecordPage">
      <MaintenanceRepairRecords 
        :machineId="detailMachineId"
        @back="handleBackFromRecord"
      />
    </div>

    <div v-else class="search-results-container">
      <el-alert
        title="搜尋結果已顯示在上方搜尋組件中"
        type="info"
        :closable="false"
        show-icon
        style="margin: 20px"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AdminMachineList from './admin/AdminMachineList.vue'
import AdminMachineSearch from './admin/AdminMachineSearch.vue'
import AdminMachineDetail from './admin/AdminMachineDetail.vue'
import MaintenanceRepairRecords from './MaintenanceRepairRecords.vue'

const searchComponentRef = ref(null)
const showOriginalList = ref(true)
const showDetailPage = ref(false)
const showRecordPage = ref(false)
const detailMachineId = ref(null)

const handleSearchComplete = () => {
  showOriginalList.value = false
  showDetailPage.value = false
  showRecordPage.value = false
}

const handleSearchClear = () => {
  showOriginalList.value = true
  showDetailPage.value = false
  showRecordPage.value = false
}

const handleShowDetail = (id) => {
  detailMachineId.value = id
  showDetailPage.value = true
  showOriginalList.value = false
  showRecordPage.value = false
  if (searchComponentRef.value) {
    searchComponentRef.value.hideSearchResults()
  }
}

// 詳情頁點返回
const handleBackFromDetail = () => {
  showDetailPage.value = false
  detailMachineId.value = null
  showRecordPage.value = false
  if (searchComponentRef.value && searchComponentRef.value.hasSearchConditions()) {
    searchComponentRef.value.showSearchResults()
    showOriginalList.value = false
  } else {
    showOriginalList.value = true
  }
}

// 詳情頁點「維修保養紀錄」
const handleShowRecord = () => {
  showRecordPage.value = true
  showDetailPage.value = false
  showOriginalList.value = false
}

// 紀錄頁點返回
const handleBackFromRecord = () => {
  showRecordPage.value = false
  showDetailPage.value = true
  showOriginalList.value = false
}

const handleMachineDeleted = () => {
  showDetailPage.value = false
  detailMachineId.value = null
  showOriginalList.value = true
  showRecordPage.value = false
  if (searchComponentRef.value) {
    searchComponentRef.value.clearSearch()
  }
}
</script>

<style scoped>
.machine-admin {
  display: flex;
  flex-direction: column;
  gap: 20px; /* 加入垂直間距 */
}
</style>