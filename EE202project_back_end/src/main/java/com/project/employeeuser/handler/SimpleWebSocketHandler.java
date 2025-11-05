package com.project.employeeuser.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 原生 WebSocket 處理器
 * 
 * 用途：處理原生 WebSocket 連接，提供即時通訊功能
 * 功能：
 * 1. 用戶連接管理和追蹤
 * 2. JSON 格式訊息處理
 * 3. 系統廣播通知
 * 4. 私人訊息路由
 * 5. 用戶狀態同步
 */
@Component
public class SimpleWebSocketHandler implements WebSocketHandler {
    // 創建 JSON 處理器-用來把訊息物件 ⇆ JSON 格式轉換
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 存儲所有活躍的 WebSocket 會話(WebSocketSession 就是 Spring WebSocket 幫你維護的連線)
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    // 存儲會話ID到用戶ID的映射
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();

    // 存儲在線用戶列表
    private final Set<String> onlineUsers = new ConcurrentSkipListSet<>();

    /**
     * WebSocket 連接建立"後"調用 "沒用到"
     * 當客戶端成功建立 WebSocket 連接時調用
     * 是 WebSocketHandler 介面的必實作方法
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();// 取得會話 ID 並記錄
        System.out.println("新的 WebSocket 連接建立: " + sessionId);

        // 發送歡迎訊息
        CustomWebSocketMessage welcomeMessage = new CustomWebSocketMessage(
                "system", // source: "system"：訊息來源為系統
                "connection", // type: "connection"：訊息類型為連接通知
                "WebSocket 連接成功，請發送 identify 訊息進行身份驗證", // data：提示用戶進行身份驗證
                System.currentTimeMillis());

        sendMessage(session, welcomeMessage);// 發送歡迎訊息給新連接的客戶端 - 調用私有方法將訊息序列化並發送
    }

    /**
     * 處理接收到的訊息
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = (String) message.getPayload();// 取得訊息內容（JSON 字串）
            JsonNode messageNode = objectMapper.readTree(payload);// 將 JSON 字串解析為 JsonNode 物件

            String action = messageNode.path("action").asText();
            System.out.println("收到訊息 - Session: " + session.getId() + ", Action: " + action);

            switch (action) {
                case "identify":// 訊息路由機制 根據 action 欄位將訊息分發到不同的處理方法- identify：用戶身份識別
                    handleIdentify(session, messageNode);
                    break;

                case "subscribe":// 沒用到
                    handleSubscribe(session, messageNode);
                    break;

                case "send":// 處理廣播訊息發送
                    handleSend(session, messageNode);
                    break;

                case "private":// 處理私人訊息
                    handlePrivateMessage(session, messageNode);
                    break;

                case "get_online_users":// 處理取得線上用戶列表請求
                    handleGetOnlineUsers(session);
                    break;

                case "ping":// 沒用到
                    handlePing(session);
                    break;

                default:// 處理未知的動作類型- 返回錯誤訊息給客戶端
                    CustomWebSocketMessage errorMessage = new CustomWebSocketMessage(
                            "system",
                            "error",
                            "未知的操作類型: " + action,
                            System.currentTimeMillis());
                    sendMessage(session, errorMessage);
            }

        } catch (Exception e) {
            System.err.println("處理訊息時發生錯誤: " + e.getMessage());
            e.printStackTrace();

            CustomWebSocketMessage errorMessage = new CustomWebSocketMessage(
                    "system",
                    "error",
                    "訊息格式錯誤: " + e.getMessage(),
                    System.currentTimeMillis());
            sendMessage(session, errorMessage);
        }
    }

    /**
     * 處理用戶身份識別
     */
    private void handleIdentify(WebSocketSession session, JsonNode messageNode) throws IOException {
        String userId = messageNode.path("userId").asText();

        if (userId != null && !userId.isEmpty()) {
            // 如果用戶已經在線，斷開舊連接
            if (userSessions.containsKey(userId)) {
                WebSocketSession oldSession = userSessions.get(userId);
                if (oldSession.isOpen()) {
                    oldSession.close(CloseStatus.NORMAL.withReason("用戶在其他地方登入"));
                }
            }

            // 註冊新的用戶會話 "沒用到"
            userSessions.put(userId, session);
            sessionToUser.put(session.getId(), userId);
            onlineUsers.add(userId);

            System.out.println("用戶 " + userId + " 已識別，當前在線用戶數: " + onlineUsers.size());

            // 發送識別成功回應
            CustomWebSocketMessage response = new CustomWebSocketMessage(
                    "system",
                    "identified",
                    "身份驗證成功，用戶ID: " + userId,
                    System.currentTimeMillis());
            sendMessage(session, response);

            // 廣播用戶上線通知 "沒用到"
            UserStatusMessage statusMessage = new UserStatusMessage(userId, "online");
            broadcastToAll("user-status", statusMessage);

            // 發送當前在線用戶列表
            OnlineUsersMessage onlineUsersMessage = new OnlineUsersMessage(onlineUsers);
            sendMessage(session, new CustomWebSocketMessage("system", "online-users", onlineUsersMessage,
                    System.currentTimeMillis()));

        } else {
            CustomWebSocketMessage errorMessage = new CustomWebSocketMessage(
                    "system",
                    "error",
                    "無效的用戶ID",
                    System.currentTimeMillis());
            sendMessage(session, errorMessage);
        }
    }

