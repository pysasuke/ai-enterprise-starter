package com.aistarter.common.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultTest {

    @Test
    void okShouldReturnSuccessCode() {
        Result<String> result = Result.ok("hello");
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("hello", result.getData());
    }

    @Test
    void okWithoutDataShouldReturnNullData() {
        Result<Void> result = Result.ok();
        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    @Test
    void failShouldReturnErrorCode() {
        Result<Void> result = Result.fail(401, "unauthorized");
        assertEquals(401, result.getCode());
        assertEquals("unauthorized", result.getMessage());
    }

    @Test
    void failWithMessageOnlyShouldUse500() {
        Result<Void> result = Result.fail("error");
        assertEquals(500, result.getCode());
    }
}
