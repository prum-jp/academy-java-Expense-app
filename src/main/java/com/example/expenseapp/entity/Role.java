package com.example.expenseapp.entity;

public enum Role {

    USER,
    APPROVER;

    public String authority() {
        return "ROLE_" + name();
    }
}
