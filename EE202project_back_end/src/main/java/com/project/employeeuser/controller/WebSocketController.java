package com.project.employeeuser.controller;

import com.project.employeeuser.handler.SimpleWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

/**
 * WebSocket REST API 控制器
 * 
 * 用途：提供 WebSocket 相關的 HTTP API 端點
 * 功能：
 * 1. 發送系統通知給所有在線用戶
 * 2. 獲取在線用戶列表
 * 3. 系統集成接口
 * 
 * 注：實際的 WebSocket 通訊由 SimpleWebSocketHandler 處理
 */
@RestController
@RequestMapping("/api/websocket")
@CrossOrigin(origins = {"http://localhost:5173", "http://172.22.34.82:5173"})
public class WebSocketController {

    @Autowired
    private SimpleWebSocketHandler webSocketHandler;

    /**
     * 發送系統通知(stop)
     * 
     * 用途：向所有在線用戶廣播通知消息
     * 路徑：POST /api/websocket/notify
     * 
     * @param message 通知內容
     * @return 發送結果
     */
    @PostMapping("/notify")
    public String sendNotification(@RequestBody String message) {
        try {
            webSocketHandler.sendSystemNotification(message);
            return "通知已發送: " + message;
        } catch (Exception e) {
            System.err.println("發送系統通知失敗: " + e.getMessage());
            return "發送通知失敗: " + e.getMessage();
        }
    }
    
    /**
     * 獲取在線用戶列表
     * 
     * 用途：取得目前所有在線用戶
     * 路徑：GET /api/websocket/online-users
     * 
     * @return 在線用戶列表
     */
    @GetMapping("/online-users")
    public Set<String> getOnlineUsers() {
        return webSocketHandler.getOnlineUsers();
    }
    
    /**
     * 獲取在線用戶數量
     * 
     * 用途：取得目前在線用戶數量
     * 路徑：GET /api/websocket/online-count
     * 
     * @return 在線用戶數量
     */
    @GetMapping("/online-count")
    public int getOnlineUserCount() {
        return webSocketHandler.getOnlineUserCount();
    }
}

/*
WebSocket REST API 使用說明：

1. 系統通知 API：
   POST http://localhost:8082/api/websocket/notify
   Content-Type: application/json
   Body: "系統維護通知：將在今晚10點進行系統更新"

2. 獲取在線用戶列表 API：
   GET http://localhost:8082/api/websocket/online-users

3. 獲取在線用戶數量 API：
   GET http://localhost:8082/api/websocket/online-count

API 說明：
- 這些 API 提供 HTTP 接口供系統集成使用
- 實際的 WebSocket 通訊（私人訊息、用戶狀態等）通過原生 WebSocket 協議處理
- WebSocket 端點：ws://localhost:8082/websocket
- 詳細的 WebSocket 協議請參考 SimpleWebSocketHandler 中的說明

這個簡化版本專注於提供必要的 REST API，將複雜的即時通訊邏輯交給 SimpleWebSocketHandler 處理！
*/