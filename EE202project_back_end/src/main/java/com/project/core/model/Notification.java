package com.project.core.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_id", nullable=false) //user_id 欄位加上 NOT NULL 限制
    private Integer userId;  //要通知給誰 ex:採購人員

    @Column(nullable = false)//通知類型 ex:庫存不足
    private String type;

    @Column(nullable = false) //庫存不足提醒, 小標題 for 鈴鐺的
    private String title;

    @Column(nullable = false)//鐵：現有 7 < 安全庫存 50
    private String message; 

    private String link; //跳轉到哪個頁面

    @Column(name = "is_read", nullable=false)  //鈴鐺已讀跟未讀
    private boolean read = false;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss") //修改顯示格式
    @Column(name = "created_at") //通知建立時間，預設為系統當下時間
    private LocalDateTime createdAt = LocalDateTime.now();
}
    