    /**
     * 處理訂閱請求 "沒用到"
     */
    private void handleSubscribe(WebSocketSession session, JsonNode messageNode) throws IOException {
        String topic = messageNode.path("topic").asText();

        CustomWebSocketMessage response = new CustomWebSocketMessage(
                "system",
                "subscribed",
                "已訂閱主題: " + topic,
                System.currentTimeMillis());
        sendMessage(session, response);
    }

    /**
     * 處理發送訊息
     */
    private void handleSend(WebSocketSession session, JsonNode messageNode) throws IOException {
        String userId = sessionToUser.get(session.getId());
        if (userId == null) {
            CustomWebSocketMessage errorMessage = new CustomWebSocketMessage(
                    "system",
                    "error",
                    "請先進行身份驗證",
                    System.currentTimeMillis());
            sendMessage(session, errorMessage);
            return;
        }
        // 提取訊息主題和內容
        String topic = messageNode.path("topic").asText();
        String messageContent = messageNode.path("message").asText();

        if ("/topic/notifications".equals(topic)) {
            // 廣播系統通知
            NotificationMessage notification = new NotificationMessage(userId, messageContent);
            broadcastToAll("notification", notification);

            System.out.println("用戶 " + userId + " 發送廣播通知: " + messageContent);
        }
    }

    /**
     * 處理私人訊息
     */
    private void handlePrivateMessage(WebSocketSession session, JsonNode messageNode) throws IOException {
        String fromUserId = sessionToUser.get(session.getId());// 私人訊息處理的開始- 取得發送者的用戶 ID
        if (fromUserId == null) {
            CustomWebSocketMessage errorMessage = new CustomWebSocketMessage(
                    "system",
                    "error",
                    "請先進行身份驗證",
                    System.currentTimeMillis());
            sendMessage(session, errorMessage);
            return;
        }
        // 提取目標用戶 ID 和訊息內容
        String toUserId = messageNode.path("toUserId").asText();
        String messageContent = messageNode.path("message").asText();
        // 檢查目標用戶是否在線
        if (userSessions.containsKey(toUserId)) {
            PrivateMessage privateMessage = new PrivateMessage(fromUserId, messageContent);
            WebSocketSession targetSession = userSessions.get(toUserId);
            // 發送私人訊息
            CustomWebSocketMessage message = new CustomWebSocketMessage(
                    "private",
                    "message",
                    privateMessage,
                    System.currentTimeMillis());
            sendMessage(targetSession, message);

            // 發送確認給發送者
            CustomWebSocketMessage confirmation = new CustomWebSocketMessage(
                    "system",
                    "sent",
                    "私人訊息已發送給 " + toUserId,
                    System.currentTimeMillis());
            sendMessage(session, confirmation);

            System.out.println("私人訊息 " + fromUserId + " -> " + toUserId + ": " + messageContent);
        } else {
            CustomWebSocketMessage errorMessage = new CustomWebSocketMessage(
                    "system",
                    "error",
                    "用戶 " + toUserId + " 不在線",
                    System.currentTimeMillis());
            sendMessage(session, errorMessage);
        }
    }

