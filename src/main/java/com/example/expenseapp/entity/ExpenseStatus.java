package com.example.expenseapp.entity;

public enum ExpenseStatus {
    DRAFT,
    PENDING,
    APPROVED,
    REJECTED;

    public boolean isEditable() {
        return this == DRAFT || this == REJECTED;
    }

    public boolean isDraft() {
        return this == DRAFT;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isApproved() {
        return this == APPROVED;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }
}
