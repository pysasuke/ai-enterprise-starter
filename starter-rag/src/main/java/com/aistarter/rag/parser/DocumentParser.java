package com.aistarter.rag.parser;

import java.io.IOException;
import java.io.InputStream;

public interface DocumentParser {

    boolean supports(String contentType, String filename);

    String parse(InputStream inputStream) throws IOException;
}
