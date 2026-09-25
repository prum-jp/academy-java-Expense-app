package com.example.expenseapp.controller;

import com.example.expenseapp.common.Messages;
import com.example.expenseapp.controller.form.ExpenseDetailForm;
import com.example.expenseapp.controller.form.ExpenseForm;
import com.example.expenseapp.controller.support.ExpenseFormMapper;
import com.example.expenseapp.entity.Expense;
import com.example.expenseapp.entity.Role;
import com.example.expenseapp.entity.User;
import com.example.expenseapp.security.LoginUser;
import com.example.expenseapp.service.ExpenseService;
import com.example.expenseapp.service.MasterDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseControllerTest {

    @Mock
    private ExpenseService expenseService;

    @Mock
    private MasterDataService masterDataService;

    @Mock
    private Messages messages;

    @Mock
    private BindingResult bindingResult;

    private ExpenseController controller;

    @BeforeEach
    void setUp() {
        controller = new ExpenseController(
                expenseService,
                masterDataService,
                new ExpenseFormMapper(),
                messages);
    }

    @Test
    void createKeepsInputWhenServiceValidationFails() {
        stubFormMasters();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(expenseService.createDraft(any(), any()))
                .thenThrow(new IllegalArgumentException("科目が不正です。"));

        ExpenseForm form = sampleForm("入力を残したい件名");
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.create(loginUser(), form, bindingResult, model, new RedirectAttributesModelMap());

        assertThat(view).isEqualTo("expenses/form");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("科目が不正です。");
        ExpenseForm kept = (ExpenseForm) model.getAttribute("expenseForm");
        assertThat(kept.getTitle()).isEqualTo("入力を残したい件名");
        assertThat(kept.getDetails().getFirst().getAmount()).isEqualTo(1500);
    }

    @Test
    void updateKeepsInputWhenServiceValidationFails() {
        stubFormMasters();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(expenseService.updateDraft(eq(12), any(), any()))
                .thenThrow(new IllegalArgumentException("税率が不正です。"));

        ExpenseForm form = sampleForm("編集中の件名");
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.update(12, loginUser(), form, bindingResult, model, new RedirectAttributesModelMap());

        assertThat(view).isEqualTo("expenses/form");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("税率が不正です。");
        ExpenseForm kept = (ExpenseForm) model.getAttribute("expenseForm");
        assertThat(kept.isEditMode()).isTrue();
        assertThat(kept.getExpenseId()).isEqualTo(12);
        assertThat(kept.getTitle()).isEqualTo("編集中の件名");
    }

    @Test
    void uploadReceiptShowsJapaneseMessageOnIoFailure() throws Exception {
        when(expenseService.attachReceipt(eq(7), any(), eq(0), any()))
                .thenThrow(new IOException("disk full"));
        when(messages.get("error.file.upload")).thenReturn("ファイルのアップロードに失敗しました。");

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        MockMultipartFile file = new MockMultipartFile("file", "receipt.png", "image/png", new byte[]{1});

        String view = controller.uploadReceipt(7, loginUser(), 0, file, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/expenses/7");
        assertThat(redirectAttributes.getFlashAttributes().get("errorMessage"))
                .isEqualTo("ファイルのアップロードに失敗しました。");
    }

    @Test
    void createRedirectsOnSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);
        Expense saved = new Expense();
        saved.setExpenseId(99);
        when(expenseService.createDraft(any(), any())).thenReturn(saved);
        when(messages.get("expense.save.success")).thenReturn("下書きを保存しました。");

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String view = controller.create(
                loginUser(),
                sampleForm("保存できる件名"),
                bindingResult,
                new ConcurrentModel(),
                redirectAttributes);

        assertThat(view).isEqualTo("redirect:/expenses/99");
        assertThat(redirectAttributes.getFlashAttributes().get("successMessage"))
                .isEqualTo("下書きを保存しました。");
    }

    @Test
    void submitShowsErrorWhenReceiptMissing() {
        when(expenseService.submit(eq(8), any(), eq(0)))
                .thenThrow(new IllegalArgumentException("申請するには領収書を1件以上添付してください。"));

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String view = controller.submit(8, loginUser(), 0, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/expenses/8");
        assertThat(redirectAttributes.getFlashAttributes().get("errorMessage"))
                .isEqualTo("申請するには領収書を1件以上添付してください。");
    }

    @Test
    void deleteReceiptShowsSuccess() {
        when(messages.get("expense.receipt.delete.success")).thenReturn("領収書を削除しました。");

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String view = controller.deleteReceipt(8, 3, loginUser(), 1, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/expenses/8");
        assertThat(redirectAttributes.getFlashAttributes().get("successMessage"))
                .isEqualTo("領収書を削除しました。");
        verify(expenseService).deleteReceipt(eq(8), any(), eq(1), eq(3));
    }

    private void stubFormMasters() {
        when(masterDataService.findAllCategories()).thenReturn(List.of());
        when(masterDataService.findAllTaxRates()).thenReturn(List.of());
    }

    private static LoginUser loginUser() {
        User user = new User();
        user.setUserId(1);
        user.setUserName("yamada");
        user.setPassword("password");
        user.setRole(Role.USER);
        return new LoginUser(user);
    }

    private static ExpenseForm sampleForm(String title) {
        ExpenseForm form = new ExpenseForm();
        form.setTitle(title);
        ExpenseDetailForm detail = new ExpenseDetailForm();
        detail.setExpenseDate(LocalDate.of(2026, 9, 16));
        detail.setCategoryId(1);
        detail.setTaxId(1);
        detail.setAmount(1500);
        detail.setMemo("交通費");
        form.setDetails(List.of(detail));
        return form;
    }
}
