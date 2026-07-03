package com.aistarter.rag.ocr;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DashScopeOcrClientTest {

    @Test
    void parsesTextFromOcrResponse() {
        OcrProperties props = new OcrProperties();
        props.setEnabled(true);
        props.setApiKey("test-key");
        props.setModel("qwen3.5-ocr");

        var client = new DashScopeOcrClient(props) {
            @Override
            protected String postJson(String body) {
                return readResource("/ocr/success-response.json");
            }
        };

        String text = client.recognize(new byte[] {1, 2, 3}, OcrTask.TEXT_RECOGNITION);
        assertThat(text).contains("退款政策");
    }

    @Test
    void buildRequestBodyIncludesOcrTask() throws Exception {
        OcrProperties props = new OcrProperties();
        props.setEnabled(true);
        props.setApiKey("test-key");
        props.setModel("qwen3.5-ocr");

        var client = new DashScopeOcrClient(props);
        String body = client.buildRequestBody(new byte[] {9}, OcrTask.DOCUMENT_PARSING);

        assertThat(body).contains("document_parsing");
        assertThat(body).contains("qwen3.5-ocr");
    }

    private static String readResource(String path) {
        try (InputStream in = DashScopeOcrClientTest.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
