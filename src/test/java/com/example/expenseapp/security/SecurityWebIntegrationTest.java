package com.example.expenseapp.security;

import com.example.expenseapp.support.AbstractMySqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class SecurityWebIntegrationTest extends AbstractMySqlIntegrationTest {

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/expenses"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login/login"));
    }

    @Test
    void applicantLoginRedirectsToExpenses() throws Exception {
        mockMvc.perform(formLogin("/login").user("yamada").password("password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses"));
    }

    @Test
    void approverLoginRedirectsToApprovals() throws Exception {
        mockMvc.perform(formLogin("/login").user("sato").password("password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/approvals"));
    }

    @Test
    @WithUserDetails("yamada")
    void userCanAccessExpenseList() throws Exception {
        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(view().name("expenses/list"));
    }

    @Test
    @WithUserDetails("yamada")
    void userCannotAccessApprovalList() throws Exception {
        mockMvc.perform(get("/approvals"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/403"));
    }

    @Test
    @WithUserDetails("sato")
    void approverCanAccessApprovalList() throws Exception {
        mockMvc.perform(get("/approvals"))
                .andExpect(status().isOk())
                .andExpect(view().name("approvals/list"));
    }

    @Test
    @WithUserDetails("sato")
    void approverCannotAccessExpenseList() throws Exception {
        mockMvc.perform(get("/expenses"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/403"));
    }

    @Test
    @WithUserDetails("yamada")
    void userCanAccessPasswordChangePage() throws Exception {
        mockMvc.perform(get("/password/change"))
                .andExpect(status().isOk())
                .andExpect(view().name("password/change"));
    }

    @Test
    @WithUserDetails("yamada")
    void ownerCanOpenOwnExpenseDetail() throws Exception {
        mockMvc.perform(get("/expenses/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("expenses/detail"));
    }

    @Test
    @WithUserDetails("takahashi")
    void otherUserCannotOpenExpenseDetail() throws Exception {
        mockMvc.perform(get("/expenses/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/403"));
    }

    @Test
    @WithUserDetails("sato")
    void currentApproverCanOpenApprovalDetail() throws Exception {
        mockMvc.perform(get("/approvals/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("approvals/detail"));
    }

    @Test
    @WithUserDetails("suzuki")
    void nextStepApproverCannotOpenApprovalDetail() throws Exception {
        mockMvc.perform(get("/approvals/2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/403"));
    }
}
