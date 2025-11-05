package com.project.core.service;

import com.project.core.dao.EmployeeRepository;
import com.project.core.dto.request.EmployeeUserCreateRequest;
import com.project.core.dto.request.EmployeeUserUpdateRequest;
import com.project.core.dto.response.EmployeeUserDto;
import com.project.core.dto.response.RoleDto;
import com.project.core.exception.DuplicateEmailException;
import com.project.core.exception.DuplicateEmployeeNumberException;
import com.project.core.exception.DuplicateUsernameException;
import com.project.core.model.Employee;
// ===== 新增：角色管理功能開始 =====
import com.project.core.dao.UserRoleRepository;
import com.project.core.dao.RoleRepository;
import com.project.core.model.UserRole;
import com.project.core.model.Role;
import com.project.core.service.SystemLogService;
// ===== 新增：角色管理功能結束 =====
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeUserServiceImpl implements EmployeeUserService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeUserServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SystemLogService systemLogService;
    
    // ===== 新增：角色管理功能開始 =====
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    // ===== 新增：角色管理功能結束 =====

    @Override
    public List<EmployeeUserDto> findAllEmployeeUsers() {
        logger.info("查詢所有員工使用者");
        return employeeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EmployeeUserDto> findEmployeeUserById(Integer id) {
        logger.info("根據ID查詢員工使用者: {}", id);
        return employeeRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    public Optional<EmployeeUserDto> findEmployeeUserByUsername(String username) {
        logger.info("根據使用者名稱查詢員工使用者: {}", username);
        return employeeRepository.findByUsername(username)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public EmployeeUserDto createEmployeeUser(EmployeeUserCreateRequest request) {
        logger.info("請求新增員工使用者: {}", request.getUsername());

        if (employeeRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateUsernameException("使用者名稱已存在: " + request.getUsername());
        }
        if (employeeRepository.findByEmployeeNumber(request.getEmployeeNumber()).isPresent()) {
            throw new DuplicateEmployeeNumberException("員工編號已存在: " + request.getEmployeeNumber());
        }
        if (request.getEmail() != null && employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("電子郵件已存在: " + request.getEmail());
        }

        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmployeeNumber(request.getEmployeeNumber());
        // 設定員工類型，如果未提供則使用預設值 INTERNAL
        if (request.getEmployeeType() == null || request.getEmployeeType().isEmpty()) {
            employee.setEmployeeType("INTERNAL"); // 預設值
        } else {
            employee.setEmployeeType(request.getEmployeeType());
        }
        employee.setBirthDate(request.getBirthDate());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setPhotoPath(request.getPhotoPath());
        employee.setDepartmentId(request.getEmployeeDepartmentId());
        employee.setManagerId(request.getManagerEmployeeUserId());
        employee.setHireDate(request.getHireDate());
        employee.setTerminationDate(request.getTerminationDate());
        employee.setUsername(request.getUsername());
        employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        employee.setIsActive(request.getIsActive());

        Employee savedEmployee = employeeRepository.save(employee);
        logger.info("員工使用者 {} 已新增", savedEmployee.getUsername());

        // 指派角色 - 失敗時整個事務回滾
        assignRolesToEmployee(savedEmployee.getEmployeeId(), request, savedEmployee.getUsername());

        systemLogService.log(
                "CREATE",
                "新增員工使用者",
                savedEmployee.getEmployeeId(),
                savedEmployee.getUsername(),
                "EMPLOYEE",
                String.valueOf(savedEmployee.getEmployeeId()),
                savedEmployee.getFirstName() + " " + savedEmployee.getLastName(),
                null,
                savedEmployee.toString(), // Consider using a more concise representation
                "新增員工使用者: " + savedEmployee.getUsername(),
                null, // IP address, can be retrieved from request context
                null, // User agent, can be retrieved from request context
                "PERSONNEL"
        );

        return convertToDto(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeUserDto updateEmployeeUser(Integer id, EmployeeUserUpdateRequest request) {
        logger.info("請求更新員工使用者，ID: {}", id);
        return employeeRepository.findById(id)
                .map(existingEmployee -> {
                    String oldEmployeeData = existingEmployee.toString(); // Capture old data for logging

                    // 檢查使用者名稱是否被修改且已存在
                    if (!existingEmployee.getUsername().equals(request.getUsername()) &&
                            employeeRepository.findByUsername(request.getUsername()).isPresent()) {
                        throw new DuplicateUsernameException("使用者名稱已存在: " + request.getUsername());
                    }
                    // 檢查員工編號是否被修改且已存在
                    if (!existingEmployee.getEmployeeNumber().equals(request.getEmployeeNumber()) &&
                            employeeRepository.findByEmployeeNumber(request.getEmployeeNumber()).isPresent()) {
                        throw new DuplicateEmployeeNumberException("員工編號已存在: " + request.getEmployeeNumber());
                    }
                    // 檢查電子郵件是否被修改且已存在
                    if (request.getEmail() != null && !request.getEmail().equals(existingEmployee.getEmail()) &&
                            employeeRepository.findByEmail(request.getEmail()).isPresent()) {
                        throw new DuplicateEmailException("電子郵件已存在: " + request.getEmail());
                    }

                    existingEmployee.setFirstName(request.getFirstName());
                    existingEmployee.setLastName(request.getLastName());
                    existingEmployee.setEmployeeNumber(request.getEmployeeNumber());
                    existingEmployee.setEmployeeType(request.getEmployeeType());
                    existingEmployee.setBirthDate(request.getBirthDate());
                    existingEmployee.setEmail(request.getEmail());
                    existingEmployee.setPhone(request.getPhone());
                    existingEmployee.setPhotoPath(request.getPhotoPath());
                    existingEmployee.setDepartmentId(request.getEmployeeDepartmentId());
                    existingEmployee.setPositionId(request.getEmployeePositionId());
                    existingEmployee.setManagerId(request.getManagerEmployeeUserId());
                    existingEmployee.setHireDate(request.getHireDate());
                    existingEmployee.setTerminationDate(request.getTerminationDate());
                    existingEmployee.setUsername(request.getUsername());

                    if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                        existingEmployee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                    }
                    existingEmployee.setIsActive(request.getIsActive());

                    Employee updatedEmployee = employeeRepository.save(existingEmployee);
                    logger.info("EmployeeUserServiceImpl - 保存到資料庫後 updatedEmployee.getIsActive(): {}", updatedEmployee.getIsActive());

                    systemLogService.log(
                            "UPDATE",
                            "更新員工使用者",
                            updatedEmployee.getEmployeeId(),
                            updatedEmployee.getUsername(),
                            "EMPLOYEE",
                            String.valueOf(updatedEmployee.getEmployeeId()),
                            updatedEmployee.getFirstName() + " " + updatedEmployee.getLastName(),
                            oldEmployeeData,
                            updatedEmployee.toString(), // Consider using a more concise representation
                            "更新員工使用者: " + updatedEmployee.getUsername(),
                            null, // IP address, can be retrieved from request context
                            null, // User agent, can be retrieved from request context
                            "PERSONNEL"
                    );

                    return convertToDto(updatedEmployee);
                })
                .orElseThrow(() -> new RuntimeException("找不到員工使用者ID: " + id));
    }

    @Override
    @Transactional
    public void deleteEmployeeUserById(Integer id) {
        logger.info("根據ID刪除員工使用者: {}", id);
        employeeRepository.findById(id).ifPresent(employee -> {
            employeeRepository.deleteById(id);
            systemLogService.log(
                    "DELETE",
                    "刪除員工使用者",
                    employee.getEmployeeId(),
                    employee.getUsername(),
                    "EMPLOYEE",
                    String.valueOf(employee.getEmployeeId()),
                    employee.getFirstName() + " " + employee.getLastName(),
                    employee.toString(), // Capture old data before deletion
                    null,
                    "刪除員工使用者: " + employee.getUsername(),
                    null, // IP address, can be retrieved from request context
                    null, // User agent, can be retrieved from request context
                    "PERSONNEL"
            );
        });
    }

    private EmployeeUserDto convertToDto(Employee employee) {
        EmployeeUserDto dto = new EmployeeUserDto();
        dto.setEmployeeUserId(employee.getEmployeeId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmployeeNumber(employee.getEmployeeNumber());
        dto.setEmployeeType(employee.getEmployeeType());
        dto.setBirthDate(employee.getBirthDate());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setPhotoPath(employee.getPhotoPath());
        dto.setEmployeeDepartmentId(employee.getDepartmentId());
        dto.setEmployeePositionId(employee.getPositionId());
        dto.setManagerEmployeeUserId(employee.getManagerId());
        dto.setHireDate(employee.getHireDate());
        dto.setTerminationDate(employee.getTerminationDate());
        dto.setUsername(employee.getUsername());
        // Password hash should not be exposed in DTO for security reasons
        // dto.setPasswordHash(employee.getPasswordHash());
        dto.setActive(employee.getIsActive() != null ? employee.getIsActive() : false); // Ensure boolean value
        dto.setLastLogin(employee.getLastLogin());
        dto.setCreatedAt(employee.getCreatedAt());
        dto.setUpdatedAt(employee.getUpdatedAt());
        return dto;
    }
    
    // ===== 新增：角色管理功能開始 =====
    @Override
    public List<String> getEmployeeRoles(Long employeeId) {
        logger.info("查詢員工角色，員工ID: {}", employeeId);
        
        // 轉換 Long 為 Integer (因為資料庫欄位是 Integer)
        Integer empId = employeeId.intValue();
        
        List<UserRole> userRoles = userRoleRepository.findByEmployeeId(empId);
        List<Integer> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        
        if (roleIds.isEmpty()) {
            logger.info("員工 {} 沒有任何角色", employeeId);
            return new ArrayList<>();
        }
        
        List<Role> roles = roleRepository.findAllById(roleIds);
        List<String> roleNames = roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
        
        logger.info("員工 {} 的角色: {}", employeeId, roleNames);
        return roleNames;
    }
    
    @Override
    @Transactional
    public void updateEmployeeRoles(Long employeeId, List<String> roleNames, String updatedBy) {
        logger.info("更新員工角色，員工ID: {}, 新角色: {}, 更新者: {}", employeeId, roleNames, updatedBy);
        
        // 轉換 Long 為 Integer
        Integer empId = employeeId.intValue();
        
        // 檢查員工是否存在
        if (!employeeRepository.existsById(empId)) {
            throw new RuntimeException("找不到員工ID: " + employeeId);
        }
        
        try {
            // 1. 刪除員工所有現有角色
            userRoleRepository.deleteByEmployeeId(empId);
            logger.info("已刪除員工 {} 的所有現有角色", employeeId);
            
            // 2. 如果有新角色，則新增
            if (roleNames != null && !roleNames.isEmpty()) {
                List<Role> roles = roleRepository.findByRoleNameIn(roleNames);
                
                // 檢查是否所有角色都存在
                if (roles.size() != roleNames.size()) {
                    List<String> foundRoleNames = roles.stream()
                            .map(Role::getRoleName)
                            .collect(Collectors.toList());
                    List<String> missingRoles = roleNames.stream()
                            .filter(name -> !foundRoleNames.contains(name))
                            .collect(Collectors.toList());
                    throw new RuntimeException("找不到角色: " + missingRoles);
                }
                
                // 新增新角色
                for (Role role : roles) {
                    UserRole userRole = new UserRole();
                    userRole.setEmployeeId(empId);
                    userRole.setRoleId(role.getRoleId());
                    userRole.setAssignedBy(updatedBy);
                    userRoleRepository.save(userRole);
                }
                
                logger.info("已為員工 {} 新增 {} 個角色", employeeId, roles.size());
            }
            
            // 記錄系統日誌
            systemLogService.log(
                    "UPDATE",
                    "更新員工角色",
                    empId,
                    updatedBy,
                    "EMPLOYEE_ROLE",
                    String.valueOf(empId),
                    "Employee Role Update",
                    null,
                    "角色: " + String.join(", ", roleNames),
                    "更新員工 " + employeeId + " 的角色",
                    null,
                    null,
                    "PERSONNEL"
            );
            
        } catch (Exception e) {
            logger.error("更新員工 {} 角色失敗: {}", employeeId, e.getMessage(), e);
            throw new RuntimeException("更新員工角色失敗: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<RoleDto> getAvailableRoles() {
        logger.info("查詢所有可用角色");
        
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::convertRoleToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 將 Role 實體轉換為 RoleDto
     * @param role 角色實體
     * @return 角色DTO
     */
    private RoleDto convertRoleToDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setRoleId(role.getRoleId());
        dto.setRoleName(role.getRoleName());
        dto.setDisplayName(role.getRoleName()); // 使用 roleName 作為 displayName
        dto.setDescription(role.getDescription());
        dto.setIsActive(true); // Role 實體沒有 isActive 欄位，預設為 true
        return dto;
    }
    
    /**
     * 為新建員工指派角色
     */
    private void assignRolesToEmployee(Integer employeeId, EmployeeUserCreateRequest request, String assignedBy) {
        logger.info("開始為員工 {} 指派角色", employeeId);
        
        // 指派角色
        if (request.getRoleNames() != null && !request.getRoleNames().isEmpty()) {
            assignRolesToEmployee(employeeId, request.getRoleNames(), assignedBy);
        }
        
        logger.info("完成為員工 {} 指派角色", employeeId);
    }
    
    /**
     * 為員工指派角色
     */
    private void assignRolesToEmployee(Integer employeeId, List<String> roleNames, String assignedBy) {
        logger.info("為員工 {} 指派角色: {}", employeeId, roleNames);
        
        List<Role> roles = roleRepository.findByRoleNameIn(roleNames);
        
        // 檢查是否所有角色都存在
        if (roles.size() != roleNames.size()) {
            List<String> foundRoleNames = roles.stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.toList());
            List<String> missingRoles = roleNames.stream()
                    .filter(name -> !foundRoleNames.contains(name))
                    .collect(Collectors.toList());
            throw new RuntimeException("找不到角色: " + missingRoles);
        }
        
        // 指派角色
        for (Role role : roles) {
            UserRole userRole = new UserRole();
            userRole.setEmployeeId(employeeId);
            userRole.setRoleId(role.getRoleId());
            userRole.setAssignedBy(assignedBy);
            userRoleRepository.save(userRole);
        }
        
        logger.info("成功為員工 {} 指派 {} 個角色", employeeId, roles.size());
    }
    // ===== 新增：角色管理功能結束 =====
}
