package com.aistarter.rag.parser;

import com.aistarter.common.exception.BusinessException;
import com.aistarter.rag.ocr.OcrProperties;
import com.aistarter.rag.ocr.OcrService;
import com.aistarter.rag.ocr.OcrTask;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PdfDocumentParser implements DocumentParser {

    private static final String PAGE_SEPARATOR = "\n\n---\n\n";

    private final OcrProperties ocrProperties;
    private final OcrService ocrService;
    private final PdfPageRenderer pdfPageRenderer;

    @Override
    public boolean supports(String contentType, String filename) {
        if ("application/pdf".equalsIgnoreCase(contentType)) {
            return true;
        }
        return filename != null && filename.toLowerCase().endsWith(".pdf");
    }

    @Override
    public ParseResult parse(InputStream inputStream) throws IOException {
        byte[] pdfBytes = inputStream.readAllBytes();
        int totalPages;
        String extractedText;
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            totalPages = document.getNumberOfPages();
            extractedText = new PDFTextStripper().getText(document).trim();
        }

        if (extractedText.length() >= ocrProperties.getMinTextLength()) {
            return ParseResult.of(extractedText, Map.of(
                    "parser", "pdf-text",
                    "ocr_used", false));
        }

        if (!ocrProperties.isEnabled()) {
            throw new BusinessException("Scanned PDF requires OCR. Set OCR_ENABLED=true to ingest this document.");
        }

        List<byte[]> pageImages = pdfPageRenderer.render(pdfBytes, ocrProperties.getMaxPages());
        List<String> pageTexts = new ArrayList<>();
        for (byte[] pageImage : pageImages) {
            try {
                String pageText = ocrService.recognize(pageImage, OcrTask.DOCUMENT_PARSING).trim();
                if (!pageText.isBlank()) {
                    pageTexts.add(pageText);
                }
            } catch (BusinessException ex) {
                // Skip failed pages; fail only if none succeed.
            }
        }

        if (pageTexts.isEmpty()) {
            throw new BusinessException("OCR produced no text from scanned PDF");
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("parser", "pdf-ocr");
        metadata.put("ocr_used", true);
        metadata.put("ocr_pages", pageTexts.size());
        if (totalPages > ocrProperties.getMaxPages()) {
            metadata.put("ocr_truncated", true);
        }
        return ParseResult.of(String.join(PAGE_SEPARATOR, pageTexts), metadata);
    }
}
