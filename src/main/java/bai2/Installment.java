package bai2;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Installment(
        int k,
        LocalDate dueDate,
        BigDecimal openingBalance,
        BigDecimal principal,
        BigDecimal interest,
        BigDecimal payment,
        BigDecimal closingBalance
) {
}
