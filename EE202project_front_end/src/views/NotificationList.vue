<template>
  <el-card>
    <template #header>
      <div class="flex items-center justify-between">
        <span>通知中心</span>
        <div class="flex items-center gap-2">
          <el-radio-group v-model="mode" size="small" @change="load">
            <el-radio-button label="unread">未讀</el-radio-button>
            <el-radio-button label="read">已讀</el-radio-button>
          </el-radio-group>
          <el-button v-if="mode === 'unread'" size="small" type="primary" :loading="opLoading" @click="markAllRead">全部已讀</el-button>
          <el-button v-if="mode === 'read'" size="small" type="danger" :loading="opLoading" @click="deleteAllRead">全部刪除</el-button>
        </div>
      </div>
    </template>

    <el-table
      :data="items"
      v-loading="loading"
      :header-cell-style="{ fontWeight: 600 }"
      style="width: 100%"
    >
      <el-table-column prop="title" label="標題" width="160" />
      <el-table-column prop="message" label="內容" />
      <el-table-column prop="createdAt" label="時間" width="180" :formatter="(_, __, cell) => formatTime(cell)"/>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="open(row)">查看</el-button>
          <el-button v-if="mode === 'unread'" size="small" type="primary" :loading="row.__op === 'read'" @click="markRead(row)">標記已讀</el-button>
          <el-button v-if="mode === 'read'" size="small" type="danger" :loading="row.__op === 'del'" @click="remove(row)">刪除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && items.length === 0" class="mt-6" description="目前沒有通知"/>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useNotificationStore } from '@/stores/Notification'

type Notice = {
  id: number
  userId: number
  type: string
  title: string
  message: string
  link?: string | null
  read: boolean
  createdAt: string
  // 前端內部使用的暫時狀態
  __op?: 'read' | 'del'
}

const store = useNotificationStore()
const router = useRouter()
const items = ref<Notice[]>([])
const loading = ref(false)
const opLoading = ref(false)
const mode = ref<'unread' | 'read'>('unread')

/** 時間格式化：2025/08/11 08:50:01 */
function formatTime(val?: string) {
  if (!val) return ''
  // 兼容後端是 ISO 字串或 LocalDateTime 字串
  const d = new Date(val)
  if (isNaN(d.getTime())) return val
  const pad = (n: number) => String(n).padStart(2, '0')
  const y = d.getFullYear()
  const m = pad(d.getMonth() + 1)
  const day = pad(d.getDate())
  const hh = pad(d.getHours())
  const mm = pad(d.getMinutes())
  const ss = pad(d.getSeconds())
  return `${y}/${m}/${day} ${hh}:${mm}:${ss}`
}

async function load() {
  loading.value = true
  try {
    const { data } = await api.get<Notice[]>('/api/notifications', {
      params: { status: mode.value }
    })
    items.value = data ?? []
  } finally {
    loading.value = false
  }
}

//查看詳情
function open(row: Notice) {
  if (row.link) router.push(row.link)
}

// 單筆標記已讀
async function markRead(row: Notice) {
  try {
    row.__op = 'read'
    await api.post(`/api/notifications/${row.id}/read`)
    ElMessage.success('已標記為已讀')
    await Promise.all([load(), store.fetchUnread()])
  } finally {
    row.__op = undefined
  }
}

// 全部標記已讀（未讀分頁才顯示）
async function markAllRead() {
  try {
    await ElMessageBox.confirm('要將所有未讀通知標記為已讀嗎？', '確認', {
      type: 'warning',
      confirmButtonText: '確定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  opLoading.value = true
  try {
    await api.post('/api/notifications/read-all')
    ElMessage.success('已全部標記為已讀')
    await Promise.all([load(), store.fetchUnread()])
  } finally {
    opLoading.value = false
  }
}

// 單筆刪除（已讀分頁才顯示）
async function remove(row: Notice) {
  try {
    await ElMessageBox.confirm('確定刪除此通知？', '確認', {
      type: 'warning',
      confirmButtonText: '刪除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    row.__op = 'del'
    await api.delete(`/api/notifications/${row.id}`)
    ElMessage.success('已刪除')
    await Promise.all([load(), store.fetchUnread()])
  } finally {
    row.__op = undefined
  }
}

// 全部刪除已讀（已讀分頁才顯示)
async function deleteAllRead() {
  try {
    await ElMessageBox.confirm('要刪除所有已讀通知嗎？此動作無法復原。', '確認', {
      type: 'warning',
      confirmButtonText: '全部刪除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  opLoading.value = true
  try {
    await api.delete('/api/notifications/read')
    ElMessage.success('已刪除所有已讀通知')
    await Promise.all([load(), store.fetchUnread()])
  } finally {
    opLoading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.flex {
  display: flex;
}
.items-center {
  align-items: center;
}
.justify-between {
  justify-content: space-between;
}
.gap-2 {
  gap: 8px;
}
.mt-6 {
  margin-top: 24px;
}
</style>
