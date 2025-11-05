import axios from "axios";
import { useAuthStore } from './stores/AuthStore';

const http = axios.create({
  baseURL: "http://localhost:8082/api", // 您的後端 API 基礎 URL
  headers: {
    "Content-type": "application/json",
  },
});

http.interceptors.request.use(
  (config) => {
    const user = JSON.parse(localStorage.getItem("user") || "{}");
    if (user && user.token) {
      config.headers.Authorization = "Bearer " + user.token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

http.interceptors.response.use(
  response => response,
  error => {
    if (error.response.status === 401) {
      const authStore = useAuthStore();
      authStore.logout();
      // Optionally, redirect to login page
      // window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default http;