package com.example.expenseapp.controller.advice;

import com.example.expenseapp.common.Messages;
import com.example.expenseapp.entity.ExpenseStatus;
import com.example.expenseapp.exception.InvalidExpenseStateException;
import com.example.expenseapp.exception.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(new Messages(messageSource));
    }

    @Test
    void optimisticLockReturnsJapaneseMessage() {
        when(messageSource.getMessage(eq("error.optimistic.lock"), isNull(), any(Locale.class)))
                .thenReturn("他のユーザーにより更新されています。");

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        HttpServletRequest request = new MockHttpServletRequest("POST", "/expenses/1/submit");

        String view = handler.handleOptimisticLock(new OptimisticLockException(), request, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/expenses/1");
        assertThat(redirectAttributes.getFlashAttributes().get("errorMessage"))
                .isEqualTo("他のユーザーにより更新されています。");
    }

    @Test
    void invalidExpenseStateBuildsMessageFromKeys() {
        when(messageSource.getMessage(eq("expense.status.PENDING"), isNull(), any(Locale.class)))
                .thenReturn("承認待ち");
        when(messageSource.getMessage(eq("expense.operation.submit"), isNull(), any(Locale.class)))
                .thenReturn("申請");
        when(messageSource.getMessage(
                eq("expense.error.invalidState"),
                eq(new Object[]{"承認待ち", "申請"}),
                any(Locale.class)))
                .thenReturn("承認待ちの状態では申請できません。");

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        HttpServletRequest request = new MockHttpServletRequest("POST", "/expenses/1/submit");

        String view = handler.handleInvalidState(
                new InvalidExpenseStateException(ExpenseStatus.PENDING, "expense.operation.submit"),
                request,
                redirectAttributes);

        assertThat(view).isEqualTo("redirect:/expenses");
        assertThat(redirectAttributes.getFlashAttributes().get("errorMessage"))
                .isEqualTo("承認待ちの状態では申請できません。");
    }

    @Test
    void illegalArgumentOnCreateRedirectsToNewForm() {
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        HttpServletRequest request = new MockHttpServletRequest("POST", "/expenses");

        String view = handler.handleIllegalArgument(
                new IllegalArgumentException("件名は必須です。"), request, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/expenses/new");
        assertThat(redirectAttributes.getFlashAttributes().get("errorMessage"))
                .isEqualTo("件名は必須です。");
    }

    @Test
    void illegalArgumentOnUpdateRedirectsToEditForm() {
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        HttpServletRequest request = new MockHttpServletRequest("POST", "/expenses/12");

        String view = handler.handleIllegalArgument(
                new IllegalArgumentException("科目が不正です。"), request, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/expenses/12/edit");
    }
}
