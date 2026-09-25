package com.example.expenseapp.audit;

import com.example.expenseapp.entity.ExpenseStatus;
import com.example.expenseapp.security.LoginUser;
import com.example.expenseapp.service.ApprovalService;
import com.example.expenseapp.service.ExpenseService;
import com.example.expenseapp.support.AbstractMySqlIntegrationTest;
import com.example.expenseapp.support.AuditLogAssertions;
import com.example.expenseapp.support.ExpenseTestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ApprovalService approvalService;

    @Test
    void submitAndApproveRecordAuditTrail() {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("監査フルテスト"));
        assertThat(AuditLogAssertions.countByAction(
                jdbcTemplate, AuditLogService.ACTION_EXPENSE_CREATE, created.getExpenseId())).isEqualTo(1);

        var submitted = submitWithReceipt(yamada, created);
        assertThat(AuditLogAssertions.countByAction(
                jdbcTemplate, AuditLogService.ACTION_EXPENSE_SUBMIT, submitted.getExpenseId())).isEqualTo(1);

        LoginUser sato = loginAs("sato");
        var afterFirst = approvalService.approve(submitted.getExpenseId(), sato, submitted.getVersion(), "1次");
        assertThat(afterFirst.getStatus()).isEqualTo(ExpenseStatus.PENDING);
        assertThat(AuditLogAssertions.countByAction(
                jdbcTemplate, AuditLogService.ACTION_EXPENSE_APPROVE, submitted.getExpenseId())).isEqualTo(1);

        LoginUser suzuki = loginAs("suzuki");
        var approved = approvalService.approve(submitted.getExpenseId(), suzuki, afterFirst.getVersion(), "最終");
        assertThat(approved.getStatus()).isEqualTo(ExpenseStatus.APPROVED);
        assertThat(AuditLogAssertions.countByAction(
                jdbcTemplate, AuditLogService.ACTION_EXPENSE_APPROVE, submitted.getExpenseId())).isEqualTo(2);
    }

    @Test
    void deleteDraftRecordsAuditLog() {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("削除監査"));

        expenseService.deleteDraft(created.getExpenseId(), yamada, created.getVersion());

        assertThat(AuditLogAssertions.countByAction(
                jdbcTemplate, AuditLogService.ACTION_EXPENSE_DELETE, created.getExpenseId())).isEqualTo(1);
    }

    @Test
    void rejectRecordsAuditLog() {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("差戻し監査"));
        var submitted = submitWithReceipt(yamada, created);

        LoginUser sato = loginAs("sato");
        approvalService.reject(submitted.getExpenseId(), sato, submitted.getVersion(), "差戻し");

        assertThat(AuditLogAssertions.countByAction(
                jdbcTemplate, AuditLogService.ACTION_EXPENSE_REJECT, submitted.getExpenseId())).isEqualTo(1);
    }
}
