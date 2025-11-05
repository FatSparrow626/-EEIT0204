package com.project.core.security;

import com.project.employeeuser.model.EmployeeUser;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class EmployeeUserDetails implements UserDetails {
    private EmployeeUser employeeUser;
    private List<GrantedAuthority> authorities;

    public EmployeeUserDetails(EmployeeUser employeeUser, List<GrantedAuthority> authorities) {
        this.employeeUser = employeeUser;
        this.authorities = authorities;
    }

    public EmployeeUser getEmployeeUser() {
        return employeeUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return employeeUser.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return employeeUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return employeeUser.getIsActive();
    }
}
