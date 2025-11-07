import axios from 'axios'
import router from '@/router'

// Axios預設請求設定
const api = axios.create({
  baseURL: 'http://localhost:8082',
  headers: {
    'Content-Type': 'application/json'
  }
})

// 請求攔截器
// 自動帶 Token + 附件上傳content-type設定(瀏覽器決定)
api.interceptors.request.use((config) => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (user?.token) {
    config.headers.Authorization = `Bearer ${user.token}`
  }

  // If the request is sending FormData, let the browser set the Content-Type.
  // Otherwise, keep the default 'application/json'.
  if (config.data instanceof FormData) {
    // Axios/browser will set the correct Content-Type with boundary.
    config.headers.delete('Content-Type');
  }

  // console.log("Request 發送前的設定：", config);
  return config
})

// 如果 401，跳回登入
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      // localStorage.removeItem('user')
      router.push('/login')
    }
    return Promise.reject(err)
  }
)

export default api