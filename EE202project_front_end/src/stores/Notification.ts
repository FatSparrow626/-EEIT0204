import {defineStore} from 'pinia'
import api from '@/services/api'

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    unreadCount: 0 as number, //未讀通知數量
    timer: null as ReturnType<typeof setInterval> | null //計時器,紀錄 setInterval 的 ID
  }),
    actions: {
      async fetchUnread(){ //從後端API抓未讀數量
        try{
          const {data} = await api.get('/api/notifications/unread-count');
          this.unreadCount= data;
        }catch(e){
          //處理401
        }
      },
        startPolling(intervalMs = 60000){
          this.stopPolling(); //先清掉舊的
          this.fetchUnread(); //再重新抓新的
          this.timer = setInterval(()=>this.fetchUnread(), intervalMs);
        },

        stopPolling(){
          if(this.timer){clearInterval(this.timer) ; this.timer=null};
        },

        decrement(n = 1) {
       this.unreadCount = Math.max(0, this.unreadCount - n)
    }
    }

})
