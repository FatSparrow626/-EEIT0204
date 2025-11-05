import { defineStore } from 'pinia';
import AuthService from '../services/AuthService';

interface User {  //登入物件包含以下 沒有一個就等於null
  token: string;
  type: string;
  employeeId: number;
  username: string;
  email: string;
  employeeType: string;
  authorities: string[];
}

interface AuthState {
  user: User | null;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({  // 一開始會從 localStorage 讀取 user
    user: JSON.parse(localStorage.getItem('user') || 'null'),
  }),
  getters: {
    loggedIn: (state) => !!state.user,  //判斷登入
    currentUser: (state) => state.user, //取得目前登入的使用者資訊
  },
  actions: {
    async login(user: any) { //呼叫 AuthService.login() 登入,並把回傳的respone存進this.user
      const response = await AuthService.login(user);
      this.user = response;// 儲存 user (包含 token)
      localStorage.setItem('user', JSON.stringify(response));// 同步存進 localStorage，避免刷新掉
      return response;
    },
    logout() {  //登出,呼叫 AuthService.logout(),然後 user = null
      AuthService.logout();
      this.user = null;
      localStorage.removeItem('user'); //登出時應該要清除 user & localStorage
    },
    async register(user: any) { // 呼叫 AuthService.register() 註冊。
      return AuthService.register(user);
    },
  },
});