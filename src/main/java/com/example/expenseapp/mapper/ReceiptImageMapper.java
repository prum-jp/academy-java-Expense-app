package com.example.expenseapp.mapper;

import com.example.expenseapp.entity.ReceiptImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReceiptImageMapper {

    int insert(ReceiptImage receiptImage);

    List<ReceiptImage> findByExpenseId(@Param("expenseId") Integer expenseId);

    int deleteByExpenseId(@Param("expenseId") Integer expenseId);

    int deleteByIdAndExpenseId(
            @Param("imageId") Integer imageId,
            @Param("expenseId") Integer expenseId);
}
