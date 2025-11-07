package com.project.HR.repository;

import com.project.HR.model.LeaveRecord;
import com.project.HR.model.LeaveStatus;
import com.project.HR.model.LeaveType;
import com.project.employeeuser.model.EmployeeUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRecordRepository
        extends JpaRepository<LeaveRecord, Integer>, JpaSpecificationExecutor<LeaveRecord> {

    /**
     * 以jpa data實現: 透過uuid查詢一筆假單
     * 
     * @param uuid uuid字串
     * @return LeaveRecord or null 回傳假單實體或null
     */
    Optional<LeaveRecord> findByUuid(String uuid);

    // FIX: Renamed for nested property search (employee.employeeDepartmentId) and
    // changed parameter type to Long
    Page<LeaveRecord> findByEmployee_EmployeeDepartmentId(Long departmentId, Pageable pageable);

    // FIX: Renamed for nested property search (employee.employeeUserId) and changed
    // parameter type to Long
    Page<LeaveRecord> findByEmployee_EmployeeUserId(Long employeeId, Pageable pageable);

    // FIX: Renamed for nested property search (employee.employeeUserId) and changed
    // parameter type to Long
    Page<LeaveRecord> findByEmployee_EmployeeUserIdAndStatusCode(Long employeeId, String statusCode, Pageable pageable);

    // FIX: Renamed for nested property search (employee.employeeDepartmentId) and
    // changed parameter type to Long
    Page<LeaveRecord> findByEmployee_EmployeeDepartmentIdAndStatusCode(Long departmentId, String statusCode,
            Pageable pageable);

    Page<LeaveRecord> findByStatusCode(String statusCode, Pageable pageable);

    // New methods for List<String> statusFilter
    Page<LeaveRecord> findByStatusCodeIn(Collection<String> statusCodes, Pageable pageable);

    // FIX: Renamed for nested property search (employee.employeeUserId) and changed
    // parameter type to Long
    Page<LeaveRecord> findByEmployee_EmployeeUserIdAndStatusCodeIn(Long employeeId, Collection<String> statusCodes,
            Pageable pageable);

    // FIX: Renamed for nested property search (employee.employeeDepartmentId) and
    // changed parameter type to Long
    Page<LeaveRecord> findByEmployee_EmployeeDepartmentIdAndStatusCodeIn(Long departmentId,
            Collection<String> statusCodes, Pageable pageable);

    /**
     * Finds all leave records for a given employee, leave type, and status.
     * This is primarily used to calculate the total used hours for a specific leave
     * type (e.g., approved annual leave).
     * 
     * @param employee  The employee entity.
     * @param leaveType The leave type entity.
     * @param status    The status entity.
     * @return A list of matching leave records.
     */
    List<LeaveRecord> findAllByEmployeeAndLeaveTypeAndStatus(EmployeeUser employee, LeaveType leaveType,
            LeaveStatus status);
}
