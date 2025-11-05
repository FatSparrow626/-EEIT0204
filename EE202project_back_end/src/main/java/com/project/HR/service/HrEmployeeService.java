package com.project.HR.service;

import com.project.HR.model.EmployeeNameDto;
import java.util.List;

public interface HrEmployeeService {
    List<EmployeeNameDto> searchEmployeesByName(String name);
}
