package dev.playground.tools;

public final class PromptTexts {

    private PromptTexts() {
    }

    public static String system() {
        return "You are a concise assistant for a Java developer learning tool calling. "
                + "You can look up weather, do arithmetic, and manage a small in-memory todo list. "
                + "Call a tool only when the user needs that capability, then answer from the tool result. "
                + "If no tool applies, answer directly. Answer in the same language as the user.";
    }
}
