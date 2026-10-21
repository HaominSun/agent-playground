package dev.playground.hello;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.ai.openai.api-key=test-key-not-used",
        "spring.ai.openai.base-url=http://127.0.0.1:9",
        "spring.main.banner-mode=off"
})
@AutoConfigureMockMvc
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatModel chatModel;

    @BeforeEach
    void stubModel() {
        ChatModelDoubles.prepare(chatModel);
        when(chatModel.call(any(Prompt.class))).thenAnswer(invocation -> {
            Prompt prompt = invocation.getArgument(0);
            String userText = prompt.getUserMessage().getText();
            if (userText != null && userText.contains("Topic:")) {
                return ChatModelDoubles.reply("""
                        {"title":"ChatClient","summary":"Fluent API","concepts":["ChatModel","entity"]}
                        """);
            }
            return ChatModelDoubles.reply("mocked answer");
        });
    }

    @Test
    void chatReturnsTheModelAnswer() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("mocked answer"));
    }

    @Test
    void structuredEndpointReturnsTheRecord() throws Exception {
        mockMvc.perform(post("/api/chat/structured")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"topic\":\"ChatClient\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("ChatClient"))
                .andExpect(jsonPath("$.summary").value("Fluent API"))
                .andExpect(jsonPath("$.concepts[0]").value("ChatModel"))
                .andExpect(jsonPath("$.concepts[1]").value("entity"));
    }

    @Test
    void blankPromptIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("prompt must not be blank"));
    }

    @Test
    void emptyModelAnswerIsBadGateway() throws Exception {
        ChatModelDoubles.prepare(chatModel);
        when(chatModel.call(any(Prompt.class))).thenReturn(ChatModelDoubles.reply(" "));

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"hello\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("model returned an empty answer"));
    }
}