    /**
     * 處理 Ping 請求"沒用到"
     */
    private void handlePing(WebSocketSession session) throws IOException {
        CustomWebSocketMessage pongMessage = new CustomWebSocketMessage(
                "system",
                "pong",
                "pong",
                System.currentTimeMillis());
        sendMessage(session, pongMessage);
    }

    /**
     * 處理獲取在線用戶列表請求
     */
    private void handleGetOnlineUsers(WebSocketSession session) throws IOException {
        String userId = sessionToUser.get(session.getId());

        if (userId == null) {
            CustomWebSocketMessage errorMessage = new CustomWebSocketMessage(
                    "system",
                    "error",
                    "請先進行身份驗證",
                    System.currentTimeMillis());
            sendMessage(session, errorMessage);
            return;
        }

        // 發送在線用戶列表
        OnlineUsersMessage onlineUsersMessage = new OnlineUsersMessage(onlineUsers);
        CustomWebSocketMessage response = new CustomWebSocketMessage(
                "system",
                "online-users",
                onlineUsersMessage,
                System.currentTimeMillis());
        sendMessage(session, response);

        System.out.println("用戶 " + userId + " 請求在線用戶列表，當前在線用戶數: " + onlineUsers.size());
    }

    /**
     * 處理傳輸錯誤
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket 傳輸錯誤 - Session: " + session.getId() + ", Error: " + exception.getMessage());
        exception.printStackTrace();
    }

    /**
     * WebSocket 連接關閉後調用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        String userId = sessionToUser.get(sessionId);

        if (userId != null) {
            // 清理用戶會話
            userSessions.remove(userId);
            sessionToUser.remove(sessionId);
            onlineUsers.remove(userId);

            System.out.println("用戶 " + userId + " 已斷線，當前在線用戶數: " + onlineUsers.size());

            // 廣播用戶下線通知
            UserStatusMessage statusMessage = new UserStatusMessage(userId, "offline");
            broadcastToAll("user-status", statusMessage);
        }

        System.out.println("WebSocket 連接已關閉 - Session: " + sessionId + ", Status: " + closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 發送訊息給特定會話
     */
    private void sendMessage(WebSocketSession session, CustomWebSocketMessage message) throws IOException {
        if (session.isOpen()) {// 檢查連接是否仍然開啟
            String jsonMessage = objectMapper.writeValueAsString(message);// 將訊息物件序列化為 JSON 字串
            session.sendMessage(new TextMessage(jsonMessage));// 通過 WebSocket 發送文字訊息
        }
    }

    /**
     * 廣播訊息給所有在線用戶
     */
    private void broadcastToAll(String type, Object data) {
        CustomWebSocketMessage message = new CustomWebSocketMessage("broadcast", type, data,
                System.currentTimeMillis());

        userSessions.values().forEach(session -> {
            try {
                sendMessage(session, message);
            } catch (IOException e) {
                System.err.println("廣播訊息失敗: " + e.getMessage());
            }
        });
    }

    /**
     * 獲取在線用戶列表
     */
    public Set<String> getOnlineUsers() {
        return new ConcurrentSkipListSet<>(onlineUsers);
    }

    /**
     * 獲取在線用戶數量 "沒用到"
     */
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }

    /**
     * 發送系統廣播通知（供外部調用）
     */
    public void sendSystemNotification(String message) {
        NotificationMessage notification = new NotificationMessage("system", message);
        broadcastToAll("notification", notification);
    }
}

/**
 * 自定義 WebSocket 訊息包裝類
 */
