package dev.playground.hello;

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

class HelloCliRunnerTest {

    @Test
    void printsThePlainAnswer() {
        ChatModel model = mock(ChatModel.class);
        ChatModelDoubles.prepare(model);
        when(model.call(any(Prompt.class))).thenReturn(ChatModelDoubles.reply("cli answer"));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        HelloCliRunner runner = new HelloCliRunner(
                new HelloChat(ChatClient.builder(model)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));

        runner.run(new DefaultApplicationArguments("--prompt=hello"));

        assertThat(buffer.toString(StandardCharsets.UTF_8)).contains("cli answer");
    }

    @Test
    void printsTheStructuredCard() {
        ChatModel model = mock(ChatModel.class);
        ChatModelDoubles.prepare(model);
        when(model.call(any(Prompt.class))).thenReturn(ChatModelDoubles.reply("""
                {"title":"ChatClient","summary":"Fluent API","concepts":["entity"]}
                """));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        HelloCliRunner runner = new HelloCliRunner(
                new HelloChat(ChatClient.builder(model)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));

        runner.run(new DefaultApplicationArguments("--prompt=ChatClient", "--structured"));

        assertThat(buffer.toString(StandardCharsets.UTF_8)).contains("title: ChatClient", "- entity");
    }
}
