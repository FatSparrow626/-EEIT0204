package com.project.HR.adapter;

import com.project.HR.model.EmployeeHr;
import com.project.employeeuser.model.EmployeeUser;

/**
 * Adapter to convert the official EmployeeUser entity (with Long IDs)
 * to the HR module's internal EmployeeHr POJO (with Integer IDs).
 */
public class EmployeeAdapter {

    /**
     * Converts an EmployeeUser object to an EmployeeHr object.
     * This method handles the Long to Integer conversion for ID fields.
     *
     * @param user The EmployeeUser entity object to convert.
     * @return An EmployeeHr POJO, or null if the input is null.
     */
    public static EmployeeHr toHr(EmployeeUser user) {
        if (user == null) {
            return null;
        }

        EmployeeHr hr = new EmployeeHr();

        // Perform conversion, handling potential nulls for numeric types
        hr.setEmployeeUserId(user.getEmployeeUserId() != null ? user.getEmployeeUserId().intValue() : null);
        hr.setEmployeeDepartmentId(user.getEmployeeDepartmentId() != null ? user.getEmployeeDepartmentId().intValue() : null);
        hr.setEmployeePositionId(user.getEmployeePositionId() != null ? user.getEmployeePositionId().intValue() : null);
        hr.setManagerEmployeeUserId(user.getManagerEmployeeUserId() != null ? user.getManagerEmployeeUserId().intValue() : null);

        // Copy all other fields directly
        hr.setEmployeeNumber(user.getEmployeeNumber());
        hr.setFirstName(user.getFirstName());
        hr.setLastName(user.getLastName());
        hr.setUsername(user.getUsername());
        hr.setPasswordHash(user.getPasswordHash());
        hr.setEmployeeType(user.getEmployeeType());
        hr.setEmail(user.getEmail());
        hr.setPhone(user.getPhone());
        hr.setBirthDate(user.getBirthDate());
        hr.setHireDate(user.getHireDate());
        hr.setTerminationDate(user.getTerminationDate());
        hr.setPhotoPath(user.getPhotoPath());
        hr.setIsActive(user.getIsActive());
        hr.setLastLogin(user.getLastLogin());
        hr.setCreatedAt(user.getCreatedAt());
        hr.setUpdatedAt(user.getUpdatedAt());

        return hr;
    }
}
