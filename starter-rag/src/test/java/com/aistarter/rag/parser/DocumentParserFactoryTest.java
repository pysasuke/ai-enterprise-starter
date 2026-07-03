package com.aistarter.rag.parser;

import com.aistarter.common.exception.BusinessException;
import com.aistarter.rag.ocr.OcrProperties;
import com.aistarter.rag.ocr.OcrService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DocumentParserFactoryTest {

    @Mock
    private OcrService ocrService;

    @Mock
    private OcrProperties ocrProperties;

    private DocumentParserFactory factory;

    @BeforeEach
    void setUp() {
        lenient().when(ocrProperties.getMinTextLength()).thenReturn(50);
        lenient().when(ocrProperties.isEnabled()).thenReturn(false);
        lenient().when(ocrProperties.getMaxPages()).thenReturn(20);

        factory = new DocumentParserFactory(List.of(
                new TxtMarkdownParser(),
                new PdfDocumentParser(ocrProperties, ocrService, new PdfPageRenderer()),
                new DocxDocumentParser(),
                new ImageDocumentParser(ocrProperties, ocrService)));
    }

    @Test
    void routesToTxtParser() throws Exception {
        ParseResult result = factory.parse("text/plain", "policy.txt",
                new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));
        assertThat(result.text()).isEqualTo("hello");
    }

    @Test
    void routesToImageParser() throws Exception {
        assertThatThrownBy(() -> factory.parse("image/png", "scan.png",
                new ByteArrayInputStream(new byte[] {1, 2})))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("OCR");
    }

    @Test
    void rejectsUnsupportedType() {
        assertThatThrownBy(() -> factory.parse("application/zip", "file.zip",
                new ByteArrayInputStream(new byte[0])))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Unsupported");
    }
}
