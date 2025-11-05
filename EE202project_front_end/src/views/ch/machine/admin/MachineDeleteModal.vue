<!-- MachineDeleteModal.vue -->
<script setup>
import { ref } from 'vue'

const props = defineProps({
  machine: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['close', 'deleted'])
const loading = ref(false)

async function confirmDelete() {
  if (!props.machine || !props.machine.machineId) return

  loading.value = true
  try {
    const userJson = localStorage.getItem('user')
    const user = userJson ? JSON.parse(userJson) : null
    const token = user ? user.token : null

    if (!token) {
      throw new Error('未找到驗證權杖，請先登入。')
    }
    const res = await fetch(`http://localhost:8082/api/machines/${props.machine.machineId}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    if (res.status === 401) {
      throw new Error('驗證已過期，請重新登入。')
    }
    if (res.status === 403) {
      throw new Error('您的權限不足。')
    }
    if (!res.ok) {
      alert('刪除失敗！')
      return
    }

    alert('刪除成功！')
    emit('deleted') // 通知父元件刷新
  } catch (err) {
    alert('網路錯誤或伺服器錯誤')
  } finally {
    loading.value = false
    emit('close') // 關閉視窗
  }
}

function cancelDelete() {
  emit('close')
}
</script>

<template>
  <div class="modal">
    <div class="modal-content">
      <div class="modal-header">
        <h3>❗ 確認刪除</h3>
        <button class="close-btn" @click="cancelDelete">✕</button>
      </div>
      <div class="modal-body">
        <p>
          你確定要刪除機台 <strong>#{{ machine.machineId }}</strong> 嗎？
        </p>
        <div class="machine-info">
          <p><strong>機台名稱：</strong>{{ machine.machineName }}</p>
          <p><strong>出廠編號：</strong>{{ machine.serialNumber }}</p>
          <p><strong>位置：</strong>{{ machine.machineLocation }}</p>
        </div>
        <p style="font-size: 13px; color: gray">此操作無法復原。</p>
      </div>
      <div class="modal-footer">
        <button @click="cancelDelete" :disabled="loading">取消</button>
        <button @click="confirmDelete" :disabled="loading" class="danger">
          {{ loading ? '刪除中...' : '確定刪除' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 90%;
  max-width: 450px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.modal-header h3 {
  margin: 0;
  color: #e74c3c;
  font-size: 18px;
}

.modal-body {
  margin-bottom: 20px;
  font-size: 16px;
}

.machine-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
  margin: 15px 0;
  border-left: 4px solid #3498db;
}

.machine-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #2c3e50;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

button {
  padding: 10px 20px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.3s ease;
}

button:first-child {
  background-color: #95a5a6;
  color: white;
}

button:first-child:hover:not(:disabled) {
  background-color: #7f8c8d;
}

button.danger {
  background-color: #e74c3c;
  color: white;
}

button.danger:hover:not(:disabled) {
  background-color: #c0392b;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.close-btn {
  font-size: 18px;
  background: none;
  border: none;
  cursor: pointer;
  color: #95a5a6;
  padding: 5px;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background-color: #f8f9fa;
  color: #e74c3c;
}
</style>
