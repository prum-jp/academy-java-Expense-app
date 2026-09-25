package com.example.expenseapp.controller.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ApprovalForm {

    @NotNull
    private Integer version;

    @Size(max = 500)
    private String comment;

    public ApprovalForm() {
    }

    public ApprovalForm(Integer version) {
        this.version = version;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
