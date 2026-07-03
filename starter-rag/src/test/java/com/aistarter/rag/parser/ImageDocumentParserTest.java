package com.aistarter.rag.parser;

import com.aistarter.common.exception.BusinessException;
import com.aistarter.rag.ocr.OcrProperties;
import com.aistarter.rag.ocr.OcrService;
import com.aistarter.rag.ocr.OcrTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageDocumentParserTest {

    @Mock
    private OcrProperties ocrProperties;

    @Mock
    private OcrService ocrService;

    @InjectMocks
    private ImageDocumentParser parser;

    @Test
    void supportsPngAndJpeg() {
        assertThat(parser.supports("image/png", "scan.png")).isTrue();
        assertThat(parser.supports("image/jpeg", "scan.jpg")).isTrue();
        assertThat(parser.supports("image/gif", "scan.gif")).isFalse();
    }

    @Test
    void parsesImageViaOcr() throws Exception {
        when(ocrProperties.isEnabled()).thenReturn(true);
        when(ocrService.recognize(any(), eq(OcrTask.TEXT_RECOGNITION))).thenReturn("退款政策：7天内可退");

        ParseResult result = parser.parse(new ByteArrayInputStream(new byte[] {1, 2, 3}));

        assertThat(result.text()).contains("退款政策");
        assertThat(result.metadata()).containsEntry("parser", "image-ocr");
        assertThat(result.metadata()).containsEntry("ocr_used", true);
    }

    @Test
    void rejectsImageWhenOcrDisabled() {
        when(ocrProperties.isEnabled()).thenReturn(false);

        assertThatThrownBy(() -> parser.parse(new ByteArrayInputStream(new byte[] {1})))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("OCR");
    }
}
