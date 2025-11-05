<template>
  <div class="machine-user">
    <!-- 搜尋組件：永遠顯示 -->
    <UserMachineSearch @search-complete="handleSearchComplete" @search-clear="handleSearchClear" />

    <!-- 原本的機台列表：只有在沒有搜尋且沒進入詳情時才顯示 -->
    <UserMachineList
      v-if="showOriginalList && !showDetailPage"
      @show-detail="handleShowDetail"
    />

    <!-- 詳情頁面 -->
    <div v-else-if="showDetailPage">
      <UserMachineDetail
        :machineId="detailMachineId"
        @back="handleBackFromDetail"
      />
    </div>

    <!-- 搜尋結果提示 -->
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
import UserMachineList from './user/UserMachineList.vue'
import UserMachineSearch from './user/UserMachineSearch.vue'
import UserMachineDetail from './user/UserMachineDetail.vue'

// 控制顯示狀態：true=顯示原本列表，false=顯示搜尋結果
const showOriginalList = ref(true)
const showDetailPage = ref(false)
const detailMachineId = ref(null)

// 當搜尋完成時
const handleSearchComplete = () => {
  showOriginalList.value = false // 隱藏原本列表
  showDetailPage.value = false
}

// 當清除搜尋時
const handleSearchClear = () => {
  showOriginalList.value = true // 顯示原本列表
  showDetailPage.value = false
}

// 顯示詳情頁面
const handleShowDetail = (id) => {
  detailMachineId.value = id
  showDetailPage.value = true
  showOriginalList.value = false
}

// 從詳情頁面返回
const handleBackFromDetail = () => {
  showDetailPage.value = false
  detailMachineId.value = null
  showOriginalList.value = true
}
</script>

<style scoped>
.machine-user {
  display: flex;
  flex-direction: column;
  gap: 20px; /* 加入垂直間距 */
}
</style>
