package com.example.expenseapp.support;

import com.example.expenseapp.service.dto.ExpenseDetailInput;
import com.example.expenseapp.service.dto.ExpenseSaveCommand;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ExpenseTestDataFactory {

    private ExpenseTestDataFactory() {
    }

    public static ExpenseSaveCommand sampleCommand(String title) {
        return commandWithAmounts(title, 1500);
    }

    public static ExpenseSaveCommand commandWithAmounts(String title, int... amounts) {
        ExpenseSaveCommand command = new ExpenseSaveCommand();
        command.setTitle(title);
        List<ExpenseDetailInput> details = new ArrayList<>();
        for (int amount : amounts) {
            details.add(detail(amount, "テスト明細 " + amount + "円"));
        }
        command.setDetails(details);
        return command;
    }

    public static ExpenseSaveCommand commandWithInvalidCategory(String title) {
        ExpenseSaveCommand command = sampleCommand(title);
        command.getDetails().getFirst().setCategoryId(9999);
        return command;
    }

    public static ExpenseSaveCommand commandWithInvalidTax(String title) {
        ExpenseSaveCommand command = sampleCommand(title);
        command.getDetails().getFirst().setTaxId(9999);
        return command;
    }

    public static ExpenseSaveCommand commandWithoutDetails(String title) {
        ExpenseSaveCommand command = new ExpenseSaveCommand();
        command.setTitle(title);
        command.setDetails(List.of());
        return command;
    }

    public static MockMultipartFile pngReceipt() {
        return new MockMultipartFile(
                "file",
                "receipt.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
    }

    private static ExpenseDetailInput detail(int amount, String memo) {
        ExpenseDetailInput detail = new ExpenseDetailInput();
        detail.setExpenseDate(LocalDate.now());
        detail.setCategoryId(1);
        detail.setTaxId(1);
        detail.setAmount(amount);
        detail.setMemo(memo);
        return detail;
    }
}
