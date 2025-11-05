<template>
  <el-badge :value="store.unreadCount || 0" :hidden="!(store.unreadCount > 0)">
    <el-button circle @click="goList">
      <el-icon><Bell /></el-icon>
    </el-button>
  </el-badge>
</template>

<script setup lang="ts">
import { onMounted,onBeforeUnmount } from 'vue';
import { Bell } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { useNotificationStore } from '@/stores/Notification';
const store = useNotificationStore();
const router = useRouter();

function goList(){
  router.push('/notifications') //對應後端API
}
onMounted(()=>store.startPolling(60000));//每60秒去抓尚未讀取通知的數量
onBeforeUnmount(()=>store.stopPolling());//銷毀之前執行stopPolling方法 去暫停計時器 避免銷毀後仍然吃效能
</script>
