package dev.playground.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class ToolLessonCliTest {

    @Test
    void readsThePrompt() {
        var args = new DefaultApplicationArguments("--prompt=北京天气");

        assertThat(ToolLessonCli.prompt(args)).isEqualTo("北京天气");
    }

    @Test
    void missingPromptFails() {
        assertThatThrownBy(() -> ToolLessonCli.prompt(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("CLI mode requires --prompt=<text>");
        assertThatThrownBy(() -> ToolLessonCli.prompt(new DefaultApplicationArguments("--prompt=  ")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("CLI mode requires --prompt=<text>");
    }
}
