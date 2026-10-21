package dev.playground.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class Arithmetic {

    private Arithmetic() {
    }

    public static String apply(String operation, double left, double right) {
        if (!Double.isFinite(left) || !Double.isFinite(right)) {
            throw new IllegalArgumentException("operands must be finite numbers");
        }
        String name = operation == null ? "" : operation.trim().toLowerCase(Locale.ROOT);
        BigDecimal result = switch (name) {
            case "add", "+" -> decimal(left).add(decimal(right));
            case "subtract", "-" -> decimal(left).subtract(decimal(right));
            case "multiply", "*" -> decimal(left).multiply(decimal(right));
            case "divide", "/" -> divide(left, right);
            default -> throw new IllegalArgumentException("unsupported operation: " + operation);
        };
        return result.stripTrailingZeros().toPlainString();
    }

    private static BigDecimal divide(double left, double right) {
        if (right == 0.0d) {
            throw new IllegalArgumentException("division by zero");
        }
        return decimal(left).divide(decimal(right), 10, RoundingMode.HALF_UP);
    }

    private static BigDecimal decimal(double value) {
        return BigDecimal.valueOf(value);
    }
}
