package dev.playground.hello;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final HelloChat helloChat;

    public ChatController(HelloChat helloChat) {
        this.helloChat = helloChat;
    }

    @PostMapping
    public ChatReply chat(@RequestBody ChatCommand command) {
        return new ChatReply(helloChat.complete(command.prompt()));
    }

    @PostMapping("/structured")
    public LessonCard structured(@RequestBody StructuredCommand command) {
        return helloChat.lessonCard(command.topic());
    }
}
