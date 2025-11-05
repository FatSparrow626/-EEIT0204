package com.project.HR.controller;

import com.project.HR.model.EmployeeNameDto;
import com.project.HR.service.HrEmployeeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "HR 輔助功能", description = "提供 HR 模組所需的各種輔助 API")
@RequestMapping("/api/hr")
public class HrApiController {

    private final HrEmployeeService hrEmployeeService;

    @Autowired
    public HrApiController(HrEmployeeService hrEmployeeService) {
        this.hrEmployeeService = hrEmployeeService;
    }

    @GetMapping("/employees/search")
    @PreAuthorize("hasAuthority('LEAVE_APPLY_SELF')")
    public ResponseEntity<List<EmployeeNameDto>> searchEmployeesByName(@RequestParam String name) {
        List<EmployeeNameDto> employees = hrEmployeeService.searchEmployeesByName(name);
        return ResponseEntity.ok(employees);
    }
}
