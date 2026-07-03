package com.aistarter.rag.ocr;

public enum OcrTask {
    TEXT_RECOGNITION("text_recognition"),
    DOCUMENT_PARSING("document_parsing");

    private final String apiValue;

    OcrTask(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }
}
