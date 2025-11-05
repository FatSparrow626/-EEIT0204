package com.project.employeeuser.config;

import com.project.employeeuser.handler.SimpleWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置類別 - 原生版本
 * 
 * 用途：
 * 1. 啟用原生 WebSocket 支持
 * 2. 註冊自定義 WebSocket 處理器
 * 3. 支援即時通訊功能（如通知、聊天、狀態更新等）
 * 
 * 原理說明：
 * - 使用原生 WebSocket API，無需 STOMP 協議
 * - 自定義訊息格式和路由邏輯
 * - 輕量化設計，減少依賴和複雜性
 */
@Configuration
@EnableWebSocket  // 啟用原生 WebSocket 功能
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private SimpleWebSocketHandler simpleWebSocketHandler;

    /**
     * 註冊 WebSocket 處理器
     * 
     * 用途：定義 WebSocket 端點和對應的處理器
     * 
     * @param registry WebSocket 處理器註冊器
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 註冊原生 WebSocket 端點 "/websocket"
        // 使用自定義的 SimpleWebSocketHandler 處理所有 WebSocket 通訊
        registry.addHandler(simpleWebSocketHandler, "/websocket")
                // 允許跨域請求，前端可以從不同域名連接
                .setAllowedOrigins("http://localhost:5173", "http://172.22.34.82:5173");
        
        System.out.println("WebSocket 處理器已註冊到端點: /websocket");
    }
}

/*
原生 WebSocket 配置說明：

1. 架構變更：
   - 移除 STOMP 和 SockJS 依賴
   - 使用原生 WebSocket API
   - 自定義訊息處理邏輯

2. 端點配置：
   - /websocket - 原生 WebSocket 端點
   - 使用 SimpleWebSocketHandler 處理所有通訊

3. 優點：
   - 輕量化設計，減少依賴
   - 完全自主控制訊息格式
   - 更好的性能表現
   - 簡化的架構設計

4. 原生 WebSocket 連接範例：
   ```javascript
   const websocket = new WebSocket('ws://localhost:8082/websocket');
   
   websocket.onopen = function() {
       // 發送身份識別
       websocket.send(JSON.stringify({
           action: 'identify',
           userId: 'user001'
       }));
   };
   
   websocket.onmessage = function(event) {
       const message = JSON.parse(event.data);
       console.log('收到訊息:', message);
       
       switch(message.type) {
           case 'notification':
               // 處理系統通知
               break;
           case 'message':
               // 處理私人訊息
               break;
           case 'user-status':
               // 處理用戶狀態更新
               break;
       }
   };
   ```

5. 訊息格式：
   客戶端請求：
   ```json
   {
     "action": "identify|subscribe|send|private|ping",
     "userId": "user001",
     "message": "訊息內容"
   }
   ```
   
   服務端回應：
   ```json
   {
     "source": "system|broadcast|private",
     "type": "notification|message|error|pong",
     "data": { ... },
     "timestamp": 1234567890
   }
   ```

6. 功能特色：
   - 用戶身份識別和會話管理
   - 實時廣播通知
   - 點對點私人訊息
   - 在線用戶狀態追蹤
   - 心跳檢測機制
   - JSON 格式訊息處理

這個原生配置提供了更直接、高效的 WebSocket 通訊解決方案！
*/