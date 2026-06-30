package com.aistarter.common.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class StringUtils {

    private StringUtils() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static String joinNonBlank(String delimiter, Collection<String> parts) {
        if (parts == null || parts.isEmpty()) {
            return "";
        }
        return parts.stream()
                .filter(part -> !isBlank(part))
                .collect(Collectors.joining(delimiter));
    }

    public static <T> String mapJoin(List<T> items, Function<T, String> mapper, String delimiter) {
        return items.stream().map(mapper).collect(Collectors.joining(delimiter));
    }
}
