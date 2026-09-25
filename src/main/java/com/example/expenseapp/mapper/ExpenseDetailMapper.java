package com.example.expenseapp.mapper;

import com.example.expenseapp.entity.ExpenseDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExpenseDetailMapper {

    int insert(ExpenseDetail detail);

    int insertBatch(@Param("expenseId") Integer expenseId, @Param("details") List<ExpenseDetail> details);

    int deleteByExpenseId(@Param("expenseId") Integer expenseId);
}
