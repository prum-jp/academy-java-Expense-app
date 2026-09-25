package com.example.expenseapp.service;

import com.example.expenseapp.audit.AuditLogService;
import com.example.expenseapp.audit.Audited;
import com.example.expenseapp.entity.ApprovalAction;
import com.example.expenseapp.entity.Expense;
import com.example.expenseapp.entity.ExpenseDetail;
import com.example.expenseapp.entity.ExpenseStatus;
import com.example.expenseapp.entity.ReceiptImage;
import com.example.expenseapp.exception.ExpenseNotFoundException;
import com.example.expenseapp.exception.InvalidExpenseStateException;
import com.example.expenseapp.exception.OptimisticLockException;
import com.example.expenseapp.mapper.ExpenseDetailMapper;
import com.example.expenseapp.mapper.ExpenseMapper;
import com.example.expenseapp.mapper.ReceiptImageMapper;
import com.example.expenseapp.security.LoginUser;
import com.example.expenseapp.service.dto.ExpenseDetailInput;
import com.example.expenseapp.service.dto.ExpenseSaveCommand;
import com.example.expenseapp.common.LikeKeywords;
import com.example.expenseapp.common.Messages;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseMapper expenseMapper;
    private final ExpenseDetailMapper expenseDetailMapper;
    private final ReceiptImageMapper receiptImageMapper;
    private final ExpenseValidator expenseValidator;
    private final ApprovalHistoryRecorder approvalHistoryRecorder;
    private final ReceiptStorageService receiptStorageService;
    private final Messages messages;

    public ExpenseService(
            ExpenseMapper expenseMapper,
            ExpenseDetailMapper expenseDetailMapper,
            ReceiptImageMapper receiptImageMapper,
            ExpenseValidator expenseValidator,
            ApprovalHistoryRecorder approvalHistoryRecorder,
            ReceiptStorageService receiptStorageService,
            Messages messages) {
        this.expenseMapper = expenseMapper;
        this.expenseDetailMapper = expenseDetailMapper;
        this.receiptImageMapper = receiptImageMapper;
        this.expenseValidator = expenseValidator;
        this.approvalHistoryRecorder = approvalHistoryRecorder;
        this.receiptStorageService = receiptStorageService;
        this.messages = messages;
    }

    @Transactional(readOnly = true)
    public List<Expense> findMyExpenses(LoginUser user, String keyword, ExpenseStatus status) {
        return expenseMapper.findMyExpenses(user.getUserId(), LikeKeywords.escape(keyword), status);
    }

    @Transactional(readOnly = true)
    public Expense getExpenseForOwner(Integer expenseId, LoginUser user) {
        return requireOwnedExpense(expenseId, user.getUserId());
    }

    @Audited(action = AuditLogService.ACTION_EXPENSE_CREATE, targetTable = AuditLogService.TABLE_EXPENSES)
    public Expense createDraft(LoginUser user, ExpenseSaveCommand command) {
        expenseValidator.validateSaveCommand(command);

        Expense expense = new Expense();
        expense.setUserId(user.getUserId());
        expense.setTitle(command.getTitle().trim());
        expense.setStatus(ExpenseStatus.DRAFT);
        expense.setTotalAmount(expenseValidator.calculateTotalAmount(command.getDetails()));
        expense.setSubmittedAt(null);
        expenseMapper.insert(expense);

        saveDetails(expense.getExpenseId(), command.getDetails());
        return requireOwnedExpense(expense.getExpenseId(), user.getUserId());
    }

    @Audited(action = AuditLogService.ACTION_EXPENSE_UPDATE, targetTable = AuditLogService.TABLE_EXPENSES, idParam = "expenseId")
    public Expense updateDraft(Integer expenseId, LoginUser user, ExpenseSaveCommand command) {
        expenseValidator.validateSaveCommand(command);
        Expense existing = requireEditableOwnedExpense(expenseId, user.getUserId());
        assertVersion(existing, command.getVersion());

        existing.setTitle(command.getTitle().trim());
        existing.setTotalAmount(expenseValidator.calculateTotalAmount(command.getDetails()));
        assertUpdated(expenseMapper.updateWithVersion(existing));

        expenseDetailMapper.deleteByExpenseId(expenseId);
        saveDetails(expenseId, command.getDetails());
        return requireOwnedExpense(expenseId, user.getUserId());
    }

    @Audited(action = AuditLogService.ACTION_EXPENSE_DELETE, targetTable = AuditLogService.TABLE_EXPENSES, idParam = "expenseId")
    public void deleteDraft(Integer expenseId, LoginUser user, Integer version) {
        Expense existing = requireEditableOwnedExpense(expenseId, user.getUserId());
        assertUpdated(expenseMapper.deleteByIdAndUserId(expenseId, user.getUserId()));
        receiptStorageService.deleteExpenseFiles(expenseId);
    }

    @Audited(action = AuditLogService.ACTION_EXPENSE_SUBMIT, targetTable = AuditLogService.TABLE_EXPENSES, idParam = "expenseId")
    public Expense submit(Integer expenseId, LoginUser user, Integer version) {
        Expense existing = requireOwnedExpense(expenseId, user.getUserId());
        if (!existing.getStatus().isEditable()) {
            throw new InvalidExpenseStateException(existing.getStatus(), "expense.operation.submit");
        }
        if (existing.getDetails().isEmpty()) {
            throw new IllegalArgumentException(messages.get("validation.details.required"));
        }

        existing.setStatus(ExpenseStatus.PENDING);
        existing.setSubmittedAt(LocalDateTime.now());
        assertUpdated(expenseMapper.updateById(existing));

        approvalHistoryRecorder.record(expenseId, user.getUserId(), ApprovalAction.SUBMIT, null);
        return requireOwnedExpense(expenseId, user.getUserId());
    }

    @Audited(action = AuditLogService.ACTION_EXPENSE_UPDATE, targetTable = AuditLogService.TABLE_EXPENSES, idParam = "expenseId")
    public Expense attachReceipt(Integer expenseId, LoginUser user, Integer version, MultipartFile file)
            throws IOException {
        Expense existing = requireEditableOwnedExpense(expenseId, user.getUserId());

        String filePath = receiptStorageService.store(expenseId, file);
        ReceiptImage image = new ReceiptImage();
        image.setExpenseId(expenseId);
        image.setFilePath(filePath);
        image.setUploadedAt(LocalDateTime.now());
        receiptImageMapper.insert(image);

        assertUpdated(expenseMapper.updateById(existing));
        return requireOwnedExpense(expenseId, user.getUserId());
    }

    @Audited(action = AuditLogService.ACTION_EXPENSE_UPDATE, targetTable = AuditLogService.TABLE_EXPENSES, idParam = "expenseId")
    public Expense deleteReceipt(Integer expenseId, LoginUser user, Integer version, Integer imageId) {
        Expense existing = requireEditableOwnedExpense(expenseId, user.getUserId());

        ReceiptImage image = existing.getReceiptImages().stream()
                .filter(item -> item.getImageId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(messages.get("error.notFound")));
        if (receiptImageMapper.deleteByIdAndExpenseId(imageId, expenseId) != 1) {
            throw new IllegalArgumentException(messages.get("error.notFound"));
        }
        receiptStorageService.deleteFile(image.getFilePath());

        assertUpdated(expenseMapper.updateById(existing));
        return requireOwnedExpense(expenseId, user.getUserId());
    }

    private Expense requireOwnedExpense(Integer expenseId, Integer userId) {
        return expenseMapper.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));
    }

    private Expense requireEditableOwnedExpense(Integer expenseId, Integer userId) {
        Expense expense = requireOwnedExpense(expenseId, userId);
        if (!expense.getStatus().isEditable()) {
            throw new InvalidExpenseStateException(expense.getStatus(), "expense.operation.edit");
        }
        return expense;
    }

    private void saveDetails(Integer expenseId, List<ExpenseDetailInput> inputs) {
        List<ExpenseDetail> details = inputs.stream()
                .map(input -> toDetail(expenseId, input))
                .collect(Collectors.toList());
        expenseDetailMapper.insertBatch(expenseId, details);
    }

    private ExpenseDetail toDetail(Integer expenseId, ExpenseDetailInput input) {
        ExpenseDetail detail = new ExpenseDetail();
        detail.setExpenseId(expenseId);
        detail.setExpenseDate(input.getExpenseDate());
        detail.setCategoryId(input.getCategoryId());
        detail.setTaxId(input.getTaxId());
        detail.setAmount(input.getAmount());
        detail.setMemo(input.getMemo());
        return detail;
    }

    private void assertVersion(Expense existing, Integer version) {
        if (version == null || !version.equals(existing.getVersion())) {
            throw new OptimisticLockException();
        }
    }

    private void assertUpdated(int rows) {
        if (rows != 1) {
            throw new OptimisticLockException();
        }
    }

}
