package com.aistarter.rag.parser;

import com.aistarter.common.exception.BusinessException;
import com.aistarter.rag.ocr.OcrProperties;
import com.aistarter.rag.ocr.OcrService;
import com.aistarter.rag.ocr.OcrTask;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfDocumentParserTest {

    @Mock
    private OcrProperties ocrProperties;

    @Mock
    private OcrService ocrService;

    @Mock
    private PdfPageRenderer pdfPageRenderer;

    @InjectMocks
    private PdfDocumentParser parser;

    @Test
    void parsesPdfTextWithoutOcr() throws Exception {
        when(ocrProperties.getMinTextLength()).thenReturn(10);

        byte[] pdfBytes = pdfWithText("Refund policy within 7 days");
        ParseResult result = parser.parse(new ByteArrayInputStream(pdfBytes));

        assertThat(result.text()).contains("Refund policy");
        assertThat(result.metadata()).containsEntry("parser", "pdf-text");
        assertThat(result.metadata()).containsEntry("ocr_used", false);
        verify(ocrService, never()).recognize(any(), any());
    }

    @Test
    void fallsBackToOcrWhenPdfHasNoText() throws Exception {
        when(ocrProperties.getMinTextLength()).thenReturn(50);
        when(ocrProperties.isEnabled()).thenReturn(true);
        when(ocrProperties.getMaxPages()).thenReturn(20);

        byte[] pdfBytes = blankPdf();
        when(pdfPageRenderer.render(pdfBytes, 20)).thenReturn(List.of(new byte[] {1, 2}));
        when(ocrService.recognize(any(), eq(OcrTask.DOCUMENT_PARSING))).thenReturn("Scanned refund policy");

        ParseResult result = parser.parse(new ByteArrayInputStream(pdfBytes));

        assertThat(result.text()).contains("Scanned refund policy");
        assertThat(result.metadata()).containsEntry("parser", "pdf-ocr");
        assertThat(result.metadata()).containsEntry("ocr_used", true);
    }

    @Test
    void rejectsScannedPdfWhenOcrDisabled() throws Exception {
        when(ocrProperties.getMinTextLength()).thenReturn(50);
        when(ocrProperties.isEnabled()).thenReturn(false);

        assertThatThrownBy(() -> parser.parse(new ByteArrayInputStream(blankPdf())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("OCR");
    }

    private static byte[] pdfWithText(String content) throws Exception {
        try (PDDocument document = new PDDocument()) {
            org.apache.pdfbox.pdmodel.PDPage page = new PDPage();
            document.addPage(page);
            try (org.apache.pdfbox.pdmodel.PDPageContentStream stream =
                    new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(
                        org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText(content);
                stream.endText();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    private static byte[] blankPdf() throws Exception {
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
