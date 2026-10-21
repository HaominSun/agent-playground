package dev.playground.tools;

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

    @Autowired
    private TodoTools todos;

    @MockitoBean
    private ChatModel chatModel;

    @BeforeEach
    void stubModel() {
        this.todos.clear();
        ChatModelDoubles.prepare(this.chatModel);
    }

    @Test
    void chatReturnsTheModelAnswerWhenNoToolIsCalled() throws Exception {
        when(this.chatModel.call(any(Prompt.class))).thenReturn(ChatModelDoubles.reply("直接回答"));

        this.mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("直接回答"));
    }

    @Test
    void chatRunsTheToolAndReturnsTheFollowUpAnswer() throws Exception {
        when(this.chatModel.call(any(Prompt.class))).thenAnswer(invocation -> {
            Prompt prompt = invocation.getArgument(0);
            if (ChatModelDoubles.hasToolResult(prompt)) {
                return ChatModelDoubles.reply("北京今天晴，22°C。");
            }
            return ChatModelDoubles.toolCall("call-weather", "getWeather", "{\"city\":\"北京\"}");
        });

        this.mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"北京天气\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("北京今天晴，22°C。"));
    }

    @Test
    void todoAddedInOneRequestIsVisibleToTheNext() throws Exception {
        when(this.chatModel.call(any(Prompt.class))).thenAnswer(invocation -> {
            Prompt prompt = invocation.getArgument(0);
            if (ChatModelDoubles.hasToolResult(prompt)) {
                return ChatModelDoubles.reply(ChatModelDoubles.toolResultText(prompt));
            }
            if ("add".equals(prompt.getUserMessage().getText())) {
                return ChatModelDoubles.toolCall("a", "addTodo", "{\"item\":\"买牛奶\"}");
            }
            return ChatModelDoubles.toolCall("b", "listTodos", "{}");
        });

        this.mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"add\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("addTodo=todos:\n1. 买牛奶"));

        this.mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"list\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("listTodos=todos:\n1. 买牛奶"));
    }

    @Test
    void blankPromptIsBadRequest() throws Exception {
        this.mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("prompt must not be blank"));
    }

    @Test
    void emptyModelAnswerIsBadGateway() throws Exception {
        when(this.chatModel.call(any(Prompt.class))).thenReturn(ChatModelDoubles.reply(" "));

        this.mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"hello\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("model returned an empty answer"));
    }

    @Test
    void unknownToolIsBadGateway() throws Exception {
        when(this.chatModel.call(any(Prompt.class))).thenReturn(ChatModelDoubles.toolCall("x", "notATool", "{}"));

        this.mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"hello\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("No ToolCallback found for tool name: notATool"));
    }
}
