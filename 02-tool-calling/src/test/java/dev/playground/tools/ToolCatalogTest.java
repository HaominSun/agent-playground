package dev.playground.tools;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;

class ToolCatalogTest {

    @Test
    void publishesNamesDescriptionsAndParameterSchemas() {
        ToolCallback[] callbacks = ToolCatalog.callbacks(new WeatherTools(), new CalculatorTools(), new TodoTools());

        assertThat(ToolCatalog.names(callbacks)).containsExactlyInAnyOrder(
                "getWeather", "calculate", "addTodo", "listTodos", "completeTodo");

        ToolCallback weather = ToolCatalog.require(callbacks, "getWeather");
        assertThat(weather.getToolDefinition().description()).contains("weather");
        assertThat(weather.getToolDefinition().inputSchema()).contains("city");

        ToolCallback calculator = ToolCatalog.require(callbacks, "calculate");
        assertThat(calculator.getToolDefinition().description()).contains("arithmetic");
        assertThat(calculator.getToolDefinition().inputSchema()).contains("operation", "left", "right");

        assertThat(ToolCatalog.require(callbacks, "listTodos").getToolDefinition().inputSchema())
                .doesNotContain("item");
        assertThat(ToolCatalog.require(callbacks, "completeTodo").getToolDefinition().inputSchema())
                .contains("index");
    }

    @Test
    void bindsJsonArgumentsOntoTheJavaMethods() {
        TodoTools todos = new TodoTools();
        ToolCallback[] callbacks = ToolCatalog.callbacks(new WeatherTools(), new CalculatorTools(), todos);

        assertThat(ToolCatalog.require(callbacks, "getWeather").call("{\"city\":\"北京\"}"))
                .isEqualTo("北京：22°C，晴");
        assertThat(ToolCatalog.require(callbacks, "calculate").call(
                "{\"operation\":\"add\",\"left\":2,\"right\":3}")).isEqualTo("5");
        assertThat(ToolCatalog.require(callbacks, "calculate").call(
                "{\"operation\":\"divide\",\"left\":1,\"right\":0}")).isEqualTo("error: division by zero");

        assertThat(ToolCatalog.require(callbacks, "addTodo").call("{\"item\":\"写 README\"}"))
                .isEqualTo("todos:\n1. 写 README");
        assertThat(ToolCatalog.require(callbacks, "listTodos").call("{}"))
                .isEqualTo("todos:\n1. 写 README");
        assertThat(ToolCatalog.require(callbacks, "completeTodo").call("{\"index\":1}"))
                .contains("completed: 写 README")
                .contains("todos: (empty)");
    }
}
