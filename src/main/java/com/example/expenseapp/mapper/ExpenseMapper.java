package com.example.expenseapp.mapper;

import com.example.expenseapp.entity.Expense;
import com.example.expenseapp.entity.ExpenseStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ExpenseMapper {

    int insert(Expense expense);

    int updateWithVersion(Expense expense);

    int updateById(Expense expense);

    int deleteByIdUserIdAndVersion(
            @Param("expenseId") Integer expenseId,
            @Param("userId") Integer userId,
            @Param("version") Integer version);

    int deleteByIdAndUserId(
            @Param("expenseId") Integer expenseId,
            @Param("userId") Integer userId);

    Optional<Expense> findById(Integer expenseId);

    Optional<Expense> findByIdAndUserId(
            @Param("expenseId") Integer expenseId,
            @Param("userId") Integer userId);

    Optional<Expense> findAccessibleByApprover(
            @Param("expenseId") Integer expenseId,
            @Param("approverUserId") Integer approverUserId);

    List<Expense> findMyExpenses(
            @Param("userId") Integer userId,
            @Param("keyword") String keyword,
            @Param("status") ExpenseStatus status);

    List<Expense> findPendingForApprover(
            @Param("approverUserId") Integer approverUserId,
            @Param("keyword") String keyword);

    List<Expense> findProcessedForApprover(
            @Param("approverUserId") Integer approverUserId,
            @Param("keyword") String keyword);
}
