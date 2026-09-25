package com.example.expenseapp.service.dto;

import java.util.ArrayList;
import java.util.List;

public class ExpenseSaveCommand {

    private Integer expenseId;
    private Integer version;
    private String title;
    private List<ExpenseDetailInput> details = new ArrayList<>();

    public Integer getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Integer expenseId) {
        this.expenseId = expenseId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ExpenseDetailInput> getDetails() {
        return details;
    }

    public void setDetails(List<ExpenseDetailInput> details) {
        this.details = details == null ? new ArrayList<>() : details;
    }
}
