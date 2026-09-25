package com.example.expenseapp.service;

import com.example.expenseapp.entity.ApprovalAction;
import com.example.expenseapp.entity.ApprovalHistory;
import com.example.expenseapp.mapper.ApprovalHistoryMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ApprovalHistoryRecorder {

    private final ApprovalHistoryMapper approvalHistoryMapper;

    public ApprovalHistoryRecorder(ApprovalHistoryMapper approvalHistoryMapper) {
        this.approvalHistoryMapper = approvalHistoryMapper;
    }

    /**
     * @param actorUserId SUBMIT 時は申請者、APPROVE/REJECT 時は承認者
     */
    public void record(Integer expenseId, Integer actorUserId, ApprovalAction action, String comment) {
        ApprovalHistory history = new ApprovalHistory();
        history.setExpenseId(expenseId);
        history.setApproverUserId(actorUserId);
        history.setAction(action);
        history.setComment(comment);
        history.setCreatedAt(LocalDateTime.now());
        approvalHistoryMapper.insert(history);
    }
}
