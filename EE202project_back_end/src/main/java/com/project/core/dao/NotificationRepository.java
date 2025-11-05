package com.project.core.dao;

import com.project.core.model.Notification;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Integer uid); // 未讀列表
    List<Notification> findByUserIdAndReadTrueOrderByCreatedAtDesc(Integer uid); //已讀列表
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer uid);              // 全部
    long countByUserIdAndReadFalse(Integer uid);                                   // 未讀數
    // 判斷是否已存在相同「未讀」通知（用來避免重複插入） //
    boolean existsByUserIdAndTypeAndMessageAndReadFalse(Integer userId, String type, String message);

//操作
//單筆刪除
@Modifying
@Transactional
@Query("DELETE FROM Notification n WHERE n.id = :id AND n.userId = :uid")
    int deleteOne(@Param("id") Integer id, @Param("uid") Integer uid);

//全部刪除(已讀)
@Modifying
@Transactional
int deleteByUserIdAndReadTrue(Integer uid);

//全部已讀
@Modifying
@Transactional
@Query("UPDATE Notification n SET n.read = true WHERE n.userId = :uid AND n.read = false")
    int markAllRead(@Param("uid") Integer uid);
}
