package com.project.HR.service;

import com.project.HR.model.*;
import com.project.core.security.EmployeeUserDetails;
import com.project.employeeuser.model.EmployeeUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LeaveRecordService {

	/**
	 * 查看單筆假單，透過uuid (e.g. 前端點擊"查看詳情"那個眼睛)
	 * 
	 * @param uuid 前端點擊後自動取得並攜帶
	 * @return 回傳Optional物件，攜帶LeaveRecordDto物件，回前端顯示
	 */
	Optional<LeaveRecordDto> getLeaveRecordDtoByUuid(String uuid);

	void deleteByUuid(String uuid);

	LeaveRecordDto createLeaveRecord(CreateLeaveRecordRequest request, EmployeeUser currentUser);

	Optional<LeaveRecordDto> updateLeaveStatus(String uuid, String status, String reason);

	Optional<LeaveRecordDto> updateLeaveRecord(String uuid, UpdateLeaveRecordRequest request);

	Optional<LeaveRecordDto> updateLeaveRecordWithAttachments(String uuid,
			UpdateLeaveRequestWithAttachments updateRequest, List<MultipartFile> newAttachments);

	List<LeaveType> getAllLeaveTypes();

	Optional<LeaveRecord> getByUuid(String uuid);

	Page<LeaveRecordDto> getLeaveRecords(EmployeeUserDetails userDetails, Pageable pageable,
			com.project.HR.model.LeaveRecordFilter filter);

	double calculateWorkHours(LocalDateTime start, LocalDateTime end);

	/**
	 * 為指定假單 1. 儲存附件到本地端資料夾(若有需求可更換儲存庫) 2. 儲存記錄到DB
	 * 
	 * @param uuid 假單的uuid (hr_leave_record表格中的leave_uuid欄位)
	 * @param file 用戶上傳的附件
	 * @return 一個 DTO 代表用戶上傳的附件
	 */
	LeaveAttachmentDto storeAttachment(String leaveRecordUuid, MultipartFile file);

	/**
	 * Deletes an attachment from a leave record.
	 * 
	 * @param leaveRecordUuid The UUID of the leave record.
	 * @param filename        The stored filename of the attachment to delete.
	 */
	void deleteAttachment(String leaveRecordUuid, String filename);

	double getAnnualLeaveBalance(EmployeeUser currentUser);
}
