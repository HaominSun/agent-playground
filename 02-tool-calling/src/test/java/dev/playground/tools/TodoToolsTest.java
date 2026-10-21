package dev.playground.tools;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TodoToolsTest {

    @Test
    void addsListsAndCompletesItems() {
        TodoTools todos = new TodoTools();

        assertThat(todos.listTodos()).isEqualTo("todos: (empty)");
        assertThat(todos.addTodo("  写 README  ")).isEqualTo("todos:\n1. 写 README");
        assertThat(todos.addTodo("跑测试")).isEqualTo("todos:\n1. 写 README\n2. 跑测试");
        assertThat(todos.completeTodo(1)).isEqualTo("completed: 写 README\ntodos:\n1. 跑测试");
        assertThat(todos.listTodos()).isEqualTo("todos:\n1. 跑测试");
    }

    @Test
    void blankItemAndMissingIndexAreErrorResults() {
        TodoTools todos = new TodoTools();

        assertThat(todos.addTodo("  ")).isEqualTo("error: item must not be blank");
        assertThat(todos.listTodos()).isEqualTo("todos: (empty)");
        assertThat(todos.completeTodo(1)).isEqualTo("error: no todo at index 1");
    }
}