class CustomWebSocketMessage {
    private String source; // 訊息來源
    private String type; // 訊息類型
    private Object data; // 訊息內容
    private long timestamp; // 時間戳

    public CustomWebSocketMessage() {
    }

    public CustomWebSocketMessage(String source, String type, Object data, long timestamp) {
        this.source = source;
        this.type = type;
        this.data = data;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

/**
 * 通知訊息
 */
class NotificationMessage {
    private String fromUserId;
    private String message;
    private long timestamp;

    public NotificationMessage(String fromUserId, String message) {
        this.fromUserId = fromUserId;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

/**
 * 私人訊息
 */
class PrivateMessage {
    private String fromUserId;
    private String message;
    private long timestamp;

    public PrivateMessage(String fromUserId, String message) {
        this.fromUserId = fromUserId;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

/**
 * 用戶狀態訊息
 */
class UserStatusMessage {
    private String userId;
    private String status;
    private long timestamp;

    public UserStatusMessage(String userId, String status) {
        this.userId = userId;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

/**
 * 在線用戶列表訊息
 */
class OnlineUsersMessage {
    private Set<String> users;
    private int count;
    private long timestamp;

    public OnlineUsersMessage(Set<String> users) {
        this.users = users;
        this.count = users.size();
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

/*
 * 1. 連接建立流程
 * 
 * 客戶端連接 → afterConnectionEstablished → 發送歡迎訊息 → 等待身份識別
 * 
 * 詳細步驟：
 * 1. 客戶端建立 WebSocket 連接到 ws://localhost:8082/websocket
 * 2. 服務器調用 afterConnectionEstablished 方法
 * 3. 生成唯一的會話 ID
 * 4. 發送歡迎訊息，提示進行身份驗證
 * 5. 等待客戶端發送 identify 訊息
 * 
 * 2. 身份識別流程
 * 
 * 收到 identify 訊息 → 檢查重複登入 → 註冊會話 → 廣播上線通知 → 發送用戶列表
 * 
 * 3. 連接關閉流程
 * 
 * 連接斷開 → afterConnectionClosed → 清理會話 → 廣播下線通知
 * 
 * 訊息接收和廣播機制
 * 
 * 1. 訊息接收流程
 * 
 * WebSocket 訊息 → JSON 解析 → 提取 action → 路由到處理方法 → 執行業務邏輯
 * 
 * 2. 廣播機制
 * 
 * - 系統廣播：向所有在線用戶發送
 * - 狀態廣播：用戶上線/下線通知
 * - 通知廣播：系統通知或用戶通知
 * 
 * 3. 私人訊息機制
 * 
 * 檢查發送者身份 → 驗證目標用戶在線 → 發送訊息 → 發送確認
 * 
 * 線上用戶管理方式
 * 
 * 1. 數據結構設計
 * 
 * - userSessions：用戶 ID → WebSocket 會話
 * - sessionToUser：會話 ID → 用戶 ID（反向查找）
 * - onlineUsers：線上用戶 ID 集合
 * 
 * 2. 用戶狀態同步
 * 
 * - 上線：廣播 user-status 訊息，status: "online"
 * - 下線：廣播 user-status 訊息，status: "offline"
 * - 查詢：提供線上用戶列表 API
 * 
 * 3. 重複登入處理
 * 
 * - 檢測到重複登入時自動斷開舊連接
 * - 確保一個用戶只有一個活躍會話
 * 
 * 自定義 JSON 通訊協議格式
 * 
 * 1. 客戶端請求格式
 * 
 * {
 * "action": "identify|subscribe|send|private|get_online_users|ping",
 * "userId": "user001",
 * "toUserId": "user002", // 私人訊息使用
 * "topic": "/topic/notifications", // 廣播訊息使用
 * "message": "content"
 * }
 * 
 * 2. 服務端回應格式
 * 
 * {
 * "source": "system|broadcast|private",
 * "type": "connection|identified|notification|message|error|user-status",
 * "data": { ... },
 * "timestamp": 1234567890
 * }
 * 
 * 
 * 
 * 
 */