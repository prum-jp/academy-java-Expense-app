package com.example.expenseapp.service;

import com.example.expenseapp.audit.AuditLogService;
import com.example.expenseapp.entity.ExpenseStatus;
import com.example.expenseapp.exception.ExpenseNotFoundException;
import com.example.expenseapp.exception.InvalidExpenseStateException;
import com.example.expenseapp.exception.OptimisticLockException;
import com.example.expenseapp.security.LoginUser;
import com.example.expenseapp.service.dto.ExpenseSaveCommand;
import com.example.expenseapp.support.AbstractMySqlIntegrationTest;
import com.example.expenseapp.support.AuditLogAssertions;
import com.example.expenseapp.support.ExpenseTestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpenseServiceIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ReceiptStorageService receiptStorageService;

    @Test
    void createDraftAndSubmit() {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("出張交通費"));

        assertThat(created.getExpenseId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(ExpenseStatus.DRAFT);
        assertThat(created.getTotalAmount()).isEqualTo(1500);
        assertThat(created.getDetails()).hasSize(1);

        var submitted = submitWithReceipt(yamada, created);
        assertThat(submitted.getStatus()).isEqualTo(ExpenseStatus.PENDING);
        assertThat(submitted.getSubmittedAt()).isNotNull();
        assertThat(submitted.getVersion()).isEqualTo(created.getVersion() + 2);
    }

    @Test
    void createDraftRecordsAuditLog() {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("監査ログテスト"));

        assertThat(AuditLogAssertions.countByAction(
                jdbcTemplate,
                AuditLogService.ACTION_EXPENSE_CREATE,
                created.getExpenseId())).isEqualTo(1);
    }

    @Test
    void updateDraftRecalculatesTotalAmount() {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("更新前"));

        ExpenseSaveCommand update = ExpenseTestDataFactory.commandWithAmounts("更新後", 1000, 2500);
        update.setVersion(created.getVersion());

        var updated = expenseService.updateDraft(created.getExpenseId(), yamada, update);
        assertThat(updated.getTitle()).isEqualTo("更新後");
        assertThat(updated.getTotalAmount()).isEqualTo(3500);
        assertThat(updated.getDetails()).hasSize(2);
        assertThat(updated.getVersion()).isEqualTo(created.getVersion() + 1);
    }

    @Test
    void deleteDraftRemovesExpense() {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("削除テスト"));

        expenseService.deleteDraft(created.getExpenseId(), yamada, created.getVersion());

        assertThatThrownBy(() -> expenseService.getExpenseForOwner(created.getExpenseId(), yamada))
                .isInstanceOf(ExpenseNotFoundException.class);
    }

    @Test
    void findMyExpensesFiltersByStatusAndKeyword() {
        LoginUser yamada = loginAs("yamada");
        var draft = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("ユニーク下書きキーワード"));
        var submitted = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("別件名"));
        submitWithReceipt(yamada, submitted);

        assertThat(expenseService.findMyExpenses(yamada, "ユニーク下書き", ExpenseStatus.DRAFT))
                .extracting("expenseId")
                .containsExactly(draft.getExpenseId());

        assertThat(expenseService.findMyExpenses(yamada, null, ExpenseStatus.PENDING))
                .extracting("expenseId")
                .contains(submitted.getExpenseId());
    }

    @Test
    void findMyExpensesDoesNotMatchOwnUsername() {
        LoginUser yamada = loginAs("yamada");
        expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("交通費のみ"));

        assertThat(expenseService.findMyExpenses(yamada, "yamada", null))
                .extracting("title")
                .doesNotContain("交通費のみ");
    }

    @Test
    void submitPendingExpenseFails() {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("二重申請テスト"));
        var submitted = submitWithReceipt(yamada, created);

        assertThatThrownBy(() -> expenseService.submit(submitted.getExpenseId(), yamada, submitted.getVersion()))
                .isInstanceOf(InvalidExpenseStateException.class);
    }

    @Test
    void editPendingExpenseFails() {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("編集不可テスト"));
        var submitted = submitWithReceipt(yamada, created);

        ExpenseSaveCommand update = ExpenseTestDataFactory.sampleCommand("更新試行");
        update.setVersion(submitted.getVersion());

        assertThatThrownBy(() -> expenseService.updateDraft(submitted.getExpenseId(), yamada, update))
                .isInstanceOf(InvalidExpenseStateException.class);
    }

    @Test
    void attachReceiptIncrementsVersion() throws Exception {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("領収書テスト"));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "receipt.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});

        var updated = expenseService.attachReceipt(created.getExpenseId(), yamada, created.getVersion(), file);

        assertThat(updated.getReceiptImages()).hasSize(1);
        assertThat(updated.getReceiptImages().getFirst().getFilePath()).endsWith(".png");
        assertThat(updated.getVersion()).isEqualTo(created.getVersion() + 1);
    }

    @Test
    void deleteDraftRemovesReceiptFiles() throws Exception {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("領収書削除"));
        var updated = expenseService.attachReceipt(
                created.getExpenseId(),
                yamada,
                created.getVersion(),
                new MockMultipartFile(
                        "file",
                        "receipt.png",
                        "image/png",
                        new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}));
        Path stored = receiptStorageService.resolvePath(updated.getReceiptImages().getFirst().getFilePath());
        assertThat(stored).exists();

        expenseService.deleteDraft(updated.getExpenseId(), yamada, updated.getVersion());

        assertThat(stored).doesNotExist();
        assertThat(stored.getParent()).doesNotExist();
    }

    @Test
    void updateDraftFailsWhenVersionIsStale() {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("楽観排他テスト"));

        ExpenseSaveCommand update = ExpenseTestDataFactory.sampleCommand("更新後タイトル");
        update.setVersion(999);

        assertThatThrownBy(() -> expenseService.updateDraft(created.getExpenseId(), yamada, update))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void submitFailsWhenVersionIsStale() throws Exception {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("申請排他テスト"));
        var withReceipt = expenseService.attachReceipt(
                created.getExpenseId(), yamada, created.getVersion(), ExpenseTestDataFactory.pngReceipt());

        assertThatThrownBy(() -> expenseService.submit(withReceipt.getExpenseId(), yamada, 999))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void cannotAccessOtherUsersExpense() {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("他人の経費"));

        LoginUser takahashi = loginAs("takahashi");
        assertThatThrownBy(() -> expenseService.getExpenseForOwner(created.getExpenseId(), takahashi))
                .isInstanceOf(ExpenseNotFoundException.class);
    }

    @Test
    void submitWithoutReceiptFails() {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("領収書なし申請"));

        assertThatThrownBy(() -> expenseService.submit(created.getExpenseId(), yamada, created.getVersion()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("申請するには領収書を1件以上添付してください。");
    }

    @Test
    void deleteReceiptRemovesFileAndRow() throws Exception {
        LoginUser yamada = loginAs("yamada");
        var created = expenseService.createDraft(yamada, ExpenseTestDataFactory.sampleCommand("領収書個別削除"));
        var attached = expenseService.attachReceipt(
                created.getExpenseId(), yamada, created.getVersion(), ExpenseTestDataFactory.pngReceipt());
        var image = attached.getReceiptImages().getFirst();
        Path stored = receiptStorageService.resolvePath(image.getFilePath());
        assertThat(stored).exists();

        var afterDelete = expenseService.deleteReceipt(
                attached.getExpenseId(), yamada, attached.getVersion(), image.getImageId());

        assertThat(afterDelete.getReceiptImages()).isEmpty();
        assertThat(afterDelete.getVersion()).isEqualTo(attached.getVersion() + 1);
        assertThat(stored).doesNotExist();
    }
}
