package com.example.expenseapp.controller;

import com.example.expenseapp.entity.Role;
import com.example.expenseapp.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SecurityController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        if (authentication.getPrincipal() instanceof LoginUser loginUser
                && loginUser.getRole() == Role.APPROVER) {
            return "redirect:/approvals";
        }
        return "redirect:/expenses";
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}
