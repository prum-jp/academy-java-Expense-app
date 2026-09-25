package com.example.expenseapp.mapper;

import com.example.expenseapp.entity.TaxRate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TaxRateMapper {

    Optional<TaxRate> findById(Integer taxId);

    List<TaxRate> findAll();
}
