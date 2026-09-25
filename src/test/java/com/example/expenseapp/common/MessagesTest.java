package com.example.expenseapp.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessagesTest {

    @Mock
    private MessageSource messageSource;

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void usesJapanWhenLocaleLanguageIsEmpty() {
        LocaleContextHolder.setLocale(Locale.ROOT);
        when(messageSource.getMessage(eq("validation.title.required"), isNull(), eq(Locale.JAPAN)))
                .thenReturn("件名は必須です。");

        String text = new Messages(messageSource).get("validation.title.required");

        assertThat(text).isEqualTo("件名は必須です。");
        verify(messageSource).getMessage("validation.title.required", null, Locale.JAPAN);
    }

    @Test
    void passesArgumentsToMessageSource() {
        LocaleContextHolder.setLocale(Locale.JAPAN);
        when(messageSource.getMessage(
                eq("expense.error.invalidState"),
                eq(new Object[]{"承認待ち", "申請"}),
                eq(Locale.JAPAN)))
                .thenReturn("承認待ちの状態では申請できません。");

        String text = new Messages(messageSource).get("expense.error.invalidState", "承認待ち", "申請");

        assertThat(text).isEqualTo("承認待ちの状態では申請できません。");
    }
}
