package com.example.expenseapp.entity;

import java.math.BigDecimal;

public class TaxRate {

    private Integer taxId;
    private BigDecimal ratePercent;
    private String description;

    public Integer getTaxId() {
        return taxId;
    }

    public void setTaxId(Integer taxId) {
        this.taxId = taxId;
    }

    public BigDecimal getRatePercent() {
        return ratePercent;
    }

    public void setRatePercent(BigDecimal ratePercent) {
        this.ratePercent = ratePercent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
