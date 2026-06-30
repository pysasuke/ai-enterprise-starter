package com.aistarter.web.exception;

import com.aistarter.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusinessExceptionShouldReturnFailResult() {
        var result = handler.handleBusinessException(new BusinessException(401, "unauthorized"));
        assertEquals(401, result.getCode());
        assertEquals("unauthorized", result.getMessage());
    }
}
