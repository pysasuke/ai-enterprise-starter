package com.aistarter.common.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilsTest {

    @Test
    void isBlankShouldDetectEmptyValues() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   "));
    }

    @Test
    void joinNonBlankShouldSkipEmptyParts() {
        String joined = StringUtils.joinNonBlank(", ", List.of("a", "", "b", "  "));
        assertEquals("a, b", joined);
    }

    @Test
    void mapJoinShouldJoinMappedValues() {
        String joined = StringUtils.mapJoin(List.of(1, 2, 3), String::valueOf, "-");
        assertEquals("1-2-3", joined);
    }
}
