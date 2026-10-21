package dev.playground.tools;

import java.io.PrintStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "lesson.cli", name = "enabled", havingValue = "true")
public class ToolCliRunner implements ApplicationRunner {

    private final ToolChat toolChat;
    private final PrintStream out;

    @Autowired
    public ToolCliRunner(ToolChat toolChat) {
        this(toolChat, System.out);
    }

    ToolCliRunner(ToolChat toolChat, PrintStream out) {
        this.toolChat = toolChat;
        this.out = out;
    }

    @Override
    public void run(ApplicationArguments args) {
        ToolLessonCli.run(this.toolChat, args, this.out);
    }
}
