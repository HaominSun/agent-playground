package dev.playground.tools;

import java.lang.reflect.Type;

import org.springframework.ai.tool.execution.ToolCallResultConverter;

/**
 * Sends the tool's return value back to the model as plain text.
 * <p>
 * The default converter JSON-encodes the value, and forwards a string unchanged when
 * that string is already valid JSON. {@code "42"} would therefore arrive as a JSON
 * number, while a sentence would arrive wrapped in quotes. These tools return text for
 * the model to read, so the converter keeps that text.
 */
public final class PlainTextToolResultConverter implements ToolCallResultConverter {

    @Override
    public String convert(Object result, Type returnType) {
        if (result == null) {
            return "";
        }
        return result.toString();
    }
}
