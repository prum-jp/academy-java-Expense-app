package com.example.expenseapp.service;

import com.example.expenseapp.entity.ExpenseCategory;
import com.example.expenseapp.entity.TaxRate;
import com.example.expenseapp.mapper.ExpenseCategoryMapper;
import com.example.expenseapp.mapper.TaxRateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MasterDataService {

    private final ExpenseCategoryMapper expenseCategoryMapper;
    private final TaxRateMapper taxRateMapper;

    public MasterDataService(
            ExpenseCategoryMapper expenseCategoryMapper,
            TaxRateMapper taxRateMapper) {
        this.expenseCategoryMapper = expenseCategoryMapper;
        this.taxRateMapper = taxRateMapper;
    }

    public List<ExpenseCategory> findAllCategories() {
        return expenseCategoryMapper.findAll();
    }

    public List<TaxRate> findAllTaxRates() {
        return taxRateMapper.findAll();
    }
}
