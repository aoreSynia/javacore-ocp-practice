package bai1;

import java.math.BigDecimal;

public enum ProductCategory {
    THUC_PHAM(new BigDecimal("0.05")),
    GIA_DUNG(new BigDecimal("0.10")),
    DIEN_TU(new BigDecimal("0.08")),
    SACH(BigDecimal.ZERO);

    private final BigDecimal vatRate;

    ProductCategory(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }
}
