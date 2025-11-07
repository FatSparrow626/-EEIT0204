package com.project.HR.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.project.HR.adapter.EmployeeAdapter;
import com.project.HR.model.CreateLeaveRecordRequest;
import com.project.HR.model.EmployeeHr;
import com.project.HR.model.LeaveAttachment;
import com.project.HR.model.LeaveAttachmentDto;
import com.project.HR.model.LeaveRecord;
import com.project.HR.model.LeaveRecordDto;
import com.project.HR.model.LeaveStatus;
import com.project.HR.model.LeaveType;
import com.project.HR.model.NationalHoliday;
import com.project.HR.model.UpdateLeaveRecordRequest;
import com.project.HR.model.UpdateLeaveRequestWithAttachments;
import com.project.HR.repository.LeaveAttachmentRepository;
import com.project.HR.repository.LeaveRecordRepository;
import com.project.HR.repository.LeaveStatusRepository;
import com.project.HR.repository.LeaveTypeRepository;
import com.project.HR.repository.NationalHolidayRepository;
import com.project.core.security.EmployeeUserDetails;
import com.project.employeeuser.dao.EmployeeUserDAO;
import com.project.employeeuser.model.EmployeeUser;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.EntityManager; // Add this import
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext; // Add this import
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;

@AllArgsConstructor // lombok 自動DI
@Service
public class LeaveRecordServiceImpl implements LeaveRecordService {

    private static final Logger logger = LoggerFactory.getLogger(LeaveRecordServiceImpl.class);

    private final LeaveRecordRepository leaveRecordRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeUserDAO employeeUserDAO;
    private final LeaveStatusRepository leaveStatusRepository;
    private final NationalHolidayRepository nationalHolidayRepository;
    private final AnnualLeaveService annualLeaveService;
    private final FileStorageService fileStorageService;
    private final LeaveAttachmentRepository leaveAttachmentRepository;
    private final EmailService emailService; // Inject EmailService

    @PersistenceContext
    private EntityManager entityManager;

    // 如果沒用lombok就要constructor DI
    // @Autowired
    // public LeaveRecordServiceImpl(
    // LeaveRecordRepository leaveRecordRepository, and so on...

    public LeaveAttachmentDto storeAttachment(String leaveRecordUuid, MultipartFile file) {

        LeaveRecord leaveRecord = leaveRecordRepository.findByUuid(leaveRecordUuid)
                .orElseThrow(() -> new EntityNotFoundException("找不到指定uuid為: " + leaveRecordUuid + " 的假單"));
        String storedFileName = fileStorageService.store(file, leaveRecordUuid);

        // 建立持久化物件->DB
        LeaveAttachment attachment = new LeaveAttachment();
        attachment.setLeaveRecord(leaveRecord); // 建立附件與指定uuid假單的關聯
        attachment.setFileName(file.getOriginalFilename()); // 前端上傳檔名
        attachment.setStoredFileName(storedFileName); // 本地檔名

        // spring 提供的Mutipart介面，可以取得前端檔案類型與大小
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());

        // 使用Spring data 操作DB
        LeaveAttachment savedAttachment = leaveAttachmentRepository.save(attachment);

