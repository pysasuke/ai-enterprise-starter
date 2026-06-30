package com.aistarter.ai.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatDtoTest {

    @Test
    void chatRequestShouldHoldMessage() {
        ChatRequest request = new ChatRequest();
        request.setMessage("hello");
        assertEquals("hello", request.getMessage());
    }

    @Test
    void chatResponseShouldHoldContent() {
        ChatResponse response = new ChatResponse("hi");
        assertEquals("hi", response.getContent());
    }
}
