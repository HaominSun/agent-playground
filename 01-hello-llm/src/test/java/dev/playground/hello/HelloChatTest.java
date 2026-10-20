package dev.playground.hello;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;

class HelloChatTest {

    @Test
    void completeReturnsModelText() {
        ChatModel model = mock(ChatModel.class);
        ChatModelDoubles.prepare(model);
        when(model.call(any(Prompt.class))).thenReturn(ChatModelDoubles.reply("hello from the model"));
        HelloChat chat = new HelloChat(ChatClient.builder(model));

        assertThat(chat.complete("  Say hello  ")).isEqualTo("hello from the model");

        ArgumentCaptor<Prompt> prompt = ArgumentCaptor.forClass(Prompt.class);
        verify(model).call(prompt.capture());
        assertThat(prompt.getValue().getUserMessage().getText()).isEqualTo("Say hello");
    }

    @Test
    void blankPromptIsRejectedBeforeTheModelIsCalled() {
        ChatModel model = mock(ChatModel.class);
        ChatModelDoubles.prepare(model);
        HelloChat chat = new HelloChat(ChatClient.builder(model));

        assertThatThrownBy(() -> chat.complete("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("prompt must not be blank");
        verify(model, never()).call(any(Prompt.class));
    }

    @Test
    void lessonCardMapsJsonOntoTheRecord() {
        ChatModel model = mock(ChatModel.class);
        ChatModelDoubles.prepare(model);
        when(model.call(any(Prompt.class))).thenReturn(ChatModelDoubles.reply("""
                {"title":"ChatClient","summary":"Fluent API over ChatModel","concepts":["ChatModel","entity"]}
                """));
        HelloChat chat = new HelloChat(ChatClient.builder(model));

        LessonCard card = chat.lessonCard("ChatClient");

        assertThat(card.title()).isEqualTo("ChatClient");
        assertThat(card.summary()).isEqualTo("Fluent API over ChatModel");
        assertThat(card.concepts()).containsExactly("ChatModel", "entity");

        ArgumentCaptor<Prompt> prompt = ArgumentCaptor.forClass(Prompt.class);
        verify(model).call(prompt.capture());
        assertThat(prompt.getValue().getUserMessage().getText()).contains("Topic: ChatClient");
    }

    @Test
    void emptyModelAnswerFails() {
        ChatModel model = mock(ChatModel.class);
        ChatModelDoubles.prepare(model);
        when(model.call(any(Prompt.class))).thenReturn(ChatModelDoubles.reply("  "));
        HelloChat chat = new HelloChat(ChatClient.builder(model));

        assertThatThrownBy(() -> chat.complete("hello"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("model returned an empty answer");
    }
}
