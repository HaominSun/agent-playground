package dev.playground.hello;

import java.io.PrintStream;
import java.util.List;

import org.springframework.boot.ApplicationArguments;

public final class LessonCli {

    private LessonCli() {
    }

    public static void run(HelloChat chat, ApplicationArguments args, PrintStream out) {
        if (structured(args)) {
            out.println(format(chat.lessonCard(prompt(args))));
            return;
        }
        out.println(chat.complete(prompt(args)));
    }

    public static String prompt(ApplicationArguments args) {
        List<String> values = args.getOptionValues("prompt");
        if (values == null || values.isEmpty() || values.getFirst() == null || values.getFirst().isBlank()) {
            throw new IllegalStateException("CLI mode requires --prompt=<text>");
        }
        return values.getFirst();
    }

    public static boolean structured(ApplicationArguments args) {
        if (!args.containsOption("structured")) {
            return false;
        }
        List<String> values = args.getOptionValues("structured");
        if (values == null || values.isEmpty()) {
            return true;
        }
        return values.stream().anyMatch(value -> value == null || value.isBlank() || Boolean.parseBoolean(value));
    }

    public static String format(LessonCard card) {
        List<String> concepts = card.concepts() == null ? List.of() : card.concepts();
        String rendered = String.join(System.lineSeparator(), concepts.stream().map(concept -> "- " + concept).toList());
        return "title: " + card.title()
                + System.lineSeparator() + "summary: " + card.summary()
                + System.lineSeparator() + "concepts:"
                + System.lineSeparator() + rendered;
    }
}
