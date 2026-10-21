package dev.playground.tools;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

@Service
public class ToolChat {

    private final ChatClient chatClient;

    public ToolChat(ChatClient.Builder builder, WeatherTools weather, CalculatorTools calculator, TodoTools todos) {
        ToolCallback[] callbacks = ToolCatalog.callbacks(weather, calculator, todos);
        this.chatClient = builder.defaultSystem(PromptTexts.system()).defaultTools((Object[]) callbacks).build();
    }

    public String complete(String prompt) {
        String text = TextInputs.require(prompt, "prompt");
        String answer = this.chatClient.prompt().user(text).call().content();
        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException("model returned an empty answer");
        }
        return answer;
    }
}
