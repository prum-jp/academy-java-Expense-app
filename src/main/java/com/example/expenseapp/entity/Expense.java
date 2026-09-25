package com.example.expenseapp.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Expense {

    private Integer expenseId;
    private Integer userId;
    private String title;
    private ExpenseStatus status;
    private Integer totalAmount;
    private LocalDateTime submittedAt;
    private Integer version;
    private User applicant;
    private List<ExpenseDetail> details = new ArrayList<>();
    private List<ReceiptImage> receiptImages = new ArrayList<>();
    private List<ApprovalHistory> approvalHistories = new ArrayList<>();

    public Integer getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Integer expenseId) {
        this.expenseId = expenseId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ExpenseStatus getStatus() {
        return status;
    }

    public void setStatus(ExpenseStatus status) {
        this.status = status;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public User getApplicant() {
        return applicant;
    }

    public void setApplicant(User applicant) {
        this.applicant = applicant;
    }

    public List<ExpenseDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ExpenseDetail> details) {
        this.details = details == null ? new ArrayList<>() : details;
    }

    public List<ReceiptImage> getReceiptImages() {
        return receiptImages;
    }

    public void setReceiptImages(List<ReceiptImage> receiptImages) {
        this.receiptImages = receiptImages == null ? new ArrayList<>() : receiptImages;
    }

    public List<ApprovalHistory> getApprovalHistories() {
        return approvalHistories;
    }

    public void setApprovalHistories(List<ApprovalHistory> approvalHistories) {
        this.approvalHistories = approvalHistories == null ? new ArrayList<>() : approvalHistories;
    }
}
