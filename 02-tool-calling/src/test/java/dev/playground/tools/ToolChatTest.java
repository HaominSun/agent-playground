package dev.playground.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;

class ToolChatTest {

    @Test
    void modelCanAnswerWithoutCallingATool() {
        ChatModel model = model();
        when(model.call(any(Prompt.class))).thenReturn(ChatModelDoubles.reply("直接回答"));
        ToolChat chat = chat(model, new TodoTools());

        assertThat(chat.complete("  什么是 tool calling  ")).isEqualTo("直接回答");

        ArgumentCaptor<Prompt> prompt = ArgumentCaptor.forClass(Prompt.class);
        verify(model, times(1)).call(prompt.capture());
        assertThat(prompt.getValue().getUserMessage().getText()).isEqualTo("什么是 tool calling");
        assertThat(prompt.getValue().getSystemMessage().getText()).contains("tool calling");
        assertThat(ChatModelDoubles.offeredToolNames(prompt.getValue())).contains(
                "getWeather", "calculate", "addTodo", "listTodos", "completeTodo");
    }

    @Test
    void blankPromptIsRejectedBeforeTheModelIsCalled() {
        ChatModel model = model();
        ToolChat chat = chat(model, new TodoTools());

        assertThatThrownBy(() -> chat.complete("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("prompt must not be blank");
        verify(model, never()).call(any(Prompt.class));
    }

    @Test
    void weatherToolResultIsFedBackIntoTheNextTurn() {
        ChatModel model = model();
        when(model.call(any(Prompt.class))).thenAnswer(invocation -> {
            Prompt prompt = invocation.getArgument(0);
            if (ChatModelDoubles.hasToolResult(prompt)) {
                return ChatModelDoubles.reply("北京今天晴，22°C。");
            }
            return ChatModelDoubles.toolCall("call-weather", "getWeather", "{\"city\":\"北京\"}");
        });
        ToolChat chat = chat(model, new TodoTools());

        assertThat(chat.complete("  北京天气  ")).isEqualTo("北京今天晴，22°C。");

        ArgumentCaptor<Prompt> prompts = ArgumentCaptor.forClass(Prompt.class);
        verify(model, times(2)).call(prompts.capture());
        Prompt first = prompts.getAllValues().get(0);
        Prompt second = prompts.getAllValues().get(1);
        assertThat(ChatModelDoubles.offeredToolNames(first)).contains("getWeather");
        assertThat(first.getUserMessage().getText()).isEqualTo("北京天气");
        assertThat(ChatModelDoubles.toolResultText(second)).isEqualTo("getWeather=北京：22°C，晴");
        assertThat(second.getUserMessage().getText()).isEqualTo("北京天气");
        assertThat(second.getSystemMessage().getText()).contains("tool calling");
    }

    @Test
    void oneResponseCanCallSeveralTools() {
        ChatModel model = model();
        when(model.call(any(Prompt.class))).thenAnswer(invocation -> {
            Prompt prompt = invocation.getArgument(0);
            if (ChatModelDoubles.hasToolResult(prompt)) {
                return ChatModelDoubles.reply("北京晴，6 乘 7 是 42。");
            }
            return ChatModelDoubles.toolCalls(
                    new AssistantMessage.ToolCall("w", "function", "getWeather", "{\"city\":\"Beijing\"}"),
                    new AssistantMessage.ToolCall("c", "function", "calculate",
                            "{\"operation\":\"multiply\",\"left\":6,\"right\":7}"));
        });
        ToolChat chat = chat(model, new TodoTools());

        assertThat(chat.complete("天气和乘法")).isEqualTo("北京晴，6 乘 7 是 42。");

        ArgumentCaptor<Prompt> prompts = ArgumentCaptor.forClass(Prompt.class);
        verify(model, times(2)).call(prompts.capture());
        assertThat(ChatModelDoubles.toolResultText(prompts.getAllValues().get(1)))
                .contains("getWeather=Beijing: 22°C, sunny")
                .contains("calculate=42");
    }

    @Test
    void loopContinuesUntilTheModelStopsCallingTools() {
        ChatModel model = model();
        when(model.call(any(Prompt.class))).thenAnswer(invocation -> {
            Prompt prompt = invocation.getArgument(0);
            int rounds = ChatModelDoubles.toolRoundCount(prompt);
            if (rounds == 0) {
                return ChatModelDoubles.toolCall("a", "addTodo", "{\"item\":\"写测试\"}");
            }
            if (rounds == 1) {
                return ChatModelDoubles.toolCall("b", "listTodos", "{}");
            }
            return ChatModelDoubles.reply("待办里有写测试");
        });
        ToolChat chat = chat(model, new TodoTools());

        assertThat(chat.complete("记一条待办")).isEqualTo("待办里有写测试");

        ArgumentCaptor<Prompt> prompts = ArgumentCaptor.forClass(Prompt.class);
        verify(model, times(3)).call(prompts.capture());
        assertThat(ChatModelDoubles.toolResultText(prompts.getAllValues().get(2)))
                .contains("addTodo=todos:\n1. 写测试")
                .contains("listTodos=todos:\n1. 写测试");
    }

    @Test
    void malformedArgumentsAreReturnedToTheModelAsTheToolResult() {
        ChatModel model = model();
        when(model.call(any(Prompt.class))).thenAnswer(invocation -> {
            Prompt prompt = invocation.getArgument(0);
            if (ChatModelDoubles.hasToolResult(prompt)) {
                return ChatModelDoubles.reply("参数不是 JSON");
            }
            return ChatModelDoubles.toolCall("bad", "calculate", "not-json");
        });
        ToolChat chat = chat(model, new TodoTools());

        assertThat(chat.complete("算一下")).isEqualTo("参数不是 JSON");

        ArgumentCaptor<Prompt> prompts = ArgumentCaptor.forClass(Prompt.class);
        verify(model, times(2)).call(prompts.capture());
        assertThat(ChatModelDoubles.toolResultText(prompts.getAllValues().get(1)))
                .startsWith("calculate=")
                .isNotEqualTo("calculate=");
    }

    @Test
    void unknownToolNameFailsTheCall() {
        ChatModel model = model();
        when(model.call(any(Prompt.class))).thenReturn(ChatModelDoubles.toolCall("x", "notATool", "{}"));
        ToolChat chat = chat(model, new TodoTools());

        assertThatThrownBy(() -> chat.complete("hello"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("notATool");
    }

    @Test
    void emptyFinalAnswerFails() {
        ChatModel model = model();
        when(model.call(any(Prompt.class))).thenAnswer(invocation -> {
            Prompt prompt = invocation.getArgument(0);
            if (ChatModelDoubles.hasToolResult(prompt)) {
                return ChatModelDoubles.reply("  ");
            }
            return ChatModelDoubles.toolCall("call-weather", "getWeather", "{\"city\":\"上海\"}");
        });
        ToolChat chat = chat(model, new TodoTools());

        assertThatThrownBy(() -> chat.complete("上海天气"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("model returned an empty answer");
    }

    private static ChatModel model() {
        ChatModel model = mock(ChatModel.class);
        ChatModelDoubles.prepare(model);
        return model;
    }

    private static ToolChat chat(ChatModel model, TodoTools todos) {
        return new ToolChat(ChatClient.builder(model), new WeatherTools(), new CalculatorTools(), todos);
    }
}
