package com.example.expenseapp.controller.advice;

import com.example.expenseapp.common.Messages;
import com.example.expenseapp.exception.ExpenseNotFoundException;
import com.example.expenseapp.exception.InvalidExpenseStateException;
import com.example.expenseapp.exception.InvalidPasswordException;
import com.example.expenseapp.exception.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Messages messages;

    public GlobalExceptionHandler(Messages messages) {
        this.messages = messages;
    }

    @ExceptionHandler(OptimisticLockException.class)
    public String handleOptimisticLock(
            OptimisticLockException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", messages.get("error.optimistic.lock"));
        return redirectAfterConflict(request);
    }

    @ExceptionHandler(ExpenseNotFoundException.class)
    public String handleNotFound(
            ExpenseNotFoundException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", messages.get("error.notFound"));
        return listRedirect(request);
    }

    @ExceptionHandler(InvalidExpenseStateException.class)
    public String handleInvalidState(
            InvalidExpenseStateException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        String statusLabel = messages.get("expense.status." + ex.getStatus());
        String operationLabel = messages.get(ex.getOperationKey());
        redirectAttributes.addFlashAttribute(
                "errorMessage",
                messages.get("expense.error.invalidState", statusLabel, operationLabel));
        return listRedirect(request);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public String handleInvalidPassword(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", messages.get("password.error.current"));
        return "redirect:/password/change";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied() {
        return "redirect:/403";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        String servletPath = requestPath(request);
        if (servletPath.startsWith("/password")) {
            return "redirect:/password/change";
        }
        if ("/expenses".equals(servletPath)) {
            return "redirect:/expenses/new";
        }
        if (servletPath.matches("/expenses/\\d+")) {
            return "redirect:" + servletPath + "/edit";
        }
        if (servletPath.matches("/expenses/\\d+/receipts")) {
            return "redirect:" + servletPath.replaceAll("/receipts$", "");
        }
        return listRedirect(request);
    }

    private String redirectAfterConflict(HttpServletRequest request) {
        String servletPath = requestPath(request);
        if (servletPath.matches("/approvals/\\d+/approve")
                || servletPath.matches("/approvals/\\d+/reject")) {
            return "redirect:" + servletPath.replaceAll("/(approve|reject)$", "");
        }
        if (servletPath.startsWith("/approvals/")) {
            return "redirect:/approvals";
        }
        if (servletPath.startsWith("/expenses/")) {
            return "redirect:" + servletPath.replaceAll("/(submit|delete|receipts)$", "");
        }
        return listRedirect(request);
    }

    private String listRedirect(HttpServletRequest request) {
        if (requestPath(request).startsWith("/approvals")) {
            return "redirect:/approvals";
        }
        return "redirect:/expenses";
    }

    private String requestPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (servletPath == null || servletPath.isEmpty()) {
            String uri = request.getRequestURI();
            return uri == null ? "" : uri;
        }
        return servletPath;
    }
}
