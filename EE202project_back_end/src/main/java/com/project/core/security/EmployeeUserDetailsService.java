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
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Integer employeeId = employeeUser.getEmployeeUserId().intValue();
        
        // 1. 載入角色權限
        Set<String> rolePermissions = loadRolePermissions(employeeId);
        
        // 2. 轉換為 GrantedAuthority
        List<GrantedAuthority> authorities = rolePermissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 3. 添加角色作為權限 (保持向後相容性)
        List<UserRole> userRoles = userRoleRepository.findByEmployeeId(employeeId);
        userRoles.forEach(userRole -> 
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.getRoleId())));

        return new EmployeeUserDetails(employeeUser, authorities);
    }

    /**
     * 載入員工的角色權限
     */
    private Set<String> loadRolePermissions(Integer employeeId) {
        List<UserRole> userRoles = userRoleRepository.findByEmployeeId(employeeId);
        
        return userRoles.stream()
                .flatMap(userRole -> {
                    List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(userRole.getRoleId());
                    return rolePermissions.stream()
                            .map(RolePermission::getPermissionCode);
                })
                .collect(Collectors.toSet());
    }

}
