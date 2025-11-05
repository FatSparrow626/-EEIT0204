package com.project.core.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.project.core.dao.EmployeeRepository;
import com.project.core.dao.NotificationRepository;
import com.project.core.dao.UserRoleRepository;
import com.project.core.model.Employee;
import com.project.core.model.Notification;
import com.project.depot.dao.MaterialRepository;
import com.project.depot.model.Material;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryAlertService {

    private final MaterialRepository materialRepo;
    private final EmployeeRepository employeeRepo;
    private final UserRoleRepository userRoleRepo;
    private final NotificationRepository notificationRepo;

   /** 只抓角色為 PURCHASE_MANAGER 的並且是啟用中的帳號 */
    private List<Employee> purchaseEmployees() {
        //找出哪些員工的角色是 PURCHASE_MANAGER。
        List<Integer> ids = userRoleRepo.findEmployeeIdsByRoleNames(List.of("PURCHASE_MANAGER" , "ADMIN"));
        if (ids == null || ids.isEmpty()) return List.of();//回傳 空的 List
        // 直接撈 Employee；如需過濾 isActive，可在這裡 .stream() 過濾，或寫成自訂查詢

        List<Employee> emps = employeeRepo.findAllById(ids);
        // 底下是只抓啟用的
        // emps = emps.stream().filter(Employee::getIsActive).toList();
        return emps;
    } 

//掃描低庫存並發通知（寫 DB)
 public void checkAndNotifyLowStock() {
    List<Material> lows = materialRepo.findByStockCurrentLessThanSafeStock();
        if(lows.isEmpty()) return; //如果沒有低於庫存則直接結束

    List<Employee> buyers = purchaseEmployees();//取出採購負責人
        if(buyers.isEmpty()) return;

    for(Material m : lows){
        //通知訊息內容
        String msg = m.getMaterialName() + ": 剩餘 " + m.getStockCurrent().intValue() +" 個"+" < 安全庫存"+m.getSafetyStock().intValue();
        //當點選訊息內容跳轉
        String link = "/materials";
        //對每一個採購人員發通知
        for(Employee u : buyers){ 

        if (notificationRepo.existsByUserIdAndTypeAndMessageAndReadFalse(
            u.getEmployeeId(), "LOW_STOCK", msg)) {
            continue; // 已有同類型、同訊息的未讀 → 跳過
            }
            try{
                Notification n = new Notification();
                n.setUserId(u.getEmployeeId()); // Integer
                n.setType("LOW_STOCK");
                n.setTitle("庫存不足提醒");
                n.setMessage(msg);
                n.setLink(link);
                notificationRepo.save(n);
            }catch(DataIntegrityViolationException ignore){ //同樣的未讀通知不會一直插入
            }
        }
    }
 }
}