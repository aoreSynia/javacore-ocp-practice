package bai2;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public record LoanComparisonResult(
        BigDecimal totalInterestGocDeu,
        BigDecimal totalInterestTraDeu,
        BigDecimal interestDifference,
        List<Installment> gocDeuSchedule,
        List<Installment> traDeuSchedule
) {
    public LoanComparisonResult {
        gocDeuSchedule = Collections.unmodifiableList(gocDeuSchedule);
        traDeuSchedule = Collections.unmodifiableList(traDeuSchedule);
    }
}
