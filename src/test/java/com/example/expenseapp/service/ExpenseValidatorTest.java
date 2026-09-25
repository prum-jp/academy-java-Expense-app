package com.example.expenseapp.service;

import com.example.expenseapp.common.Messages;
import com.example.expenseapp.mapper.ExpenseCategoryMapper;
import com.example.expenseapp.mapper.TaxRateMapper;
import com.example.expenseapp.service.dto.ExpenseSaveCommand;
import com.example.expenseapp.support.ExpenseTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseValidatorTest {

    @Mock
    private ExpenseCategoryMapper expenseCategoryMapper;

    @Mock
    private TaxRateMapper taxRateMapper;

    @Mock
    private MessageSource messageSource;

    private ExpenseValidator expenseValidator;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.JAPAN);
        expenseValidator = new ExpenseValidator(
                expenseCategoryMapper, taxRateMapper, new Messages(messageSource));
        lenient().when(messageSource.getMessage(anyString(), isNull(), eq(Locale.JAPAN)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void rejectsBlankTitle() {
        ExpenseSaveCommand command = ExpenseTestDataFactory.sampleCommand("");
        command.setTitle("");

        assertThatThrownBy(() -> expenseValidator.validateSaveCommand(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("validation.title.required");
    }

    @Test
    void rejectsMissingDetails() {
        ExpenseSaveCommand command = ExpenseTestDataFactory.commandWithoutDetails("明細なし");

        assertThatThrownBy(() -> expenseValidator.validateSaveCommand(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("validation.details.required");
    }

    @Test
    void rejectsInvalidCategory() {
        ExpenseSaveCommand command = ExpenseTestDataFactory.commandWithInvalidCategory("科目不正");
        when(expenseCategoryMapper.findById(9999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseValidator.validateSaveCommand(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("validation.category.invalid");
    }

    @Test
    void rejectsInvalidTaxRate() {
        ExpenseSaveCommand command = ExpenseTestDataFactory.commandWithInvalidTax("税率不正");
        when(expenseCategoryMapper.findById(1)).thenReturn(Optional.of(new com.example.expenseapp.entity.ExpenseCategory()));
        when(taxRateMapper.findById(9999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseValidator.validateSaveCommand(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("validation.tax.invalid");
    }

    @Test
    void calculateTotalAmountSumsDetails() {
        ExpenseSaveCommand command = ExpenseTestDataFactory.commandWithAmounts("合計", 1000, 2500, 500);

        assertThat(expenseValidator.calculateTotalAmount(command.getDetails())).isEqualTo(4000);
    }

    @Test
    void acceptsValidCommand() {
        ExpenseSaveCommand command = ExpenseTestDataFactory.sampleCommand("正常");
        when(expenseCategoryMapper.findById(1)).thenReturn(Optional.of(new com.example.expenseapp.entity.ExpenseCategory()));
        when(taxRateMapper.findById(1)).thenReturn(Optional.of(new com.example.expenseapp.entity.TaxRate()));

        expenseValidator.validateSaveCommand(command);
    }
}
