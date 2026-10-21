package dev.playground.tools;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PlainTextToolResultConverterTest {

    private final PlainTextToolResultConverter converter = new PlainTextToolResultConverter();

    @Test
    void keepsTextThatWouldBeAmbiguousJson() {
        assertThat(this.converter.convert("42", String.class)).isEqualTo("42");
        assertThat(this.converter.convert("北京：22°C，晴", String.class)).isEqualTo("北京：22°C，晴");
        assertThat(this.converter.convert("todos:\n1. 写测试", String.class)).isEqualTo("todos:\n1. 写测试");
    }

    @Test
    void nullBecomesAnEmptyResult() {
        assertThat(this.converter.convert(null, String.class)).isEmpty();
    }
}
