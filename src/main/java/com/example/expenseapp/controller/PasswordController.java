package com.example.expenseapp.controller;

import com.example.expenseapp.common.Messages;
import com.example.expenseapp.controller.form.PasswordChangeForm;
import com.example.expenseapp.security.LoginUser;
import com.example.expenseapp.service.PasswordService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/password")
public class PasswordController {

    private final PasswordService passwordService;
    private final Messages messages;

    public PasswordController(PasswordService passwordService, Messages messages) {
        this.passwordService = passwordService;
        this.messages = messages;
    }

    @GetMapping("/change")
    public String showChangeForm(@ModelAttribute("passwordForm") PasswordChangeForm form) {
        return "password/change";
    }

    @PostMapping("/change")
    public String changePassword(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @ModelAttribute("passwordForm") PasswordChangeForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "password/change";
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.error.mismatch");
            return "password/change";
        }

        try {
            passwordService.changePassword(
                    loginUser.getUserId(),
                    form.getCurrentPassword(),
                    form.getNewPassword(),
                    form.getConfirmPassword());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "password/change";
        }

        redirectAttributes.addFlashAttribute("successMessage", messages.get("password.success"));
        return "redirect:/password/change";
    }
}
