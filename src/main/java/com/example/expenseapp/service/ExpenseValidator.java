package com.example.expenseapp.service;

import com.example.expenseapp.common.Messages;
import com.example.expenseapp.mapper.ExpenseCategoryMapper;
import com.example.expenseapp.mapper.TaxRateMapper;
import com.example.expenseapp.service.dto.ExpenseDetailInput;
import com.example.expenseapp.service.dto.ExpenseSaveCommand;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class ExpenseValidator {

    private final ExpenseCategoryMapper expenseCategoryMapper;
    private final TaxRateMapper taxRateMapper;
    private final Messages messages;

    public ExpenseValidator(
            ExpenseCategoryMapper expenseCategoryMapper,
            TaxRateMapper taxRateMapper,
            Messages messages) {
        this.expenseCategoryMapper = expenseCategoryMapper;
        this.taxRateMapper = taxRateMapper;
        this.messages = messages;
    }

    public void validateSaveCommand(ExpenseSaveCommand command) {
        if (command == null) {
            throw validationError("validation.command.required");
        }
        if (!StringUtils.hasText(command.getTitle())) {
            throw validationError("validation.title.required");
        }
        if (command.getTitle().length() > 100) {
            throw validationError("validation.title.max");
        }
        List<ExpenseDetailInput> details = command.getDetails();
        if (details == null || details.isEmpty()) {
            throw validationError("validation.details.required");
        }
        for (ExpenseDetailInput detail : details) {
            validateDetail(detail);
        }
    }

    private void validateDetail(ExpenseDetailInput detail) {
        if (detail.getExpenseDate() == null) {
            throw validationError("validation.expenseDate.required");
        }
        if (detail.getCategoryId() == null
                || expenseCategoryMapper.findById(detail.getCategoryId()).isEmpty()) {
            throw validationError("validation.category.invalid");
        }
        if (detail.getTaxId() == null || taxRateMapper.findById(detail.getTaxId()).isEmpty()) {
            throw validationError("validation.tax.invalid");
        }
        if (detail.getAmount() == null || detail.getAmount() < 0) {
            throw validationError("validation.amount.invalid");
        }
        if (detail.getMemo() != null && detail.getMemo().length() > 255) {
            throw validationError("validation.memo.max");
        }
    }

    public int calculateTotalAmount(List<ExpenseDetailInput> details) {
        return details.stream()
                .mapToInt(ExpenseDetailInput::getAmount)
                .sum();
    }

    private IllegalArgumentException validationError(String code) {
        return new IllegalArgumentException(messages.get(code));
    }
}
