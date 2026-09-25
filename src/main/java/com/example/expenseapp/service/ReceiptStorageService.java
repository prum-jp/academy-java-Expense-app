package com.example.expenseapp.service;

import com.example.expenseapp.common.Messages;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class ReceiptStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf");
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PDF_SIGNATURE = {0x25, 0x50, 0x44, 0x46};

    private final Path receiptBaseDir;
    private final Messages messages;

    public ReceiptStorageService(
            @Value("${app.upload.receipt-dir}") String receiptDir,
            Messages messages) {
        this.receiptBaseDir = Path.of(receiptDir).toAbsolutePath().normalize();
        this.messages = messages;
    }

    public String store(Integer expenseId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(messages.get("validation.file.required"));
        }
        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(messages.get("validation.file.type"));
        }
        byte[] content = file.getBytes();
        if (!matchesSignature(content, extension)) {
            throw new IllegalArgumentException(messages.get("validation.file.type"));
        }

        Path expenseDir = receiptBaseDir.resolve(String.valueOf(expenseId));
        Files.createDirectories(expenseDir);

        String storedName = UUID.randomUUID() + "." + extension;
        Path destination = expenseDir.resolve(storedName).normalize();
        if (!destination.startsWith(expenseDir)) {
            throw new IllegalArgumentException(messages.get("validation.file.path"));
        }

        Files.write(destination, content);
        return receiptBaseDir.relativize(destination).toString().replace('\\', '/');
    }

    public void deleteFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return;
        }
    }

    public Path resolvePath(String filePath) {
        Path resolved = receiptBaseDir.resolve(filePath).normalize();
        if (!resolved.startsWith(receiptBaseDir)) {
            throw new IllegalArgumentException(messages.get("validation.file.path"));
        }
        return resolved;
    }

    public MediaType mediaTypeFor(String filename) {
        String extension = extractExtension(filename);
        return switch (extension) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "png" -> MediaType.IMAGE_PNG;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    public Path findFirstFile(Integer expenseId) {
        if (expenseId == null) {
            return null;
        }
        Path expenseDir = receiptBaseDir.resolve(String.valueOf(expenseId)).normalize();
        if (!expenseDir.startsWith(receiptBaseDir) || !Files.isDirectory(expenseDir)) {
            return null;
        }
        try (Stream<Path> paths = Files.list(expenseDir)) {
            return paths.filter(Files::isRegularFile).findFirst().orElse(null);
        } catch (IOException ignored) {
            return null;
        }
    }

    public void deleteExpenseFiles(Integer expenseId) {
        if (expenseId == null) {
            return;
        }
    }

    private boolean matchesSignature(byte[] content, String extension) {
        return switch (extension) {
            case "png" -> startsWith(content, PNG_SIGNATURE);
            case "jpg", "jpeg" -> startsWith(content, JPEG_SIGNATURE);
            case "pdf" -> startsWith(content, PDF_SIGNATURE);
            default -> false;
        };
    }

    private static boolean startsWith(byte[] content, byte[] signature) {
        if (content.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (content[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private String extractExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            throw new IllegalArgumentException(messages.get("validation.file.type"));
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
