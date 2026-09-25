package com.example.expenseapp.controller;

import com.example.expenseapp.controller.form.ExpenseForm;
import com.example.expenseapp.controller.support.ExpenseFormMapper;
import com.example.expenseapp.entity.ExpenseStatus;
import com.example.expenseapp.security.LoginUser;
import com.example.expenseapp.service.ExpenseService;
import com.example.expenseapp.service.MasterDataService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/expenses")
@PreAuthorize("hasRole('USER')")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final MasterDataService masterDataService;
    private final ExpenseFormMapper expenseFormMapper;
    private final Messages messages;

    public ExpenseController(
            ExpenseService expenseService,
            MasterDataService masterDataService,
            ExpenseFormMapper expenseFormMapper,
            Messages messages) {
        this.expenseService = expenseService;
        this.masterDataService = masterDataService;
        this.expenseFormMapper = expenseFormMapper;
        this.messages = messages;
    }

    @GetMapping
    public String list(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) ExpenseStatus status,
            Model model) {
        model.addAttribute("expenses", expenseService.findMyExpenses(loginUser, keyword, status));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", ExpenseStatus.values());
        return "expenses/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        prepareFormModel(model, expenseFormMapper.newForm());
        return "expenses/form";
    }

    @PostMapping
    public String create(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @ModelAttribute("expenseForm") ExpenseForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareFormModel(model, form);
            return "expenses/form";
        }

        try {
            var expense = expenseService.createDraft(loginUser, expenseFormMapper.toCommand(form));
            redirectAttributes.addFlashAttribute("successMessage", messages.get("expense.save.success"));
            return "redirect:/expenses/" + expense.getExpenseId();
        } catch (IllegalArgumentException ex) {
            return rejectForm(model, form, ex.getMessage());
        }
    }

    @GetMapping("/{expenseId}")
    @PreAuthorize("@expenseAuthorizationService.isOwner(#expenseId, authentication.principal)")
    public String detail(
            @PathVariable Integer expenseId,
            @AuthenticationPrincipal LoginUser loginUser,
            Model model) {
        var expense = expenseService.getExpenseForOwner(expenseId, loginUser);
        model.addAttribute("expense", expense);
        model.addAttribute("editable", expense.getStatus().isEditable());
        return "expenses/detail";
    }

    @GetMapping("/{expenseId}/edit")
    @PreAuthorize("@expenseAuthorizationService.canEdit(#expenseId, authentication.principal)")
    public String showEditForm(
            @PathVariable Integer expenseId,
            @AuthenticationPrincipal LoginUser loginUser,
            Model model) {
        var expense = expenseService.getExpenseForOwner(expenseId, loginUser);
        if (!expense.getStatus().isEditable()) {
            return "redirect:/expenses/" + expenseId;
        }
        prepareFormModel(model, expenseFormMapper.fromExpense(expense));
        return "expenses/form";
    }

    @PostMapping("/{expenseId}")
    @PreAuthorize("@expenseAuthorizationService.canEdit(#expenseId, authentication.principal)")
    public String update(
            @PathVariable Integer expenseId,
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @ModelAttribute("expenseForm") ExpenseForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        form.setExpenseId(expenseId);
        if (bindingResult.hasErrors()) {
            form.setEditMode(true);
            prepareFormModel(model, form);
            return "expenses/form";
        }

        try {
            expenseService.updateDraft(expenseId, loginUser, expenseFormMapper.toCommand(form));
            redirectAttributes.addFlashAttribute("successMessage", messages.get("expense.save.success"));
            return "redirect:/expenses/" + expenseId;
        } catch (IllegalArgumentException ex) {
            form.setEditMode(true);
            return rejectForm(model, form, ex.getMessage());
        }
    }

    @PostMapping("/{expenseId}/delete")
    @PreAuthorize("@expenseAuthorizationService.canEdit(#expenseId, authentication.principal)")
    public String delete(
            @PathVariable Integer expenseId,
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam Integer version,
            RedirectAttributes redirectAttributes) {
        expenseService.deleteDraft(expenseId, loginUser, version);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                messages.get("expense.delete.success"));
        return "redirect:/expenses";
    }

    @PostMapping("/{expenseId}/submit")
    @PreAuthorize("@expenseAuthorizationService.canEdit(#expenseId, authentication.principal)")
    public String submit(
            @PathVariable Integer expenseId,
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam Integer version,
            RedirectAttributes redirectAttributes) {
        try {
            expenseService.submit(expenseId, loginUser, version);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    messages.get("expense.submit.success"));
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/expenses/" + expenseId;
    }

    @PostMapping("/{expenseId}/receipts")
    @PreAuthorize("@expenseAuthorizationService.canEdit(#expenseId, authentication.principal)")
    public String uploadReceipt(
            @PathVariable Integer expenseId,
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam Integer version,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            expenseService.attachReceipt(expenseId, loginUser, version, file);
            redirectAttributes.addFlashAttribute("successMessage", messages.get("expense.save.success"));
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", messages.get("error.file.upload"));
        }
        return "redirect:/expenses/" + expenseId;
    }

    @PostMapping("/{expenseId}/receipts/{imageId}/delete")
    @PreAuthorize("@expenseAuthorizationService.canEdit(#expenseId, authentication.principal)")
    public String deleteReceipt(
            @PathVariable Integer expenseId,
            @PathVariable Integer imageId,
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam Integer version,
            RedirectAttributes redirectAttributes) {
        try {
            expenseService.deleteReceipt(expenseId, loginUser, version, imageId);
            redirectAttributes.addFlashAttribute("successMessage", messages.get("expense.receipt.delete.success"));
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/expenses/" + expenseId;
    }

    private String rejectForm(Model model, ExpenseForm form, String errorMessage) {
        model.addAttribute("errorMessage", errorMessage);
        prepareFormModel(model, form);
        return "expenses/form";
    }

    private void prepareFormModel(Model model, ExpenseForm form) {
        model.addAttribute("expenseForm", form);
        model.addAttribute("categories", masterDataService.findAllCategories());
        model.addAttribute("taxRates", masterDataService.findAllTaxRates());
    }
}
