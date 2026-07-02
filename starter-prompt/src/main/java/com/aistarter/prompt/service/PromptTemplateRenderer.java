package com.aistarter.prompt.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PromptTemplateRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(\\w+)}}");

    public String render(String template, Map<String, ?> variables) {
        if (template == null) {
            return "";
        }
        Map<String, ?> vars = variables != null ? variables : Map.of();
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = vars.get(key);
            matcher.appendReplacement(builder, Matcher.quoteReplacement(
                    value != null ? String.valueOf(value) : matcher.group(0)));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }
}
