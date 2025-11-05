package com.project.employeeuser.controller;

import com.project.employeeuser.service.EmployeeUserService;
import com.project.employeeuser.model.EmployeeUser;
import com.project.employeeuser.dto.ProfileUpdateRequest;
import com.project.employeeuser.dto.PasswordChangeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "個人資料管理", description = "處理員工個人資料的相關操作")
public class ProfileController {
    //創建日誌記錄器，用於記錄操作日誌和錯誤
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private EmployeeUserService employeeUserService;

    // 檔案上傳路徑配置 (可從 application.properties 讀取)
    @Value("${file.upload.avatar.path:uploads/avatars}")
    private String avatarUploadPath;

    @Value("${file.upload.avatar.max-size:2097152}") // 2MB = 2 * 1024 * 1024
    private long maxFileSize;

    /**
     * 獲取當前登入用戶的個人資料
     * 只需要用戶已認證即可訪問
     */
    @Operation(summary = "獲取當前用戶個人資料", description = "獲取當前登入用戶的個人資料詳細資訊")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            EmployeeUser employee = employeeUserService.findByUsername(username);
            return ResponseEntity.ok(employee);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("獲取個人資料失敗: " + e.getMessage());
        }
    }

    /**
     * 獲取當前用戶的角色權限
     */
    @Operation(summary = "獲取當前用戶角色", description = "獲取當前登入用戶的角色權限列表")
    @GetMapping("/me/roles")
    public ResponseEntity<?> getCurrentUserRoles() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 暫時返回空列表，可以後續擴展
            return ResponseEntity.ok(employeeUserService.getUserRoles(username));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("獲取角色資料失敗: " + e.getMessage());
        }
    }

    /**
     * 更新當前登入用戶的個人資料
     * 只允許更新部分可編輯欄位（email, phone, birthDate）
     */
    @Operation(summary = "更新當前用戶個人資料", description = "更新當前登入用戶的個人資料資訊")
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUserProfile(
            @Parameter(description = "個人資料更新請求物件", required = true) @RequestBody ProfileUpdateRequest profileUpdateRequest) {
        try {
            logger.info("開始更新個人資料，接收到的資料: {}", profileUpdateRequest);
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            logger.info("當前用戶名: {}", username);

            EmployeeUser currentEmployee = employeeUserService.findByUsername(username);
            if (currentEmployee == null) {
                logger.error("找不到用戶: {}", username);
                return ResponseEntity.status(404).body("找不到用戶資料");
            }
            logger.info("找到用戶，ID: {}", currentEmployee.getEmployeeUserId());

            // 驗證電子郵件是否已被其他用戶使用
            String email = null;
            if (profileUpdateRequest.getEmail() != null && !profileUpdateRequest.getEmail().trim().isEmpty()) {
                if (employeeUserService.existsByEmail(profileUpdateRequest.getEmail()) && 
                    !profileUpdateRequest.getEmail().equals(currentEmployee.getEmail())) {
                    return ResponseEntity.status(400).body("此電子郵件已被使用");
                }
                email = profileUpdateRequest.getEmail().trim();
            }
            
            // 處理電話
            String phone = null;
            if (profileUpdateRequest.getPhone() != null && !profileUpdateRequest.getPhone().trim().isEmpty()) {
                phone = profileUpdateRequest.getPhone().trim();
            }
            
            // 處理生日日期轉換
            LocalDate birthDate = null;
            if (profileUpdateRequest.getBirthDate() != null && !profileUpdateRequest.getBirthDate().trim().isEmpty()) {
                try {
                    birthDate = LocalDate.parse(profileUpdateRequest.getBirthDate());
                } catch (DateTimeParseException e) {
                    return ResponseEntity.status(400).body("生日日期格式錯誤，請使用 YYYY-MM-DD 格式");
                }
            }

            // 使用專用的個人資料更新方法
            logger.info("準備更新: email={}, phone={}, birthDate={}", email, phone, birthDate);
            EmployeeUser updatedEmployee = employeeUserService.updateUserProfile(
                currentEmployee.getEmployeeUserId(), email, phone, birthDate);
            
            logger.info("個人資料更新成功，用戶ID: {}", updatedEmployee.getEmployeeUserId());
            return ResponseEntity.ok(updatedEmployee);

        } catch (Exception e) {
            logger.error("更新個人資料時發生錯誤: ", e);
            return ResponseEntity.status(500)
                    .body("更新個人資料失敗: " + e.getMessage());
        }
    }

    /**
     * 獲取當前登入用戶的主管資訊
     * 只需要用戶已認證即可訪問
     */
    @Operation(summary = "獲取當前用戶主管資訊", description = "獲取當前登入用戶的直屬主管詳細資訊")
    @GetMapping("/me/manager")
    public ResponseEntity<?> getCurrentUserManager() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            EmployeeUser currentEmployee = employeeUserService.findByUsername(username);
            if (currentEmployee == null) {
                return ResponseEntity.status(404).body("找不到用戶資料");
            }

            // 如果沒有主管，返回null
            if (currentEmployee.getManagerEmployeeUserId() == null) {
                return ResponseEntity.ok().body(null);
            }

            // 獲取主管資訊
            Optional<EmployeeUser> manager = employeeUserService.getEmployeeUserById(currentEmployee.getManagerEmployeeUserId());
            if (manager.isPresent()) {
                return ResponseEntity.ok(manager.get());
            } else {
                return ResponseEntity.ok().body(null);
            }

        } catch (Exception e) {
            logger.error("獲取主管資訊時發生錯誤: ", e);
            return ResponseEntity.status(500)
                    .body("獲取主管資訊失敗: " + e.getMessage());
        }
    }

    /**
     * 修改當前登入用戶的密碼
     * 需要提供舊密碼進行驗證
     */
    @Operation(summary = "修改用戶密碼", description = "修改當前登入用戶的登入密碼")
    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(
            @Parameter(description = "密碼修改請求物件", required = true) @RequestBody PasswordChangeRequest passwordChangeRequest) {
        try {
            logger.info("開始密碼修改請求");
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            logger.info("當前用戶: {}", username);

            // 驗證請求資料完整性
            if (passwordChangeRequest.getOldPassword() == null || passwordChangeRequest.getOldPassword().trim().isEmpty()) {
                return ResponseEntity.status(400).body("請輸入舊密碼");
            }
            
            if (passwordChangeRequest.getNewPassword() == null || passwordChangeRequest.getNewPassword().trim().isEmpty()) {
                return ResponseEntity.status(400).body("請輸入新密碼");
            }
            
            if (passwordChangeRequest.getConfirmPassword() == null || passwordChangeRequest.getConfirmPassword().trim().isEmpty()) {
                return ResponseEntity.status(400).body("請確認新密碼");
            }

            // 檢查新密碼與確認密碼是否一致
            if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmPassword())) {
                return ResponseEntity.status(400).body("兩次輸入的密碼不一致");
            }

            // 密碼強度驗證（與 UserView.vue 相同的規則）
            String newPassword = passwordChangeRequest.getNewPassword();
            
            // 1. 密碼長度至少6位
            if (newPassword.length() < 6) {
                return ResponseEntity.status(400).body("密碼長度至少6位");
            }
            
            // 2. 密碼必須包含字母和數字
            boolean hasLetter = newPassword.matches(".*[A-Za-z].*");
            boolean hasNumber = newPassword.matches(".*\\d.*");
            if (!hasLetter || !hasNumber) {
                return ResponseEntity.status(400).body("密碼必須包含字母和數字");
            }
            
            // 3. 禁止使用常見弱密碼
            String[] weakPasswords = {
                "123456", "111111", "222222", "333333", "444444", "555555",
                "666666", "777777", "888888", "999999", "000000",
                "password", "admin123", "abc123", "qwerty", "123abc",
                "admin", "user123", "pass123", "1234567", "abcdefg"
            };
            
            for (String weakPassword : weakPasswords) {
                if (newPassword.toLowerCase().equals(weakPassword)) {
                    return ResponseEntity.status(400).body("請避免使用常見弱密碼");
                }
            }

            // 調用服務方法修改密碼
            employeeUserService.changePassword(username, passwordChangeRequest.getOldPassword(), newPassword);
            
            logger.info("密碼修改成功，用戶: {}", username);
            return ResponseEntity.ok().body("密碼修改成功");

        } catch (Exception e) {
            logger.error("密碼修改時發生錯誤: ", e);
            if (e.getMessage().contains("舊密碼不正確")) {
                return ResponseEntity.status(400).body("舊密碼不正確");
            }
            return ResponseEntity.status(500).body("密碼修改失敗: " + e.getMessage());
        }
    }

    /**
     * 上傳當前登入用戶的頭像
     * 處理檔案驗證、儲存和資料庫更新
     */
    @Operation(summary = "上傳用戶頭像", description = "上傳當前登入用戶的個人頭像圖片")
    @PostMapping("/me/avatar")
    public ResponseEntity<?> uploadAvatar(
            @Parameter(description = "頭像圖片檔案", required = true) @RequestParam("file") MultipartFile file) {
        try {
            logger.info("開始頭像上傳請求，檔案名稱: {}, 大小: {} bytes", 
                       file.getOriginalFilename(), file.getSize());

            // 獲取當前登入用戶
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            logger.info("當前用戶: {}", username);

            EmployeeUser currentEmployee = employeeUserService.findByUsername(username);
            if (currentEmployee == null) {
                return ResponseEntity.status(404).body("找不到用戶資料");
            }

            // 檔案驗證
            String validationError = validateAvatarFile(file);
            if (validationError != null) {
                return ResponseEntity.status(400).body(validationError);
            }

            // 儲存檔案並獲取檔案路徑
            String savedFilePath = saveAvatarFile(file, currentEmployee.getEmployeeUserId());
            logger.info("檔案儲存成功，路徑: {}", savedFilePath);

            // 更新資料庫中的照片路徑
            // 直接更新當前員工的 photoPath 並保存
            currentEmployee.setPhotoPath(savedFilePath);
            
            // 使用現有的 updateUserProfile 方法，但先設置好 photoPath
            EmployeeUser updatedEmployee = employeeUserService.updateUserProfile(
                currentEmployee.getEmployeeUserId(),
                currentEmployee.getEmail(),
                currentEmployee.getPhone(),
                currentEmployee.getBirthDate()
            );

            // 確保回傳的物件包含正確的 photoPath
            updatedEmployee.setPhotoPath(savedFilePath);

            // 建立回傳資料
            Map<String, Object> response = new HashMap<>();
            response.put("message", "頭像上傳成功");
            response.put("photoPath", savedFilePath);
            response.put("employee", updatedEmployee);

            logger.info("頭像上傳完成，用戶: {}, 檔案路徑: {}", username, savedFilePath);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("頭像上傳時發生錯誤: ", e);
            return ResponseEntity.status(500).body("頭像上傳失敗: " + e.getMessage());
        }
    }

    /**
     * 驗證頭像檔案
     */
    private String validateAvatarFile(MultipartFile file) {
        // 檢查檔案是否為空
        if (file.isEmpty()) {
            return "請選擇要上傳的檔案";
        }

        // 檢查檔案大小
        if (file.getSize() > maxFileSize) {
            return "檔案大小不能超過 2MB";
        }

        // 檢查檔案類型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return "無效的檔案名稱";
        }

        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png");
        
        if (!allowedExtensions.contains(fileExtension)) {
            return "只支援 JPG、JPEG、PNG 格式的圖片";
        }

        // 檢查 MIME 類型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return "檔案必須是圖片格式";
        }

        List<String> allowedMimeTypes = Arrays.asList("image/jpeg", "image/jpg", "image/png");
        if (!allowedMimeTypes.contains(contentType)) {
            return "不支援的圖片格式";
        }

        return null; // 驗證通過
    }

    /**
     * 儲存頭像檔案
     */
    private String saveAvatarFile(MultipartFile file, Long employeeUserId) throws IOException {
        // 建立上傳目錄
        Path uploadDir = Paths.get(avatarUploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            logger.info("建立上傳目錄: {}", uploadDir.toAbsolutePath());
        }

        // 產生唯一檔案名稱
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = "avatar_" + employeeUserId + "_" + UUID.randomUUID().toString() + "." + fileExtension;
        
        // 儲存檔案
        Path filePath = uploadDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        logger.info("檔案儲存至: {}", filePath.toAbsolutePath());
        
        // 回傳相對路徑（用於資料庫儲存和前端存取）
        return avatarUploadPath + "/" + uniqueFilename;
    }

    /**
     * 獲取檔案副檔名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}