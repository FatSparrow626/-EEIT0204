package com.project.core.config;

import com.project.core.dao.*;
import com.project.core.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Create Departments
        Department warehouseDept = createDepartment("Warehouse", "倉庫管理部");
        Department productionDept = createDepartment("Production", "工單管理部");
        Department procurementDept = createDepartment("Procurement", "採購管理部");
        Department equipmentDept = createDepartment("Equipment", "機台管理部");
        Department personnelDept = createDepartment("Personnel", "人員管理部");
        Department leaveDept = createDepartment("Leave", "請假管理部");

        // Create Positions
        Position managerPos = createPosition("Manager", "Manages a team");
        Position staffPos = createPosition("Staff", "General staff member");
        Position adminPos = createPosition("System Administrator", "Manages the system");

        // Create Permissions
        Permission employeeView = createPermission("EMPLOYEE_VIEW", "View Employee Data", "PERSONNEL");
        Permission employeeManage = createPermission("EMPLOYEE_MANAGE", "Manage Employee Data", "PERSONNEL");
        Permission inventoryView = createPermission("INVENTORY_VIEW", "View Inventory Data", "INVENTORY");
        Permission inventoryManage = createPermission("INVENTORY_MANAGE", "Manage Inventory Data", "INVENTORY");
        Permission inventoryHistoryView = createPermission("INVENTORY_HISTORY_VIEW", "View Inventory History",
                "INVENTORY");
        Permission inboundView = createPermission("INBOUND_VIEW", "View Inbound Orders", "INVENTORY");
        Permission inboundManage = createPermission("INBOUND_MANAGE", "Manage Inbound Orders", "INVENTORY");
        Permission systemLogView = createPermission("SYSTEM_LOG_VIEW", "View System Logs", "SYSTEM");
        Permission systemLogManage = createPermission("SYSTEM_LOG_MANAGE", "Manage System Logs", "SYSTEM");
        Permission supplierView = createPermission("SUPPLIER_VIEW", "View Suppliers", "SUPPLIER");
        Permission supplierManage = createPermission("SUPPLIER_MANAGE", "Manage Suppliers", "SUPPLIER");
        Permission reportView = createPermission("REPORT_VIEW", "View Reports", "SYSTEM");
        Permission workorderView = createPermission("WORKORDER_VIEW", "View Work Orders", "WORKORDER");
        Permission workorderManage = createPermission("WORKORDER_MANAGE", "Manage Work Orders", "WORKORDER");
        Permission bomView = createPermission("BOM_VIEW", "View BOM", "BOM");
        Permission bomManage = createPermission("BOM_MANAGE", "Manage BOM", "BOM");

        // 機台查看、管理
        Permission machineView = createPermission("MACHINE_VIEW", "View Machine Data", "MACHINE");
        Permission machineManage = createPermission("MACHINE_MANAGE", "Manage Machine Data", "MACHINE");
        // 檔案查看、管理
        Permission machineFileView = createPermission("MACHINE_FILE_VIEW", "View Machine Files", "MACHINE");
        Permission machineFileManage = createPermission("MACHINE_FILE_MANAGE", "Manage Machine Files", "MACHINE");

        // 機台維修查看、申請、管理
        Permission repairView = createPermission("REPAIR_VIEW", "View Repair Records", "MAINTENANCE");
        Permission repairCreate = createPermission("REPAIR_CREATE", "Create Repair Requests", "MAINTENANCE");
        Permission repairManage = createPermission("REPAIR_MANAGE", "Manage Repair Operations", "MAINTENANCE");

        // 機台保養查詢、管理
        Permission maintenanceView = createPermission("MAINTENANCE_VIEW", "View Maintenance Records", "MAINTENANCE");
        Permission maintenanceManage = createPermission("MAINTENANCE_MANAGE", "Manage Maintenance Operations",
                "MAINTENANCE");

        // 物料管理
        Permission materialView = createPermission("MATERIAL_VIEW", "View Material Data", "MATERIAL");
        Permission materialManage = createPermission("MATERIAL_MANAGE", "Manage Material Data", "MATERIAL");

        // 領料單管理
        Permission pickingOrderView = createPermission("PICKING_ORDER_VIEW", "View Picking Orders", "PICKING");
        Permission pickingOrderManage = createPermission("PICKING_ORDER_MANAGE", "Manage Picking Orders", "PICKING");
        Permission pickingOrderApprove = createPermission("PICKING_ORDER_APPROVE", "Approve Picking Orders", "PICKING");

        // 請假管理
        Permission leaveApplySelf = createPermission("LEAVE_APPLY_SELF", "Apply for own leave", "LEAVE");
        Permission leaveViewSelf = createPermission("LEAVE_VIEW_SELF", "View own leave records", "LEAVE");
        Permission leaveEditSelf = createPermission("LEAVE_EDIT_SELF", "Edit own leave applications", "LEAVE");
        Permission leaveDeleteSelf = createPermission("LEAVE_DELETE_SELF", "Delete own leave applications", "LEAVE");
        Permission leaveViewDepartment = createPermission("LEAVE_VIEW_DEPARTMENT", "View department leave records",
                "LEAVE");
        Permission leaveApprove = createPermission("LEAVE_APPROVE", "Approve leave applications", "LEAVE");
        Permission leaveManageAll = createPermission("LEAVE_MANAGE_ALL", "Manage all leave records", "LEAVE");
        // 行銷管理 (展示用)
        Permission marketingCampaignView = createPermission("MARKETING_CAMPAIGN_VIEW", "View Marketing Campaigns",
                "MARKETING");
        Permission marketingCampaignManage = createPermission("MARKETING_CAMPAIGN_MANAGE", "Manage Marketing Campaigns",
                "MARKETING");
        Permission marketingLeadsView = createPermission("MARKETING_LEADS_VIEW", "View Marketing Leads", "MARKETING");
        Permission marketingReportView = createPermission("MARKETING_REPORT_VIEW", "View Marketing Reports",
                "MARKETING");
        // Create Roles
        Role adminRole = createRole("ADMIN", "System Administrator");
        Role warehouseRole = createRole("WAREHOUSE_STAFF", "Warehouse Staff");
        Role warehouseManagerRole = createRole("WAREHOUSE_MANAGER", "Warehouse Manager");
        Role workOrderManager = createRole("WORKORDER_MANAGER",
                "Responsible for managing work orders and material requisitions, with access to all requisition records.");
        Role purchaseManager = createRole("PURCHASE_MANAGER", "Purchase Manager");
        Role employeeAccountManager = createRole("EMPLOYEE_ACCOUNT_MANAGER", "Employee Account Manager");
        Role machineManager = createRole("MACHINE_MANAGER", "Machine Manager");
        Role machineEmployee = createRole("MACHINE_EMPLOYEE", "Machine Employee");

        // 請假管理角色
        Role leaveManagerRole = createRole("LEAVE_MANAGER", "Supervisor with approval rights for leave");
        Role leaveEmployeeRole = createRole("LEAVE_EMPLOYEE", "Regular Employee for Leave");
        Role superLeaveManagerRole = createRole("SUPER_LEAVE_MANAGER", "Supervises and manages all leave records");

        // 行銷管理角色 (展示用)
        Role marketingStaffRole = createRole("MARKETING_STAFF", "Marketing Staff");
        Role marketingManagerRole = createRole("MARKETING_MANAGER", "Marketing Manager");

        // Assign Permissions to Roles
        assignPermissionToRole(adminRole, employeeView);
        assignPermissionToRole(adminRole, employeeManage);
        assignPermissionToRole(adminRole, inventoryView);
        assignPermissionToRole(adminRole, inventoryManage);
        assignPermissionToRole(adminRole, inventoryHistoryView);
        assignPermissionToRole(adminRole, inboundView);
        assignPermissionToRole(adminRole, inboundManage);
        assignPermissionToRole(adminRole, systemLogView);
        assignPermissionToRole(adminRole, systemLogManage);
        assignPermissionToRole(adminRole, supplierView);
        assignPermissionToRole(adminRole, supplierManage);
        assignPermissionToRole(adminRole, reportView);
        assignPermissionToRole(adminRole, workorderView);
        assignPermissionToRole(adminRole, workorderManage);
        assignPermissionToRole(adminRole, bomView);
        assignPermissionToRole(adminRole, bomManage);
        assignPermissionToRole(adminRole, machineView);
        assignPermissionToRole(adminRole, machineManage);
        assignPermissionToRole(adminRole, machineFileView);
        assignPermissionToRole(adminRole, machineFileManage);
        assignPermissionToRole(adminRole, repairView);
        assignPermissionToRole(adminRole, repairCreate);
        assignPermissionToRole(adminRole, repairManage);
        assignPermissionToRole(adminRole, maintenanceView);
        assignPermissionToRole(adminRole, maintenanceManage);
        assignPermissionToRole(adminRole, materialView);
        assignPermissionToRole(adminRole, materialManage);
        assignPermissionToRole(adminRole, pickingOrderView);
        assignPermissionToRole(adminRole, pickingOrderManage);
        assignPermissionToRole(adminRole, pickingOrderApprove);

        assignPermissionToRole(warehouseRole, inventoryView);
        assignPermissionToRole(warehouseRole, inventoryManage);

        assignPermissionToRole(warehouseManagerRole, employeeView);
        assignPermissionToRole(warehouseManagerRole, inventoryView);
        assignPermissionToRole(warehouseManagerRole, inventoryManage);
        assignPermissionToRole(warehouseManagerRole, inventoryHistoryView);
        assignPermissionToRole(warehouseManagerRole, inboundView);
        assignPermissionToRole(warehouseManagerRole, inboundManage);

        assignPermissionToRole(workOrderManager, workorderView);
        assignPermissionToRole(workOrderManager, workorderManage);
        assignPermissionToRole(workOrderManager, bomView);
        assignPermissionToRole(workOrderManager, bomManage);

        assignPermissionToRole(purchaseManager, supplierView);
        assignPermissionToRole(purchaseManager, supplierManage);
        assignPermissionToRole(purchaseManager, reportView);

        assignPermissionToRole(employeeAccountManager, employeeView);
        assignPermissionToRole(employeeAccountManager, employeeManage);

        // 指派 LEAVE to adminRole
        assignPermissionToRole(adminRole, leaveApplySelf);
        assignPermissionToRole(adminRole, leaveViewSelf);
        assignPermissionToRole(adminRole, leaveViewDepartment);
        assignPermissionToRole(adminRole, leaveApprove);
        assignPermissionToRole(adminRole, leaveManageAll);

        // 機台主管(後台)
        assignPermissionToRole(machineManager, machineView);
        assignPermissionToRole(machineManager, machineManage);
        assignPermissionToRole(machineManager, machineFileView);
        assignPermissionToRole(machineManager, machineFileManage);
        assignPermissionToRole(machineManager, repairView);
        assignPermissionToRole(machineManager, repairCreate);
        assignPermissionToRole(machineManager, repairManage);
        assignPermissionToRole(machineManager, maintenanceView);
        assignPermissionToRole(machineManager, maintenanceManage);

        // 機台員工(前台)
        assignPermissionToRole(machineEmployee, machineView);
        // assignPermissionToRole(machineEmployee, machineManage);
        assignPermissionToRole(machineEmployee, machineFileView);
        // assignPermissionToRole(machineEmployee, machineFileManage);
        assignPermissionToRole(machineEmployee, repairView);
        assignPermissionToRole(machineEmployee, repairCreate);
        // assignPermissionToRole(machineEmployee, repairManage);
        assignPermissionToRole(machineEmployee, maintenanceView);
        // assignPermissionToRole(machineEmployee, maintenanceManage);

        // 請假管理
        // Assign permissions for LEAVE_EMPLOYEE role
        assignPermissionToRole(leaveEmployeeRole, leaveApplySelf);
        assignPermissionToRole(leaveEmployeeRole, leaveViewSelf);
        assignPermissionToRole(leaveEmployeeRole, leaveEditSelf);
        assignPermissionToRole(leaveEmployeeRole, leaveDeleteSelf);

        // Assign permissions for LEAVE_MANAGER role
        assignPermissionToRole(leaveManagerRole, leaveApplySelf);
        assignPermissionToRole(leaveManagerRole, leaveViewSelf);
        assignPermissionToRole(leaveManagerRole, leaveViewDepartment);
        assignPermissionToRole(leaveManagerRole, leaveApprove);
        assignPermissionToRole(leaveManagerRole, leaveEditSelf);
        assignPermissionToRole(leaveManagerRole, leaveDeleteSelf); // [FIX] Add delete self permission

        // Assign permissions for SUPER_LEAVE_MANAGER role
        // Inherits all LEAVE_MANAGER permissions and adds LEAVE_MANAGE_ALL
        assignPermissionToRole(superLeaveManagerRole, leaveApplySelf);
        assignPermissionToRole(superLeaveManagerRole, leaveViewSelf);
        assignPermissionToRole(superLeaveManagerRole, leaveViewDepartment);
        assignPermissionToRole(superLeaveManagerRole, leaveApprove);
        assignPermissionToRole(superLeaveManagerRole, leaveEditSelf);
        assignPermissionToRole(superLeaveManagerRole, leaveDeleteSelf);
        assignPermissionToRole(superLeaveManagerRole, leaveManageAll); // The super power

        // 行銷管理角色權限 (展示用)
        assignPermissionToRole(marketingStaffRole, marketingCampaignView);
        assignPermissionToRole(marketingStaffRole, marketingLeadsView);
        assignPermissionToRole(marketingManagerRole, marketingCampaignManage);
        assignPermissionToRole(marketingManagerRole, marketingLeadsView);
        assignPermissionToRole(marketingManagerRole, marketingReportView);

        // Create a test employee (admin)
        if (employeeRepository.findByUsername("admin").isEmpty()) {
            Employee admin = new Employee();
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setEmployeeNumber("EMP001");
            admin.setEmployeeType("SYSTEM_ADMIN");
            admin.setBirthDate(LocalDate.of(1990, 1, 1));
            admin.setEmail("admin@example.com");
            admin.setPhone("123-456-7890");
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("password")); // Encrypt password
            admin.setIsActive(true);
            admin.setDepartmentId(personnelDept.getDepartmentId());
            admin.setPositionId(adminPos.getPositionId());
            employeeRepository.save(admin);

            assignRoleToUser(admin, adminRole);
        }

        // Create a test employee (warehouse staff)
        if (employeeRepository.findByUsername("warehouse").isEmpty()) {
            Employee warehouseStaff = new Employee();
            warehouseStaff.setFirstName("Warehouse");
            warehouseStaff.setLastName("Staff");
            warehouseStaff.setEmployeeNumber("EMP002");
            warehouseStaff.setEmployeeType("INTERNAL");
            warehouseStaff.setBirthDate(LocalDate.of(1995, 5, 10));
            warehouseStaff.setEmail("warehouse@example.com");
            warehouseStaff.setPhone("098-765-4321");
            warehouseStaff.setUsername("warehouse");
            warehouseStaff.setPasswordHash(passwordEncoder.encode("password")); // Encrypt password
            warehouseStaff.setIsActive(true);
            warehouseStaff.setDepartmentId(warehouseDept.getDepartmentId());
            warehouseStaff.setPositionId(staffPos.getPositionId());
            employeeRepository.save(warehouseStaff);

            assignRoleToUser(warehouseStaff, warehouseRole);
        }

        // Create a test employee (warehouse manager)
        if (employeeRepository.findByUsername("warehouse_manager").isEmpty()) {
            Employee warehouseManager = new Employee();
            warehouseManager.setFirstName("Warehouse");
            warehouseManager.setLastName("Manager");
            warehouseManager.setEmployeeNumber("EMP003");
            warehouseManager.setEmployeeType("INTERNAL");
            warehouseManager.setBirthDate(LocalDate.of(1985, 3, 15));
            warehouseManager.setEmail("warehouse_manager@example.com");
            warehouseManager.setPhone("555-123-4567");
            warehouseManager.setUsername("warehouse_manager");
            warehouseManager.setPasswordHash(passwordEncoder.encode("password"));
            warehouseManager.setIsActive(true);
            warehouseManager.setDepartmentId(warehouseDept.getDepartmentId());
            warehouseManager.setPositionId(managerPos.getPositionId());
            employeeRepository.save(warehouseManager);

            assignRoleToUser(warehouseManager, warehouseManagerRole);
        }

        // Create a test employee (workorder manager)
        if (employeeRepository.findByUsername("workorder_manager").isEmpty()) {
            Employee workorderManager = new Employee();
            workorderManager.setFirstName("Workorder");
            workorderManager.setLastName("Manager");
            workorderManager.setEmployeeNumber("EMP004");
            workorderManager.setEmployeeType("INTERNAL");
            workorderManager.setBirthDate(LocalDate.of(1988, 7, 20));
            workorderManager.setEmail("workorder_manager@example.com");
            workorderManager.setPhone("555-987-6543");
            workorderManager.setUsername("workorder_manager");
            workorderManager.setPasswordHash(passwordEncoder.encode("password"));
            workorderManager.setIsActive(true);
            workorderManager.setDepartmentId(warehouseDept.getDepartmentId());
            workorderManager.setPositionId(managerPos.getPositionId());
            employeeRepository.save(workorderManager);

            assignRoleToUser(workorderManager, workOrderManager);
        }

        // Create a test employee (purchase manager)
        if (employeeRepository.findByUsername("purchase_manager").isEmpty()) {
            Employee purchaseManagerUser = new Employee();
            purchaseManagerUser.setFirstName("Purchase");
            purchaseManagerUser.setLastName("Manager");
            purchaseManagerUser.setEmployeeNumber("EMP005");
            purchaseManagerUser.setEmployeeType("INTERNAL");
            purchaseManagerUser.setBirthDate(LocalDate.of(1990, 9, 10));
            purchaseManagerUser.setEmail("purchase_manager@example.com");
            purchaseManagerUser.setPhone("555-456-7890");
            purchaseManagerUser.setUsername("purchase_manager");
            purchaseManagerUser.setPasswordHash(passwordEncoder.encode("password"));
            purchaseManagerUser.setIsActive(true);
            purchaseManagerUser.setDepartmentId(warehouseDept.getDepartmentId());
            purchaseManagerUser.setPositionId(managerPos.getPositionId());
            employeeRepository.save(purchaseManagerUser);

            assignRoleToUser(purchaseManagerUser, purchaseManager);
        }

        // Create a test employee (account manager)
        if (employeeRepository.findByUsername("account_manager").isEmpty()) {
            Employee accountManagerUser = new Employee();
            accountManagerUser.setFirstName("Account");
            accountManagerUser.setLastName("Manager");
            accountManagerUser.setEmployeeNumber("EMP006");
            accountManagerUser.setEmployeeType("INTERNAL");
            accountManagerUser.setBirthDate(LocalDate.of(1987, 11, 25));
            accountManagerUser.setEmail("account_manager@example.com");
            accountManagerUser.setPhone("555-111-2233");
            accountManagerUser.setUsername("account_manager");
            accountManagerUser.setPasswordHash(passwordEncoder.encode("password"));
            accountManagerUser.setIsActive(true);
            accountManagerUser.setDepartmentId(personnelDept.getDepartmentId());
            accountManagerUser.setPositionId(managerPos.getPositionId());
            employeeRepository.save(accountManagerUser);

            assignRoleToUser(accountManagerUser, employeeAccountManager);
        }

        // Create a test employee (machine manager)
        if (employeeRepository.findByUsername("machine_manager").isEmpty()) {
            Employee machineManagerUser = new Employee();
            machineManagerUser.setFirstName("Machine");
            machineManagerUser.setLastName("Manager");
            machineManagerUser.setEmployeeNumber("EMP007");
            machineManagerUser.setEmployeeType("INTERNAL");
            machineManagerUser.setBirthDate(LocalDate.of(1992, 6, 15));
            machineManagerUser.setEmail("machine_manager@example.com");
            machineManagerUser.setPhone("555-777-8888");
            machineManagerUser.setUsername("machine_manager");
            machineManagerUser.setPasswordHash(passwordEncoder.encode("password"));
            machineManagerUser.setIsActive(true);
            machineManagerUser.setDepartmentId(equipmentDept.getDepartmentId());
            machineManagerUser.setPositionId(managerPos.getPositionId());
            employeeRepository.save(machineManagerUser);

            assignRoleToUser(machineManagerUser, machineManager);
        }

    }

    private Department createDepartment(String name, String description) {
        return departmentRepository.findByDepartmentName(name)
                .orElseGet(() -> {
                    Department dept = new Department();
                    dept.setDepartmentName(name);
                    dept.setDescription(description);
                    return departmentRepository.save(dept);
                });
    }

    private Position createPosition(String name, String description) {
        return positionRepository.findByPositionName(name)
                .orElseGet(() -> {
                    Position pos = new Position();
                    pos.setPositionName(name);
                    pos.setDescription(description);
                    return positionRepository.save(pos);
                });
    }

    private Permission createPermission(String code, String name, String module) {
        return permissionRepository.findByPermissionCode(code)
                .orElseGet(() -> {
                    Permission perm = new Permission();
                    perm.setPermissionCode(code);
                    perm.setPermissionName(name);
                    perm.setModule(module);
                    return permissionRepository.save(perm);
                });
    }

    private Role createRole(String name, String description) {
        return roleRepository.findByRoleName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName(name);
                    role.setDescription(description);
                    return roleRepository.save(role);
                });
    }

    private void assignPermissionToRole(Role role, Permission permission) {
        if (rolePermissionRepository.findByRoleIdAndPermissionCode(role.getRoleId(), permission.getPermissionCode())
                .isEmpty()) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(role.getRoleId());
            rp.setPermissionCode(permission.getPermissionCode());
            rolePermissionRepository.save(rp);
        }
    }

    private void assignRoleToUser(Employee employee, Role role) {
        if (userRoleRepository.findByEmployeeIdAndRoleId(employee.getEmployeeId(), role.getRoleId()).isEmpty()) {
            UserRole ur = new UserRole();
            ur.setEmployeeId(employee.getEmployeeId());
            ur.setRoleId(role.getRoleId());
            userRoleRepository.save(ur);
        }
    }
}
