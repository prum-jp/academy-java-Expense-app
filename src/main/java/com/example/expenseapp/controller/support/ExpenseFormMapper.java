package com.example.expenseapp.controller.support;

import com.example.expenseapp.controller.form.ExpenseDetailForm;
import com.example.expenseapp.controller.form.ExpenseForm;
import com.example.expenseapp.entity.Expense;
import com.example.expenseapp.entity.ExpenseDetail;
import com.example.expenseapp.service.dto.ExpenseDetailInput;
import com.example.expenseapp.service.dto.ExpenseSaveCommand;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExpenseFormMapper {

    public ExpenseSaveCommand toCommand(ExpenseForm form) {
        ExpenseSaveCommand command = new ExpenseSaveCommand();
        command.setExpenseId(form.getExpenseId());
        command.setVersion(form.getVersion());
        command.setTitle(form.getTitle());
        command.setDetails(form.getDetails().stream()
                .map(this::toDetailInput)
                .collect(Collectors.toList()));
        return command;
    }

    public ExpenseForm fromExpense(Expense expense) {
        ExpenseForm form = new ExpenseForm();
        form.setExpenseId(expense.getExpenseId());
        form.setVersion(expense.getVersion());
        form.setEditMode(true);
        form.setTitle(expense.getTitle());
        form.setDetails(expense.getDetails().stream()
                .map(this::fromDetail)
                .collect(Collectors.toList()));
        return form;
    }

    public ExpenseForm newForm() {
        ExpenseForm form = new ExpenseForm();
        form.setEditMode(false);
        form.getDetails().add(new ExpenseDetailForm());
        return form;
    }

    private ExpenseDetailInput toDetailInput(ExpenseDetailForm form) {
        ExpenseDetailInput input = new ExpenseDetailInput();
        input.setExpenseDate(form.getExpenseDate());
        input.setCategoryId(form.getCategoryId());
        input.setTaxId(form.getTaxId());
        input.setAmount(form.getAmount());
        input.setMemo(form.getMemo());
        return input;
    }

    private ExpenseDetailForm fromDetail(ExpenseDetail detail) {
        ExpenseDetailForm form = new ExpenseDetailForm();
        form.setExpenseDate(detail.getExpenseDate());
        form.setCategoryId(detail.getCategoryId());
        form.setTaxId(detail.getTaxId());
        form.setAmount(detail.getAmount());
        form.setMemo(detail.getMemo());
        return form;
    }
}
