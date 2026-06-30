package com.aistarter.ai.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisChatMemoryTest {

    private StringRedisTemplate redisTemplate;
    private ListOperations<String, String> listOperations;
    private RedisChatMemory chatMemory;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        listOperations = mock(ListOperations.class);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        chatMemory = new RedisChatMemory(redisTemplate);
    }

    @Test
    void addShouldPushToRedisList() {
        chatMemory.add("session1", "user", "hello");
        verify(listOperations).rightPush("chat:memory:session1", "user:hello");
        verify(redisTemplate).expire(eq("chat:memory:session1"), any(Duration.class));
    }

    @Test
    void getHistoryShouldReturnStoredMessages() {
        when(listOperations.range("chat:memory:session1", 0, -1))
                .thenReturn(List.of("user:hello", "assistant:hi"));
        assertEquals(2, chatMemory.getHistory("session1").size());
    }
}
