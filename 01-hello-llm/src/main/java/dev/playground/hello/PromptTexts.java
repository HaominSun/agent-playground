package dev.playground.hello;

public final class PromptTexts {

    private PromptTexts() {
    }

    public static String tutorSystem() {
        return "You are a concise tutor for a Java developer learning LLM agents. "
                + "Answer in the same language as the user.";
    }

    public static String lessonCard(String topic) {
        return "Create a short teaching card for a Java developer learning LLM agents. Topic: " + topic;
    }
}
