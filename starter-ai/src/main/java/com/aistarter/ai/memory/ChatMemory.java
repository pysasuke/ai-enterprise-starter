package com.aistarter.ai.memory;

import java.util.List;

public interface ChatMemory {

    void add(String sessionId, String role, String content);

    List<String> getHistory(String sessionId);

    void clear(String sessionId);
}
