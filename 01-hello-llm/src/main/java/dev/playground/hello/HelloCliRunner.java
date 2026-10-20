package dev.playground.hello;

import java.io.PrintStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "lesson.cli", name = "enabled", havingValue = "true")
public class HelloCliRunner implements ApplicationRunner {

    private final HelloChat helloChat;
    private final PrintStream out;

    @Autowired
    public HelloCliRunner(HelloChat helloChat) {
        this(helloChat, System.out);
    }

    HelloCliRunner(HelloChat helloChat, PrintStream out) {
        this.helloChat = helloChat;
        this.out = out;
    }

    @Override
    public void run(ApplicationArguments args) {
        LessonCli.run(helloChat, args, out);
    }
}
