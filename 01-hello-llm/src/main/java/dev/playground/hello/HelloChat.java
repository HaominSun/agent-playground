package dev.playground.hello;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class HelloChat {

    private final ChatClient chatClient;

    public HelloChat(ChatClient.Builder builder) {
        this.chatClient = builder.defaultSystem(PromptTexts.tutorSystem()).build();
    }

    public String complete(String prompt) {
        String text = TextInputs.require(prompt, "prompt");
        String answer = chatClient.prompt().user(text).call().content();
        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException("model returned an empty answer");
        }
        return answer;
    }

    public LessonCard lessonCard(String topic) {
        String text = TextInputs.require(topic, "topic");
        LessonCard card = chatClient.prompt()
                .user(PromptTexts.lessonCard(text))
                .call()
                .entity(LessonCard.class);
        if (card == null) {
            throw new IllegalStateException("model returned an empty lesson card");
        }
        return card;
    }
}
