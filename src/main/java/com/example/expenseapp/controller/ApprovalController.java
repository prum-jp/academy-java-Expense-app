package com.example.expenseapp.controller;

import com.example.expenseapp.controller.form.ApprovalForm;
import com.example.expenseapp.security.ExpenseAuthorizationService;
import com.example.expenseapp.security.LoginUser;
import com.example.expenseapp.service.ApprovalService;
import com.example.expenseapp.common.Messages;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/approvals")
@PreAuthorize("hasRole('APPROVER')")
public class ApprovalController {

    private final ApprovalService approvalService;
    private final ExpenseAuthorizationService expenseAuthorizationService;
    private final Messages messages;

    public ApprovalController(
            ApprovalService approvalService,
            ExpenseAuthorizationService expenseAuthorizationService,
            Messages messages) {
        this.approvalService = approvalService;
        this.expenseAuthorizationService = expenseAuthorizationService;
        this.messages = messages;
    }

    @GetMapping({"", "/"})
    public String list(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {
        model.addAttribute("expenses", approvalService.findPendingExpenses(loginUser, keyword));
        model.addAttribute("processedExpenses", approvalService.findProcessedExpenses(loginUser, keyword));
        model.addAttribute("keyword", keyword);
        return "approvals/list";
    }

    @GetMapping("/{expenseId}")
    @PreAuthorize("@expenseAuthorizationService.isRouteMember(#expenseId, authentication.principal)")
    public String detail(
            @PathVariable Integer expenseId,
            @AuthenticationPrincipal LoginUser loginUser,
            Model model) {
        var expense = approvalService.getExpenseForApprover(expenseId, loginUser);
        model.addAttribute("expense", expense);
        model.addAttribute("canApprove", expenseAuthorizationService.canApprove(expenseId, loginUser));
        if (!model.containsAttribute("approvalForm")) {
            model.addAttribute("approvalForm", new ApprovalForm(expense.getVersion()));
        }
        return "approvals/detail";
    }

    @PostMapping("/{expenseId}/approve")
    @PreAuthorize("@expenseAuthorizationService.isRouteMember(#expenseId, authentication.principal)")
    public String approve(
            @PathVariable Integer expenseId,
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @ModelAttribute("approvalForm") ApprovalForm approvalForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.approvalForm", bindingResult);
            redirectAttributes.addFlashAttribute("approvalForm", approvalForm);
            return "redirect:/approvals/" + expenseId;
        }

        approvalService.approve(expenseId, loginUser, approvalForm.getVersion(), approvalForm.getComment());
        redirectAttributes.addFlashAttribute(
                "successMessage",
                messages.get("approval.approve.success"));
        return redirectAfterApprovalAction(expenseId, loginUser);
    }

    @PostMapping("/{expenseId}/reject")
    @PreAuthorize("@expenseAuthorizationService.isRouteMember(#expenseId, authentication.principal)")
    public String reject(
            @PathVariable Integer expenseId,
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @ModelAttribute("approvalForm") ApprovalForm approvalForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.approvalForm", bindingResult);
            redirectAttributes.addFlashAttribute("approvalForm", approvalForm);
            return "redirect:/approvals/" + expenseId;
        }

        try {
            approvalService.reject(expenseId, loginUser, approvalForm.getVersion(), approvalForm.getComment());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/approvals/" + expenseId;
        }
        redirectAttributes.addFlashAttribute(
                "successMessage",
                messages.get("approval.reject.success"));
        return redirectAfterApprovalAction(expenseId, loginUser);
    }

    private String redirectAfterApprovalAction(Integer expenseId, LoginUser loginUser) {
        if (expenseAuthorizationService.canView(expenseId, loginUser)) {
            return "redirect:/approvals/" + expenseId;
        }
        return "redirect:/approvals";
    }
}
