package com.example.expenseapp.service;

import com.example.expenseapp.audit.AuditLogService;
import com.example.expenseapp.audit.Audited;
import com.example.expenseapp.common.Messages;
import com.example.expenseapp.exception.InvalidPasswordException;
import com.example.expenseapp.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class PasswordService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final Messages messages;

    public PasswordService(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            Messages messages) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.messages = messages;
    }

    @Audited(action = AuditLogService.ACTION_PASSWORD_CHANGE, targetTable = AuditLogService.TABLE_USERS, idParam = "userId")
    public void changePassword(Integer userId, String currentPassword, String newPassword, String confirmPassword) {
        validateNewPassword(newPassword, confirmPassword);

        var user = userMapper.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(messages.get("validation.user.notFound")));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException();
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException(messages.get("validation.password.same"));
        }

        userMapper.updatePassword(userId, passwordEncoder.encode(newPassword));
    }

    private void validateNewPassword(String newPassword, String confirmPassword) {
        if (!StringUtils.hasText(newPassword) || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(messages.get("validation.password.min"));
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException(messages.get("password.error.mismatch"));
        }
    }
}
