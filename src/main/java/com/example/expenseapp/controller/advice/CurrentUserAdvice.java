package com.example.expenseapp.controller.advice;

import com.example.expenseapp.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.example.expenseapp.controller")
public class CurrentUserAdvice {

    @ModelAttribute("currentUser")
    public LoginUser currentUser(@AuthenticationPrincipal LoginUser loginUser) {
        return loginUser;
    }

    @ModelAttribute("requestUri")
    public String requestUri(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
