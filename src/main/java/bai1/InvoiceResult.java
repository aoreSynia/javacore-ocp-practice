package bai1;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record InvoiceResult(
        List<InvoiceLineDetail> lineDetails,
        BigDecimal totalGrossAmount,
        BigDecimal totalLineDiscount,
        BigDecimal totalAfterLineDiscount,
        BigDecimal totalInvoiceDiscount,
        BigDecimal totalTaxableAmount,
        BigDecimal totalVat,
        BigDecimal totalPayment,
        Map<BigDecimal, BigDecimal> taxByRate
) {
    public InvoiceResult {
        lineDetails = Collections.unmodifiableList(lineDetails);
        taxByRate = Collections.unmodifiableMap(taxByRate);
    }
}
