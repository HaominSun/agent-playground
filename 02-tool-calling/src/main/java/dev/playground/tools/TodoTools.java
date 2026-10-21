package dev.playground.tools;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class TodoTools {

    private final List<String> items = new ArrayList<>();

    @Tool(description = "Add one item to the in-memory todo list and return the updated list.",
            resultConverter = PlainTextToolResultConverter.class)
    public String addTodo(@ToolParam(description = "Text of the todo item") String item) {
        String text;
        try {
            text = TextInputs.require(item, "item");
        }
        catch (IllegalArgumentException exception) {
            return "error: " + exception.getMessage();
        }
        synchronized (this.items) {
            this.items.add(text);
            return render();
        }
    }

    @Tool(description = "List the open todo items. The list is empty until addTodo is called.",
            resultConverter = PlainTextToolResultConverter.class)
    public String listTodos() {
        synchronized (this.items) {
            return render();
        }
    }

    @Tool(description = "Mark a todo done by its 1-based index and return the remaining list.",
            resultConverter = PlainTextToolResultConverter.class)
    public String completeTodo(@ToolParam(description = "1-based index from listTodos") int index) {
        synchronized (this.items) {
            if (index < 1 || index > this.items.size()) {
                return "error: no todo at index " + index;
            }
            String removed = this.items.remove(index - 1);
            return "completed: " + removed + "\n" + render();
        }
    }

    void clear() {
        synchronized (this.items) {
            this.items.clear();
        }
    }

    private String render() {
        if (this.items.isEmpty()) {
            return "todos: (empty)";
        }
        StringBuilder builder = new StringBuilder("todos:");
        for (int i = 0; i < this.items.size(); i++) {
            builder.append('\n').append(i + 1).append(". ").append(this.items.get(i));
        }
        return builder.toString();
    }
}
