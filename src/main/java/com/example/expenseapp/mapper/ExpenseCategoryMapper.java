package com.example.expenseapp.mapper;

import com.example.expenseapp.entity.ExpenseCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ExpenseCategoryMapper {

    Optional<ExpenseCategory> findById(Integer categoryId);

    List<ExpenseCategory> findAll();
}
