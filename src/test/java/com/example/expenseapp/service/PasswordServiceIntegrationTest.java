package com.example.expenseapp.service;

import com.example.expenseapp.audit.AuditLogService;
import com.example.expenseapp.exception.InvalidPasswordException;
import com.example.expenseapp.mapper.UserMapper;
import com.example.expenseapp.security.LoginUser;
import com.example.expenseapp.support.AbstractMySqlIntegrationTest;
import com.example.expenseapp.support.AuditLogAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordServiceIntegrationTest extends AbstractMySqlIntegrationTest {

    private static final String DEFAULT_PASSWORD = "password";
    private static final String TEMP_PASSWORD = "newpass123";

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void restorePassword() {
        LoginUser takahashi = loginAs("takahashi");
        userMapper.updatePassword(takahashi.getUserId(), passwordEncoder.encode(DEFAULT_PASSWORD));
    }

    @Test
    void changePasswordSuccess() {
        LoginUser takahashi = loginAs("takahashi");
        int beforeCount = AuditLogAssertions.countByUserAndAction(
                jdbcTemplate, takahashi.getUserId(), AuditLogService.ACTION_PASSWORD_CHANGE);

        passwordService.changePassword(
                takahashi.getUserId(), DEFAULT_PASSWORD, TEMP_PASSWORD, TEMP_PASSWORD);

        var updated = userMapper.findById(takahashi.getUserId()).orElseThrow();
        assertThat(passwordEncoder.matches(TEMP_PASSWORD, updated.getPassword())).isTrue();
        assertThat(AuditLogAssertions.countByUserAndAction(
                jdbcTemplate, takahashi.getUserId(), AuditLogService.ACTION_PASSWORD_CHANGE))
                .isEqualTo(beforeCount + 1);
    }

    @Test
    void changePasswordFailsWithWrongCurrentPassword() {
        LoginUser takahashi = loginAs("takahashi");
        assertThatThrownBy(() -> passwordService.changePassword(
                takahashi.getUserId(), "wrong-password", TEMP_PASSWORD, TEMP_PASSWORD))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void changePasswordFailsWhenConfirmationMismatch() {
        LoginUser takahashi = loginAs("takahashi");
        assertThatThrownBy(() -> passwordService.changePassword(
                takahashi.getUserId(), DEFAULT_PASSWORD, TEMP_PASSWORD, "different123"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changePasswordFailsWhenSameAsCurrent() {
        LoginUser takahashi = loginAs("takahashi");
        assertThatThrownBy(() -> passwordService.changePassword(
                takahashi.getUserId(), DEFAULT_PASSWORD, DEFAULT_PASSWORD, DEFAULT_PASSWORD))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changePasswordFailsWhenTooShort() {
        LoginUser takahashi = loginAs("takahashi");
        assertThatThrownBy(() -> passwordService.changePassword(
                takahashi.getUserId(), DEFAULT_PASSWORD, "short", "short"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
