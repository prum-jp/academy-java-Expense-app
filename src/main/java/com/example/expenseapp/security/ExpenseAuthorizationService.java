package com.example.expenseapp.security;

import com.example.expenseapp.entity.Expense;
import com.example.expenseapp.mapper.ApprovalRouteMapper;
import com.example.expenseapp.mapper.ExpenseMapper;
import org.springframework.stereotype.Component;

/**
 * 経費データへの IDOR 対策用。Controller / Service から SpEL で参照する。
 * <pre>
 * {@code @PreAuthorize("@expenseAuthorizationService.isOwner(#expenseId, authentication.principal)")}
 * </pre>
 */
@Component("expenseAuthorizationService")
public class ExpenseAuthorizationService {

    private final ExpenseMapper expenseMapper;
    private final ApprovalRouteMapper approvalRouteMapper;

    public ExpenseAuthorizationService(
            ExpenseMapper expenseMapper,
            ApprovalRouteMapper approvalRouteMapper) {
        this.expenseMapper = expenseMapper;
        this.approvalRouteMapper = approvalRouteMapper;
    }

    public boolean canView(Integer expenseId, LoginUser loginUser) {
        if (expenseId == null || loginUser == null) {
            return false;
        }
        if (isOwner(expenseId, loginUser)) {
            return true;
        }
        if (!loginUser.isApprover()) {
            return false;
        }
        Expense expense = expenseMapper.findAccessibleByApprover(expenseId, loginUser.getUserId()).orElse(null);
        if (expense == null) {
            return false;
        }
        if (!expense.getStatus().isPending()) {
            return true;
        }
        if (expense.getApplicant() == null || expense.getApplicant().getDeptId() == null) {
            return false;
        }
        Integer deptId = expense.getApplicant().getDeptId();
        return approvalRouteMapper.findCurrentStep(deptId, expenseId)
                .map(current -> approvalRouteMapper.findByDeptIdOrderByStep(deptId).stream()
                        .filter(route -> loginUser.getUserId().equals(route.getApproverUserId()))
                        .anyMatch(route -> route.getStepNumber() != null
                                && route.getStepNumber() == current.getStepNumber()))
                .orElse(false);
    }

    public boolean canEdit(Integer expenseId, LoginUser loginUser) {
        if (expenseId == null || loginUser == null) {
            return false;
        }
        return expenseMapper.findByIdAndUserId(expenseId, loginUser.getUserId())
                .map(expense -> expense.getStatus() != null && expense.getStatus().isEditable())
                .orElse(false);
    }

    public boolean canApprove(Integer expenseId, LoginUser loginUser) {
        if (expenseId == null || loginUser == null || !loginUser.isApprover()) {
            return false;
        }
        Expense expense = expenseMapper.findAccessibleByApprover(expenseId, loginUser.getUserId()).orElse(null);
        if (expense == null || !expense.getStatus().isPending()) {
            return false;
        }
        if (expense.getApplicant() == null || expense.getApplicant().getDeptId() == null) {
            return false;
        }
        return approvalRouteMapper.findCurrentStep(expense.getApplicant().getDeptId(), expenseId)
                .map(route -> loginUser.getUserId().equals(route.getApproverUserId()))
                .orElse(false);
    }

    public boolean isRouteMember(Integer expenseId, LoginUser loginUser) {
        if (expenseId == null || loginUser == null || !loginUser.isApprover()) {
            return false;
        }
        return expenseMapper.findAccessibleByApprover(expenseId, loginUser.getUserId()).isPresent();
    }

    public boolean isOwner(Integer expenseId, LoginUser loginUser) {
        if (expenseId == null || loginUser == null) {
            return false;
        }
        return expenseMapper.findByIdAndUserId(expenseId, loginUser.getUserId()).isPresent();
    }
}
