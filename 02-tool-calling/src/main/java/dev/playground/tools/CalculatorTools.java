package dev.playground.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTools {

    @Tool(description = "Add, subtract, multiply, or divide two numbers. Call this instead of doing the arithmetic yourself.",
            resultConverter = PlainTextToolResultConverter.class)
    public String calculate(
            @ToolParam(description = "Operation: add, subtract, multiply, or divide") String operation,
            @ToolParam(description = "Left operand") double left,
            @ToolParam(description = "Right operand") double right) {
        try {
            return Arithmetic.apply(operation, left, right);
        }
        catch (IllegalArgumentException exception) {
            return "error: " + exception.getMessage();
        }
    }
}
