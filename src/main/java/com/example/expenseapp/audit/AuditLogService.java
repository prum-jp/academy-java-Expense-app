package com.example.expenseapp.audit;

import com.example.expenseapp.entity.AuditLog;
import com.example.expenseapp.mapper.AuditLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class AuditLogService {

    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_PASSWORD_CHANGE = "PASSWORD_CHANGE";
    public static final String ACTION_EXPENSE_CREATE = "EXPENSE_CREATE";
    public static final String ACTION_EXPENSE_UPDATE = "EXPENSE_UPDATE";
    public static final String ACTION_EXPENSE_DELETE = "EXPENSE_DELETE";
    public static final String ACTION_EXPENSE_SUBMIT = "EXPENSE_SUBMIT";
    public static final String ACTION_EXPENSE_APPROVE = "EXPENSE_APPROVE";
    public static final String ACTION_EXPENSE_REJECT = "EXPENSE_REJECT";

    public static final String TABLE_USERS = "users";
    public static final String TABLE_EXPENSES = "expenses";

    private final AuditLogMapper auditLogMapper;

    public AuditLogService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * ビジネストランザクション内から呼び出す。呼び出し元のトランザクションに参加する。
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(Integer userId, String action, String targetTable, Integer targetId) {
        insert(userId, action, targetTable, targetId);
    }

    /**
     * 独立したトランザクションで記録する（ログインなど、業務トランザクション外の操作向け）。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordStandalone(Integer userId, String action, String targetTable, Integer targetId) {
        insert(userId, action, targetTable, targetId);
    }

    private void insert(Integer userId, String action, String targetTable, Integer targetId) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(targetTable, "targetTable");
        Objects.requireNonNull(targetId, "targetId");

        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setTargetTable(targetTable);
        log.setTargetId(targetId);
        log.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(log);
    }
}
