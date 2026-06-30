package com.aistarter.agent.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseAgentDtoTest {

    @Test
    void requestShouldHoldQuestion() {
        DatabaseAgentRequest request = new DatabaseAgentRequest();
        request.setQuestion("为什么慢？");
        assertEquals("为什么慢？", request.getQuestion());
    }

    @Test
    void responseShouldHoldAnalysis() {
        DatabaseAgentResponse response = new DatabaseAgentResponse("缺少索引");
        assertEquals("缺少索引", response.getAnalysis());
    }
}
