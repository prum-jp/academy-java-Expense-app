package com.example.expenseapp.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class LoginUsers {

    private LoginUsers() {
    }

    public static Optional<LoginUser> findCurrent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return Optional.of(loginUser);
        }
        return Optional.empty();
    }

    public static LoginUser requireCurrent() {
        return findCurrent().orElseThrow(() -> new IllegalStateException("ログインしていません。"));
    }
}
