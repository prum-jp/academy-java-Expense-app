package com.example.expenseapp.common;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Component
public class Messages {

    private final MessageSource messageSource;

    public Messages(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String get(String code) {
        return get(code, (Object[]) null);
    }

    public String get(String code, Object... args) {
        return messageSource.getMessage(code, args, locale());
    }

    public Locale locale() {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale == null || !StringUtils.hasText(locale.getLanguage())) {
            return Locale.JAPAN;
        }
        return locale;
    }
}
