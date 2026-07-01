package com.aistarter.rag.parser;

import com.aistarter.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentParserFactoryTest {

    private final DocumentParserFactory factory = new DocumentParserFactory(List.of(
            new TxtMarkdownParser(),
            new PdfDocumentParser(),
            new DocxDocumentParser()));

    @Test
    void routesToTxtParser() throws Exception {
        String text = factory.parse("text/plain", "policy.txt",
                new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));
        assertThat(text).isEqualTo("hello");
    }

    @Test
    void rejectsUnsupportedType() {
        assertThatThrownBy(() -> factory.parse("application/zip", "file.zip",
                new ByteArrayInputStream(new byte[0])))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Unsupported");
    }
}
