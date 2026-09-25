package com.example.expenseapp.service;

import com.example.expenseapp.audit.AuditLogService;
import com.example.expenseapp.audit.Audited;
import com.example.expenseapp.entity.ApprovalAction;
import com.example.expenseapp.entity.ApprovalRoute;
import com.example.expenseapp.entity.Expense;
import com.example.expenseapp.entity.ExpenseStatus;
import com.example.expenseapp.exception.ExpenseNotFoundException;
import com.example.expenseapp.exception.InvalidExpenseStateException;
import com.example.expenseapp.exception.OptimisticLockException;
import com.example.expenseapp.mapper.ApprovalRouteMapper;
import com.example.expenseapp.mapper.ExpenseMapper;
import com.example.expenseapp.security.ExpenseAuthorizationService;
import com.example.expenseapp.security.LoginUser;
import com.example.expenseapp.common.LikeKeywords;
import com.example.expenseapp.common.Messages;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class ApprovalService {

    private final ExpenseMapper expenseMapper;
    private final ApprovalRouteMapper approvalRouteMapper;
    private final ApprovalHistoryRecorder approvalHistoryRecorder;
    private final ExpenseAuthorizationService expenseAuthorizationService;
    private final Messages messages;

    public ApprovalService(
            ExpenseMapper expenseMapper,
            ApprovalRouteMapper approvalRouteMapper,
            ApprovalHistoryRecorder approvalHistoryRecorder,
            ExpenseAuthorizationService expenseAuthorizationService,
            Messages messages) {
        this.expenseMapper = expenseMapper;
        this.approvalRouteMapper = approvalRouteMapper;
        this.approvalHistoryRecorder = approvalHistoryRecorder;
        this.expenseAuthorizationService = expenseAuthorizationService;
        this.messages = messages;
    }

    @Transactional(readOnly = true)
    public List<Expense> findPendingExpenses(LoginUser approver, String keyword) {
        if (!approver.isApprover()) {
            throw new AccessDeniedException(messages.get("error.access.denied"));
        }
        return expenseMapper.findPendingForApprover(approver.getUserId(), LikeKeywords.escape(keyword));
    }

    @Transactional(readOnly = true)
    public List<Expense> findProcessedExpenses(LoginUser approver, String keyword) {
        if (!approver.isApprover()) {
            throw new AccessDeniedException(messages.get("error.access.denied"));
        }
        return expenseMapper.findProcessedForApprover(approver.getUserId(), LikeKeywords.escape(keyword));
    }

    @Transactional(readOnly = true)
    public Expense getExpenseForApprover(Integer expenseId, LoginUser approver) {
        Expense expense = expenseMapper.findAccessibleByApprover(expenseId, approver.getUserId())
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));
        if (expense.getStatus().isPending()
                && !expenseAuthorizationService.canApprove(expenseId, approver)) {
            throw new AccessDeniedException(messages.get("error.access.denied"));
        }
        if (!expenseAuthorizationService.canView(expenseId, approver)) {
            throw new AccessDeniedException(messages.get("error.access.denied"));
        }
        return expense;
    }

    @Audited(action = AuditLogService.ACTION_EXPENSE_APPROVE, targetTable = AuditLogService.TABLE_EXPENSES, idParam = "expenseId")
    public Expense approve(Integer expenseId, LoginUser approver, Integer version, String comment) {
        Expense expense = requirePendingExpenseForApproval(expenseId, approver, version);

        ApprovalRoute currentStep = requireCurrentStep(expense);
        int maxStep = findMaxStep(expense.getApplicant().getDeptId());

        approvalHistoryRecorder.record(
                expenseId,
                approver.getUserId(),
                ApprovalAction.APPROVE,
                normalizeComment(comment));

        ExpenseStatus newStatus = currentStep.getStepNumber() >= maxStep
                ? ExpenseStatus.APPROVED
                : ExpenseStatus.PENDING;
        expense.setStatus(newStatus);
        assertUpdated(expenseMapper.updateById(expense));

        return loadAccessibleExpense(expenseId, approver.getUserId());
    }

    @Audited(action = AuditLogService.ACTION_EXPENSE_REJECT, targetTable = AuditLogService.TABLE_EXPENSES, idParam = "expenseId")
    public Expense reject(Integer expenseId, LoginUser approver, Integer version, String comment) {
        Expense expense = requirePendingExpenseForApproval(expenseId, approver, version);
        requireCurrentStep(expense);

        approvalHistoryRecorder.record(
                expenseId,
                approver.getUserId(),
                ApprovalAction.REJECT,
                normalizeComment(comment));

        expense.setStatus(ExpenseStatus.REJECTED);
        assertUpdated(expenseMapper.updateById(expense));

        return loadAccessibleExpense(expenseId, approver.getUserId());
    }

    private Expense loadAccessibleExpense(Integer expenseId, Integer approverUserId) {
        return expenseMapper.findAccessibleByApprover(expenseId, approverUserId)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));
    }

    private Expense requirePendingExpenseForApproval(Integer expenseId, LoginUser approver, Integer version) {
        Expense expense = expenseMapper.findAccessibleByApprover(expenseId, approver.getUserId())
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));
        if (!expense.getStatus().isPending()) {
            throw new InvalidExpenseStateException(expense.getStatus(), "expense.operation.approve");
        }
        return expense;
    }

    private ApprovalRoute requireCurrentStep(Expense expense) {
        Integer deptId = expense.getApplicant().getDeptId();
        return approvalRouteMapper.findCurrentStep(deptId, expense.getExpenseId())
                .orElseThrow(() -> new InvalidExpenseStateException(expense.getStatus(), "expense.operation.approve"));
    }

    private int findMaxStep(Integer deptId) {
        return approvalRouteMapper.findByDeptIdOrderByStep(deptId).stream()
                .map(ApprovalRoute::getStepNumber)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalStateException(messages.get("error.approval.route.missing", deptId)));
    }

    private String normalizeComment(String comment) {
        if (!StringUtils.hasText(comment)) {
            return null;
        }
        String trimmed = comment.trim();
        if (trimmed.length() > 500) {
            throw new IllegalArgumentException(messages.get("validation.comment.max"));
        }
        return trimmed;
    }

    private void assertUpdated(int rows) {
        if (rows != 1) {
            throw new OptimisticLockException();
        }
    }
}
