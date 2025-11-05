package com.project.core.controller;

import com.project.core.dao.DepartmentRepository;
import com.project.core.model.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "部門管理", description = "部門資料查詢")
@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = {"http://localhost:5173", "http://172.22.34.82:5173"})
public class DepartmentController {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * 獲取所有部門列表
     * @return 部門列表
     */
    @Operation(summary = "獲取所有部門", description = "返回系統中所有可用的部門列表")
    @GetMapping
    public ResponseEntity<?> getAllDepartments() {
        try {
            logger.info("開始獲取所有部門列表");
            
            List<Department> departments = departmentRepository.findAll();
            
            logger.info("成功獲取 {} 個部門", departments.size());
            return ResponseEntity.ok(departments);
            
        } catch (Exception e) {
            logger.error("獲取部門列表時發生錯誤: ", e);
            return ResponseEntity.status(500)
                    .body("獲取部門列表失敗: " + e.getMessage());
        }
    }

    /**
     * 根據部門ID獲取部門資訊
     * @param id 部門ID
     * @return 部門資訊
     */
    @Operation(summary = "根據ID獲取部門", description = "根據部門ID返回特定部門的詳細資訊")
    @GetMapping("/{id}")
    public ResponseEntity<?> getDepartmentById(@PathVariable Integer id) {
        try {
            logger.info("開始獲取部門ID: {}", id);
            
            return departmentRepository.findById(id)
                    .<ResponseEntity<?>>map(department -> {
                        logger.info("成功找到部門: {}", department.getDepartmentName());
                        return ResponseEntity.ok(department);
                    })
                    .orElseGet(() -> {
                        logger.warn("找不到部門ID: {}", id);
                        return ResponseEntity.status(404)
                                .body("找不到部門ID: " + id);
                    });
                    
        } catch (Exception e) {
            logger.error("獲取部門資訊時發生錯誤: ", e);
            return ResponseEntity.status(500)
                    .body("獲取部門資訊失敗: " + e.getMessage());
        }
    }
}