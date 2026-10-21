package dev.playground.tools;

public final class TextInputs {

    private TextInputs() {
    }

    public static String require(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
