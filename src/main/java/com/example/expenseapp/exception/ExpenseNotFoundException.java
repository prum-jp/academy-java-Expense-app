package com.example.expenseapp.exception;

public class ExpenseNotFoundException extends RuntimeException {

    private final Integer expenseId;

    public ExpenseNotFoundException(Integer expenseId) {
        super("指定されたデータが見つかりません。");
        this.expenseId = expenseId;
    }

    public Integer getExpenseId() {
        return expenseId;
    }
}
