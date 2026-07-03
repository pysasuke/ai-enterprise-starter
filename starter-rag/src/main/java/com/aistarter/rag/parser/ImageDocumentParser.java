package com.aistarter.rag.parser;

import com.aistarter.common.exception.BusinessException;
import com.aistarter.rag.ocr.OcrProperties;
import com.aistarter.rag.ocr.OcrService;
import com.aistarter.rag.ocr.OcrTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ImageDocumentParser implements DocumentParser {

    private static final Set<String> CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/webp");

    private final OcrProperties ocrProperties;
    private final OcrService ocrService;

    @Override
    public boolean supports(String contentType, String filename) {
        if (contentType != null && CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return true;
        }
        if (filename == null) {
            return false;
        }
        String lower = filename.toLowerCase();
        return lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".webp");
    }

    @Override
    public ParseResult parse(InputStream inputStream) throws IOException {
        if (!ocrProperties.isEnabled()) {
            throw new BusinessException("Image upload requires OCR. Set OCR_ENABLED=true to ingest this document.");
        }
        byte[] imageBytes = inputStream.readAllBytes();
        String text = ocrService.recognize(imageBytes, OcrTask.TEXT_RECOGNITION).trim();
        if (text.isBlank()) {
            throw new BusinessException("OCR produced no text from image");
        }
        return ParseResult.of(text, Map.of(
                "parser", "image-ocr",
                "ocr_used", true,
                "ocr_pages", 1));
    }
}
