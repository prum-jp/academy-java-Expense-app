package com.example.expenseapp.entity;

public enum ApprovalAction {

    SUBMIT,
    APPROVE,
    REJECT;

    public boolean isDecision() {
        return this == APPROVE || this == REJECT;
    }
}
