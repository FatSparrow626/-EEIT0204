package com.project.HR.service;

import com.project.HR.adapter.EmployeeAdapter;
import com.project.HR.model.EmployeeHr;
import com.project.HR.model.LeaveRecord;
import com.project.HR.repository.LeaveRecordRepository;
import com.project.core.security.EmployeeUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service("leaveSecurityService")
public class LeaveSecurityService {

    @Autowired
    private LeaveRecordRepository leaveRecordRepository;

    /**
     * Checks if the current user has permission to view a specific leave record.
     *
     * @param userDetails The details of the currently authenticated user.
     * @param uuid        The UUID of the leave record to check.
     * @return true if the user has permission, false otherwise.
     */
    @Transactional(readOnly = true)
    public boolean canView(EmployeeUserDetails userDetails, String uuid) {
        if (userDetails == null || userDetails.getEmployeeUser() == null) {
            return false;
        }

        // Find the leave record by its UUID
        return leaveRecordRepository.findByUuid(uuid).map(leaveRecord -> {
            Long currentUserId = userDetails.getEmployeeUser().getEmployeeUserId();
            Long recordOwnerId = leaveRecord.getEmployee().getEmployeeUserId();

            // 1. Check if the user is the owner of the record
            if (Objects.equals(currentUserId, recordOwnerId)) {
                return true;
            }

            // 2. Check if the user has the LEAVE_MANAGE_ALL permission (Super Manager)
            boolean hasManageAllPermission = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> "LEAVE_MANAGE_ALL".equals(auth.getAuthority()));
            if (hasManageAllPermission) {
                return true;
            }

            // 3. Check if the user has LEAVE_VIEW_DEPARTMENT and is in the same department
            boolean hasViewDepartmentPermission = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> "LEAVE_VIEW_DEPARTMENT".equals(auth.getAuthority()));

            if (hasViewDepartmentPermission) {
                // Use the adapter to handle Long -> Integer conversion
                EmployeeHr currentUserHr = EmployeeAdapter.toHr(userDetails.getEmployeeUser());
                EmployeeHr recordOwnerHr = EmployeeAdapter.toHr(leaveRecord.getEmployee());

                Integer currentUserDeptId = currentUserHr.getEmployeeDepartmentId();
                Integer recordOwnerDeptId = recordOwnerHr.getEmployeeDepartmentId();

                if (currentUserDeptId != null && Objects.equals(currentUserDeptId, recordOwnerDeptId)) {
                    return true;
                }
            }

            // 4. If none of the above, deny access
            return false;
        }).orElse(false); // If record not found, deny access
    }
}
