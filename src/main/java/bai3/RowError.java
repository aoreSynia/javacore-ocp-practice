package bai3;

public record RowError(
        int lineNumber,
        String orderId,
        String reason
) {
}
