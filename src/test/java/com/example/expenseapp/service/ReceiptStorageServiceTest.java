package com.example.expenseapp.service;

import com.example.expenseapp.common.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ReceiptStorageServiceTest {

    private static final byte[] PNG_BYTES = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPEG_BYTES = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
    private static final byte[] PDF_BYTES = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34};

    @Mock
    private MessageSource messageSource;

    @TempDir
    Path tempDir;

    private ReceiptStorageService receiptStorageService;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(java.util.Locale.JAPAN);
        lenient().when(messageSource.getMessage(any(String.class), isNull(), eq(java.util.Locale.JAPAN)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        receiptStorageService = new ReceiptStorageService(tempDir.toString(), new Messages(messageSource));
    }

    @Test
    void storeCreatesFileUnderExpenseDirectory() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "receipt.png", "image/png", PNG_BYTES);

        String storedPath = receiptStorageService.store(42, file);

        assertThat(storedPath).startsWith("42/");
        assertThat(storedPath).endsWith(".png");
        assertThat(Files.exists(tempDir.resolve(storedPath))).isTrue();
        assertThat(receiptStorageService.mediaTypeFor(storedPath)).isEqualTo(MediaType.IMAGE_PNG);
    }

    @Test
    void storeRejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "receipt.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> receiptStorageService.store(1, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("validation.file.required");
    }

    @Test
    void storeRejectsUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "receipt.exe", "application/octet-stream", new byte[]{1});

        assertThatThrownBy(() -> receiptStorageService.store(1, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("validation.file.type");
    }

    @Test
    void storeRejectsMismatchedContent() {
        MockMultipartFile file = new MockMultipartFile("file", "receipt.png", "image/png", JPEG_BYTES);

        assertThatThrownBy(() -> receiptStorageService.store(1, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("validation.file.type");
    }

    @Test
    void mediaTypeUsesExtension() {
        assertThat(receiptStorageService.mediaTypeFor("a.jpg")).isEqualTo(MediaType.IMAGE_JPEG);
        assertThat(receiptStorageService.mediaTypeFor("a.jpeg")).isEqualTo(MediaType.IMAGE_JPEG);
        assertThat(receiptStorageService.mediaTypeFor("a.pdf")).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(receiptStorageService.mediaTypeFor("a.gif")).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Test
    void deleteExpenseFilesRemovesDirectory() throws Exception {
        receiptStorageService.store(7, new MockMultipartFile("file", "receipt.pdf", "application/pdf", PDF_BYTES));
        Path expenseDir = tempDir.resolve("7");
        assertThat(expenseDir).exists();

        receiptStorageService.deleteExpenseFiles(7);

        assertThat(expenseDir).doesNotExist();
    }

    @Test
    void deleteFileRemovesStoredReceipt() throws Exception {
        String storedPath = receiptStorageService.store(
                9, new MockMultipartFile("file", "receipt.png", "image/png", PNG_BYTES));
        Path stored = tempDir.resolve(storedPath);
        assertThat(stored).exists();

        receiptStorageService.deleteFile(storedPath);

        assertThat(stored).doesNotExist();
    }

    @Test
    void resolvePathRejectsPathTraversal() {
        assertThatThrownBy(() -> receiptStorageService.resolvePath("../secret.txt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("validation.file.path");
    }
}
