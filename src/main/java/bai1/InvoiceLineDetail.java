package bai1;

import java.math.BigDecimal;

public record InvoiceLineDetail(
        String productCode,
        ProductCategory category,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal grossAmount,
        BigDecimal discountPercent,
        BigDecimal lineDiscount,
        BigDecimal afterLineDiscount,
        BigDecimal allocatedDiscount,
        BigDecimal taxableAmount,
        BigDecimal vatAmount
) {
}
