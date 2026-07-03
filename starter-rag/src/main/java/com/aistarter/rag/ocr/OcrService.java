package com.aistarter.rag.ocr;

public interface OcrService {

    String recognize(byte[] imageBytes, OcrTask task);
}
