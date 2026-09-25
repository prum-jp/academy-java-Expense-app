package com.example.expenseapp.controller.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class ExpenseDetailForm {

    @NotNull(message = "{validation.expenseDate.required}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expenseDate;

    @NotNull(message = "{validation.category.invalid}")
    private Integer categoryId;

    @NotNull(message = "{validation.tax.invalid}")
    private Integer taxId;

    @NotNull(message = "{validation.amount.invalid}")
    @Min(value = 0, message = "{validation.amount.invalid}")
    private Integer amount;

    @Size(max = 255, message = "{validation.memo.max}")
    private String memo;

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getTaxId() {
        return taxId;
    }

    public void setTaxId(Integer taxId) {
        this.taxId = taxId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
