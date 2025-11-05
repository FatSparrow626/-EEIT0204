package com.project.HR.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeNameDto {
    private Integer employeeId;
    private String fullName;
}
