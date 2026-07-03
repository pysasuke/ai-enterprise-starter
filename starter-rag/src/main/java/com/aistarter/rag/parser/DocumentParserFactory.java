package com.aistarter.rag.parser;

import com.aistarter.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class DocumentParserFactory {

    private final List<DocumentParser> parsers;

    public DocumentParserFactory(List<DocumentParser> parsers) {
        this.parsers = parsers;
    }

    public ParseResult parse(String contentType, String filename, InputStream inputStream) throws IOException {
        return parsers.stream()
                .filter(parser -> parser.supports(contentType, filename))
                .findFirst()
                .map(parser -> {
                    try {
                        return parser.parse(inputStream);
                    } catch (IOException ex) {
                        throw new BusinessException("Failed to parse document: " + ex.getMessage());
                    }
                })
                .orElseThrow(() -> new BusinessException("Unsupported document type: " + contentType));
    }
}
