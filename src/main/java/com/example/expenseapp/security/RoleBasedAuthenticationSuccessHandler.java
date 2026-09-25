package com.example.expenseapp.security;

import com.example.expenseapp.audit.AuditLogService;
import com.example.expenseapp.entity.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuditLogService auditLogService;

    public RoleBasedAuthenticationSuccessHandler(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        if (authentication.getPrincipal() instanceof LoginUser loginUser) {
            auditLogService.recordStandalone(
                    loginUser.getUserId(),
                    AuditLogService.ACTION_LOGIN,
                    AuditLogService.TABLE_USERS,
                    loginUser.getUserId());
            loginUser.eraseCredentials();
        }

        String target = loginUserIsApprover(authentication) ? "/approvals" : "/expenses";
        response.sendRedirect(request.getContextPath() + target);
    }

    private boolean loginUserIsApprover(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> Role.APPROVER.authority().equals(auth.getAuthority()));
    }
}