        // 回傳附件DTO
        return convertToDto(savedAttachment);
    }

    /**
     * 附件DTO轉換器:
     * 將LeaveAttachment entity轉成LeaveAttachmentDto
     * 同時用spring web 的 api產生下載連結(/localhost:8080 + /api/leave/attachments/ +
     * <leaveUuid> +/+ <storedFileName>)
     * 
     * @param attachment
     * @return LeaveAttachmentDto 附件的DTO
     */
    private LeaveAttachmentDto convertToDto(LeaveAttachment attachment) {
        String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/leave/attachments/")
                .path(attachment.getLeaveRecord().getUuid())
                .path("/")
                .path(attachment.getStoredFileName())
                .toUriString();

        // 如果是圖片檔就加上inline查詢參數讓前端可以直接預覽
        if (attachment.getFileType() != null && attachment.getFileType().startsWith("image/")) {
            downloadUrl += "?inline=true";
        }

        return new LeaveAttachmentDto(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getStoredFileName(),
                attachment.getFileType(),
                attachment.getFileSize(),
                downloadUrl);
    }

    public LeaveRecordDto createLeaveRecord(CreateLeaveRecordRequest request, EmployeeUser currentUser) {
        LeaveRecord newRecord = new LeaveRecord();
        newRecord.setEmployee(currentUser);

        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException("LeaveType not found with id: " + request.getLeaveTypeId()));
        newRecord.setLeaveType(leaveType);

        if (request.getAgentId() != null) {
            EmployeeUser agent = employeeUserDAO.findById(request.getAgentId().longValue())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Agent ID: " + request.getAgentId()));
            newRecord.setAgent(agent);
        }

        newRecord.setReason(request.getReason());
        newRecord.setStartDatetime(request.getStartDatetime());
        newRecord.setEndDatetime(request.getEndDatetime());

        double calculatedHours = calculateWorkHours(request.getStartDatetime(), request.getEndDatetime());
        newRecord.setHours(BigDecimal.valueOf(calculatedHours));

        newRecord.setUuid(UUID.randomUUID().toString());
        LeaveStatus pendingStatus = leaveStatusRepository.findByCode("PENDING")
                .orElseThrow(() -> new RuntimeException("LeaveStatus PENDING not found"));
        newRecord.setStatus(pendingStatus);

        LeaveRecord savedRecord = leaveRecordRepository.save(newRecord);

        // --- EMAIL NOTIFICATION TO MANAGER ---
        try {
            if (currentUser.getManagerEmployeeUserId() != null) {
                employeeUserDAO.findById(currentUser.getManagerEmployeeUserId()).ifPresent(manager -> {
                    if (manager.getEmail() != null && !manager.getEmail().isEmpty()) {
                        String subject = String.format("[請假審核通知] 員工 %s %s 提交了請假申請", currentUser.getLastName(),
                                currentUser.getFirstName());
                        String text = String.format(
                                "您好，\n\n員工 %s %s 已提交新的請假申請。\n\n請點擊下方連結登入系統進行審核：\nhttp://localhost:5173/kh/leave/list\n\n此為系統自動發送，請勿直接回覆。",
                                currentUser.getLastName(), currentUser.getFirstName());
                        emailService.sendSimpleMail(manager.getEmail(), subject, text);
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Failed to send leave application notification email for record UUID: {}",
                    savedRecord.getUuid(), e);
        }
        // --- END OF EMAIL NOTIFICATION ---

        return convertToDto(savedRecord);
    }

    @Transactional
    @Override
    public Optional<LeaveRecordDto> updateLeaveRecordWithAttachments(
            String uuid, UpdateLeaveRequestWithAttachments updateRequest, List<MultipartFile> newAttachments) {

        return leaveRecordRepository.findByUuid(uuid)
                .map(existingRecord -> {
                    // VALIDATION BLOCK
                    final int MAX_ATTACHMENTS = 5;
                    final long MAX_TOTAL_SIZE_BYTES = 50 * 1024 * 1024; // 50MB

                    List<String> attachmentsToDelete = updateRequest.getAttachmentsToDelete() != null
                            ? updateRequest.getAttachmentsToDelete()
                            : Collections.emptyList();

                    // Validate total attachment count
                    long currentAttachmentCount = existingRecord.getAttachments().stream()
                            .filter(att -> !attachmentsToDelete.contains(att.getStoredFileName()))
                            .count();
                    long newAttachmentCount = (newAttachments != null) ? newAttachments.size() : 0;
                    if (currentAttachmentCount + newAttachmentCount > MAX_ATTACHMENTS) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "附件總數不得超過 " + MAX_ATTACHMENTS + " 個");
                    }

                    // Validate total attachment size
                    long currentAttachmentSize = existingRecord.getAttachments().stream()
                            .filter(att -> !attachmentsToDelete.contains(att.getStoredFileName()))
                            .mapToLong(LeaveAttachment::getFileSize)
                            .sum();
                    long newAttachmentSize = (newAttachments != null)
                            ? newAttachments.stream().mapToLong(MultipartFile::getSize).sum()
                            : 0;
                    if (currentAttachmentSize + newAttachmentSize > MAX_TOTAL_SIZE_BYTES) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "附件總大小不得超過50MB");
                    }
                    // END VALIDATION BLOCK

                    // 1. 更新基本資料
                    existingRecord.setReason(updateRequest.getReason());
                    existingRecord.setStartDatetime(updateRequest.getStartDatetime());
                    existingRecord.setEndDatetime(updateRequest.getEndDatetime());

                    // 重新計算時數
                    double calculatedHours = calculateWorkHours(updateRequest.getStartDatetime(),
                            updateRequest.getEndDatetime());
                    existingRecord.setHours(BigDecimal.valueOf(calculatedHours));

                    // 2. 刪除指定的附件 (使用 orphanRemoval=true)
                    if (!attachmentsToDelete.isEmpty()) {
                        for (String storedFileName : attachmentsToDelete) {
                            fileStorageService.delete(uuid, storedFileName);
                        }
                        existingRecord.getAttachments()
                                .removeIf(attachment -> attachmentsToDelete.contains(attachment.getStoredFileName()));
                    }

                    // 3. 新增新的附件
                    if (newAttachments != null && !newAttachments.isEmpty()) {
                        for (MultipartFile file : newAttachments) {
                            String storedFileName = fileStorageService.store(file, uuid);
                            LeaveAttachment newAttachment = new LeaveAttachment();
                            newAttachment.setLeaveRecord(existingRecord);
                            newAttachment.setFileName(file.getOriginalFilename());
                            newAttachment.setStoredFileName(storedFileName);
                            newAttachment.setFileType(file.getContentType());
                            newAttachment.setFileSize(file.getSize());
                            existingRecord.getAttachments().add(newAttachment);
                        }
                    }

                    // 4. 儲存更新後的請假紀錄 (級聯操作會一併處理附件)
                    LeaveRecord updatedRecord = leaveRecordRepository.save(existingRecord);

                    // 5. 返回更新後的 DTO
                    return convertToDto(updatedRecord);
                });
    }

    public Optional<LeaveRecordDto> updateLeaveRecord(String uuid, UpdateLeaveRecordRequest request) {
        return leaveRecordRepository.findByUuid(uuid)
                .map(existingRecord -> {
                    existingRecord.setReason(request.getReason());
                    existingRecord.setStartDatetime(request.getStartDatetime());
                    existingRecord.setEndDatetime(request.getEndDatetime());

                    double calculatedHours = calculateWorkHours(request.getStartDatetime(), request.getEndDatetime());
                    existingRecord.setHours(BigDecimal.valueOf(calculatedHours));

                    LeaveRecord updatedRecord = leaveRecordRepository.save(existingRecord);
                    return convertToDto(updatedRecord);
                });
    }

    public double calculateWorkHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !start.isBefore(end)) {
            return 0.0;
        }

        List<NationalHoliday> holidays = nationalHolidayRepository.findByDateBetween(start.toLocalDate(),
                end.toLocalDate());
        Set<LocalDate> holidayDates = holidays.stream()
                .filter(h -> !"MAKEUP_WORKDAY".equals(h.getType()))
                .map(NationalHoliday::getDate)
                .collect(Collectors.toSet());
        Set<LocalDate> makeupWorkdays = holidays.stream()
                .filter(h -> "MAKEUP_WORKDAY".equals(h.getType()))
                .map(NationalHoliday::getDate)
                .collect(Collectors.toSet());

        double totalMinutes = 0;
        LocalDateTime loopDateTime = start;

        while (loopDateTime.isBefore(end)) {
            LocalDate loopDate = loopDateTime.toLocalDate();
            DayOfWeek dayOfWeek = loopDate.getDayOfWeek();
            boolean isWorkingDay = makeupWorkdays.contains(loopDate) ||
                    (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
                            && !holidayDates.contains(loopDate));

            if (isWorkingDay) {
                LocalDateTime workDayStart = loopDate.atTime(9, 0);
                LocalDateTime workDayEnd = loopDate.atTime(18, 0);
                LocalDateTime lunchStart = loopDate.atTime(12, 0);
                LocalDateTime lunchEnd = loopDate.atTime(13, 0);

                LocalDateTime nextMinute = loopDateTime.plusMinutes(1);
                LocalDateTime effectiveEnd = nextMinute.isAfter(end) ? end : nextMinute;

                if (loopDateTime.isBefore(workDayStart))
                    loopDateTime = workDayStart;
                if (effectiveEnd.isAfter(workDayEnd))
                    effectiveEnd = workDayEnd;

                if (!(loopDateTime.isBefore(lunchEnd) && effectiveEnd.isAfter(lunchStart))) {
                    if (loopDateTime.isBefore(effectiveEnd)) {
                        totalMinutes += Duration.between(loopDateTime, effectiveEnd).toMinutes();
                    }
                }
            }
            loopDateTime = loopDateTime.plusMinutes(1);
        }

        double totalHours = totalMinutes / 60.0;
        return Math.round(totalHours * 2) / 2.0;
    }

    // 尚未實作
    public double getAnnualLeaveBalance(EmployeeUser currentUser) {
        double totalEntitlementDays = annualLeaveService.calculateAnnualLeaveEntitlementDays(currentUser.getHireDate());
        double totalEntitlementHours = annualLeaveService.convertDaysToHours(totalEntitlementDays);

        LeaveType annualLeaveType = leaveTypeRepository.findByName("特休")
                .orElseThrow(() -> new RuntimeException("LeaveType '特休' not found in database."));
        LeaveStatus approvedStatus = leaveStatusRepository.findByCode("APPROVED")
                .orElseThrow(() -> new RuntimeException("LeaveStatus 'APPROVED' not found in database."));

        List<LeaveRecord> approvedAnnualLeaves = leaveRecordRepository
                .findAllByEmployeeAndLeaveTypeAndStatus(currentUser, annualLeaveType, approvedStatus);

        double usedHours = approvedAnnualLeaves.stream()
                .map(LeaveRecord::getHours)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        return totalEntitlementHours - usedHours;
    }

    public Optional<LeaveRecordDto> updateLeaveStatus(String uuid, String status, String reason) {
        return leaveRecordRepository.findByUuid(uuid)
                .map(existingRecord -> {
                    LeaveStatus newStatus = leaveStatusRepository.findByCode(status)
                            .orElseThrow(() -> new RuntimeException("LeaveStatus " + status + " not found"));
                    existingRecord.setStatus(newStatus);

                    if ("REJECTED".equals(status) && reason != null && !reason.isEmpty()) {
                        existingRecord.setRejectionReason(reason);
                    }
                    if ("APPROVED".equals(status) || "REJECTED".equals(status)) {
                        existingRecord.setReviewedAt(LocalDateTime.now());
                    }

                    LeaveRecord updatedRecord = leaveRecordRepository.save(existingRecord);

                    // --- EMAIL NOTIFICATION TO APPLICANT ---
                    try {
                        EmployeeUser applicant = existingRecord.getEmployee();
                        if (applicant != null && applicant.getEmail() != null && !applicant.getEmail().isEmpty()) {
                            String statusInChinese = "APPROVED".equals(status) ? "審核通過" : "被駁回";
                            String subject = String.format("[請假狀態更新] 您的請假申請已%s", statusInChinese);
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                            String text = String.format(
                                    "您好，\n\n您提交的請假申請（期間：%s - %s）已被主管%s。\n\n詳情請點擊下方連結查看：\nhttp://localhost:5173/kh/leave/edit/%s\n\n此為系統自動發送，請勿直接回覆。",
                                    existingRecord.getStartDatetime().format(formatter),
                                    existingRecord.getEndDatetime().format(formatter),
                                    statusInChinese,
                                    existingRecord.getUuid());
                            emailService.sendSimpleMail(applicant.getEmail(), subject, text);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to send leave status update email for record UUID: {}",
                                updatedRecord.getUuid(), e);
                    }
                    // --- END OF EMAIL NOTIFICATION ---

                    return convertToDto(updatedRecord);
                });
    }

    public void deleteByUuid(String uuid) {
        leaveRecordRepository.findByUuid(uuid).ifPresent(leaveRecordRepository::delete);
    }

    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeRepository.findAll();
    }

    public Optional<LeaveRecord> getByUuid(String uuid) {
        return leaveRecordRepository.findByUuid(uuid);
    }

    public Page<LeaveRecordDto> getLeaveRecords(EmployeeUserDetails userDetails, Pageable pageable,
            com.project.HR.model.LeaveRecordFilter filter) {

        Specification<LeaveRecord> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            EmployeeUser currentUser = userDetails.getEmployeeUser();

            boolean canManageAll = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> "LEAVE_MANAGE_ALL".equals(auth.getAuthority()));
            boolean canViewDepartment = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> "LEAVE_VIEW_DEPARTMENT".equals(auth.getAuthority()));

            String viewMode = filter.getViewMode();

            // 根據權限和視圖模式決定查詢範圍
            if (canManageAll && "companyAll".equals(viewMode)) {
                // 全公司視圖：不添加任何員工或部門的限制
            } else if (canManageAll && "companyProcessed".equals(viewMode)) {
                // 全公司已處理視圖
                predicates.add(root.get("status").get("code").in(Arrays.asList("APPROVED", "REJECTED")));
            } else if (canViewDepartment
                    && ("departmentPending".equals(viewMode) || "departmentProcessed".equals(viewMode))) {
                // 部門視圖
                predicates.add(criteriaBuilder.equal(root.get("employee").get("employeeDepartmentId"),
                        currentUser.getEmployeeDepartmentId()));

                if ("departmentPending".equals(viewMode)) {
                    predicates.add(criteriaBuilder.equal(root.get("status").get("code"), "PENDING"));
                } else { // departmentProcessed
                    predicates.add(root.get("status").get("code").in(Arrays.asList("APPROVED", "REJECTED")));
                }

            } else { // 預設為個人視圖
                predicates.add(
                        criteriaBuilder.equal(root.get("employee").get("employeeUserId"),
                                currentUser.getEmployeeUserId()));
                if (filter.getStatusFilter() != null && !filter.getStatusFilter().equalsIgnoreCase("ALL")) {
                    predicates.add(criteriaBuilder.equal(root.get("status").get("code"), filter.getStatusFilter()));
                }
            }

            // 員工姓名過濾 (適用於部門和全公司視圖)
            if (filter.getEmployeeName() != null && !filter.getEmployeeName().trim().isEmpty()) {
                String namePattern = "%" + filter.getEmployeeName().trim() + "%";
                jakarta.persistence.criteria.Expression<String> fullName1 = criteriaBuilder.concat(
                        root.get("employee").get("lastName"), " ");
                fullName1 = criteriaBuilder.concat(fullName1, root.get("employee").get("firstName"));
                jakarta.persistence.criteria.Expression<String> fullName2 = criteriaBuilder.concat(
                        root.get("employee").get("firstName"), " ");
                fullName2 = criteriaBuilder.concat(fullName2, root.get("employee").get("lastName"));
                Predicate nameMatch = criteriaBuilder.or(
                        criteriaBuilder.like(fullName1, namePattern),
                        criteriaBuilder.like(fullName2, namePattern));
                predicates.add(nameMatch);
            }

            // 日期區間過濾 (適用於部門和全公司視圖)
            if (filter.getStartDate() != null && filter.getEndDate() != null) {
                LocalDateTime startOfDay = filter.getStartDate().atStartOfDay();
                LocalDateTime endOfDay = filter.getEndDate().atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThan(root.get("startDatetime"), endOfDay));
                predicates.add(criteriaBuilder.greaterThan(root.get("endDatetime"), startOfDay));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<LeaveRecord> leaveRecordsPage = leaveRecordRepository.findAll(spec, pageable);

        return leaveRecordsPage.map(this::convertToDto);
    }

    /**
     * 呼叫repository透過uuid查詢假單，並轉成DTO回傳
     */
    public Optional<LeaveRecordDto> getLeaveRecordDtoByUuid(String uuid) {
        return leaveRecordRepository.findByUuid(uuid)
                .map(leaveRecord -> this.convertToDto(leaveRecord)); // 呼叫假單的DTO
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAttachment(String leaveRecordUuid, String filename) {
        System.out.println(
                "DEBUG: Entering deleteAttachment method for UUID: " + leaveRecordUuid + ", filename: " + filename);
        LeaveAttachment attachment = leaveAttachmentRepository
                .findByLeaveRecordUuidAndStoredFileName(leaveRecordUuid, filename)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Attachment not found with filename: " + filename + " for leave record: " + leaveRecordUuid));

        System.out.println("DEBUG: Found attachment for deletion: " + attachment.getStoredFileName() + ", ID: "
                + attachment.getId());
        System.out.println("DEBUG: Attachment's LeaveRecord ID: " + attachment.getLeaveRecord().getId());

        try {
            // Delete the physical file
            fileStorageService.delete(leaveRecordUuid, attachment.getStoredFileName());
            System.out.println("DEBUG: Physical file deleted successfully: " + attachment.getStoredFileName());
        } catch (Exception e) {
            System.err.println(
                    "ERROR: Failed to delete physical file " + attachment.getStoredFileName() + ": " + e.getMessage());
            // Re-throw or handle as appropriate, maybe throw a custom exception
            throw new RuntimeException("Failed to delete physical file", e);
        }

        try {
            // Delete the database record using the repository
            leaveAttachmentRepository.delete(attachment);
            System.out.println("DEBUG: Database record deleted successfully for: " + attachment.getStoredFileName()
                    + " using leaveAttachmentRepository.delete().");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to delete database record for " + attachment.getStoredFileName() + ": "
                    + e.getMessage());
            // Re-throw or handle as appropriate
            throw new RuntimeException("Failed to delete database record", e);
        }
        System.out.println("DEBUG: Exiting deleteAttachment method successfully.");
    }

    /**
     * 假單DTO轉換器:
     * 將LeaveRecord entity轉成LeaveRecordDto
     * 
     * @param record
     * @return LeaveRecordDto 假單的DTO
     */
    private LeaveRecordDto convertToDto(LeaveRecord record) {
        LeaveRecordDto dto = new LeaveRecordDto();
        dto.setUuid(record.getUuid());
        dto.setReason(record.getReason());
        dto.setStartDatetime(record.getStartDatetime());
        dto.setEndDatetime(record.getEndDatetime());
        dto.setHours(record.getHours());
        dto.setRejectionReason(record.getRejectionReason());
        dto.setReviewedAt(record.getReviewedAt());

        // 多段if的功能: 把entity對應的物件挑選資訊並扁平化，以便前端顯示
        if (record.getStatus() != null) {
            dto.setStatusCode(record.getStatus().getCode());
            dto.setStatusName(record.getStatus().getName());
        }

        if (record.getEmployee() != null) {
            EmployeeHr employeeHr = EmployeeAdapter.toHr(record.getEmployee());
            dto.setEmployeeId(employeeHr.getEmployeeUserId());
            dto.setEmployeeName(employeeHr.getFullName());
        }
        if (record.getAgent() != null) {
            EmployeeHr agentHr = EmployeeAdapter.toHr(record.getAgent());
            dto.setAgentId(agentHr.getEmployeeUserId());
            dto.setAgentName(agentHr.getFullName());
        }

        dto.setLeaveTypeName(record.getLeaveType().getName());

        // 處理假單附件List: 從entity轉成DTO
        if (record.getAttachments() != null && !record.getAttachments().isEmpty()) {
            List<LeaveAttachmentDto> attachmentDtos = record.getAttachments().stream()
                    .map(attachment -> this.convertToDto(attachment)) // 呼叫附件DTO轉換器
                    .collect(Collectors.toList());
            dto.setAttachments(attachmentDtos);
        } else {
            dto.setAttachments(Collections.emptyList());
        }

        return dto;
    }
}
