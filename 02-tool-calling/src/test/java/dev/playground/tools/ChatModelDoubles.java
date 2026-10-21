package dev.playground.tools;

import static org.mockito.Mockito.lenient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;

final class ChatModelDoubles {

    private ChatModelDoubles() {
    }

    /**
     * Mockito does not invoke interface default methods. ChatClient copies tools onto
     * the prompt only when {@code getOptions().mutate()} is a
     * {@link ToolCallingChatOptions.Builder}. The OpenAI client's options implement that
     * interface; a bare {@code ChatOptions} would drop the tools and skip the loop.
     */
    static void prepare(ChatModel model) {
        ToolCallingChatOptions options = DefaultToolCallingChatOptions.builder().model("test-model").build();
        lenient().doReturn(options).when(model).getOptions();
    }

    static ChatResponse reply(String content) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(content))));
    }

    static ChatResponse toolCall(String id, String name, String arguments) {
        return toolCalls(new AssistantMessage.ToolCall(id, "function", name, arguments));
    }

    static ChatResponse toolCalls(AssistantMessage.ToolCall... calls) {
        AssistantMessage message = AssistantMessage.builder()
                .content("")
                .toolCalls(List.copyOf(Arrays.asList(calls)))
                .build();
        return new ChatResponse(List.of(new Generation(message)));
    }

    static boolean hasToolResult(Prompt prompt) {
        return toolRoundCount(prompt) > 0;
    }

    static int toolRoundCount(Prompt prompt) {
        int count = 0;
        for (Message message : prompt.getInstructions()) {
            if (message instanceof ToolResponseMessage) {
                count++;
            }
        }
        return count;
    }

    static String toolResultText(Prompt prompt) {
        StringBuilder builder = new StringBuilder();
        for (Message message : prompt.getInstructions()) {
            if (message instanceof ToolResponseMessage toolResponse) {
                for (ToolResponseMessage.ToolResponse response : toolResponse.getResponses()) {
                    if (!builder.isEmpty()) {
                        builder.append('\n');
                    }
                    builder.append(response.name()).append('=').append(response.responseData());
                }
            }
        }
        return builder.toString();
    }

    static List<String> offeredToolNames(Prompt prompt) {
        if (!(prompt.getOptions() instanceof ToolCallingChatOptions options) || options.getToolCallbacks() == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        for (ToolCallback callback : options.getToolCallbacks()) {
            names.add(callback.getToolDefinition().name());
        }
        return names;
    }
}
