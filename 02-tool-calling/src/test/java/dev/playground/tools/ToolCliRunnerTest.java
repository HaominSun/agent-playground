package dev.playground.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.DefaultApplicationArguments;

class ToolCliRunnerTest {

    @Test
    void printsTheAnswerAfterTheToolLoop() {
        ChatModel model = mock(ChatModel.class);
        ChatModelDoubles.prepare(model);
        when(model.call(any(Prompt.class))).thenAnswer(invocation -> {
            Prompt prompt = invocation.getArgument(0);
            if (ChatModelDoubles.hasToolResult(prompt)) {
                return ChatModelDoubles.reply("42");
            }
            return ChatModelDoubles.toolCall("c", "calculate",
                    "{\"operation\":\"multiply\",\"left\":6,\"right\":7}");
        });
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ToolCliRunner runner = new ToolCliRunner(
                new ToolChat(ChatClient.builder(model), new WeatherTools(), new CalculatorTools(), new TodoTools()),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));

        runner.run(new DefaultApplicationArguments("--prompt=6 乘 7"));

        assertThat(buffer.toString(StandardCharsets.UTF_8)).contains("42");
    }
}
