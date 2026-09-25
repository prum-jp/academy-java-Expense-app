package com.example.expenseapp.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpenseStatusTest {

    @Test
    void editableStates() {
        assertThat(ExpenseStatus.DRAFT.isEditable()).isTrue();
        assertThat(ExpenseStatus.REJECTED.isEditable()).isTrue();
        assertThat(ExpenseStatus.PENDING.isEditable()).isFalse();
        assertThat(ExpenseStatus.APPROVED.isEditable()).isFalse();
    }

    @Test
    void pendingAndApprovedFlags() {
        assertThat(ExpenseStatus.PENDING.isPending()).isTrue();
        assertThat(ExpenseStatus.APPROVED.isApproved()).isTrue();
        assertThat(ExpenseStatus.REJECTED.isRejected()).isTrue();
        assertThat(ExpenseStatus.DRAFT.isDraft()).isTrue();
    }
}
