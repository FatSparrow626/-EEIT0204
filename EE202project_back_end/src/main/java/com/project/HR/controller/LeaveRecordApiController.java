package com.project.HR.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.HR.model.CreateLeaveRecordRequest;
import com.project.HR.model.LeaveAttachmentDto;
import com.project.HR.model.LeaveRecordDto;
import com.project.HR.model.UpdateLeaveRequestWithAttachments;
import com.project.HR.service.FileStorageService;
import com.project.HR.service.LeaveRecordService;
import com.project.core.security.EmployeeUserDetails;
import com.project.employeeuser.model.EmployeeUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor // lombok自動DI
@Tag(name = "請假紀錄管理", description = "處理員工請假紀錄的相關操作")
@RequestMapping("/api/leave")
public class LeaveRecordApiController {
    private final LeaveRecordService leaveRecordService;
    private final FileStorageService fileStorageService;

    // 新增檔案上傳的 API 端點
    @Operation(summary = "上傳單一附件", description = "為指定的請假單上傳一個附件")
    @PostMapping("/{uuid}/attachments")
    public ResponseEntity<LeaveAttachmentDto> uploadAttachment(@PathVariable String uuid,
            @RequestParam("file") MultipartFile file) {
        LeaveAttachmentDto attachmentDto = leaveRecordService.storeAttachment(uuid, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(attachmentDto);
    }

    // 下載檔案的 API
    @Operation(summary = "下載附件", description = "根據請假單UUID和檔案的儲存名稱下載附件")
    @GetMapping("/attachments/{leaveRecordUuid}/{filename:.+}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable String leaveRecordUuid,
            @PathVariable String filename, @RequestParam(required = false) Boolean inline, HttpServletRequest request) {
        Resource resource = fileStorageService.loadAsResource(leaveRecordUuid, filename);

        // 判斷這是什麼類型的檔案
        String contentType = null;
        try {
            // 透過ServletContext去對比出本地副檔名 <-> MIME type，回傳正確的Content-Type字串
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // 不做事，spring 會自動回傳對應HTTP回應
        }

        if (contentType == null) {
            contentType = "application/octet-stream"; // 如果判斷不出來，就當作是通用的二進位檔案
        }

        // 決定要讓瀏覽器「直接打開」還是「下載儲存」
        String dispositionType = (inline != null && inline) ? "inline" : "attachment";

        // 打包成一個完整的 HTTP 回應，交給使用者
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType)) // 設定Content-Type
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        dispositionType + "; filename=\"" + resource.getFilename() + "\"") // 決定圖片預覽 or 下載(且檔名是當初上傳檔名)
                .body(resource);
    }

    @Operation(summary = "刪除附件", description = "根據請假單UUID和檔案的儲存名稱刪除附件")
    @DeleteMapping("/attachments/{leaveRecordUuid}/{filename:.+}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable String leaveRecordUuid, @PathVariable String filename,
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        if (userDetails == null || userDetails.getEmployeeUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<LeaveRecordDto> leaveRecordOptional = leaveRecordService.getLeaveRecordDtoByUuid(leaveRecordUuid);

        if (leaveRecordOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LeaveRecordDto existingRecord = leaveRecordOptional.get();
        Integer recordOwnerId = existingRecord.getEmployeeId();
        Long currentUserId = userDetails.getEmployeeUser().getEmployeeUserId();

        boolean isOwner = recordOwnerId != null && currentUserId.longValue() == recordOwnerId.longValue();
        boolean hasManageAll = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("LEAVE_MANAGE_ALL"));

        if (!isOwner && !hasManageAll) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        leaveRecordService.deleteAttachment(leaveRecordUuid, filename);

        return ResponseEntity.noContent().build();
    }

    // 尚未實現
    @Operation(summary = "獲取當前用戶的剩餘特休時數", description = "計算並返回當前登入使用者的剩餘特休時數")
    @GetMapping("/annual-leave-balance")
    public ResponseEntity<Map<String, Double>> getAnnualLeaveBalance(
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        EmployeeUser currentUser = userDetails.getEmployeeUser();
        double balance = leaveRecordService.getAnnualLeaveBalance(currentUser);
        return ResponseEntity.ok(Collections.singletonMap("balanceHours", balance));
    }

    @Operation(summary = "計算請假時數", description = "根據開始和結束時間，自動計算扣除假日與週末後的實際工作時數")
    @GetMapping("/calculate-hours")
    public ResponseEntity<Map<String, Double>> calculateHours(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            double hours = leaveRecordService.calculateWorkHours(start, end);
            return ResponseEntity.ok(Collections.singletonMap("calculatedHours", hours));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "獲取請假表單所需資料", description = "提供建立請假表單所需的初始資料，例如假別列表")
    @GetMapping("/form-data")
    public ResponseEntity<Map<String, Object>> getLeaveFormData() {
        Map<String, Object> formData = new HashMap<>();
        formData.put("leaveTypes", leaveRecordService.getAllLeaveTypes());
        return ResponseEntity.ok(formData);
    }

    @Operation(summary = "獲取請假紀錄", description = "根據用戶權限、視圖模式和狀態返回個人或部門的請假紀錄")
    @GetMapping("/records")
    public ResponseEntity<Page<LeaveRecordDto>> getLeaveRecords(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            com.project.HR.model.LeaveRecordFilter filter) { // 使用 Filter 物件

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Page.empty());
        }

        Page<LeaveRecordDto> recordsPage = leaveRecordService.getLeaveRecords(userDetails, pageable, filter);
        return ResponseEntity.ok(recordsPage);
    }

    @Operation(summary = "根據UUID獲取單筆請假紀錄", description = "根據請假紀錄的唯一識別碼(UUID)查詢詳細資訊")
    @Parameter(name = "uuid", description = "請假紀錄的唯一識別碼", required = true)
    @GetMapping("/records/{uuid}")
    public ResponseEntity<LeaveRecordDto> getLeaveRecordByUuid(@PathVariable String uuid) {
        return leaveRecordService.getLeaveRecordDtoByUuid(uuid)
                .map(dto -> ResponseEntity.ok(dto))// optional 有值 -> 回傳200 + dto
                .orElse(ResponseEntity.notFound().build()); // optional 是空的 -> 回傳404
    }

    @Operation(summary = "新增請假紀錄", description = "為當前登入的員工新增一筆請假紀錄")
    @Parameter(name = "request", description = "請假紀錄的建立請求物件", required = true)
    @PostMapping("/records")
    public ResponseEntity<LeaveRecordDto> createLeaveRecord(@RequestBody CreateLeaveRecordRequest request,
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        EmployeeUser currentUser = userDetails.getEmployeeUser();
        LeaveRecordDto savedRecord = leaveRecordService.createLeaveRecord(request, currentUser);
        return new ResponseEntity<>(savedRecord, HttpStatus.CREATED);
    }

    @Operation(summary = "更新請假單狀態 (審核)", description = "更新指定請假單的狀態，用於主管審核")
    @Parameter(name = "uuid", description = "要更新的請假紀錄的唯一識別碼", required = true)
    @Parameter(name = "status", description = "新的狀態 (例如: APPROVED, REJECTED)", required = true)
    @PutMapping("/records/{uuid}/status")
    public ResponseEntity<?> updateLeaveStatus(@PathVariable String uuid,
            @RequestBody Map<String, String> statusUpdate,
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {

        if (userDetails == null || userDetails.getEmployeeUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String newStatus = statusUpdate.get("status");
        String reason = statusUpdate.get("reason");
        if (newStatus == null || newStatus.isEmpty()) {
            return ResponseEntity.badRequest().body("Status cannot be empty.");
        }

        Optional<LeaveRecordDto> recordOptional = leaveRecordService.getLeaveRecordDtoByUuid(uuid);
        if (recordOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        LeaveRecordDto record = recordOptional.get();

        // Corrected type comparison
        Integer recordOwnerId = record.getEmployeeId();
        Long operatorId = userDetails.getEmployeeUser().getEmployeeUserId();

        if (recordOwnerId != null && operatorId.longValue() == recordOwnerId.longValue()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Users cannot approve their own leave requests.");
        }

        return leaveRecordService.updateLeaveStatus(uuid, newStatus, reason)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "更新請假紀錄 (包含附件)", description = "根據UUID更新指定的請假紀錄，可同時處理附件的刪除與新增")
    @PutMapping(value = "/records/{uuid}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<LeaveRecordDto> updateLeaveRecord(
            @PathVariable String uuid,
            @RequestPart("updateRequest") UpdateLeaveRequestWithAttachments updateRequest,
            @RequestPart(value = "newAttachments", required = false) List<MultipartFile> newAttachments,
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        if (userDetails == null || userDetails.getEmployeeUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return leaveRecordService.getLeaveRecordDtoByUuid(uuid)
                .map(existingRecord -> {
                    Integer recordOwnerId = existingRecord.getEmployeeId();
                    Long currentUserId = userDetails.getEmployeeUser().getEmployeeUserId();

                    boolean isOwner = recordOwnerId != null && currentUserId.longValue() == recordOwnerId.longValue();
                    boolean hasManageAll = userDetails.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("LEAVE_MANAGE_ALL"));
                    // 新增 LEAVE_EDIT_SELF 權限檢查
                    boolean hasEditSelf = userDetails.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("LEAVE_EDIT_SELF"));

                    // 更新判斷條件：若沒有 MANAGE_ALL 權限，則必須是擁有者且具備 EDIT_SELF 權限
                    if (!hasManageAll && !(isOwner && hasEditSelf)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<LeaveRecordDto>build();
                    }

                    return leaveRecordService.updateLeaveRecordWithAttachments(uuid, updateRequest, newAttachments)
                            .map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "刪除請假紀錄", description = "根據UUID刪除指定的請假紀錄")
    @Parameter(name = "uuid", description = "要刪除的請假紀錄的唯一識別碼", required = true)
    @DeleteMapping("/records/{uuid}")
    public ResponseEntity<Void> deleteLeaveRecord(@PathVariable String uuid,
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        if (userDetails == null || userDetails.getEmployeeUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<LeaveRecordDto> leaveRecordOptional = leaveRecordService.getLeaveRecordDtoByUuid(uuid);

        if (leaveRecordOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LeaveRecordDto existingRecord = leaveRecordOptional.get();

        // Corrected type comparison
        Integer recordOwnerId = existingRecord.getEmployeeId();
        Long currentUserId = userDetails.getEmployeeUser().getEmployeeUserId();

        boolean isOwner = recordOwnerId != null && currentUserId.longValue() == recordOwnerId.longValue();
        boolean hasManageAll = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("LEAVE_MANAGE_ALL"));

        if (!isOwner && !hasManageAll) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        leaveRecordService.deleteByUuid(uuid);
        return ResponseEntity.noContent().build();
    }
}