package com.project.HR.service;

import com.project.HR.model.EmployeeNameDto;
import com.project.HR.repository.HrEmployeeRepository; // 1. 依賴修正後的 Repository
import com.project.employeeuser.model.EmployeeUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HrEmployeeServiceImpl implements HrEmployeeService {

    // 2. 依賴的對象是 HrEmployeeRepository
    private final HrEmployeeRepository hrEmployeeRepository;

    @Autowired
    public HrEmployeeServiceImpl(HrEmployeeRepository hrEmployeeRepository) {
        this.hrEmployeeRepository = hrEmployeeRepository;
    }

    @Override
    public List<EmployeeNameDto> searchEmployeesByName(String name) {
        // 3. 呼叫 Repository 的方法，查詢將在資料庫高效執行
        List<EmployeeUser> foundUsers = hrEmployeeRepository.findByNameContainingIgnoreCase(name);

        // 4. 在服務層將 EmployeeUser 實體轉換為 HR 模組所需的 DTO
        return foundUsers.stream()
                .map(user -> new EmployeeNameDto(
                        user.getEmployeeUserId().intValue(),
                        user.getLastName() + " " + user.getFirstName()
                ))
                .collect(Collectors.toList());
    }
}
