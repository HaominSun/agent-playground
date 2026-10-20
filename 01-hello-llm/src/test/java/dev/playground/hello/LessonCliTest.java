package dev.playground.hello;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class LessonCliTest {

    @Test
    void readsPromptAndStructuredFlag() {
        var plain = new DefaultApplicationArguments("--prompt=Explain ChatClient");
        assertThat(LessonCli.prompt(plain)).isEqualTo("Explain ChatClient");
        assertThat(LessonCli.structured(plain)).isFalse();

        var flagged = new DefaultApplicationArguments("--prompt=ChatClient", "--structured");
        assertThat(LessonCli.structured(flagged)).isTrue();
        assertThat(LessonCli.structured(new DefaultApplicationArguments("--structured=true"))).isTrue();
        assertThat(LessonCli.structured(new DefaultApplicationArguments("--structured=false"))).isFalse();
    }

    @Test
    void missingPromptFails() {
        assertThatThrownBy(() -> LessonCli.prompt(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("CLI mode requires --prompt=<text>");
    }

    @Test
    void formatsALessonCard() {
        String text = LessonCli.format(new LessonCard("ChatClient", "Fluent API", List.of("ChatModel", "entity")));

        assertThat(text).isEqualTo(String.join(System.lineSeparator(),
                "title: ChatClient",
                "summary: Fluent API",
                "concepts:",
                "- ChatModel",
                "- entity"));
    }
}
