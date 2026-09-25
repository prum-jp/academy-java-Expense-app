package com.example.expenseapp.controller;

import com.example.expenseapp.common.Messages;
import com.example.expenseapp.controller.form.PasswordChangeForm;
import com.example.expenseapp.entity.Role;
import com.example.expenseapp.entity.User;
import com.example.expenseapp.security.LoginUser;
import com.example.expenseapp.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class PasswordControllerTest {

    @Mock
    private PasswordService passwordService;

    @Mock
    private Messages messages;

    private PasswordController controller;

    @BeforeEach
    void setUp() {
        controller = new PasswordController(passwordService, messages);
    }

    @Test
    void changePasswordKeepsFormWhenServiceValidationFails() {
        doThrow(new IllegalArgumentException("新しいパスワードは現在のパスワードと異なる必要があります。"))
                .when(passwordService)
                .changePassword(anyInt(), any(), any(), any());

        PasswordChangeForm form = new PasswordChangeForm();
        form.setCurrentPassword("password");
        form.setNewPassword("password1");
        form.setConfirmPassword("password1");
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.changePassword(
                loginUser(),
                form,
                new BeanPropertyBindingResult(form, "passwordForm"),
                model,
                new RedirectAttributesModelMap());

        assertThat(view).isEqualTo("password/change");
        assertThat(model.getAttribute("errorMessage"))
                .isEqualTo("新しいパスワードは現在のパスワードと異なる必要があります。");
    }

    private static LoginUser loginUser() {
        User user = new User();
        user.setUserId(1);
        user.setUserName("yamada");
        user.setPassword("password");
        user.setRole(Role.USER);
        return new LoginUser(user);
    }
}
