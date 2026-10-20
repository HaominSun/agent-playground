package dev.playground.hello;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;

import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;

final class ChatModelDoubles {

    private ChatModelDoubles() {
    }

    /**
     * Mockito does not invoke interface default methods. ChatClient calls
     * {@code getOptions().mutate()}, so a bare mock has to return real options.
     */
    static void prepare(ChatModel model) {
        ChatOptions options = ChatOptions.builder().model("test-model").build();
        lenient().doReturn(options).when(model).getOptions();
    }

    static ChatResponse reply(String content) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(content))));
    }
}
