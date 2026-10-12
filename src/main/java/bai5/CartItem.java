package bai5;

import java.math.BigDecimal;
import java.util.Objects;

public record CartItem(
        String productCode,
        String category,
        BigDecimal unitPrice,
        int quantity
) {
    public CartItem {
        Objects.requireNonNull(productCode, "productCode không được null");
        Objects.requireNonNull(category, "category không được null");
        Objects.requireNonNull(unitPrice, "unitPrice không được null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải > 0");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá không được âm");
        }
    }

    public CartItem(String productCode, String category, long unitPrice, int quantity) {
        this(productCode, category, BigDecimal.valueOf(unitPrice), quantity);
    }

    public BigDecimal totalOriginalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

