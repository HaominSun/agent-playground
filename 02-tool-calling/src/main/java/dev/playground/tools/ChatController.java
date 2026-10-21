package dev.playground.tools;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ToolChat toolChat;

    public ChatController(ToolChat toolChat) {
        this.toolChat = toolChat;
    }

    @PostMapping
    public ChatReply chat(@RequestBody ChatCommand command) {
        return new ChatReply(this.toolChat.complete(command.prompt()));
    }
}
