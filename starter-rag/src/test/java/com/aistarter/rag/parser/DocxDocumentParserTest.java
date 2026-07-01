package com.aistarter.rag.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class DocxDocumentParserTest {

    private final DocxDocumentParser parser = new DocxDocumentParser();

    @Test
    void parsesDocxText() throws Exception {
        byte[] docxBytes;
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("Enterprise knowledge base content");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            docxBytes = out.toByteArray();
        }

        String text = parser.parse(new ByteArrayInputStream(docxBytes));
        assertThat(text).contains("Enterprise knowledge base");
    }
}
