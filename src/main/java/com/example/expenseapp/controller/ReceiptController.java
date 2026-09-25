package com.example.expenseapp.controller;

import com.example.expenseapp.entity.ReceiptImage;
import com.example.expenseapp.mapper.ReceiptImageMapper;
import com.example.expenseapp.service.ReceiptStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Path;

@Controller
public class ReceiptController {

    private final ReceiptImageMapper receiptImageMapper;
    private final ReceiptStorageService receiptStorageService;

    public ReceiptController(
            ReceiptImageMapper receiptImageMapper,
            ReceiptStorageService receiptStorageService) {
        this.receiptImageMapper = receiptImageMapper;
        this.receiptStorageService = receiptStorageService;
    }

    @GetMapping("/expenses/{expenseId}/receipts/{imageId}")
    public ResponseEntity<Resource> showReceipt(
            @PathVariable Integer expenseId,
            @PathVariable Integer imageId) throws Exception {
        ReceiptImage image = receiptImageMapper.findByExpenseId(expenseId).stream()
                .filter(item -> item.getImageId().equals(imageId))
                .findFirst()
                .orElse(null);
        if (image == null) {
            Path leftover = receiptStorageService.findFirstFile(expenseId);
            if (leftover == null) {
                return ResponseEntity.notFound().build();
            }
            Resource leftoverResource = new UrlResource(leftover.toUri());
            if (!leftoverResource.exists() || !leftoverResource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            String leftoverName = leftover.getFileName().toString();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + leftoverName + "\"")
                    .contentType(receiptStorageService.mediaTypeFor(leftoverName))
                    .body(leftoverResource);
        }

        Path filePath = receiptStorageService.resolvePath(image.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String filename = filePath.getFileName().toString();
        MediaType mediaType = receiptStorageService.mediaTypeFor(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(resource);
    }
}
