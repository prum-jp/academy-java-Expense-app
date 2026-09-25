package com.example.expenseapp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.example.expenseapp.mapper")
@EnableTransactionManagement(order = Ordered.HIGHEST_PRECEDENCE)
public class ExpenseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseApplication.class, args);
    }
}
