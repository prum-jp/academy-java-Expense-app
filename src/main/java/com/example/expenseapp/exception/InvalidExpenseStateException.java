package com.example.expenseapp.exception;

import com.example.expenseapp.entity.ExpenseStatus;

public class InvalidExpenseStateException extends RuntimeException {

    private final ExpenseStatus status;
    private final String operationKey;

    public InvalidExpenseStateException(ExpenseStatus status, String operationKey) {
        super("この状態では操作できません。");
        this.status = status;
        this.operationKey = operationKey;
    }

    public ExpenseStatus getStatus() {
        return status;
    }

    public String getOperationKey() {
        return operationKey;
    }
}
