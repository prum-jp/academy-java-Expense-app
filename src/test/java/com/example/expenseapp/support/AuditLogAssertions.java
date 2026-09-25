package com.example.expenseapp.support;

import org.springframework.jdbc.core.JdbcTemplate;

public final class AuditLogAssertions {

    private AuditLogAssertions() {
    }

    public static int countByAction(JdbcTemplate jdbcTemplate, String action, int targetId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM audit_logs
                WHERE action = ?
                  AND target_id = ?
                """,
                Integer.class,
                action,
                targetId);
        return count == null ? 0 : count;
    }

    public static int countByUserAndAction(JdbcTemplate jdbcTemplate, int userId, String action) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM audit_logs
                WHERE user_id = ?
                  AND action = ?
                """,
                Integer.class,
                userId,
                action);
        return count == null ? 0 : count;
    }
}
