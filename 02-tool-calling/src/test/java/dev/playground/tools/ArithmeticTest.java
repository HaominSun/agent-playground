package dev.playground.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ArithmeticTest {

    @Test
    void appliesTheFourOperations() {
        assertThat(Arithmetic.apply("add", 2, 3)).isEqualTo("5");
        assertThat(Arithmetic.apply("Subtract", 10, 4)).isEqualTo("6");
        assertThat(Arithmetic.apply("*", 6, 7)).isEqualTo("42");
        assertThat(Arithmetic.apply("divide", 10, 4)).isEqualTo("2.5");
        assertThat(Arithmetic.apply("add", 0.1, 0.2)).isEqualTo("0.3");
    }

    @Test
    void rejectsDivisionByZeroAndUnknownOperations() {
        assertThatThrownBy(() -> Arithmetic.apply("divide", 1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("division by zero");
        assertThatThrownBy(() -> Arithmetic.apply("pow", 2, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unsupported operation: pow");
    }
}
