package com.aistarter.mcp.tools;

import com.aistarter.mcp.tool.Tool;
import com.aistarter.mcp.tool.ToolExecutionResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CalculateAreaTool implements Tool {

    @Override
    public String getName() {
        return "calculateArea";
    }

    @Override
    public String getDescription() {
        return "Calculate geometric area. Shapes: rectangle, triangle, circle, trapezoid.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "shape", Map.of(
                                "type", "string",
                                "enum", List.of("rectangle", "triangle", "circle", "trapezoid")),
                        "width", Map.of("type", "number"),
                        "height", Map.of("type", "number"),
                        "radius", Map.of("type", "number"),
                        "topBase", Map.of("type", "number"),
                        "bottomBase", Map.of("type", "number")),
                "required", List.of("shape"));
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> arguments) {
        try {
            String shape = String.valueOf(arguments.get("shape"));
            double area = switch (shape) {
                case "rectangle" -> num(arguments, "width") * num(arguments, "height");
                case "triangle" -> num(arguments, "width") * num(arguments, "height") / 2.0;
                case "circle" -> Math.PI * Math.pow(num(arguments, "radius"), 2);
                case "trapezoid" -> (num(arguments, "topBase") + num(arguments, "bottomBase"))
                        * num(arguments, "height") / 2.0;
                default -> throw new IllegalArgumentException("Unsupported shape: " + shape);
            };
            return ToolExecutionResult.ok(String.format("%.2f", area));
        } catch (Exception e) {
            return ToolExecutionResult.fail(e.getMessage());
        }
    }

    private static double num(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing parameter: " + key);
        }
        return ((Number) value).doubleValue();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
