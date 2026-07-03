package com.aistarter.rag.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

import java.util.Map;

@Component
public class DocxDocumentParser implements DocumentParser {

    private static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    @Override
    public boolean supports(String contentType, String filename) {
        if (DOCX_CONTENT_TYPE.equalsIgnoreCase(contentType)) {
            return true;
        }
        return filename != null && filename.toLowerCase().endsWith(".docx");
    }

    @Override
    public ParseResult parse(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            String text = document.getParagraphs().stream()
                    .map(XWPFParagraph::getText)
                    .filter(line -> line != null && !line.isBlank())
                    .collect(Collectors.joining("\n"))
                    .trim();
            return ParseResult.of(text, Map.of("parser", "docx", "ocr_used", false));
        }
    }
}
