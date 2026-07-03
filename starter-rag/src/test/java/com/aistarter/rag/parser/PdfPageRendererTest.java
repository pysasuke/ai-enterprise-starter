package com.aistarter.rag.parser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class PdfPageRendererTest {

    private final PdfPageRenderer renderer = new PdfPageRenderer();

    @Test
    void rendersEachPdfPageToPng() throws Exception {
        byte[] pdfBytes;
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < 2; i++) {
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                    stream.beginText();
                    stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    stream.newLineAtOffset(50, 700);
                    stream.showText("Page " + (i + 1));
                    stream.endText();
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            pdfBytes = out.toByteArray();
        }

        var images = renderer.render(pdfBytes, 2);
        assertThat(images).hasSize(2);
        assertThat(images.get(0).length).isGreaterThan(100);
        assertThat(images.get(1).length).isGreaterThan(100);
    }
}
