package com.example.expenseapp.support;

import com.example.expenseapp.entity.Expense;
import com.example.expenseapp.entity.User;
import com.example.expenseapp.mapper.UserMapper;
import com.example.expenseapp.security.LoginUser;
import com.example.expenseapp.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractMySqlIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("expense_db")
            .withUsername("expense_user")
            .withPassword("expense_pass")
            .withInitScript("db/init.sql");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ExpenseService expenseServiceSupport;

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    protected LoginUser loginAs(String userName) {
        User user = userMapper.findByUserName(userName)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userName));
        LoginUser loginUser = new LoginUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities()));
        return loginUser;
    }

    protected Expense submitWithReceipt(LoginUser user, Expense draft) {
        try {
            Expense withReceipt = expenseServiceSupport.attachReceipt(
                    draft.getExpenseId(),
                    user,
                    draft.getVersion(),
                    ExpenseTestDataFactory.pngReceipt());
            return expenseServiceSupport.submit(withReceipt.getExpenseId(), user, withReceipt.getVersion());
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
