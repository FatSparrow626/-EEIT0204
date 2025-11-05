<script setup>
import { ref } from 'vue'
import RepairList from './admin/AdminRepairList.vue'
import RepairSearch from './admin/AdminRepairSearch.vue'

const showOriginalList = ref(true)

// 搜尋完成時改為 false，隱藏原始列表，顯示搜尋結果
function handleSearchComplete() {
  showOriginalList.value = false
  console.log('搜尋完成，隱藏原始列表')
}

// 清除搜尋時改為 true，顯示原始列表，隱藏搜尋結果
function handleSearchClear() {
  showOriginalList.value = true
  console.log('清除搜尋，顯示原始列表')
}
</script>

<template>
  <div class="repair-admin">
   

    <!-- 搜尋組件永遠顯示 -->
    <RepairSearch 
      @search-complete="handleSearchComplete" 
      @search-clear="handleSearchClear" 
    />

    <!-- 原本列表，搜尋時隱藏 -->
    <RepairList v-if="showOriginalList" />

    <!-- 搜尋結果區塊，搜尋時顯示 -->
    <div v-else class="search-results-container">
      <el-alert
        title="提示"
        type="info"
        description="目前顯示的是搜尋結果，點擊上方的「清除」按鈕可回到完整列表"
        :closable="false"
        show-icon
        style="margin: 20px 0;"
      />
    </div>
  </div>
</template>

<style scoped>
.repair-admin {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.page-header h1 {
  margin: 0 0 10px 0;
  font-size: 2.5em;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.subtitle {
  margin: 0;
  font-size: 1.1em;
  opacity: 0.9;
  font-weight: 300;
}

.search-results-container {
  flex: 1;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .repair-admin {
    padding: 10px;
    gap: 15px;
  }
  
  .page-header {
    padding: 20px 15px;
  }
  
  .page-header h1 {
    font-size: 2em;
  }
}
</style>