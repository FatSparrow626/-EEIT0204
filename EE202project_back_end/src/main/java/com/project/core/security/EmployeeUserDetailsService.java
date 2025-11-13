package com.project.core.security;

import com.project.employeeuser.dao.EmployeeUserDAO;
import com.project.employeeuser.model.EmployeeUser;
import com.project.core.dao.RolePermissionRepository;
import com.project.core.dao.UserRoleRepository;
import com.project.core.model.RolePermission;
import com.project.core.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmployeeUserDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeUserDAO employeeUserDAO;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EmployeeUser employeeUser = employeeUserDAO.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("找不到此用戶名稱: " + username));

        Integer employeeId = employeeUser.getEmployeeUserId().intValue();

        // 1. 先準備 GrantedAuthority 列表 (Spring Security 授權只認得這個GrantedAuthority介面)
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 2. 查角色權限 -> 以權限授權 (支援以權限讓Spring授權)
        Set<String> rolePermissions = loadRolePermissions(employeeId);

        for (String permission : rolePermissions) {
            GrantedAuthority authority = new SimpleGrantedAuthority(permission);
            authorities.add(authority);
        }

        // 3. 查用戶角色 -> 以角色授權 (支援以角色讓Spring授權-Spring預設) (保持向後相容性)
        List<UserRole> userRoles = userRoleRepository.findByEmployeeId(employeeId);

        for (UserRole userRole : userRoles) {
            String role = userRole.getRoleId().toString();
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
            authorities.add(authority);
        }

        // 傳回自訂的UserDetails實作 (loadUserByUsername的回傳型態由UserDetails介面定義)
        return new EmployeeUserDetails(employeeUser, authorities);
    }

    /**
     * 載入員工的角色權限: 員工 -> 角色 -> 權限
     */
    private Set<String> loadRolePermissions(Integer employeeId) {
        // 1. 查員工角色
        List<UserRole> userRoles = userRoleRepository.findByEmployeeId(employeeId);

        // 2. 查角色權限並彙總
        Set<String> rolePermissions = new HashSet<>();

        // 3. 迭代每個角色以獲取其權限
        for (UserRole userRole : userRoles) {
            List<RolePermission> permissions = rolePermissionRepository.findByRoleId(userRole.getRoleId());

            for (RolePermission permission : permissions) {
                rolePermissions.add(permission.getPermissionCode());
            }
        }
        // 4. 回傳去重後的權限
        return rolePermissions;
    }

}
