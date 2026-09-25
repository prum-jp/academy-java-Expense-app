package com.example.expenseapp.entity;

import java.time.LocalDateTime;

/**
 * 経費申請に対する操作履歴。
 * <p>
 * DB列 {@code approver_user_id} は名称上「承認者」だが、実際には操作者の user_id を格納する。
 * {@link ApprovalAction#SUBMIT} の場合は申請者、{@link ApprovalAction#APPROVE} /
 * {@link ApprovalAction#REJECT} の場合は承認者となる。
 */
public class ApprovalHistory {

    private Integer historyId;
    private Integer expenseId;
    /** 操作者の user_id（SUBMIT 時は申請者、APPROVE/REJECT 時は承認者） */
    private Integer approverUserId;
    private ApprovalAction action;
    private String comment;
    private LocalDateTime createdAt;
    /** 操作者（approver_user_id に対応するユーザー） */
    private User approver;

    public Integer getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Integer historyId) {
        this.historyId = historyId;
    }

    public Integer getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Integer expenseId) {
        this.expenseId = expenseId;
    }

    public Integer getApproverUserId() {
        return approverUserId;
    }

    public void setApproverUserId(Integer approverUserId) {
        this.approverUserId = approverUserId;
    }

    public ApprovalAction getAction() {
        return action;
    }

    public void setAction(ApprovalAction action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }
}
