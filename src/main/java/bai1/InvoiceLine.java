package bai1;

import java.math.BigDecimal;

public record InvoiceLine(
        String productCode,
        ProductCategory category,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal discountPercent
) {
    public InvoiceLine(String productCode, ProductCategory category, long unitPrice, int quantity, double discountPercent) {
        this(productCode, category, BigDecimal.valueOf(unitPrice), quantity, BigDecimal.valueOf(discountPercent));
    }

    public InvoiceLine(String productCode, ProductCategory category, String unitPrice, int quantity, String discountPercent) {
        this(productCode, category, new BigDecimal(unitPrice), quantity, new BigDecimal(discountPercent));
    }
}
