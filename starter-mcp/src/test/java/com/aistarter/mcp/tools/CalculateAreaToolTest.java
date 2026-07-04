package com.aistarter.mcp.tools;

import com.aistarter.mcp.tool.ToolExecutionResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculateAreaToolTest {

    private final CalculateAreaTool tool = new CalculateAreaTool();

    @Test
    void rectangleArea() {
        ToolExecutionResult result = tool.execute(Map.of("shape", "rectangle", "width", 3, "height", 4));
        assertTrue(result.success());
        assertEquals("12.00", result.result());
    }

    @Test
    void triangleArea() {
        ToolExecutionResult result = tool.execute(Map.of("shape", "triangle", "width", 6, "height", 8));
        assertTrue(result.success());
        assertEquals("24.00", result.result());
    }

    @Test
    void circleArea() {
        ToolExecutionResult result = tool.execute(Map.of("shape", "circle", "radius", 2));
        assertTrue(result.success());
        assertEquals(String.format("%.2f", Math.PI * 4), result.result());
    }

    @Test
    void trapezoidArea() {
        ToolExecutionResult result = tool.execute(Map.of(
                "shape", "trapezoid", "topBase", 3, "bottomBase", 5, "height", 4));
        assertTrue(result.success());
        assertEquals("16.00", result.result());
    }

    @Test
    void missingParamsReturnsFailure() {
        ToolExecutionResult result = tool.execute(Map.of("shape", "rectangle", "width", 3));
        assertFalse(result.success());
        assertNotNull(result.error());
    }
}
