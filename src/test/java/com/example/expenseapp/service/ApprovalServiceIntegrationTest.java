package com.example.expenseapp.service;

import com.example.expenseapp.entity.ApprovalAction;
import com.example.expenseapp.entity.ExpenseStatus;
import com.example.expenseapp.exception.OptimisticLockException;
import com.example.expenseapp.mapper.ApprovalHistoryMapper;
import com.example.expenseapp.security.LoginUser;
import com.example.expenseapp.service.dto.ExpenseSaveCommand;
import com.example.expenseapp.support.AbstractMySqlIntegrationTest;
import com.example.expenseapp.support.ExpenseTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApprovalServiceIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private ApprovalHistoryMapper approvalHistoryMapper;

    private Integer expenseId;
    private Integer expenseVersion;

    @BeforeEach
    void preparePendingExpense() {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("承認フローテスト"));
        var submitted = submitWithReceipt(yamada, created);
        expenseId = submitted.getExpenseId();
        expenseVersion = submitted.getVersion();
    }

    @Test
    void findPendingByApplicantNameKeyword() {
        LoginUser sato = loginAs("sato");
        var results = approvalService.findPendingExpenses(sato, "yamada");
        assertThat(results).extracting("expenseId").contains(expenseId);
    }

    @Test
    void findPendingByTitleKeyword() {
        LoginUser sato = loginAs("sato");
        var results = approvalService.findPendingExpenses(sato, "承認フロー");
        assertThat(results).extracting("expenseId").contains(expenseId);
    }

    @Test
    void keywordMismatchExcludesExpense() {
        LoginUser sato = loginAs("sato");
        var results = approvalService.findPendingExpenses(sato, "存在しないキーワード");
        assertThat(results).extracting("expenseId").doesNotContain(expenseId);
    }

    @Test
    void approveMovesToNextStep() {
        LoginUser sato = loginAs("sato");
        var afterFirstApproval = approvalService.approve(expenseId, sato, expenseVersion, "1次承認");
        assertThat(afterFirstApproval.getStatus()).isEqualTo(ExpenseStatus.PENDING);

        LoginUser suzuki = loginAs("suzuki");
        var pendingForFinal = approvalService.findPendingExpenses(suzuki, "承認フロー");
        assertThat(pendingForFinal).extracting("expenseId").contains(expenseId);

        var approved = approvalService.approve(expenseId, suzuki, afterFirstApproval.getVersion(), "最終承認");
        assertThat(approved.getStatus()).isEqualTo(ExpenseStatus.APPROVED);
    }

    @Test
    void rejectReturnsExpenseToRejectedStatus() {
        LoginUser sato = loginAs("sato");
        var rejected = approvalService.reject(expenseId, sato, expenseVersion, "差戻し理由");

        assertThat(rejected.getStatus()).isEqualTo(ExpenseStatus.REJECTED);
        assertThat(approvalHistoryMapper.findByExpenseId(expenseId))
                .extracting("action")
                .contains(ApprovalAction.SUBMIT, ApprovalAction.REJECT);

        LoginUser yamada = loginAs("yamada");
        var editable = expenseService.getExpenseForOwner(expenseId, yamada);
        assertThat(editable.getStatus()).isEqualTo(ExpenseStatus.REJECTED);
        assertThat(editable.getStatus().isEditable()).isTrue();
    }

    @Test
    void developmentDepartmentApprovalRoute() {
        LoginUser takahashi = loginAs("takahashi");
        var created = expenseService.createDraft(takahashi, ExpenseTestDataFactory.sampleCommand("開発部経費"));
        var submitted = submitWithReceipt(takahashi, created);

        LoginUser ito = loginAs("ito");
        var afterFirst = approvalService.approve(submitted.getExpenseId(), ito, submitted.getVersion(), "開発部承認");

        LoginUser suzuki = loginAs("suzuki");
        var approved = approvalService.approve(submitted.getExpenseId(), suzuki, afterFirst.getVersion(), "最終承認");
        assertThat(approved.getStatus()).isEqualTo(ExpenseStatus.APPROVED);
    }

    @Test
    void wrongApproverCannotApprove() {
        LoginUser suzuki = loginAs("suzuki");
        assertThatThrownBy(() -> approvalService.approve(expenseId, suzuki, expenseVersion, "越権承認"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("アクセス権限がありません。");
    }

    @Test
    void futureApproverCannotOpenPendingDetail() {
        LoginUser suzuki = loginAs("suzuki");
        assertThatThrownBy(() -> approvalService.getExpenseForApprover(expenseId, suzuki))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("アクセス権限がありません。");
    }

    @Test
    void applicantCannotApproveOwnExpense() {
        LoginUser yamada = loginAs("yamada");
        assertThatThrownBy(() -> approvalService.approve(expenseId, yamada, expenseVersion, "自己承認"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void approveFailsWhenVersionIsStale() {
        LoginUser sato = loginAs("sato");
        assertThatThrownBy(() -> approvalService.approve(expenseId, sato, 999, "排他エラー"))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void userRoleCannotListPendingExpenses() {
        LoginUser yamada = loginAs("yamada");
        assertThatThrownBy(() -> approvalService.findPendingExpenses(yamada, "yamada"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void afterFirstApprovalPreviousApproverCannotApproveAgain() {
        LoginUser sato = loginAs("sato");
        var afterFirst = approvalService.approve(expenseId, sato, expenseVersion, "1次承認");

        assertThatThrownBy(() -> approvalService.approve(expenseId, sato, afterFirst.getVersion(), "再承認"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectAfterSecondStepThenResubmitStartsFromFirstApprover() {
        LoginUser sato = loginAs("sato");
        var afterFirst = approvalService.approve(expenseId, sato, expenseVersion, "1次承認");

        LoginUser suzuki = loginAs("suzuki");
        var rejected = approvalService.reject(expenseId, suzuki, afterFirst.getVersion(), "最終差戻し");
        assertThat(rejected.getStatus()).isEqualTo(ExpenseStatus.REJECTED);

        LoginUser yamada = loginAs("yamada");
        ExpenseSaveCommand update = ExpenseTestDataFactory.sampleCommand("再申請タイトル");
        update.setVersion(rejected.getVersion());
        var updated = expenseService.updateDraft(expenseId, yamada, update);
        expenseService.submit(expenseId, yamada, updated.getVersion());

        LoginUser firstApprover = loginAs("sato");
        assertThat(approvalService.findPendingExpenses(firstApprover, "再申請タイトル"))
                .extracting("expenseId")
                .contains(expenseId);

        LoginUser finalApprover = loginAs("suzuki");
        assertThat(approvalService.findPendingExpenses(finalApprover, "再申請タイトル"))
                .extracting("expenseId")
                .doesNotContain(expenseId);
    }

    @Test
    void rejectWithoutCommentFails() {
        LoginUser sato = loginAs("sato");
        assertThatThrownBy(() -> approvalService.reject(expenseId, sato, expenseVersion, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("差戻しコメントは必須です。");
    }

    @Test
    void processedListContainsApprovedByCurrentUser() {
        LoginUser sato = loginAs("sato");
        approvalService.approve(expenseId, sato, expenseVersion, "1次承認");

        assertThat(approvalService.findProcessedExpenses(sato, "承認フロー"))
                .extracting("expenseId")
                .contains(expenseId);
        assertThat(approvalService.findPendingExpenses(sato, "承認フロー"))
                .extracting("expenseId")
                .doesNotContain(expenseId);

        LoginUser suzuki = loginAs("suzuki");
        assertThat(approvalService.findProcessedExpenses(suzuki, "承認フロー"))
                .extracting("expenseId")
                .doesNotContain(expenseId);
        assertThat(approvalService.getExpenseForApprover(expenseId, sato).getExpenseId()).isEqualTo(expenseId);
    }

    @Test
    void userRoleCannotListProcessedExpenses() {
        LoginUser yamada = loginAs("yamada");
        assertThatThrownBy(() -> approvalService.findProcessedExpenses(yamada, "yamada"))
                .isInstanceOf(AccessDeniedException.class);
    }
}
