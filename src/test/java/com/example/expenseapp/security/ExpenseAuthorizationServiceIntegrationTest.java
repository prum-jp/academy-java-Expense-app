package com.example.expenseapp.security;

import com.example.expenseapp.entity.ExpenseStatus;
import com.example.expenseapp.service.ApprovalService;
import com.example.expenseapp.service.ExpenseService;
import com.example.expenseapp.support.AbstractMySqlIntegrationTest;
import com.example.expenseapp.support.ExpenseTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ExpenseAuthorizationServiceIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private ExpenseAuthorizationService expenseAuthorizationService;

    private Integer expenseId;
    private Integer expenseVersion;

    @BeforeEach
    void preparePendingExpense() {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("権限テスト"));
        var submitted = submitWithReceipt(yamada, created);
        expenseId = submitted.getExpenseId();
        expenseVersion = submitted.getVersion();
    }

    @Test
    void ownerCanEditDraft() {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("下書き編集権限"));
        assertThat(expenseAuthorizationService.canEdit(created.getExpenseId(), yamada)).isTrue();
    }

    @Test
    void ownerCanViewButCannotEditWhilePending() {
        LoginUser yamada = loginAs("yamada");
        assertThat(expenseAuthorizationService.isOwner(expenseId, yamada)).isTrue();
        assertThat(expenseAuthorizationService.canView(expenseId, yamada)).isTrue();
        assertThat(expenseAuthorizationService.canEdit(expenseId, yamada)).isFalse();
        assertThat(expenseAuthorizationService.canApprove(expenseId, yamada)).isFalse();
    }

    @Test
    void currentApproverCanViewAndApprove() {
        LoginUser sato = loginAs("sato");
        assertThat(expenseAuthorizationService.isOwner(expenseId, sato)).isFalse();
        assertThat(expenseAuthorizationService.canView(expenseId, sato)).isTrue();
        assertThat(expenseAuthorizationService.canApprove(expenseId, sato)).isTrue();
        assertThat(expenseAuthorizationService.canEdit(expenseId, sato)).isFalse();
    }

    @Test
    void finalApproverCannotViewOrApproveBeforeTheirStep() {
        LoginUser suzuki = loginAs("suzuki");
        assertThat(expenseAuthorizationService.canView(expenseId, suzuki)).isFalse();
        assertThat(expenseAuthorizationService.canApprove(expenseId, suzuki)).isFalse();
    }

    @Test
    void unrelatedUserCannotAccess() {
        LoginUser takahashi = loginAs("takahashi");
        assertThat(expenseAuthorizationService.isOwner(expenseId, takahashi)).isFalse();
        assertThat(expenseAuthorizationService.canView(expenseId, takahashi)).isFalse();
        assertThat(expenseAuthorizationService.canEdit(expenseId, takahashi)).isFalse();
        assertThat(expenseAuthorizationService.canApprove(expenseId, takahashi)).isFalse();
    }

    @Test
    void afterFirstApprovalOnlyNextApproverCanApprove() {
        LoginUser sato = loginAs("sato");
        var afterFirst = approvalService.approve(expenseId, sato, expenseVersion, "1次承認");

        assertThat(expenseAuthorizationService.canView(expenseId, sato)).isTrue();
        assertThat(expenseAuthorizationService.canApprove(expenseId, sato)).isFalse();

        LoginUser suzuki = loginAs("suzuki");
        assertThat(expenseAuthorizationService.canView(expenseId, suzuki)).isTrue();
        assertThat(expenseAuthorizationService.canApprove(expenseId, suzuki)).isTrue();

        approvalService.approve(expenseId, suzuki, afterFirst.getVersion(), "最終承認");
        assertThat(expenseAuthorizationService.canApprove(expenseId, suzuki)).isFalse();
        assertThat(expenseAuthorizationService.canView(expenseId, suzuki)).isTrue();
        assertThat(expenseAuthorizationService.canView(expenseId, sato)).isTrue();
    }

    @Test
    void afterRejectOwnerCanEditAgain() {
        LoginUser sato = loginAs("sato");
        approvalService.reject(expenseId, sato, expenseVersion, "差戻し");

        LoginUser yamada = loginAs("yamada");
        assertThat(expenseAuthorizationService.canEdit(expenseId, yamada)).isTrue();
        assertThat(expenseAuthorizationService.canApprove(expenseId, sato)).isFalse();

        var expense = expenseService.getExpenseForOwner(expenseId, yamada);
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.REJECTED);
    }

    @Test
    void nullArgumentsAreDenied() {
        LoginUser yamada = loginAs("yamada");
        assertThat(expenseAuthorizationService.isOwner(null, yamada)).isFalse();
        assertThat(expenseAuthorizationService.isOwner(expenseId, null)).isFalse();
        assertThat(expenseAuthorizationService.canView(null, yamada)).isFalse();
        assertThat(expenseAuthorizationService.canView(expenseId, null)).isFalse();
        assertThat(expenseAuthorizationService.canEdit(null, yamada)).isFalse();
        assertThat(expenseAuthorizationService.canApprove(expenseId, null)).isFalse();
    }
}
