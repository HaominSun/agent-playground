package dev.playground.tools;

import java.util.Arrays;
import java.util.List;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;

public final class ToolCatalog {

    private ToolCatalog() {
    }

    public static ToolCallback[] callbacks(WeatherTools weather, CalculatorTools calculator, TodoTools todos) {
        return ToolCallbacks.from(weather, calculator, todos);
    }

    public static List<String> names(ToolCallback[] callbacks) {
        return Arrays.stream(callbacks).map(callback -> callback.getToolDefinition().name()).toList();
    }

    public static ToolCallback require(ToolCallback[] callbacks, String name) {
        for (ToolCallback callback : callbacks) {
            if (name.equals(callback.getToolDefinition().name())) {
                return callback;
            }
        }
        throw new IllegalArgumentException("no tool named " + name);
    }
}
