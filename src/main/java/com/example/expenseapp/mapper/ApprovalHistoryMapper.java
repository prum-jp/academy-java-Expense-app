package com.example.expenseapp.mapper;

import com.example.expenseapp.entity.ApprovalHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApprovalHistoryMapper {

    int insert(ApprovalHistory history);

    List<ApprovalHistory> findByExpenseId(@Param("expenseId") Integer expenseId);
}
