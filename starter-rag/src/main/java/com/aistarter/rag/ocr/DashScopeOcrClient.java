package com.aistarter.rag.ocr;

import com.aistarter.common.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

public class DashScopeOcrClient implements OcrService {

    private static final URI OCR_ENDPOINT = URI.create(
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation");

    private final OcrProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public DashScopeOcrClient(OcrProperties properties) {
        this(properties, new ObjectMapper(), HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build());
    }

    DashScopeOcrClient(OcrProperties properties, ObjectMapper objectMapper, HttpClient httpClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    @Override
    public String recognize(byte[] imageBytes, OcrTask task) {
        if (!properties.isEnabled()) {
            throw new BusinessException("OCR is disabled. Set OCR_ENABLED=true to ingest scanned documents.");
        }
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new BusinessException("DASHSCOPE_API_KEY is required for OCR");
        }
        try {
            String body = buildRequestBody(imageBytes, task);
            String response = postJson(body);
            return extractText(response);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("OCR failed: " + ex.getMessage());
        }
    }

    String buildRequestBody(byte[] imageBytes, OcrTask task) throws IOException {
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", properties.getModel());

        ObjectNode imageContent = objectMapper.createObjectNode();
        imageContent.put("image", "data:image/png;base64," + base64);

        ArrayNode content = objectMapper.createArrayNode().add(imageContent);
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.set("content", content);

        ObjectNode input = objectMapper.createObjectNode();
        input.set("messages", objectMapper.createArrayNode().add(message));
        root.set("input", input);

        ObjectNode ocrOptions = objectMapper.createObjectNode();
        ocrOptions.put("task", task.apiValue());
        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.set("ocr_options", ocrOptions);
        root.set("parameters", parameters);

        return objectMapper.writeValueAsString(root);
    }

    protected String postJson(String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(OCR_ENDPOINT)
                .timeout(Duration.ofSeconds(properties.getPageTimeoutSeconds()))
                .header("Authorization", "Bearer " + properties.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new BusinessException("OCR failed: HTTP " + response.statusCode() + " " + response.body());
        }
        return response.body();
    }

    String extractText(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode ocrResult = root.path("output").path("ocr_result");
        if (!ocrResult.isMissingNode() && !ocrResult.isNull()) {
            if (ocrResult.isTextual()) {
                return ocrResult.asText().trim();
            }
            JsonNode textNode = ocrResult.path("text");
            if (!textNode.isMissingNode()) {
                return textNode.asText().trim();
            }
        }

        JsonNode content = root.path("output").path("choices").path(0).path("message").path("content");
        if (content.isTextual()) {
            return content.asText().trim();
        }
        if (content.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : content) {
                JsonNode textNode = item.path("text");
                if (!textNode.isMissingNode()) {
                    if (builder.length() > 0) {
                        builder.append('\n');
                    }
                    builder.append(textNode.asText());
                }
            }
            String text = builder.toString().trim();
            if (!text.isBlank()) {
                return text;
            }
        }
        throw new BusinessException("OCR failed: empty response");
    }
}
