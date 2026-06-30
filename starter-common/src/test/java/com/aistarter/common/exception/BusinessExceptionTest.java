package com.aistarter.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessExceptionTest {

    @Test
    void defaultCodeShouldBe400() {
        BusinessException ex = new BusinessException("bad request");
        assertEquals(400, ex.getCode());
        assertEquals("bad request", ex.getMessage());
    }

    @Test
    void customCodeShouldBeUsed() {
        BusinessException ex = new BusinessException(401, "unauthorized");
        assertEquals(401, ex.getCode());
    }
}
