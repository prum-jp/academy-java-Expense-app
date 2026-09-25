package com.example.expenseapp.service;

import com.example.expenseapp.support.AbstractMySqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class MasterDataServiceIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private MasterDataService masterDataService;

    @Test
    void loadsSeedCategories() {
        assertThat(masterDataService.findAllCategories())
                .hasSize(5)
                .extracting("categoryName")
                .contains("交通費", "会議費", "宿泊費", "消耗品費", "その他");
    }

    @Test
    void loadsSeedTaxRates() {
        assertThat(masterDataService.findAllTaxRates())
                .hasSize(3)
                .extracting("ratePercent")
                .containsExactlyInAnyOrder(
                        new java.math.BigDecimal("10.00"),
                        new java.math.BigDecimal("8.00"),
                        new java.math.BigDecimal("0.00"));
    }
}
