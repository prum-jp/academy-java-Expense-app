package com.example.expenseapp.security;

import com.example.expenseapp.entity.Role;
import com.example.expenseapp.entity.User;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class LoginUser implements UserDetails, CredentialsContainer {

    private final Integer userId;
    private final String username;
    private String password;
    private final String email;
    private final Integer deptId;
    private final Integer positionId;
    private final Role role;
    private final String deptName;
    private final String positionName;

    public LoginUser(User user) {
        Objects.requireNonNull(user, "user");
        this.userId = user.getUserId();
        this.username = user.getUserName();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.deptId = user.getDeptId();
        this.positionId = user.getPositionId();
        this.role = user.getRole();
        this.deptName = user.getDepartment() != null ? user.getDepartment().getDeptName() : null;
        this.positionName = user.getPosition() != null ? user.getPosition().getPositionName() : null;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public Integer getPositionId() {
        return positionId;
    }

    public Role getRole() {
        return role;
    }

    public String getDeptName() {
        return deptName;
    }

    public String getPositionName() {
        return positionName;
    }

    public boolean isApprover() {
        return role == Role.APPROVER;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.authority()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
        return true;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
