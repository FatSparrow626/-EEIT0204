package com.project.core.controller;

import com.project.core.dao.EmployeeRepository;
import com.project.core.dao.NotificationRepository;
import com.project.core.model.Notification;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    // 從目前登入者取 employeeId（以 username 反查）
    private Integer currentUserId(Authentication auth) {
        String username = auth.getName();
        return employeeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username))
                .getEmployeeId();
    }

    /** 未讀數量（紅點用） */
    @GetMapping("/unread-count")
    public long unreadCount(Authentication auth) {
        return notificationRepository.countByUserIdAndReadFalse(currentUserId(auth));
    }

    /** 通知列表（預設只回未讀；?status=all 回全部） */
    @GetMapping
    public List<Notification> list(@RequestParam(defaultValue = "unread") String status,
                                   Authentication auth) {
        Integer uid = currentUserId(auth);
        // 根據 status 參數決定查詢條件
        return switch (status.toLowerCase()) {
            case "read"   -> notificationRepository.findByUserIdAndReadTrueOrderByCreatedAtDesc(uid);
            case "unread" -> notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(uid);
            default       -> notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(uid);
        };
    }

    /** 單筆標記已讀 */
    @PostMapping("/{id}/read")
    public void markRead(@PathVariable Integer id, Authentication auth) {
        Integer uid = currentUserId(auth);
        Notification n = notificationRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        // 只允許本人操作（避免越權）
        if (!n.getUserId().equals(uid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }

    /** 全部標記已讀 */
    @PostMapping("/read-all")
    public int markAllRead(Authentication auth) {
        Integer uid = currentUserId(auth);
        return notificationRepository.markAllRead(uid);
    }
    //單筆刪除
    @DeleteMapping("/{id}")
    public void deleteOne(@PathVariable Integer id, Authentication auth) {
        Integer uid = currentUserId(auth);
        int affected = notificationRepository.deleteOne(id, uid);
        if (affected == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found");
        }
    }
    //全部刪除
    @DeleteMapping("/read")
    public int deleteAll(Authentication auth) {
        Integer uid = currentUserId(auth);
        return notificationRepository.deleteByUserIdAndReadTrue(uid);
    }
}
