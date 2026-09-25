package com.example.expenseapp.controller.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class ExpenseForm {

    private Integer expenseId;
    private Integer version;
    private boolean editMode;

    @NotBlank(message = "{validation.title.required}")
    @Size(max = 100, message = "{validation.title.max}")
    private String title;

    @Valid
    @NotEmpty(message = "{validation.details.required}")
    private List<ExpenseDetailForm> details = new ArrayList<>();

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

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ExpenseDetailForm> getDetails() {
        return details;
    }

    public void setDetails(List<ExpenseDetailForm> details) {
        this.details = details == null ? new ArrayList<>() : details;
    }
}
