package com.project.employeeuser.service;

import com.project.employeeuser.dao.EmployeeUserDAO;
import com.project.employeeuser.model.EmployeeUser;
import com.project.employeeuser.model.EmployeeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeUserService {

    @Autowired
    private EmployeeUserDAO employeeUserDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<EmployeeUser> getAllEmployeeUsers() {
        return employeeUserDAO.findAll();
    }

    public Optional<EmployeeUser> getEmployeeUserById(Long id) {
        return employeeUserDAO.findById(id);
    }

    @Transactional
    public EmployeeUser createEmployeeUser(EmployeeUser employeeUser) {
        // Encode password before saving
        employeeUser.setPasswordHash(passwordEncoder.encode(employeeUser.getPasswordHash()));
        employeeUser.setIsActive(true); // Default to active
        employeeUser.setCreatedAt(LocalDateTime.now());
        employeeUser.setUpdatedAt(LocalDateTime.now());
        return employeeUserDAO.save(employeeUser);
    }

    @Transactional
    public EmployeeUser updateEmployeeUser(Long id, EmployeeUser employeeUserDetails) {
        EmployeeUser employeeUser = employeeUserDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeUser not found with id " + id));

        employeeUser.setEmployeeNumber(employeeUserDetails.getEmployeeNumber());
        employeeUser.setFirstName(employeeUserDetails.getFirstName());
        employeeUser.setLastName(employeeUserDetails.getLastName());
        employeeUser.setUsername(employeeUserDetails.getUsername());
        employeeUser.setEmployeeType(employeeUserDetails.getEmployeeType());
        employeeUser.setEmail(employeeUserDetails.getEmail());
        employeeUser.setPhone(employeeUserDetails.getPhone());
        employeeUser.setBirthDate(employeeUserDetails.getBirthDate());
        employeeUser.setHireDate(employeeUserDetails.getHireDate());
        employeeUser.setTerminationDate(employeeUserDetails.getTerminationDate());
        employeeUser.setPhotoPath(employeeUserDetails.getPhotoPath());
        employeeUser.setEmployeeDepartmentId(employeeUserDetails.getEmployeeDepartmentId());
        employeeUser.setEmployeePositionId(employeeUserDetails.getEmployeePositionId());
        employeeUser.setManagerEmployeeUserId(employeeUserDetails.getManagerEmployeeUserId());
        employeeUser.setIsActive(employeeUserDetails.getIsActive());
        employeeUser.setUpdatedAt(LocalDateTime.now());

        // Only update password if a new one is provided
        if (employeeUserDetails.getPasswordHash() != null && !employeeUserDetails.getPasswordHash().isEmpty()) {
            employeeUser.setPasswordHash(passwordEncoder.encode(employeeUserDetails.getPasswordHash()));
        }

        return employeeUserDAO.save(employeeUser);
    }

    @Transactional
    public void deleteEmployeeUser(Long id) {
        EmployeeUser employeeUser = employeeUserDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeUser not found with id " + id));
        employeeUserDAO.delete(employeeUser);
    }

    public boolean existsByUsername(String username) {
        return employeeUserDAO.existsByUsername(username);
    }

    public boolean existsByEmployeeNumber(String employeeNumber) {
        return employeeUserDAO.existsByEmployeeNumber(employeeNumber);
    }

    public boolean existsByEmail(String email) {
        return employeeUserDAO.existsByEmail(email);
    }

    // 根據 username 查找員工
    public EmployeeUser findByUsername(String username) {
        return employeeUserDAO.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Employee not found with username: " + username));
    }

    // 獲取用戶角色（暫時返回空列表，後續可以擴展）
    public List<String> getUserRoles(String username) {
        // 如果你有角色系統，可以在這裡實作
        // 目前先返回空列表
        return List.of();
    }

    /**
     * 更新用戶個人資料（僅限安全可編輯欄位）
     * 只更新 email, phone, birthDate 欄位
     */
    @Transactional
    public EmployeeUser updateUserProfile(Long id, String email, String phone, java.time.LocalDate birthDate) {
        EmployeeUser employeeUser = employeeUserDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeUser not found with id " + id));

        // 只更新允許的個人資料欄位
        if (email != null) {
            employeeUser.setEmail(email);
        }
        if (phone != null) {
            employeeUser.setPhone(phone);
        }
        if (birthDate != null) {
            employeeUser.setBirthDate(birthDate);
        }
        
        employeeUser.setUpdatedAt(LocalDateTime.now());
        return employeeUserDAO.save(employeeUser);
    }

    /**
     * 修改用戶密碼
     * 驗證舊密碼並更新為新密碼
     */
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        EmployeeUser employeeUser = employeeUserDAO.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Employee not found with username: " + username));

        // 驗證舊密碼
        if (!passwordEncoder.matches(oldPassword, employeeUser.getPasswordHash())) {
            throw new RuntimeException("舊密碼不正確");
        }

        // 更新為新密碼
        employeeUser.setPasswordHash(passwordEncoder.encode(newPassword));
        employeeUser.setUpdatedAt(LocalDateTime.now());
        employeeUserDAO.save(employeeUser);
    }

    /**
     * 重設密碼功能
     * 透過員工編號、電子信箱、入職日期驗證身份並重設密碼
     */
    @Transactional
    public void resetPassword(String employeeNumber, String email, LocalDate hireDate, String newPassword) {
        // 查找符合所有三個條件的員工
        EmployeeUser employeeUser = employeeUserDAO.findByEmployeeNumberAndEmailAndHireDate(employeeNumber, email, hireDate)
                .orElseThrow(() -> new RuntimeException("找不到符合條件的員工資料，請確認輸入的資訊是否正確"));

        // 檢查員工是否為啟用狀態
        if (!employeeUser.getIsActive()) {
            throw new RuntimeException("該員工帳戶已被停用，無法重設密碼");
        }

        // 更新密碼並加密儲存
        employeeUser.setPasswordHash(passwordEncoder.encode(newPassword));
        employeeUser.setUpdatedAt(LocalDateTime.now());
        employeeUserDAO.save(employeeUser);
    }
}
