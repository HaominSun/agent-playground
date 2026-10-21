package dev.playground.tools;

import java.io.PrintStream;
import java.util.List;

import org.springframework.boot.ApplicationArguments;

public final class ToolLessonCli {

    private ToolLessonCli() {
    }

    public static void run(ToolChat chat, ApplicationArguments args, PrintStream out) {
        out.println(chat.complete(prompt(args)));
    }

    public static String prompt(ApplicationArguments args) {
        List<String> values = args.getOptionValues("prompt");
        if (values == null || values.isEmpty() || values.getFirst() == null || values.getFirst().isBlank()) {
            throw new IllegalStateException("CLI mode requires --prompt=<text>");
        }
        return values.getFirst();
    }
}
