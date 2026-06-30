package com.aistarter.ai.memory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisChatMemory implements ChatMemory {

    private static final String KEY_PREFIX = "chat:memory:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    @Override
    public void add(String sessionId, String role, String content) {
        String key = KEY_PREFIX + sessionId;
        redisTemplate.opsForList().rightPush(key, role + ":" + content);
        redisTemplate.expire(key, TTL);
    }

    @Override
    public List<String> getHistory(String sessionId) {
        String key = KEY_PREFIX + sessionId;
        List<String> history = redisTemplate.opsForList().range(key, 0, -1);
        return history == null ? Collections.emptyList() : history;
    }

    @Override
    public void clear(String sessionId) {
        redisTemplate.delete(KEY_PREFIX + sessionId);
    }
}
