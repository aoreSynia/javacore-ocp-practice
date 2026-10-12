package bai3;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderRecord(
        int lineNumber,
        String orderId,
        LocalDate orderDate,
        String region,
        String productCode,
        String category,
        long quantity,
        BigDecimal unitPrice,
        OrderStatus status
) {
    public BigDecimal lineAmount() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal netRevenueImpact() {
        return switch (status) {
            case COMPLETED -> lineAmount();
            case RETURNED -> lineAmount().negate();
            case CANCELLED -> BigDecimal.ZERO;
        };
    }
}
