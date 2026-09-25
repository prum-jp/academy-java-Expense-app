package com.example.expenseapp.security;

import com.example.expenseapp.entity.Department;
import com.example.expenseapp.entity.Role;
import com.example.expenseapp.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginUserTest {

    @Test
    void exposesRoleAndOrganization() {
        User user = new User();
        user.setUserId(1);
        user.setUserName("yamada");
        user.setPassword("secret");
        user.setEmail("yamada@example.com");
        user.setDeptId(1);
        user.setPositionId(1);
        user.setRole(Role.USER);

        Department department = new Department();
        department.setDeptName("営業部");
        user.setDepartment(department);

        LoginUser loginUser = new LoginUser(user);

        assertThat(loginUser.getUserId()).isEqualTo(1);
        assertThat(loginUser.getUsername()).isEqualTo("yamada");
        assertThat(loginUser.getDeptName()).isEqualTo("営業部");
        assertThat(loginUser.isApprover()).isFalse();
        assertThat(loginUser.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void eraseCredentialsClearsPassword() {
        User user = new User();
        user.setUserId(2);
        user.setUserName("sato");
        user.setPassword("secret");
        user.setRole(Role.APPROVER);

        LoginUser loginUser = new LoginUser(user);
        assertThat(loginUser.getPassword()).isEqualTo("secret");

        loginUser.eraseCredentials();
        assertThat(loginUser.getPassword()).isNull();
        assertThat(loginUser.isApprover()).isTrue();
    }
}
